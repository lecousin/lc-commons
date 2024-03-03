package net.lecousin.commons.reactive.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import lombok.extern.slf4j.Slf4j;
import net.lecousin.commons.function.FunctionWrapper;
import reactor.core.publisher.Mono;

/**
 * Event emitting an object.
 * 
 * @param <T> type of event
 */
@Slf4j
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
			listeners.remove(listener);
		}
	}
	
	/**
	 * Emit an event.
	 * @param event the event to send to the listeners
	 * @return a mono that subscribe to all listeners to signal the event
	 */
	public Mono<Void> emit(Mono<T> event) {
		return event.flatMap(value -> Mono.fromRunnable(() -> {
			List<Function<T, Publisher<?>>> list;
			synchronized (this) {
				list = new ArrayList<>(listeners);
			}
			for (Function<T, Publisher<?>> listener : list)
				callListener(value, listener);
		}));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> void callListener(T ev, Function<T, Publisher<?>> listener) {
		try {
			listener.apply(ev).subscribe(new Subscriber() {
				@Override
				public void onSubscribe(Subscription s) {
					// nothing
				}

				@Override
				public void onNext(Object t) {
					// nothing
				}

				@Override
				public void onError(Throwable t) {
					log.error("Event listener error", t);
				}

				@Override
				public void onComplete() {
					// nothing
				}
				
			});
		} catch (Exception e) {
			log.error("Event listener error", e);
		}
	}
	
}
