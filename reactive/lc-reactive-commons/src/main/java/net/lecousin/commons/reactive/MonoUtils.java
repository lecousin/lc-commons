package net.lecousin.commons.reactive;

import java.util.Optional;
import java.util.function.Supplier;

import reactor.core.publisher.Mono;

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
	
}
