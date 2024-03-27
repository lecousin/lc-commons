package net.lecousin.commons.reactive;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.function.FailableRunnable;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;

/** Utilities for Mono. */
public final class MonoUtils {

	private MonoUtils() {
		// no instance
	}
	
	/**
	 * Create a {@link Mono#defer}} that first execute the checks then execute the operation.
	 *  
	 * @param <T> type of item
	 * @param check performs the checks and return an optional exception
	 * @param doIfNoError performs the operation if no exception is returned by the checks
	 * @return the deferred Mono
	 */
	public static <T> Mono<T> deferWithCheck(Supplier<Optional<Exception>> check, Supplier<Mono<T>> doIfNoError) {
		return Mono.defer(() -> {
			Optional<Exception> error = check.get();
			if (error.isPresent()) return Mono.error(error.get());
			return doIfNoError.get();
		});
	}
	
	/**
	 * Similar to {@link Mono#fromRunnable(Runnable)} but using a FailableRunnable.
	 * @param runnable failable runnable
	 * @return empty on success, or the error thrown by the runnable
	 */
	public static Mono<Void> fromFailableRunnable(FailableRunnable<? extends Exception> runnable) {
		return Mono.fromCallable(() -> {
			runnable.run();
			return null;
		});
	}
	
	/**
	 * Zip the given Mono, like {@link Mono#zip(Mono, Mono)}, but ensure they are processed
	 * in parallel by calling publishOn(Shedulers.parallel()) on each Mono.
	 * @param <A> type of first Mono
	 * @param <B> type of second Mono
	 * @param a first Mono
	 * @param b second Mono
	 * @return result
	 */
	public static <A, B> Mono<Tuple2<A, B>> zipParallel(Mono<A> a, Mono<B> b) {
		return Mono.zip(
			a.publishOn(Schedulers.parallel()),
			b.publishOn(Schedulers.parallel())
		);
	}
	
	/**
	 * Zip the given Mono, like {@link Mono#zip(Mono, Mono, Mono)}, but ensure they are processed
	 * in parallel by calling publishOn(Shedulers.parallel()) on each Mono.
	 * @param <A> type of first Mono
	 * @param <B> type of second Mono
	 * @param <C> type of third Mono
	 * @param a first Mono
	 * @param b second Mono
	 * @param c third Mono
	 * @return result
	 */
	public static <A, B, C> Mono<Tuple3<A, B, C>> zipParallel(Mono<A> a, Mono<B> b, Mono<C> c) {
		return Mono.zip(
				a.publishOn(Schedulers.parallel()),
				b.publishOn(Schedulers.parallel()),
				c.publishOn(Schedulers.parallel())
		);
	}
	
	/**
	 * Zip the given Mono, like {@link Mono#zip(Mono, Mono, Mono, Mono)}, but ensure they are processed
	 * in parallel by calling publishOn(Shedulers.parallel()) on each Mono.
	 * @param <A> type of first Mono
	 * @param <B> type of second Mono
	 * @param <C> type of third Mono
	 * @param <D> type of fourth Mono
	 * @param a first Mono
	 * @param b second Mono
	 * @param c third Mono
	 * @param d fourth Mono
	 * @return result
	 */
	public static <A, B, C, D> Mono<Tuple4<A, B, C, D>> zipParallel(Mono<A> a, Mono<B> b, Mono<C> c, Mono<D> d) {
		return Mono.zip(
				a.publishOn(Schedulers.parallel()),
				b.publishOn(Schedulers.parallel()),
				c.publishOn(Schedulers.parallel()),
				d.publishOn(Schedulers.parallel())
		);
	}

	
	/**
	 * Launch the given Mono in parallel.
	 * @param a first Mono
	 * @param b second Mono
	 * @return a Mono completed once all are completed
	 */
	public static Mono<Void> zipVoidParallel(Mono<Void> a, Mono<Void> b) {
		return zipParallel(
			a.then(Mono.just(0)),
			b.then(Mono.just(0))
		).then();
	}
	
	/**
	 * Launch the given Mono in parallel.
	 * @param a first Mono
	 * @param b second Mono
	 * @param c third Mono
	 * @return a Mono completed once all are completed
	 */
	public static Mono<Void> zipVoidParallel(Mono<Void> a, Mono<Void> b, Mono<Void> c) {
		return zipParallel(
			a.then(Mono.just(0)),
			b.then(Mono.just(0)),
			c.then(Mono.just(0))
		).then();
	}
	
	/**
	 * Launch the given Mono in parallel.
	 * @param a first Mono
	 * @param b second Mono
	 * @param c third Mono
	 * @param d fourth Mono
	 * @return a Mono completed once all are completed
	 */
	public static Mono<Void> zipVoidParallel(Mono<Void> a, Mono<Void> b, Mono<Void> c, Mono<Void> d) {
		return zipParallel(
			a.then(Mono.just(0)),
			b.then(Mono.just(0)),
			c.then(Mono.just(0)),
			d.then(Mono.just(0))
		).then();
	}
	
}
