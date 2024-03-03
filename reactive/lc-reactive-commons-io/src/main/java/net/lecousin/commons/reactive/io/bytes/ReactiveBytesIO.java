package net.lecousin.commons.reactive.io.bytes;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.lecousin.commons.exceptions.ExceptionsUtils;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.reactive.FluxUtils;
import net.lecousin.commons.reactive.io.ReactiveIO;
import net.lecousin.commons.reactive.io.ReactiveIOChecks;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * IO working on bytes.
 */
public interface ReactiveBytesIO extends ReactiveIO {

	/**
	 * Readable bytes IO.
	 */
	interface Readable extends ReactiveBytesIO, ReactiveIO.Readable {

		/**
		 * Read a single byte.
		 * @return byte read on success, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed
		 *  <li> EOFException if no more byte can be read
		 *  <li> IOException in case an error occurred while reading
		 * </ul>
		 */
		Mono<Byte> readByte();
		
		/**
		 * Read <i>some</i> bytes. At least one byte is read, but the buffer is not necessarily
		 * filled, if no more byte can be read because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #readBytesFully(ByteBuffer)} this operation will read as much bytes
		 * as possible in a single operation, but will not fill the buffer if reading more bytes
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buffer the buffer to fill
		 * @return number of bytes read, or -1 if the end is reached, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> IOException in case an error occurred while reading</li>
		 * </ul>
		 */
		Mono<Integer> readBytes(ByteBuffer buffer);
		
		/**
		 * Read <i>some</i> bytes. At least one byte is read, but the requested bytes are not necessarily
		 * read, if no more byte can be read because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #readBytesFully(byte[], int, int)} this operation will read as much bytes
		 * as possible in a single operation, but will not fill the buffer if reading more bytes
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buf the buffer to fill
		 * @param off offset in the buffer
		 * @param len maximum number of bytes to read
		 * @return number of bytes read, or -1 if the end is reached, or <ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> NegativeValueException if off or len is negative</li>
		 *  <li> LimitExceededException if off + len &gt; buf.length</li>
		 *  <li> IOException in case an error occurred while reading</li>
		 * </ul>
		 */
		default Mono<Integer> readBytes(byte[] buf, int off, int len) {
			return ReactiveIOChecks.deferByteArray(this, buf, off, len, () -> readBytes(ByteBuffer.wrap(buf, off, len)));
		}
		
		/**
		 * Read <i>some</i> bytes. At least one byte is read, but the buffer is not necessarily
		 * filled, if no more byte can be read because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #readBytesFully(byte[])} this operation will read as much bytes
		 * as possible in a single operation, but will not fill the buffer if reading more bytes
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buf the buffer to fill
		 * @return number of bytes read, or -1 if the end is reached, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> IOException in case an error occurred while reading</li>
		 * </ul>
		 */
		default Mono<Integer> readBytes(byte[] buf) {
			return ReactiveIOChecks.deferByteArray(this, buf, 0, 0, () -> readBytes(buf, 0, buf.length));
		}
		
		/**
		 * Read some bytes and return the buffer.<br/>
		 * Compared to the method {@link #readBytes(ByteBuffer)}, this method lets the implementation allocate
		 * a buffer before to read (and potentially make a better decision if it knows in advance the amount of bytes that
		 * can be read at once). However the counterpart is that each call will allocate a new buffer, making reuse of buffers impossible.
		 * 
		 * @return a buffer if some bytes can be read, or empty in case the end is reached, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> IOException in case an error occurred while reading</li>
		 * </ul>
		 */
		Mono<ByteBuffer> readBuffer();
		
		/**
		 * Read bytes to fill the given buffer.<br/>
		 * Compared to {@link #readBytes(ByteBuffer)} this method ensures that the buffer is filled, or
		 * EOFException is thrown.
		 * 
		 * @param buffer the buffer to fill
		 * @return the buffer on success, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> EOFException if the buffer cannot be filled because it would reached the end</li>
		 *  <li> IOException in case an error occurred while reading</li>
		 * </ul>
		 */
		default Mono<ByteBuffer> readBytesFully(ByteBuffer buffer) {
			return ReactiveIOChecks.deferByteBuffer(this, buffer, () ->
				readBytes(buffer)
				.expand(nb -> {
					if (nb <= 0) return Mono.error(new EOFException());
					if (!buffer.hasRemaining()) return Mono.empty();
					return readBytes(buffer);
				}).then(Mono.just(buffer))
			);
		}
		
