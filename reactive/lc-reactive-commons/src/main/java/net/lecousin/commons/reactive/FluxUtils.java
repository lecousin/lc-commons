package net.lecousin.commons.reactive;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import reactor.core.publisher.Flux;

/** Utilities for Flux. */
public final class FluxUtils {

	private FluxUtils() {
		// no instance
	}
	
	/**
	 * Similar to {@link Flux#bufferUntil(Predicate)} but uses an accumulator function and a predicate on the accumulated value
	 * to decide when to stop buffering and emit a list of items.<br/>
	 * For example, for a Flux of ByteBuffer, we may want to create bunches of buffers for a certain amount of bytes.
	 * 
	 * @param <T> type of item
	 * @param <A> type of the accumulator result
	 * @param source source flux to buffer
	 * @param initial initial value for the accumulator
	 * @param accumulator accumulator function, taking as arguments the previous accumulation result and the new item
	 * @param accumulationPredicate predicate that decide to stop buffering based on the accumulation result
	 * @return a Flux of List
	 */
	public static <T, A> Flux<List<T>> bufferUntil(Flux<T> source, A initial, BiFunction<A, T, A> accumulator, Predicate<A> accumulationPredicate) {
		return source.bufferUntil(new Predicate<>() {
			private A value = initial;
			@Override
			public boolean test(T element) {
				value = accumulator.apply(value, element);
				if (accumulationPredicate.test(value)) {
					value = initial;
					return true;
				}
				return false;
			}
		});
	}
	
	/**
	 * Create a {@link Flux#defer}} that first execute the checks then execute the operation.
	 *  
	 * @param <T> type of item
	 * @param check performs the checks and return an optional exception
	 * @param doIfNoError performs the operation if no exception is returned by the checks
	 * @return the deferred Flux
	 */
	public static <T> Flux<T> deferWithCheck(Supplier<Optional<Exception>> check, Supplier<Flux<T>> doIfNoError) {
		return Flux.defer(() -> {
			Optional<Exception> error = check.get();
			if (error.isPresent()) return Flux.error(error.get());
			return doIfNoError.get();
		});
	}
}
