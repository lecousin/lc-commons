package net.lecousin.commons.reactive.events;

import java.util.function.Function;

import org.reactivestreams.Publisher;

import net.lecousin.commons.events.Cancellable;

/** An object that can be listened. */
public interface ReactiveObjectListenable<T> extends ReactiveListenable {

	/** Subscribe to the object.
	 * @param listener listener
	 */
	void listen(Function<T, Publisher<?>> listener);

	/** Unsubscribe to the object.
	 * @param listener listener
	 */
	void unlisten(Function<T, Publisher<?>> listener);
	
	/**
	 * Subscribe to the object, and return a Cancellable allowing to unsubscribe.
	 * @param listener listener
	 * @return Cancellable that can be used to unsubscribe
	 */
	default Cancellable subscribe(Function<T, Publisher<?>> listener) {
		listen(listener);
		return () -> {
			unlisten(listener);
			return true;
		};
	}

}