		/**
		 * Read <code>len</code> bytes to the given buffer.<br/>
		 * Compared to {@link #readBytes(byte[], int, int)} this method ensures that the requested
		 * number of bytes are read, or EOFException is thrown.
		 * 
		 * @param buf the buffer to fill
		 * @param off offset in the buffer
		 * @param len number of bytes to read
		 * @return the byte array on success, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> EOFException if the buffer cannot be filled because it would reached the end</li>
		 *  <li> NegativeValueException if off or len is negative</li>
		 *  <li> LimitExceededException if off + len &gt; buf.length</li>
		 *  <li> IOException in case an error occurred while reading</li>
		 * </ul>
		 */
		default Mono<byte[]> readBytesFully(byte[] buf, int off, int len) {
			return ReactiveIOChecks.deferByteArray(this, buf, off, len, () ->
				readBytes(buf, off, len).zipWith(Mono.just(0))
				.expand(tuple -> {
					int nb = tuple.getT1();
					if (nb <= 0) return Mono.error(new EOFException());
					nb += tuple.getT2();
					if (len == nb) return Mono.empty();
					return readBytes(buf, off + nb, len - nb).zipWith(Mono.just(nb));
				}).then(Mono.just(buf))
			);
		}
		
		/**
		 * Read bytes to fill the given buffer.<br/>
		 * Compared to {@link #readBytes(byte[])} this method ensures that the buffer is filled, or
		 * EOFException is thrown.
		 * 
		 * @param buf the buffer to fill
		 * @return the byte array on success, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> EOFException if the buffer cannot be filled because it would reached the end</li>
		 *  <li> IOException in case an error occurred while reading</li>
		 * </ul>
		 */
		default Mono<byte[]> readBytesFully(byte[] buf) {
			return ReactiveIOChecks.deferByteArray(this, buf, 0, 0, () -> readBytesFully(buf, 0, buf.length));
		}
		
		/**
		 * Skip up to <code>toSkip</code> bytes.
		 * 
		 * @param toSkip maximum number of bytes to skip
		 * @return the number of bytes skipped, or -1 if no byte can be skipped because end is reached, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> IOException in case an error occurred while skipping bytes</li>
		 * </ul>
		 */
		Mono<Long> skipUpTo(long toSkip);
		
		/**
		 * Skip exactly <code>toSkip</code> bytes.
		 * 
		 * @param toSkip number of bytes to skip
		 * @return empty on success, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> EOFException if the requested number of bytes cannot be skipped because it would reach the end</li>
		 *  <li> IOException in case an error occurred while skipping bytes</li>
		 * </ul>
		 */
		default Mono<Void> skipFully(long toSkip) {
			return ReactiveIOChecks.deferNotClosedAnd(this, () -> NegativeValueException.checker(toSkip, "toSkip"), () ->
				skipUpTo(toSkip).zipWith(Mono.just(0L))
				.expand(tuple -> {
					long nb = tuple.getT1();
					if (nb <= 0) return Mono.error(new EOFException());
					nb += tuple.getT2();
					if (toSkip == nb) return Mono.empty();
					return skipUpTo(toSkip - nb).zipWith(Mono.just(nb));
				}).then()
			);
		}
		
		/**
		 * Convert this readable I/O into a Flux of ByteBuffer providing all remaining bytes.<br/>
		 * 
		 * @param nbAdvancedBuffers maximum number of buffers to read in advance in case the consumption is slower than the production
		 * @return the flux of buffers, or ClosedChannelException in case the IO is closed before the returned Flux is complete
		 */
		default Flux<ByteBuffer> toFlux(int nbAdvancedBuffers) {
			return FluxUtils.createBuffered(nbAdvancedBuffers, () -> this.readBuffer().subscribeOn(getScheduler()).publishOn(Schedulers.parallel()));
		}
		
		/**
		 * Readable and Seekable bytes IO.
		 */
		interface Seekable extends ReactiveBytesIO.Readable, ReactiveIO.Seekable {

			/**
			 * Read a single byte at the given position.
			 * @param pos position
			 * @return byte read on success, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> EOFException if the position is at the end</li>
			 *  <li> NegativeValueException if pos is negative</li>
			 *  <li> IOException in case an error occurred while reading</li>
			 * </ul>
			 */
			Mono<Byte> readByteAt(long pos);
			
