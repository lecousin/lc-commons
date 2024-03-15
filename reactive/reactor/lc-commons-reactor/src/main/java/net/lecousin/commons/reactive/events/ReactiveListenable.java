package net.lecousin.commons.reactive.events;

import java.util.function.Supplier;

import org.reactivestreams.Publisher;

import net.lecousin.commons.events.Cancellable;

/** An object that can be listened. */
public interface ReactiveListenable {

	/** Subscribe to the object.
	 * @param listener listener
	 */
	void listen(Supplier<Publisher<?>> listener);
	
	/** Unsubscribe to the object.
	 * @param listener listener
	 */
	void unlisten(Supplier<Publisher<?>> listener);

	/** @return true if at least one listener is currently subscribed. */
	boolean hasListeners();
	
	/**
	 * Subscribe to the object, and return a Cancellable allowing to unsubscribe.
	 * @param listener listener
	 * @return Cancellable that can be used to unsubscribe
	 */
	default Cancellable subscribe(Supplier<Publisher<?>> listener) {
		listen(listener);
		return () -> {
			unlisten(listener);
			return true;
		};
	}

}
