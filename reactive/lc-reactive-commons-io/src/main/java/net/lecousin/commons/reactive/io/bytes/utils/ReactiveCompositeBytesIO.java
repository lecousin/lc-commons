package net.lecousin.commons.reactive.io.bytes.utils;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.reactive.io.AbstractReactiveIO;
import net.lecousin.commons.reactive.io.ReactiveIO;
import net.lecousin.commons.reactive.io.ReactiveIOChecks;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

/**
 * Composite IO.
 */
public final class ReactiveCompositeBytesIO extends AbstractReactiveIO implements ReactiveBytesIO.ReadWrite {

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
	
	
	
	private List<? extends ReactiveBytesIO> ios;
	private final boolean closeIosOnClose;
	private final Scheduler scheduler;
	private Cursor posCursor;
	
	private ReactiveCompositeBytesIO(List<? extends ReactiveBytesIO> ios, boolean closeIosOnClose) {
		this.ios = new ArrayList<>(ios);
		this.closeIosOnClose = closeIosOnClose;
		this.scheduler = computeScheduler();
		posCursor = new Cursor();
	}
	
	private Scheduler computeScheduler() {
		if (ios == null)
			return Schedulers.parallel();
		if (ios.isEmpty())
			return Schedulers.parallel();
		Iterator<? extends ReactiveBytesIO> it = ios.iterator();
		Scheduler s = it.next().getScheduler();
		while (it.hasNext()) {
			if (it.next().getScheduler() != s) {
				s = Schedulers.boundedElastic();
				break;
			}
		}
		return s;
	}
	
	// Cursor
	
	private final class Cursor {
		private int index;
		private ReactiveBytesIO io;
		private long posInIO;
		private long posGlobal;
		
		private Mono<Boolean> goNext() {
			if (index >= ios.size() - 1) {
				io = null;
				return Mono.just(false);
			}
			io = ios.get(++index);
			posInIO = 0;
			if (io instanceof ReactiveIO.Seekable s)
				return s.seek(SeekFrom.START, 0).thenReturn(true);
			return Mono.just(true);
		}
		
		private Mono<Boolean> goPrevious() {
			if (index == 0) {
				return Mono.just(false);
			}
			io = ios.get(--index);
			this.posGlobal -= this.posInIO;
			return ((ReactiveIO.Seekable) io).seek(SeekFrom.END, 0)
				.map(p -> {
					this.posInIO = p;
					return true;
				});
		}
		
		private void moved(long move) {
			posInIO += move;
			posGlobal += move;
		}
	}
	
	private Mono<Void> resetCursor(Cursor cursor) {
		cursor.index = -1;
		cursor.posInIO = 0;
		cursor.posGlobal = 0;
		return cursor.goNext().then();
	}
	
	private Mono<Long> moveCursorAt(Cursor cursor, long pos) {
		if (pos < 0) return Mono.error(new IllegalArgumentException("Cannot move beyond the start: " + pos));
		if (pos == 0) return resetCursor(cursor).then(Mono.fromSupplier(() -> cursor.posGlobal));
		return Mono.just(pos)
			.expand(toReach -> {
				if (toReach == cursor.posGlobal) return Mono.empty();
				if (toReach > cursor.posGlobal) {
					// move forward
					if (cursor.io == null) return Mono.error(new EOFException());
					return ((ReactiveIO.KnownSize) cursor.io).size()
						.flatMap(ioSize -> {
							long moveInIO = toReach - cursor.posGlobal;
							if (cursor.posInIO + moveInIO <= ioSize) {
								return ((ReactiveIO.Seekable) cursor.io)
									.seek(SeekFrom.CURRENT, moveInIO)
									.flatMap(done -> {
										cursor.moved(moveInIO);
										return Mono.empty();
									});
							}
							cursor.moved(ioSize - cursor.posInIO);
							return cursor.goNext().flatMap(ok -> ok.booleanValue() ? Mono.just(toReach) : Mono.error(new EOFException()));
						});
				}
				// move backward
				long moveInIO = toReach - cursor.posGlobal;
				if (cursor.posInIO > 0 && -moveInIO <= cursor.posInIO) {
					// just need to move backward inside current IO
					return ((ReactiveIO.Seekable) cursor.io)
						.seek(SeekFrom.CURRENT, moveInIO)
							.flatMap(done -> {
								cursor.moved(moveInIO);
								return Mono.empty();
							});
				}
				return cursor.goPrevious().thenReturn(toReach);
			})
			.then(Mono.fromSupplier(() -> cursor.posGlobal));
	}