			/**
			 * Read <i>some</i> bytes at the given position. At least one byte is read, but the buffer is not necessarily
			 * filled, if no more byte can be read because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #readBytesFullyAt(long,ByteBuffer)} this operation will read as much bytes
			 * as possible in a single operation, but will not fill the buffer if reading more bytes
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buffer the buffer to fill
			 * @return number of bytes read, or -1 if the end is reached, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> NegativeValueException if pos is negative</li>
			 *  <li> IOException in case an error occurred while reading</li>
			 * </ul>
			 */
			Mono<Integer> readBytesAt(long pos, ByteBuffer buffer);
			
			/**
			 * Read <i>some</i> bytes at the given position. At least one byte is read, but the requested bytes are not necessarily
			 * read, if no more byte can be read because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #readBytesFullyAt(long, byte[], int, int)} this operation will read as much bytes
			 * as possible in a single operation, but will not fill the buffer if reading more bytes
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buf the buffer to fill
			 * @param off offset in the buffer
			 * @param len maximum number of bytes to read
			 * @return number of bytes read, or -1 if the end is reached, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> NegativeValueException if pos, off or len is negative</li>
			 *  <li> LimitExceededException if off + len &gt; buf.length</li>
			 *  <li> IOException in case an error occurred while reading</li>
			 * </ul>
			 */
			default Mono<Integer> readBytesAt(long pos, byte[] buf, int off, int len) {
				return ReactiveIOChecks.deferByteArray(this, pos, buf, off, len, () -> readBytesAt(pos, ByteBuffer.wrap(buf, off, len)));
			}
			
			/**
			 * Read <i>some</i> bytes at the given position. At least one byte is read, but the buffer is not necessarily
			 * filled, if no more byte can be read because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #readBytesFullyAt(long,byte[])} this operation will read as much bytes
			 * as possible in a single operation, but will not fill the buffer if reading more bytes
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buf the buffer to fill
			 * @return number of bytes read, or -1 if the end is reached, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> NegativeValueException if pos is negative</li>
			 *  <li> IOException in case an error occurred while reading</li>
			 * </ul>
			 */
			default Mono<Integer> readBytesAt(long pos, byte[] buf) {
				return ReactiveIOChecks.deferByteArray(this, pos, buf, () -> readBytesAt(pos, buf, 0, buf.length));
			}
			
			/**
			 * Read bytes at the given position to fill the given buffer.<br/>
			 * Compared to {@link #readBytesAt(long,ByteBuffer)} this method ensures that the buffer is filled, or
			 * EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buffer the buffer to fill
			 * @return empty on success, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> EOFException if the buffer cannot be filled because it would reached the end</li>
			 *  <li> NegativeValueException if pos is negative</li>
			 *  <li> IOException in case an error occurred while reading</li>
			 * </ul>
			 */
			default Mono<ByteBuffer> readBytesFullyAt(long pos, ByteBuffer buffer) {
				return ReactiveIOChecks.deferByteBuffer(this, pos, buffer, () ->
					readBytesAt(pos, buffer).zipWith(Mono.just(0L))
					.expand(tuple -> {
						long nb = tuple.getT1();
						if (nb <= 0) return Mono.error(new EOFException());
						if (!buffer.hasRemaining()) return Mono.empty();
						nb += tuple.getT2();
						return readBytesAt(pos + nb, buffer).zipWith(Mono.just(nb));
					}).then(Mono.just(buffer))
				);
			}
			
			/**
			 * Read <code>len</code> bytes at the given position into the given buffer.<br/>
			 * Compared to {@link #readBytesAt(long, byte[], int, int)} this method ensures that the requested
			 * number of bytes are read, or EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buf the buffer to fill
			 * @param off offset in the buffer
			 * @param len number of bytes to read
			 * @return empty on success, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> EOFException if the buffer cannot be filled because it would reached the end</li>
			 *  <li> NegativeValueException if pos, off or len is negative</li>
			 *  <li> LimitExceededException if off + len &gt; buf.length</li>
			 *  <li> IOException in case an error occurred while reading</li>
			 * </ul>
			 */
			default Mono<byte[]> readBytesFullyAt(long pos, byte[] buf, int off, int len) {
				return ReactiveIOChecks.deferByteArray(this, pos, buf, off, len, () ->
					readBytesAt(pos, buf, off, len).zipWith(Mono.just(0))
					.expand(tuple -> {
						int nb = tuple.getT1();
						if (nb <= 0) return Mono.error(new EOFException());
						nb += tuple.getT2();
						if (nb == len) return Mono.empty();
						return readBytesAt(pos + nb, buf, off + nb, len - nb).zipWith(Mono.just(nb));
					}).then(Mono.just(buf))
				);
			}
			
