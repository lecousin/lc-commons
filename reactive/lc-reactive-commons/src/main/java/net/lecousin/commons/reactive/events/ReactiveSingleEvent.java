package net.lecousin.commons.reactive.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;

import net.lecousin.commons.function.FunctionWrapper;
import reactor.core.publisher.Mono;

/**
 * Event that can be emitted only once.
 * 
 * @param <T> type of event
 */
public class ReactiveSingleEvent<T> implements ReactiveObjectListenable<T> {

	private List<Function<T, Publisher<?>>> listeners = new LinkedList<>();
	private T event;
	private boolean emitted = false;
	
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
		return emitted;
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
		ReactiveEvent.callListeners(event, List.of(listener)).subscribe();
	}
	
	@Override
	public void listen(Supplier<Publisher<?>> listener) {
		synchronized (this) {
			if (listeners != null) {
				listeners.add(FunctionWrapper.asFunction(listener));
				return;
			}
		}
		ReactiveEvent.callListeners(event, List.of(FunctionWrapper.asFunction(listener))).subscribe();
	}
	
	@SuppressWarnings({"unlikely-arg-type", "java:S2175"})
	@Override
	public synchronized void unlisten(Supplier<Publisher<?>> listener) {
		if (listeners != null)
			listeners.removeIf(element -> element.equals(listener));
	}

	@Override
	public synchronized void unlisten(Function<T, Publisher<?>> listener) {
		if (listeners != null)
			listeners.remove(listener);
	}

	
	/** Emit the event.
	 * 
	 * @param ev the event
	 * @return a Mono that emits the event, call listeners, and complete when all listeners are completed.
	 */
	public Mono<Void> emit(Mono<T> ev) {
		if (emitted) return Mono.empty();
		this.emitted = true;
		return Mono.defer(() -> ev.flatMap(value -> {
			List<Function<T, Publisher<?>>> list;
			synchronized (this) {
				list = new ArrayList<>(listeners);
				listeners = null;
				this.event = value;
			}
			return ReactiveEvent.callListeners(value, list);
		}));
	}
	
}
