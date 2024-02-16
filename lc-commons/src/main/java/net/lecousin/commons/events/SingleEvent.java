package net.lecousin.commons.events;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import net.lecousin.commons.function.FunctionWrapper;

/**
 * Event that can occur only once.
 * The emitted event is kept so it can be retrieved at any time.
 * The listeners are ensured to be called in order.
 * @param <T> type of event
 */
@Slf4j
public class SingleEvent<T> implements ObjectListenable<T> {

	private List<Consumer<T>> listeners;
	private T emitted;
	
	/** Constructor. */
	public SingleEvent() {
		listeners = new LinkedList<>();
	}
	
	/** Event already emitted.
	 * @param event event
	 */
	public SingleEvent(T event) {
		this.emitted = event;
	}
	
	/** Check is the event has been already emitted.
	 * <p>Note: this method can return true while some listeners are not yet called.</p>
	 * 
	 * @return true if the event has been emitted.
	 */
	public boolean isEmitted() {
		return this.emitted != null;
	}
	
	/** @return emitted event, or null if not yet emitted. */
	public T getEmittedEvent() {
		return this.emitted;
	}
	
	@Override
	public synchronized boolean hasListeners() {
		return listeners != null && !listeners.isEmpty();
	}
	
	@Override
	public void listen(Consumer<T> listener) {
		synchronized (this) {
			if (listeners != null) {
				listeners.add(listener);
				return;
			}
		}
		listener.accept(emitted);
	}
	
	@Override
	public void listen(Runnable listener) {
		synchronized (this) {
			if (listeners != null) {
				listeners.add(FunctionWrapper.asConsumer(listener));
				return;
			}
		}
		listener.run();
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public synchronized void unlisten(Runnable listener) {
		if (listeners != null)
			listeners.removeIf(c -> c.equals(listener));
	}

	@Override
	public synchronized void unlisten(Consumer<T> listener) {
		if (listeners != null)
			listeners.remove(listener);
	}

	
	/**
	 * Emit the event
	 * @param event event
	 * @throws IllegalStateException if the event is already emitted
	 */
	public void emit(T event) {
		emit(event, null);
	}

	/**
	 * Emit the event
	 * @param event event
	 * @param beforeListeners if specified, it is called before emission in a synchronized section, giving the opportunity to cancel emission by returning false.
	 * @throws IllegalStateException if the event is already emitted
	 */
	public void emit(T event, BooleanSupplier beforeListeners) {
		synchronized (this) {
			if (this.emitted != null)
				throw new IllegalStateException("Event already emitted");
			if (beforeListeners != null && !Boolean.TRUE.equals(beforeListeners.getAsBoolean()))
				return;
			this.emitted = event;
		}
		do {
			List<Consumer<T>> list;
			synchronized (this) {
				if (listeners.isEmpty()) {
					listeners = null;
					break;
				}
				list = listeners;
				listeners = new LinkedList<>();
			}
			for (Consumer<T> listener : list)
				try {
					listener.accept(event);
				} catch (Exception e) {
					log.error("Event listener error", e);
				}
		} while (true);
	}
	
	/**
	 * Block the current thread until the event is emitted.
	 * @param timeout maximum number of milliseconds to wait, 0 or negative value to wait indefinitely.
	 * @return the event
	 * @throws InterruptedException if the thread is interrupted
	 * @throws TimeoutException if the timeout is reached before emission
	 */
	@SuppressWarnings("java:S2446")
	public T waitEvent(long timeout) throws InterruptedException, TimeoutException {
		if (this.emitted != null)
			return this.emitted;
		Object lock = new Object();
		Consumer<T> listener = e -> {
			synchronized (lock) {
				lock.notify();
			}
		};
		synchronized (this) {
			if (listeners == null)
				return this.emitted;
			listeners.add(listener);
		}
		long start = System.currentTimeMillis();
		while (this.emitted == null) {
			long remaining = timeout > 0 ? timeout - (System.currentTimeMillis() - start) : Long.MAX_VALUE;
			if (remaining <= 0)
				throw new TimeoutException(this.toString());
			internalWait(lock, remaining);
		}
		return this.emitted;
	}
	
	@Generated // cannot reproduce a lock wake up without notify
	@SuppressWarnings({"java:S2445", "java:S2274"})
	private void internalWait(Object lock, long remaining) throws InterruptedException {
		synchronized (lock) {
			if (this.emitted == null)
				lock.wait(remaining);
		}
	}
	
}