			/**
			 * Read bytes at the given position to fill the given buffer.<br/>
			 * Compared to {@link #readBytesAt(long,byte[])} this method ensures that the buffer is filled, or
			 * EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buf the buffer to fill
			 * @return empty on success, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> EOFException if the buffer cannot be filled because it would reached the end</li>
			 *  <li> NegativeValueException if pos is negative</li>
			 *  <li> IOException in case an error occurred while reading</li>
			 * </ul>
			 */
			default Mono<byte[]> readBytesFullyAt(long pos, byte[] buf) {
				return ReactiveIOChecks.deferByteArray(this, pos, buf, () -> readBytesFullyAt(pos, buf, 0, buf.length));
			}
			
			/** @return a Readable view of this IO. */
			default ReactiveBytesIO.Readable asReadableBytesIO() {
				return new ReactiveBytesIOView.Readable(this);
			}
			
		}

		
	}
	
	
	
	/**
	 * Writable bytes IO.
	 */
	interface Writable extends ReactiveBytesIO, ReactiveIO.Writable {

		/**
		 * Write a single byte.
		 * @param value byte to write
		 * @return empty on success, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> EOFException if no more byte can be written</li>
		 *  <li> IOException in case an error occurred while writing</li>
		 * </ul>
		 */
		Mono<Void> writeByte(byte value);
		
		/**
		 * Write <i>some</i> bytes. At least one byte is written, but the buffer is not necessarily
		 * consumed, if no more byte can be written because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #writeBytesFully(ByteBuffer)} this operation will write as much bytes
		 * as possible in a single operation, but will not write all if writing more bytes
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buffer the buffer to write
		 * @return number of bytes written, or -1 if the end is reached, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> IOException in case an error occurred while writing</li>
		 * </ul>
		 */
		Mono<Integer> writeBytes(ByteBuffer buffer);
		
		/**
		 * Write up to <code>len</code> bytes. At least one byte is written, but the requested number of bytes
		 * are not necessarily written, if no more byte can be written because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #writeBytesFully(byte[],int,int)} this operation will write as much bytes
		 * as possible in a single operation, but will not write all if writing more bytes
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buf the buffer to write
		 * @param off offset in the buffer
		 * @param len maximum number of bytes to write
		 * @return number of bytes written, or -1 if the end is reached, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> NegativeValueException if off or len is negative</li>
		 *  <li> LimitExceededException if off + len &gt; buf.length</li>
		 *  <li> IOException in case an error occurred while writing</li>
		 * </ul>
		 */
		default Mono<Integer> writeBytes(byte[] buf, int off, int len) {
			return ReactiveIOChecks.deferByteArray(this, buf, off, len, () -> writeBytes(ByteBuffer.wrap(buf, off, len)));
		}
		
		/**
		 * Write <i>some</i> bytes. At least one byte is written, but the buffer is not necessarily
		 * consumed, if no more byte can be written because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #writeBytesFully(byte[])} this operation will write as much bytes
		 * as possible in a single operation, but will not write all if writing more bytes
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buf the buffer to write
		 * @return number of bytes written, or -1 if the end is reached, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> IOException in case an error occurred while writing</li>
		 * </ul>
		 */
		default Mono<Integer> writeBytes(byte[] buf) {
			return ReactiveIOChecks.deferByteArray(this, buf, () -> writeBytes(buf, 0, buf.length));
		}
		
		/**
		 * Write all bytes from the given buffer.<br/>
		 * If it cannot write all bytes because end is reached, EOFException is thrown.
		 * 
		 * @param buffer the buffer to write
		 * @return empty on success, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> EOFException if all bytes cannot be written because end is reached</li>
		 *  <li> IOException in case an error occurred while writing</li>
		 * </ul>
		 */
		default Mono<Void> writeBytesFully(ByteBuffer buffer) {
			return ReactiveIOChecks.deferByteBuffer(this, buffer, () ->
				writeBytes(buffer)
				.expand(nb -> {
					if (nb <= 0) return Mono.error(new EOFException());
					if (!buffer.hasRemaining()) return Mono.empty();
					return writeBytes(buffer);
				}).then()
			);
		}
		
