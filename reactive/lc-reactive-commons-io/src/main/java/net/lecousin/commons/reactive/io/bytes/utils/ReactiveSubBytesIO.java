package net.lecousin.commons.reactive.io.bytes.utils;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.Optional;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.reactive.io.AbstractReactiveIO;
import net.lecousin.commons.reactive.io.ReactiveIOChecks;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/** Sub-part from a ReactiveBytesIO. */
public final class ReactiveSubBytesIO extends AbstractReactiveIO implements ReactiveBytesIO.ReadWrite {
	
	/**
	 * Create a Read-Write sub-IO.
	 * @param <T> type of IO
	 * @param io IO
	 * @param start start of the sub-io in the given IO
	 * @param end end of the sub-io in the given IO
	 * @param closeIoOnClose if true, the given IO will be closed when the sub-io is closed
	 * @return the sub-io
	 */
	public static <T extends ReactiveBytesIO.Readable.Seekable & ReactiveBytesIO.Writable.Seekable> ReactiveBytesIO.ReadWrite fromReadWrite(T io, long start, long end, boolean closeIoOnClose) {
		return new ReactiveSubBytesIO(io, start, end, closeIoOnClose);
	}
	
	/**
	 * Create a Readable sub-IO.
	 * @param io IO
	 * @param start start of the sub-io in the given IO
	 * @param end end of the sub-io in the given IO
	 * @param closeIoOnClose if true, the given IO will be closed when the sub-io is closed
	 * @return the sub-io
	 */
	public static ReactiveBytesIO.Readable.Seekable fromReadable(ReactiveBytesIO.Readable.Seekable io, long start, long end, boolean closeIoOnClose) {
		return new ReactiveSubBytesIO(io, start, end, closeIoOnClose).asReadableSeekableBytesIO();
	}
	
	/**
	 * Create a Writable sub-IO.
	 * @param io IO
	 * @param start start of the sub-io in the given IO
	 * @param end end of the sub-io in the given IO
	 * @param closeIoOnClose if true, the given IO will be closed when the sub-io is closed
	 * @return the sub-io
	 */
	public static ReactiveBytesIO.Writable.Seekable fromWritable(ReactiveBytesIO.Writable.Seekable io, long start, long end, boolean closeIoOnClose) {
		return new ReactiveSubBytesIO(io, start, end, closeIoOnClose).asWritableSeekableBytesIO();
	}
	
	
	private ReactiveBytesIO io;
	private long start;
	private long end;
	private boolean closeMainIO;
	private long position = 0;
	
	private ReactiveSubBytesIO(ReactiveBytesIO io, long start, long end, boolean closeMainIO) {
		this.io = io;
		this.start = start;
		this.end = end;
		this.closeMainIO = closeMainIO;
	}
	
	private static final int DEFAULT_BUFFER_SIZE = 8192;
	
	// IO
	
	@Override
	public Mono<Void> closeInternal() {
		return Mono.defer(() -> {
			if (io == null) return Mono.empty();
			ReactiveBytesIO i = io;
			io = null;
			if (closeMainIO)
				return i.close();
			return Mono.empty();
		});
	}
	
	@Override
	public Scheduler getScheduler() {
		if (io == null) return Schedulers.parallel();
		return io.getScheduler();
	}
	
	// checks
	
	private Optional<Exception> check(long pos, long amount) {
		if (pos < 0) return Optional.of(new NegativeValueException(pos, IOChecks.FIELD_POS));
		if (start + pos + amount > end) return Optional.of(new EOFException());
		return Optional.empty();
	}
	
	// Seekable
	
	@Override
	public Mono<Long> position() {
		return ReactiveIOChecks.deferNotClosed(this, () -> Mono.just(position));
	}
	
