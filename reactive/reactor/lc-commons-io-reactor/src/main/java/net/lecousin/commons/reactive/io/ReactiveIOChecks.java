package net.lecousin.commons.reactive.io;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Optional;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.lecousin.commons.exceptions.ExceptionsUtils;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.reactive.MonoUtils;
import reactor.core.publisher.Mono;

/**
 * Utilities to check I/O state and input arguments on I/O operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReactiveIOChecks {
	
	/**
	 * Check if the given I/O is closed.
	 * 
	 * @param io the I/O
	 * @return ClosedChannelException if the I/O is closed, else empty
	 */
	public static Optional<Exception> checkNotClosed(ReactiveIO io) {
		return io.isClosed() ? Optional.of(new ClosedChannelException()) : Optional.empty();
	}
	
	/**
	 * Create a deferred Mono, that first checks that the I/O is not closed when subscribed, then delegate
	 * to the given Supplier the production of the Mono.
	 * 
	 * @param <T> type of result
	 * @param io I/O
	 * @param deferred the operation
	 * @return the deferred Mono
	 */
	public static <T> Mono<T> deferNotClosed(ReactiveIO io, Supplier<Mono<T>> deferred) {
		return MonoUtils.deferWithCheck(() -> checkNotClosed(io), deferred);
	}
	
	/**
	 * Create a deferred Mono, that first checks that the I/O is not closed when subscribed,
	 * then call the additional check,
	 * and finally delegate to the given Supplier the production of the Mono.
	 * 
	 * @param <T> type of result
	 * @param io I/O
	 * @param and additional checks
	 * @param deferred the operation
	 * @return the deferred Mono
	 */
	public static <T> Mono<T> deferNotClosedAnd(ReactiveIO io, Supplier<Optional<Exception>> and, Supplier<Mono<T>> deferred) {
		return MonoUtils.deferWithCheck(() -> checkNotClosed(io).or(and), deferred);
	}
	
	/**
	 * Create a deferred Mono, that first checks that the I/O is not closed when subscribed,
	 * then checks the given byte array parameters are valid,
	 * and finally delegate to the given Supplier the production of the Mono.
	 * 
	 * @param <T> type of result
	 * @param io I/O
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @param deferred the operation
	 * @return the deferred Mono
	 */
	public static <T> Mono<T> deferByteArray(ReactiveIO io, byte[] buf, int off, int len, Supplier<Mono<T>> deferred) {
		return deferNotClosedAnd(io, () -> IOChecks.arrayChecker(buf, off, len), deferred);
	}

	/**
	 * Create a deferred Mono, that first checks that the I/O is not closed when subscribed,
	 * then checks the given byte array parameters are valid as well as the given position,
	 * and finally delegate to the given Supplier the production of the Mono.
	 * 
	 * @param <T> type of result
	 * @param io I/O
	 * @param pos position
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @param deferred the operation
	 * @return the deferred Mono
	 */
	public static <T> Mono<T> deferByteArray(ReactiveIO io, long pos, byte[] buf, int off, int len, Supplier<Mono<T>> deferred) {
		return deferNotClosedAnd(io, () -> NegativeValueException.checker(pos, IOChecks.FIELD_POS).or(() -> IOChecks.arrayChecker(buf, off, len)), deferred);
	}
	
	/**
	 * Create a deferred Mono, that first checks that the I/O is not closed when subscribed,
	 * then checks the given byte array parameters are valid,
	 * and finally delegate to the given Supplier the production of the Mono.
	 * 
	 * @param <T> type of result
	 * @param io I/O
	 * @param buf buffer
	 * @param deferred the operation
	 * @return the deferred Mono
	 */
	public static <T> Mono<T> deferByteArray(ReactiveIO io, byte[] buf, Supplier<Mono<T>> deferred) {
		return deferNotClosedAnd(io, () -> IOChecks.arrayChecker(buf, 0, 0), deferred);
	}
	
	/**
	 * Create a deferred Mono, that first checks that the I/O is not closed when subscribed,
	 * then checks the given byte array parameters are valid as well as the given position,
	 * and finally delegate to the given Supplier the production of the Mono.
	 * 
	 * @param <T> type of result
	 * @param io I/O
	 * @param pos position
	 * @param buf buffer
	 * @param deferred the operation
	 * @return the deferred Mono
	 */
	public static <T> Mono<T> deferByteArray(ReactiveIO io, long pos, byte[] buf, Supplier<Mono<T>> deferred) {
		return deferByteArray(io, pos, buf, 0, 0, deferred);
	}
	
	/**
	 * Create a deferred Mono, that first checks that the I/O is not closed when subscribed,
	 * then checks the given byte array parameters are valid,
	 * then call the additional checker,
	 * and finally delegate to the given Supplier the production of the Mono.
	 * 
	 * @param <T> type of result
	 * @param io I/O
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @param and additional checks
	 * @param deferred the operation
	 * @return the deferred Mono
	 */
	public static <T> Mono<T> deferByteArrayAnd(ReactiveIO io, byte[] buf, int off, int len, Supplier<Optional<Exception>> and, Supplier<Mono<T>> deferred) {
		return MonoUtils.deferWithCheck(
			() -> checkNotClosed(io).or(() -> IOChecks.arrayChecker(buf, off, len)).or(and),
			deferred);
	}
	
	/**
	 * Create a deferred Mono, that first checks that the I/O is not closed when subscribed,
	 * then checks the given buffer is not null,
	 * and finally delegate to the given Supplier the production of the Mono.
	 * 
	 * @param <T> type of result
	 * @param io I/O
	 * @param buffer buffer
	 * @param deferred the operation
	 * @return the deferred Mono
	 */
	public static <T> Mono<T> deferByteBuffer(ReactiveIO io, ByteBuffer buffer, Supplier<Mono<T>> deferred) {
		return MonoUtils.deferWithCheck(
			() -> checkNotClosed(io).or(() -> ExceptionsUtils.nonNullChecker(buffer, IOChecks.FIELD_BUFFER)),
			deferred);
	}
	
	/**
	 * Create a deferred Mono, that first checks that the I/O is not closed when subscribed,
	 * then checks the given buffer is not null and the given position is valid,
	 * and finally delegate to the given Supplier the production of the Mono.
	 * 
	 * @param <T> type of result
	 * @param io I/O
	 * @param pos position
	 * @param buffer buffer
	 * @param deferred the operation
	 * @return the deferred Mono
	 */
	public static <T> Mono<T> deferByteBuffer(ReactiveIO io, long pos, ByteBuffer buffer, Supplier<Mono<T>> deferred) {
		return MonoUtils.deferWithCheck(
			() -> checkNotClosed(io)
				.or(() -> NegativeValueException.checker(pos, IOChecks.FIELD_POS))
				.or(() -> ExceptionsUtils.nonNullChecker(buffer, IOChecks.FIELD_BUFFER)),
			deferred);
	}
	
	/**
	 * Create a deferred Mono, that first checks that the I/O is not closed when subscribed,
	 * then checks the given buffer is not null,
	 * then call the given additional checks,
	 * and finally delegate to the given Supplier the production of the Mono.
	 * 
	 * @param <T> type of result
	 * @param io I/O
	 * @param buffer buffer
	 * @param and additional checks
	 * @param deferred the operation
	 * @return the deferred Mono
	 */
	public static <T> Mono<T> deferByteBufferAnd(ReactiveIO io, ByteBuffer buffer, Supplier<Optional<Exception>> and, Supplier<Mono<T>> deferred) {
		return MonoUtils.deferWithCheck(
			() -> checkNotClosed(io).or(() -> ExceptionsUtils.nonNullChecker(buffer, IOChecks.FIELD_BUFFER)).or(and),
			deferred);
	}

}