		/**
		 * Write all bytes from the all the given buffer.<br/>
		 * If it cannot write all bytes because end is reached, EOFException is thrown.
		 * 
		 * @param buffers the buffers to write
		 * @return empty on success, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> EOFException if all bytes cannot be written because end is reached</li>
		 *  <li> IOException in case an error occurred while writing</li>
		 * </ul>
		 */
		default Mono<Void> writeBytesFully(List<ByteBuffer> buffers) {
			return FluxUtils.deferWithCheck(
				() -> ReactiveIOChecks.checkNotClosed(this)
					.or(() -> ExceptionsUtils.nonNullChecker(buffers, "buffers")),
				() -> Flux.fromIterable(buffers)
			)
			.concatMap(this::writeBytesFully)
			.then();
		}
		
		/**
		 * Subscribe to the given Flux, and write all bytes from all emitted buffers.
		 * 
		 * @param buffers the buffers to write
		 * @return empty on success, or<ul>
		 *  <li> ClosedChannelException in case the IO is closed when the returned Mono is subscribed to</li>
		 *  <li> NullPointerException in case buffers is null</li>
		 *  <li> EOFException in case the end is reached before all bytes are written</li>
		 * </ul>
		 */
		default Mono<Void> writeBytesFully(Flux<ByteBuffer> buffers) {
			return ReactiveIOChecks.deferNotClosedAnd(this,
				() -> buffers != null ? Optional.empty() : Optional.of(new NullPointerException("buffers")),
				() -> buffers.publishOn(getScheduler()).concatMap(this::writeBytesFully).then().publishOn(Schedulers.parallel())
			);
		}
		
		/**
		 * Write exactly <code>len</code> bytes from the given buffer.<br/>
		 * If it cannot write all bytes because end is reached, EOFException is thrown.
		 * 
		 * @param buf the buffer to write
		 * @param off offset in the buffer
		 * @param len number of bytes to write
		 * @return empty on success, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> EOFException if all bytes cannot be written because end is reached</li>
		 *  <li> NegativeValueException if off or len is negative</li>
		 *  <li> LimitExceededException if off + len &gt; buf.length</li>
		 *  <li> IOException in case an error occurred while writing</li>
		 * </ul>
		 */
		default Mono<Void> writeBytesFully(byte[] buf, int off, int len) {
			return ReactiveIOChecks.deferByteArray(this, buf, off, len, () ->
				writeBytes(buf, off, len).zipWith(Mono.just(0))
				.expand(tuple -> {
					int nb = tuple.getT1();
					if (nb <= 0) return Mono.error(new EOFException());
					nb += tuple.getT2();
					if (nb == len) return Mono.empty();
					return writeBytes(buf, off + nb, len - nb).zipWith(Mono.just(nb));
				}).then()
			);
		}
		
		/**
		 * Write all bytes from the given buffer.<br/>
		 * If it cannot write all bytes because end is reached, EOFException is thrown.
		 * 
		 * @param buf the buffer to write
		 * @return empty on success, or<ul>
		 *  <li> ClosedChannelException if this IO is already closed</li>
		 *  <li> EOFException if all bytes cannot be written because end is reached</li>
		 *  <li> IOException in case an error occurred while writing</li>
		 * </ul>
		 */
		default Mono<Void> writeBytesFully(byte[] buf) {
			return ReactiveIOChecks.deferByteArray(this, buf, () -> writeBytesFully(buf, 0, buf.length));
		}

		
		/**
		 * Writable and Seekable bytes IO.
		 */
		interface Seekable extends ReactiveBytesIO.Writable, ReactiveIO.Seekable {

			/**
			 * Write a single byte at the given position.
			 * @param pos position
			 * @param value byte to write
			 * @return empty on success, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> EOFException if pos is beyond the end</li>
			 *  <li> NegativeValueException if pos is negative</li>
			 *  <li> IOException in case an error occurred while writing</li>
			 * </ul>
			 */
			Mono<Void> writeByteAt(long pos, byte value);
			
