package net.lecousin.commons.reactive.io.bytes.utils;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.List;

import net.lecousin.commons.reactive.io.ReactiveIOChecks;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import net.lecousin.commons.reactive.io.utils.AbstractReactiveCompositeIO;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

/**
 * Composite IO.
 */
public final class ReactiveCompositeBytesIO extends AbstractReactiveCompositeIO<ReactiveBytesIO> implements ReactiveBytesIO.ReadWrite {

	/**
	 * Create a reactive readable I/O from a list of I/O.
	 * @param ios list
	 * @param closeIosOnClose if true, all IOs in the list will be closed when the composite IO is closed
	 * @return the composite IO
	 */
	public static Mono<ReactiveBytesIO.Readable> fromReadable(List<? extends ReactiveBytesIO.Readable> ios, boolean closeIosOnClose) {
		return Mono.fromSupplier(() -> new ReactiveCompositeBytesIO(ios, closeIosOnClose))
			.flatMap(io -> io.resetCursor(io.posCursor).thenReturn(io.asReadableBytesIO()));
	}
	
	/**
	 * Create a reactive readable seekable I/O from a list of I/O.
	 * @param ios list
	 * @param closeIosOnClose if true, all IOs in the list will be closed when the composite IO is closed
	 * @return the composite IO
	 */
	public static Mono<ReactiveBytesIO.Readable.Seekable> fromReadableSeekable(List<? extends ReactiveBytesIO.Readable.Seekable> ios, boolean closeIosOnClose) {
		return Mono.fromSupplier(() -> new ReactiveCompositeBytesIO(ios, closeIosOnClose))
			.flatMap(io -> io.resetCursor(io.posCursor).thenReturn(io.asReadableSeekableBytesIO()));
	}
	
	/**
	 * Create a reactive writable I/O from a list of I/O.
	 * @param ios list
	 * @param closeIosOnClose if true, all IOs in the list will be closed when the composite IO is closed
	 * @return the composite IO
	 */
	public static Mono<ReactiveBytesIO.Writable> fromWritable(List<? extends ReactiveBytesIO.Writable> ios, boolean closeIosOnClose) {
		return Mono.fromSupplier(() -> new ReactiveCompositeBytesIO(ios, closeIosOnClose))
			.flatMap(io -> io.resetCursor(io.posCursor).thenReturn(io.asWritableBytesIO()));
	}
	
	/**
	 * Create a reactive writable seekable I/O from a list of I/O.
	 * @param ios list
	 * @param closeIosOnClose if true, all IOs in the list will be closed when the composite IO is closed
	 * @return the composite IO
	 */
	public static Mono<ReactiveBytesIO.Writable.Seekable> fromWritableSeekable(List<? extends ReactiveBytesIO.Writable.Seekable> ios, boolean closeIosOnClose) {
		return Mono.fromSupplier(() -> new ReactiveCompositeBytesIO(ios, closeIosOnClose))
			.flatMap(io -> io.resetCursor(io.posCursor).thenReturn(io.asWritableSeekableBytesIO()));
	}

	/**
	 * Create a reactive readable and writable I/O from a list of I/O.
	 * @param <T> type of Read-Write IO
	 * @param ios list
	 * @param closeIosOnClose if true, all IOs in the list will be closed when the composite IO is closed
	 * @return the composite IO
	 */
	public static <T extends ReactiveBytesIO.Readable.Seekable & ReactiveBytesIO.Writable.Seekable> Mono<ReactiveBytesIO.ReadWrite> fromReadWrite(List<T> ios, boolean closeIosOnClose) {
		return Mono.fromSupplier(() -> new ReactiveCompositeBytesIO(ios, closeIosOnClose))
			.flatMap(io -> io.resetCursor(io.posCursor).thenReturn(io));
	}
	
	
	
	
	private ReactiveCompositeBytesIO(List<? extends ReactiveBytesIO> ios, boolean closeIosOnClose) {
		super(ios, closeIosOnClose);
	}
	

	// Readable
	
	@Override
	public Mono<Integer> readBytes(ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBuffer(this, buffer, () -> {
			if (buffer.remaining() == 0) return Mono.just(0);
			if (posCursor.io == null) return Mono.just(-1);
			return readPart(buffer);
		});
	}
	
	private Mono<Integer> readPart(ByteBuffer buffer) {
		return ((ReactiveBytesIO.Readable) posCursor.io).readBytes(buffer)
		.flatMap(nb -> {
			if (nb > 0) {
				posCursor.moved(nb);
				return Mono.just(nb);
			}
			return posCursor.goNext()
				.flatMap(ok -> ok.booleanValue() ? readPart(buffer) : Mono.just(-1));
		});
	}
	
