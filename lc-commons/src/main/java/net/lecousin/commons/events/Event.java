package net.lecousin.commons.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import net.lecousin.commons.function.FunctionWrapper;

/**
 * Event emitting an object.
 * <p>
 * Note that listeners subscribing to the event before it is emitted are ensured to be called in order,
 * however if a listener subscribes while the others listeners are being called, it is not called before a new event is emitted.
 * </p>
 * 
 * @param <T> type of event
 */
@Slf4j
public class Event<T> implements ObjectListenable<T> {

	private List<Consumer<T>> listeners = new LinkedList<>();
	
	@Override
	public boolean hasListeners() {
		return !listeners.isEmpty();
	}
	
	@Override
	public void listen(Consumer<T> listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	@Override
	public void listen(Runnable listener) {
		listen(FunctionWrapper.asConsumer(listener));
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void unlisten(Runnable listener) {
		synchronized (listeners) {
			listeners.removeIf(c -> c.equals(listener));
		}
	}

	@Override
	public void unlisten(Consumer<T> listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	/**
	 * Emit an event.
	 * @param event the event to send to the listeners
	 */
	public void emit(T event) {
		List<Consumer<T>> list;
		synchronized (this) {
			list = new ArrayList<>(listeners);
		}
		for (Consumer<T> listener : list)
			try {
				listener.accept(event);
			} catch (Exception e) {
				log.error("Event listener error", e);
			}
	}
	
}