			/**
			 * Write <i>some</i> bytes at the given position. At least one byte is written, but the buffer is not necessarily
			 * consumed, if no more byte can be written because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #writeBytesFullyAt(long,ByteBuffer)} this operation will write as much bytes
			 * as possible in a single operation, but will not write all if writing more bytes
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buffer the buffer to write
			 * @return number of bytes written, or -1 if the end is reached, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> NegativeValueException if pos is negative</li>
			 *  <li> IOException in case an error occurred while writing</li>
			 * </ul>
			 */
			Mono<Integer> writeBytesAt(long pos, ByteBuffer buffer);
			
			/**
			 * Write up to <code>len</code> bytes at the given position. At least one byte is written, but the requested number of bytes
			 * are not necessarily written, if no more byte can be written because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #writeBytesFullyAt(long,byte[],int,int)} this operation will write as much bytes
			 * as possible in a single operation, but will not write all if writing more bytes
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buf the buffer to write
			 * @param off offset in the buffer
			 * @param len maximum number of bytes to write
			 * @return number of bytes written, or -1 if the end is reached, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> NegativeValueException if pos, off or len is negative</li>
			 *  <li> LimitExceededException if off + len &gt; buf.length</li>
			 *  <li> IOException in case an error occurred while writing</li>
			 * </ul>
			 */
			default Mono<Integer> writeBytesAt(long pos, byte[] buf, int off, int len) {
				return ReactiveIOChecks.deferByteArray(this, pos, buf, off, len, () -> writeBytesAt(pos, ByteBuffer.wrap(buf, off, len)));
			}
			
			/**
			 * Write <i>some</i> bytes at the given position. At least one byte is written, but the buffer is not necessarily
			 * consumed, if no more byte can be written because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #writeBytesFully(byte[])} this operation will write as much bytes
			 * as possible in a single operation, but will not write all if writing more bytes
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buf the buffer to write
			 * @return number of bytes written, or -1 if the end is reached, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> NegativeValueException if pos is negative</li>
			 *  <li> IOException in case an error occurred while writing</li>
			 * </ul>
			 */
			default Mono<Integer> writeBytesAt(long pos, byte[] buf) {
				return ReactiveIOChecks.deferByteArray(this, pos, buf, () -> writeBytesAt(pos, buf, 0, buf.length));
			}
			
			/**
			 * Write all bytes from the given buffer at the given position.<br/>
			 * If it cannot write all bytes because end is reached, EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buffer the buffer to write
			 * @return empty on success, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> EOFException if all bytes cannot be written because end is reached</li>
			 *  <li> NegativeValueException if pos is negative</li>
			 *  <li> IOException in case an error occurred while writing</li>
			 * </ul>
			 */
			default Mono<Void> writeBytesFullyAt(long pos, ByteBuffer buffer) {
				return ReactiveIOChecks.deferByteBuffer(this, pos, buffer, () ->
					writeBytesAt(pos, buffer).zipWith(Mono.just(0L))
					.expand(tuple -> {
						long nb = tuple.getT1();
						if (nb <= 0) return Mono.error(new EOFException());
						if (!buffer.hasRemaining()) return Mono.empty();
						nb += tuple.getT2();
						return writeBytesAt(pos + nb, buffer).zipWith(Mono.just(nb));
					}).then()
				);
			}
			
			/**
			 * Write exactly <code>len</code> bytes from the given buffer at the given position.<br/>
			 * If it cannot write all bytes because end is reached, EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buf the buffer to write
			 * @param off offset in the buffer
			 * @param len number of bytes to write
			 * @return empty on success, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> EOFException if all bytes cannot be written because end is reached</li>
			 *  <li> NegativeValueException if pos, off or len is negative</li>
			 *  <li> LimitExceededException if off + len &gt; buf.length</li>
			 *  <li> IOException in case an error occurred while writing</li>
			 * </ul>
			 */
			default Mono<Void> writeBytesFullyAt(long pos, byte[] buf, int off, int len) {
				return ReactiveIOChecks.deferByteArray(this, pos, buf, off, len, () ->
					writeBytesAt(pos, buf, off, len).zipWith(Mono.just(0))
					.expand(tuple -> {
						int nb = tuple.getT1();
						if (nb <= 0) return Mono.error(new EOFException());
						nb += tuple.getT2();
						if (nb == len) return Mono.empty();
						return writeBytesAt(pos + nb, buf, off + nb, len - nb).zipWith(Mono.just(nb));
					}).then()
				);
			}
			
