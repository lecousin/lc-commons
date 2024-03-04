package net.lecousin.commons.reactive.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;

import net.lecousin.commons.function.FunctionWrapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Event emitting an object.
 * 
 * @param <T> type of event
 */
public class ReactiveEvent<T> implements ReactiveObjectListenable<T> {

	private List<Function<T, Publisher<?>>> listeners = new LinkedList<>();
	
	@Override
	public boolean hasListeners() {
		return !listeners.isEmpty();
	}
	
	@Override
	public void listen(Function<T, Publisher<?>> listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void unlisten(Function<T, Publisher<?>> listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	@Override
	public void listen(Supplier<Publisher<?>> listener) {
		listen(FunctionWrapper.asFunction(listener));
	}
	
	@SuppressWarnings({"unlikely-arg-type", "java:S2175"})
	@Override
	public void unlisten(Supplier<Publisher<?>> listener) {
		synchronized (listeners) {
			listeners.removeIf(element -> element.equals(listener));
		}
	}
	
	/**
	 * Emit an event.
	 * @param event the event to send to the listeners
	 * @return a mono that will complete once all listeners are completed
	 */
	public Mono<Void> emit(Mono<T> event) {
		return event.flatMap(value -> Mono.defer(() -> {
			List<Function<T, Publisher<?>>> list;
			synchronized (this) {
				list = new ArrayList<>(listeners);
			}
			return callListeners(value, list);
		}));
	}
	
	/**
	 * Utility method that calls listeners, and aggregate errors if needed.
	 * @param <T> type of event
	 * @param ev event
	 * @param listeners listeners
	 * @return empty once all listeners completed
	 */
	public static <T> Mono<Void> callListeners(T ev, List<Function<T, Publisher<?>>> listeners) {
		List<Throwable> errors = new LinkedList<>();
		return Flux.fromIterable(listeners)
			.flatMap(listener ->
				Mono.defer(() -> Flux.from(listener.apply(ev)).then())
				.onErrorResume(error -> {
					synchronized (errors) {
						errors.add(error);
					}
					return Mono.empty();
				})
			)
			.then(Mono.defer(() -> {
				if (errors.isEmpty()) return Mono.empty();
				if (errors.size() == 1) return Mono.error(errors.get(0));
				RuntimeException e = new RuntimeException("Error calling event listeners");
				for (Throwable error : errors) e.addSuppressed(error);
				return Mono.error(e);
			}));
	}
	
}