	@Override
	public Mono<Integer> readBytes(byte[] buf, int off, int len) {
		return ReactiveIOChecks.deferByteArray(this, buf, off, len, () -> {
			if (len == 0) return Mono.just(0);
			if (posCursor.io == null) return Mono.just(-1);
			return readPart(buf, off, len);
		});
	}

	private Mono<Integer> readPart(byte[] buf, int off, int len) {
		return ((ReactiveBytesIO.Readable) posCursor.io).readBytes(buf, off, len)
		.flatMap(nb -> {
			if (nb > 0) {
				posCursor.moved(nb);
				return Mono.just(nb);
			}
			return posCursor.goNext()
				.flatMap(ok -> ok.booleanValue() ? readPart(buf, off, len) : Mono.just(-1));
		});
	}
	
	@Override
	public Mono<ByteBuffer> readBuffer() {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			if (posCursor.io == null) return Mono.empty();
			return readNextPart();
		});
	}
	
	private Mono<ByteBuffer> readNextPart() {
		return ((ReactiveBytesIO.Readable) posCursor.io).readBuffer()
		.doOnNext(b -> posCursor.moved(b.remaining()))
		.switchIfEmpty(Mono.defer(() ->
			posCursor.goNext()
			.flatMap(ok -> ok.booleanValue() ? readNextPart() : Mono.empty())
		));
	}
	
	@Override
	public Mono<Byte> readByte() {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			if (posCursor.io == null) return Mono.error(new EOFException());
			return readNextByte();
		});
	}

	private Mono<Byte> readNextByte() {
		return ((ReactiveBytesIO.Readable) posCursor.io).readByte()
		.doOnNext(b -> posCursor.moved(1))
		.onErrorResume(EOFException.class, err -> posCursor.goNext().flatMap(ok -> ok.booleanValue() ? readNextByte() : Mono.error(err)));
	}
	
	// Readable Seekable
	
	private Mono<Integer> readPartAt(SeekableCursor cursor, long pos, ByteBuffer buffer) {
		long posInIO = pos - cursor.posGlobal;
		int l = (int) Math.min(buffer.remaining(), cursor.ioSize - posInIO);
		int previousLimit = buffer.limit();
		buffer.limit(buffer.position() + l);
		return ((ReactiveBytesIO.Readable.Seekable) cursor.io).readBytesFullyAt(posInIO, buffer)
		.flatMap(b -> {
			buffer.limit(previousLimit);
			if (l == cursor.ioSize - posInIO && buffer.hasRemaining()) {
				return cursor.goNext().thenReturn(l);
			}
			return Mono.just(l);
		});
	}
	
	@Override
	@SuppressWarnings("java:S3358")
	public Mono<ByteBuffer> readBytesFullyAt(long pos, ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBuffer(this, buffer, () -> {
			if (buffer.remaining() == 0) return Mono.just(buffer);
			return createSeekableCursor(pos).map(cursor -> Tuples.of(cursor, pos))
				.expand(tuple -> buffer.remaining() == 0 ? Mono.empty()
					: readPartAt(tuple.getT1(), tuple.getT2(), buffer)
						.flatMap(read -> read > 0 ? Mono.just(Tuples.of(tuple.getT1(), tuple.getT2() + read)) : Mono.error(new EOFException())))
				.then(Mono.just(buffer));
		});
	}
	
	@Override
	public Mono<Integer> readBytesAt(long pos, ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBuffer(this, buffer, () -> {
			if (buffer.remaining() == 0) return Mono.just(0);
			return createSeekableCursor(pos)
				.flatMap(cursor -> {
					long posInIO = pos - cursor.posGlobal;
					int l = (int) Math.min(buffer.remaining(), cursor.ioSize - posInIO);
					int previousLimit = buffer.limit();
					buffer.limit(buffer.position() + l);
					return ((ReactiveBytesIO.Readable.Seekable) cursor.io).readBytesAt(posInIO, buffer)
						.map(b -> {
							buffer.limit(previousLimit);
							return l;
						});
				})
				.onErrorResume(EOFException.class, error -> Mono.just(-1));
		});
	}

	@Override
	public Mono<Byte> readByteAt(long pos) {
		return ReactiveIOChecks.deferNotClosed(this, () -> createSeekableCursor(pos)
			.flatMap(cursor -> ((ReactiveBytesIO.Readable.Seekable) cursor.io).readByteAt(pos - cursor.posGlobal)));
	}
	
	// Writable
	
	@Override
	public Mono<Integer> writeBytes(ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBuffer(this, buffer, () -> {
			if (buffer.remaining() == 0) return Mono.just(0);
			if (posCursor.io == null) return Mono.just(-1);
			return writePart(buffer);
		});
	}
	
	private Mono<Integer> writePart(ByteBuffer buffer) {
		return ((ReactiveBytesIO.Writable) posCursor.io).writeBytes(buffer)
		.flatMap(nb -> {
			if (nb > 0) {
				posCursor.moved(nb);
				return Mono.just(nb);
			}
			return posCursor.goNext()
				.flatMap(ok -> ok.booleanValue() ? writePart(buffer) : Mono.just(-1));
		});
	}
	
	@Override
	public Mono<Integer> writeBytes(byte[] buf, int off, int len) {
		return ReactiveIOChecks.deferByteArray(this, buf, off, len, () -> {
			if (len == 0) return Mono.just(0);
			if (posCursor.io == null) return Mono.just(-1);
			return writePart(buf, off, len);
		});
	}

	private Mono<Integer> writePart(byte[] buf, int off, int len) {
		return ((ReactiveBytesIO.Writable) posCursor.io).writeBytes(buf, off, len)
		.flatMap(nb -> {
			if (nb > 0) {
				posCursor.moved(nb);
				return Mono.just(nb);
			}
			return posCursor.goNext()
				.flatMap(ok -> ok.booleanValue() ? writePart(buf, off, len) : Mono.just(-1));
		});
	}
	
	@Override
	public Mono<Void> writeByte(byte value) {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			if (posCursor.io == null) return Mono.error(new EOFException());
			return writeNextByte(value);
		});
	}

	private Mono<Void> writeNextByte(byte value) {
		return ((ReactiveBytesIO.Writable) posCursor.io).writeByte(value)
		.doOnNext(b -> posCursor.moved(1))
		.onErrorResume(EOFException.class, err -> 
			posCursor.goNext()
			.flatMap(ok -> ok.booleanValue() ? writeNextByte(value) : Mono.error(err))
		);
	}
	
	// Writable Seekable
	
	private Mono<Integer> writePartAt(SeekableCursor cursor, long pos, ByteBuffer buffer, boolean fully) {
		long posInIO = pos - cursor.posGlobal;
		int l = (int) Math.min(buffer.remaining(), cursor.ioSize - posInIO);
		int previousLimit = buffer.limit();
		buffer.limit(buffer.position() + l);
		return ((ReactiveBytesIO.Writable.Seekable) cursor.io).writeBytesAt(posInIO, buffer)
		.then(Mono.defer(() -> {
			buffer.limit(previousLimit);
			if (fully && l == cursor.ioSize - posInIO && buffer.hasRemaining()) {
				return cursor.goNext().thenReturn(l);
			}
			return Mono.just(l);
		}));
	}
	
	@Override
	@SuppressWarnings("java:S3358")
	public Mono<Void> writeBytesFullyAt(long pos, ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBuffer(this, buffer, () -> {
			if (buffer.remaining() == 0) return Mono.empty();
			return createSeekableCursor(pos).map(cursor -> Tuples.of(cursor, pos))
				.expand(tuple -> buffer.remaining() == 0 ? Mono.empty()
					: writePartAt(tuple.getT1(), tuple.getT2(), buffer, true)
						.flatMap(written -> written > 0 ? Mono.just(Tuples.of(tuple.getT1(), tuple.getT2() + written)) : Mono.error(new EOFException())))
				.then();
		});
	}
	
	@Override
	public Mono<Integer> writeBytesAt(long pos, ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBuffer(this, buffer, () -> {
			if (buffer.remaining() == 0) return Mono.just(0);
			return createSeekableCursor(pos)
				.flatMap(cursor -> writePartAt(cursor, pos, buffer, false))
				.onErrorResume(EOFException.class, e -> Mono.just(-1));
		});
	}
	
	@Override
	public Mono<Void> writeByteAt(long pos, byte value) {
		return ReactiveIOChecks.deferNotClosed(this, () -> createSeekableCursor(pos)
			.flatMap(cursor -> ((ReactiveBytesIO.Writable.Seekable) cursor.io).writeByteAt(pos - cursor.posGlobal, value)));
	}

}