	@Override
	public Mono<Long> seek(SeekFrom from, long offset) {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			if (from == null) return Mono.error(new NullPointerException("from"));
			long p = position;
			switch (from) {
			case CURRENT: p += offset; break;
			case START: p = offset; break;
			case END: p = end - start - offset; break;
			}
			if (p < 0) return Mono.error(new IllegalArgumentException("Cannot move to position " + p));
			if (p + start > end) return Mono.error(new EOFException());
			this.position = p;
			return Mono.just(p);
		});
	}
	
	// Known Size
	
	@Override
	public Mono<Long> size() {
		return ReactiveIOChecks.deferNotClosed(this, () -> Mono.just(end - start));
	}
	
	// Readable
	
	@Override
	public Mono<Long> skipUpTo(long toSkip) {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			if (toSkip < 0) return Mono.error(new NegativeValueException(toSkip, "toSkip"));
			if (toSkip == 0) return Mono.just(0L);
			if (position == end - start) return Mono.just(-1L);
			long nb = Math.min(toSkip, end - start - position);
			this.position += nb;
			return Mono.just(nb);
		});
	}
	
	@Override
	public Mono<Void> skipFully(long toSkip) {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			if (toSkip < 0) return Mono.error(new NegativeValueException(toSkip, "toSkip"));
			if (position + toSkip > end - start) return Mono.error(new EOFException());
			this.position += toSkip;
			return Mono.empty();
		});
	}
	
	@Override
	public Mono<ByteBuffer> readBuffer() {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			int l = (int) Math.min(DEFAULT_BUFFER_SIZE, end - (start + position));
			if (l == 0) return Mono.empty();
			return readBytesFully(ByteBuffer.allocate(l)).map(ByteBuffer::flip);
		});
	}
	
	@Override
	public Mono<ByteBuffer> readBytesFullyAt(long pos, ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBufferAnd(this, buffer, () -> check(pos, buffer.remaining()),
			() -> ((ReactiveBytesIO.Readable.Seekable) io).readBytesFullyAt(start + pos, buffer)
		);
	}
	
	@Override
	public Mono<ByteBuffer> readBytesFully(ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBufferAnd(this, buffer, () -> check(position, buffer.remaining()),
			() -> {
				int l = buffer.remaining();
				return ((ReactiveBytesIO.Readable.Seekable) io).readBytesFullyAt(start + position, buffer)
					.doOnNext(r -> this.position += l);
			}
		);
	}
	
	@Override
	public Mono<byte[]> readBytesFullyAt(long pos, byte[] buf, int off, int len) {
		return ReactiveIOChecks.deferByteArrayAnd(this, buf, off, len, () -> check(pos, len),
			() -> ((ReactiveBytesIO.Readable.Seekable) io).readBytesFullyAt(start + pos, buf, off, len)
		);
	}
	
	@Override
	public Mono<byte[]> readBytesFully(byte[] buf, int off, int len) {
		return ReactiveIOChecks.deferByteArrayAnd(this, buf, off, len, () -> check(position, len),
			() -> ((ReactiveBytesIO.Readable.Seekable) io).readBytesFullyAt(start + position, buf, off, len).doOnNext(r -> this.position += len)
		);
	}
	
	@Override
	public Mono<Integer> readBytesAt(long pos, ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBuffer(this, pos, buffer, () -> {
			if (buffer.remaining() == 0) return Mono.just(0);
			if (pos >= end - start) return Mono.just(-1);
			int l = (int) Math.min(buffer.remaining(), end - start - pos);
			int previousLimit = buffer.limit();
			buffer.limit(buffer.position() + l);
			return ((ReactiveBytesIO.Readable.Seekable) io).readBytesFullyAt(start + pos, buffer)
				.map(r -> {
					buffer.limit(previousLimit);
					return l;
				});
		});
	}

	@Override
	public Mono<Integer> readBytes(ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBuffer(this, buffer, () -> {
			if (buffer.remaining() == 0) return Mono.just(0);
			if (end - start - position == 0) return Mono.just(-1);
			int l = (int) Math.min(buffer.remaining(), end - start - position);
			int previousLimit = buffer.limit();
			buffer.limit(buffer.position() + l);
			return ((ReactiveBytesIO.Readable.Seekable) io).readBytesFullyAt(start + position, buffer)
				.map(r -> {
					this.position += l;
					buffer.limit(previousLimit);
					return l;
				});
		});
	}
	
	@Override
	public Mono<Integer> readBytesAt(long pos, byte[] buf, int off, int len) {
		return ReactiveIOChecks.deferByteArray(this, pos, buf, off, len, () -> {
			if (len == 0) return Mono.just(0);
			if (start + pos >= end) return Mono.just(-1);
			int l = (int) Math.min(len, end - start - pos);
			return ((ReactiveBytesIO.Readable.Seekable) io).readBytesFullyAt(start + pos, buf, off, l)
				.map(r -> l);
		});
	}
	
	@Override
	public Mono<Integer> readBytes(byte[] buf, int off, int len) {
		return ReactiveIOChecks.deferByteArray(this, buf, off, len, () -> {
			if (len == 0) return Mono.just(0);
			if (start + position >= end) return Mono.just(-1);
			int l = (int) Math.min(len, end - start - position);
			return ((ReactiveBytesIO.Readable.Seekable) io).readBytesFullyAt(start + position, buf, off, l)
				.map(r -> {
					this.position += l;
					return l;
				});
		});
	}
	
	@Override
	public Mono<Byte> readByte() {
		return ReactiveIOChecks.deferNotClosedAnd(this,
			() -> check(position, 1),
			() -> ((ReactiveBytesIO.Readable.Seekable) io).readByteAt(start + position).doOnNext(r -> this.position++)
		);
	}
	
	@Override
	public Mono<Byte> readByteAt(long pos) {
		return ReactiveIOChecks.deferNotClosedAnd(this,
			() -> check(pos, 1),
			() -> ((ReactiveBytesIO.Readable.Seekable) io).readByteAt(start + pos)
		);
	}

	// Writable
	
	@Override
	@SuppressWarnings("java:S1612") // false positive because io may be null
	public Mono<Void> flush() {
		return ReactiveIOChecks.deferNotClosed(this, () -> ((ReactiveBytesIO.Writable.Seekable) io).flush());
	}
	
	@Override
	public Mono<Void> writeBytesFullyAt(long pos, ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBufferAnd(this, buffer,
			() -> check(pos, buffer.remaining()),
			() -> ((ReactiveBytesIO.Writable.Seekable) io).writeBytesFullyAt(start + pos, buffer)
		);
	}

	@Override
	public Mono<Integer> writeBytesAt(long pos, ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBuffer(this, buffer, () -> {
			if (pos < 0) return Mono.error(new NegativeValueException(pos, IOChecks.FIELD_POS));
			int l = buffer.remaining();
			if (l == 0) return Mono.just(0);
			if (start + pos >= end) return Mono.just(-1);
			if (start + pos + l > end) {
				l = (int) (end - (start + pos));
				int pl = buffer.limit();
				buffer.limit(buffer.position() + l);
				return ((ReactiveBytesIO.Writable.Seekable) io).writeBytesAt(start + pos, buffer)
					.doFinally(s -> buffer.limit(pl));
			}
			return ((ReactiveBytesIO.Writable.Seekable) io).writeBytesAt(start + pos, buffer);
		});
	}
	
	@Override
	public Mono<Void> writeBytesFully(ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBufferAnd(this, buffer,
			() -> check(position, buffer.remaining()),
			() -> {
				int l = buffer.remaining();
				return ((ReactiveBytesIO.Writable.Seekable) io).writeBytesFullyAt(start + position, buffer).doOnSuccess(r -> this.position += l);
			}
		);
	}
	
	@Override
	public Mono<Integer> writeBytes(ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBuffer(this, buffer,
			() -> {
				if (buffer.remaining() == 0) return Mono.just(0);
				if (start + position == end) return Mono.just(-1);
				int l = (int) Math.min(buffer.remaining(), end - (start + position));
				int pl = buffer.limit();
				buffer.limit(buffer.position() + l);
				return ((ReactiveBytesIO.Writable.Seekable) io).writeBytesAt(start + position, buffer)
					.doFinally(s -> buffer.limit(pl))
					.doOnSuccess(r -> this.position += l)
					.thenReturn(l);
			}
		);
	}
	
	@Override
	public Mono<Void> writeBytesFullyAt(long pos, byte[] buf, int off, int len) {
		return ReactiveIOChecks.deferByteArrayAnd(this, buf, off, len,
			() -> check(pos, len),
			() -> ((ReactiveBytesIO.Writable.Seekable) io).writeBytesFullyAt(start + pos, buf, off, len)
		);
	}

	@Override
	public Mono<Integer> writeBytesAt(long pos, byte[] buf, int off, int len) {
		return ReactiveIOChecks.deferByteArray(this, buf, off, len, () -> {
			if (pos < 0) return Mono.error(new NegativeValueException(pos, IOChecks.FIELD_POS));
			if (len == 0) return Mono.just(0);
			if (start + pos >= end) return Mono.just(-1);
			int l = len;
			if (start + pos + l > end) {
				l = (int) (end - (start + pos));
			}
			return ((ReactiveBytesIO.Writable.Seekable) io).writeBytesAt(start + pos, buf, off, l);
		});
	}
	
	@Override
	public Mono<Void> writeBytesFully(byte[] buf, int off, int len) {
		return ReactiveIOChecks.deferByteArrayAnd(this, buf, off, len,
			() -> check(position, len),
			() -> ((ReactiveBytesIO.Writable.Seekable) io).writeBytesFullyAt(start + position, buf, off, len).doOnSuccess(r -> this.position += len)
		);		
	}
	
	@Override
	public Mono<Void> writeByteAt(long pos, byte value) {
		return ReactiveIOChecks.deferNotClosedAnd(this,
			() -> check(pos, 1),
			() -> ((ReactiveBytesIO.Writable.Seekable) io).writeByteAt(start + pos, value)
		);		
	}
	
	@Override
	public Mono<Void> writeByte(byte value) {
		return ReactiveIOChecks.deferNotClosedAnd(this,
			() -> check(position, 1),
			() -> ((ReactiveBytesIO.Writable.Seekable) io).writeByteAt(start + position, value).doOnSuccess(r -> this.position++)
		);		
	}
	
}