			/**
			 * Write all bytes from the given buffer at the given position.<br/>
			 * If it cannot write all bytes because end is reached, EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buf the buffer to write
			 * @return empty on success, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> EOFException if all bytes cannot be written because end is reached</li>
			 *  <li> NegativeValueException if pos is negative</li>
			 *  <li> IOException in case an error occurred while writing</li>
			 * </ul>
			 */
			default Mono<Void> writeBytesFullyAt(long pos, byte[] buf) {
				return ReactiveIOChecks.deferByteArray(this, pos, buf, () -> writeBytesFullyAt(pos, buf, 0, buf.length));
			}
			
			/**
			 * Write all bytes ath the given position from the all the given buffer.<br/>
			 * If it cannot write all bytes because end is reached, EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buffers the buffers to write
			 * @return empty on success, or<ul>
			 *  <li> ClosedChannelException if this IO is already closed</li>
			 *  <li> NegativeValueException if pos is negative</li>
			 *  <li> EOFException if all bytes cannot be written because end is reached</li>
			 *  <li> IOException in case an error occurred while writing</li>
			 * </ul>
			 */
			default Mono<Void> writeBytesFullyAt(long pos, List<ByteBuffer> buffers) {
				return FluxUtils.deferWithCheck(
					() -> ReactiveIOChecks.checkNotClosed(this)
						.or(() -> NegativeValueException.checker(pos, IOChecks.FIELD_POS))
						.or(() -> ExceptionsUtils.nonNullChecker(buffers, "buffers")),
					() -> {
						List<Tuple2<Long, ByteBuffer>> list = new ArrayList<>(buffers.size());
						long p = pos;
						for (ByteBuffer b : buffers) {
							int r = b.remaining();
							if (r > 0) {
								list.add(Tuples.of(p, b));
								p += r;
							}
						}
						return Flux.fromIterable(list);
					}
				)
				.concatMap(tuple -> writeBytesFullyAt(tuple.getT1(), tuple.getT2()))
				.then();
			}
			
			/** @return a Writable view of this IO. */
			default ReactiveBytesIO.Writable asWritableBytesIO() {
				return ReactiveBytesIOView.Writable.of(this);
			}
			
			/** Writable Seekable and Appendable BytesIO. */
			interface Appendable extends ReactiveBytesIO.Writable.Seekable, ReactiveIO.Writable.Appendable {
				
			}
			
			/** Writable Seekable and Resizable BytesIO. */
			interface Resizable extends ReactiveBytesIO.Writable.Seekable, ReactiveIO.Writable.Resizable {
				
				/** @return a non-resizable view of this BytesIO. */
				default ReactiveBytesIO.Writable.Seekable asNonResizableWritableSeekableBytesIO() {
					return ReactiveBytesIOView.Writable.Seekable.of(this);
				}
				
			}
			
			/** Writable Seekable Appendable and Resizable BytesIO. */
			interface AppendableResizable extends ReactiveBytesIO.Writable.Seekable.Appendable, ReactiveBytesIO.Writable.Seekable.Resizable {
				
			}
		}
		
	}
	
	/** Readable and Writable Seekable BytesIO. */
	interface ReadWrite extends ReactiveBytesIO.Readable.Seekable, ReactiveBytesIO.Writable.Seekable {
		
		/** @return a Readable and Seekable view of this IO. */
		default ReactiveBytesIO.Readable.Seekable asReadableSeekableBytesIO() {
			return new ReactiveBytesIOView.Readable.Seekable(this);
		}
		
		/** @return a Writable and Seekable view of this IO. */
		default ReactiveBytesIO.Writable.Seekable asWritableSeekableBytesIO() {
			return ReactiveBytesIOView.Writable.Seekable.of(this);
		}
		
		/** Readable and Writable Seekable Resizable BytesIO. */
		interface Resizable extends ReadWrite, ReactiveBytesIO.Writable.Seekable.Resizable {
			
			/** @return a non-resizable view of this BytesIO. */
			default ReactiveBytesIO.ReadWrite asNonResizableReadWriteBytesIO() {
				return ReactiveBytesIOView.ReadWrite.of(this);
			}
			
		}
		
		/** Readable and Writable Seekable Appendable BytesIO. */
		interface Appendable extends ReadWrite, ReactiveBytesIO.Writable.Seekable.Appendable {
			
		}
		
		/** Readable and Writable Seekable Appendable and Resizable BytesIO. */
		interface AppendableResizable extends ReadWrite.Appendable, ReadWrite.Resizable {
			
		}
		
	}

}
