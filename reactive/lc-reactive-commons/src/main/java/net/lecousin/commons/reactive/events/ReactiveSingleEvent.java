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
 * Event that can be emitted only once.
 * 
 * @param <T> type of event
 */
@Slf4j
public class ReactiveSingleEvent<T> implements ReactiveObjectListenable<T> {

	private List<Function<T, Publisher<?>>> listeners = new LinkedList<>();
	private T event;
	
	/** Constructor. */
	public ReactiveSingleEvent() {
		listeners = new LinkedList<>();
	}
	
	/** Check is the event has been already emitted.
	 * <p>Note: this method can return true while some listeners are not yet called.</p>
	 * 
	 * @return true if the event has been emitted.
	 */
	public boolean isEmitted() {
		return this.event != null;
	}
	
	@Override
	public synchronized boolean hasListeners() {
		return listeners != null && !listeners.isEmpty();
	}
	
	@Override
	public void listen(Function<T, Publisher<?>> listener) {
		synchronized (this) {
			if (listeners != null) {
				listeners.add(listener);
				return;
			}
		}
		callListener(listener);
	}
	
	@Override
	public void listen(Supplier<Publisher<?>> listener) {
		synchronized (this) {
			if (listeners != null) {
				listeners.add(FunctionWrapper.asFunction(listener));
				return;
			}
		}
		callListener(FunctionWrapper.asFunction(listener));
	}
	
	@SuppressWarnings({"unlikely-arg-type", "java:S2175"})
	@Override
	public synchronized void unlisten(Supplier<Publisher<?>> listener) {
		if (listeners != null)
			listeners.remove(listener);
	}

	@Override
	public synchronized void unlisten(Function<T, Publisher<?>> listener) {
		if (listeners != null)
			listeners.remove(listener);
	}

	
	/** Emit the event
	 * 
	 * @param ev the event
	 * @return a Mono that emits the event and subscribe to all listeners
	 */
	public Mono<Void> emit(Mono<T> ev) {
		return ev.flatMap(value -> Mono.fromRunnable(() -> {
			this.event = value;
			List<Function<T, Publisher<?>>> list;
			synchronized (this) {
				list = new ArrayList<>(listeners);
				listeners = null;
			}
			for (Function<T, Publisher<?>> listener : list)
				callListener(listener);
		}));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void callListener(Function<T, Publisher<?>> listener) {
		try {
			listener.apply(event).subscribe(new Subscriber() {
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