	// SeekableCursor
	
	private final class SeekableCursor {
		private int index;
		private ReactiveIO.Seekable io;
		private long posGlobal;
		private long ioSize;
		
		private Mono<Void> goNext() {
			if (++index >= ios.size())
				return Mono.error(new EOFException());
			io = (ReactiveIO.Seekable) ios.get(index);
			posGlobal += ioSize;
			return io.size().doOnSuccess(size -> ioSize = size).then();
		}
	}
	
	private Mono<SeekableCursor> createSeekableCursor(long pos) {
		if (pos < 0) return Mono.error(new NegativeValueException(pos, IOChecks.FIELD_POS));
		SeekableCursor cursor = new SeekableCursor();
		cursor.index = 0;
		cursor.posGlobal = 0;
		if (this.ios.isEmpty()) {
			if (pos == 0) return Mono.just(cursor);
			return Mono.error(new EOFException());
		}
		cursor.io = (ReactiveIO.Seekable) this.ios.get(0);
		return moveSeekableCursorTo(cursor, pos);
	}
	
	private Mono<SeekableCursor> moveSeekableCursorTo(SeekableCursor cursor, long pos) {
		return ((ReactiveIO.KnownSize) cursor.io).size()
			.flatMap(size -> {
				if (pos >= cursor.posGlobal + size) {
					if (++cursor.index >= this.ios.size())
						return Mono.error(new EOFException());
					cursor.io = (ReactiveIO.Seekable) this.ios.get(cursor.index);
					cursor.posGlobal += size;
					return moveSeekableCursorTo(cursor, pos);
				}
				cursor.ioSize = size;
				return Mono.just(cursor);
			});
	}

	// IO
	
	@Override
	public Mono<Void> closeInternal() {
		return Mono.defer(() -> {
			if (ios == null) return Mono.empty();
			List<? extends ReactiveIO> list = ios;
			ios = null;
			if (!closeIosOnClose) return Mono.empty();
			return Flux.fromIterable(list)
				.flatMap(ReactiveIO::close)
				.then();
		});
	}
	
	@Override
	public Scheduler getScheduler() {
		return scheduler;
	}
	
	
	// KnownSize & Seekable
	
	@Override
	public Mono<Long> position() {
		return Mono.fromCallable(() -> {
			if (isClosed()) throw new ClosedChannelException();
			return posCursor.posGlobal;
		});
	}
	
	@Override
	public Mono<Long> size() {
		return Flux.defer(() -> {
			if (isClosed()) return Flux.error(new ClosedChannelException());
			return Flux.fromIterable(ios);
		})
		.flatMap(io -> ((ReactiveIO.KnownSize) io).size())
		.reduce(0L, (size, add) -> size + add);
	}
	
	@Override
	public Mono<Long> seek(SeekFrom from, long offset) {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			Objects.requireNonNull(from, "from");
			switch (from) {
			case START:
				return moveCursorAt(posCursor, offset);
			case CURRENT:
				return moveCursorAt(posCursor, posCursor.posGlobal + offset);
			case END:
				return size()
					.flatMap(size -> moveCursorAt(posCursor, size - offset));
			}
			return position();
		});
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
	
	@Override
	public Mono<Long> skipUpTo(long nbBytes) {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			if (nbBytes < 0) return Mono.error(new NegativeValueException(nbBytes, "nbBytes"));
			if (nbBytes == 0) return Mono.just(0L);
			if (posCursor.io == null) return Mono.just(-1L);
			return skipNextUpTo(nbBytes);
		});
	}
	
	private Mono<Long> skipNextUpTo(long nbBytes) {
		return ((ReactiveBytesIO.Readable) posCursor.io).skipUpTo(nbBytes)
			.flatMap(nb -> {
				if (nb > 0) {
					posCursor.moved(nb);
					return Mono.just(nb);
				}
				return posCursor.goNext()
					.flatMap(ok -> ok.booleanValue() ? skipNextUpTo(nbBytes) : Mono.just(-1L));
			});
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
	public Mono<Void> flush() {
		return Flux.defer(() -> {
			if (isClosed()) return Flux.error(new ClosedChannelException());
			return Flux.fromIterable(ios);
		})
		.flatMap(io -> ((ReactiveBytesIO.Writable) io).flush())
		.then();	
	}
	
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
