package net.lecousin.commons.events;

import java.util.function.Consumer;

/**
 * An object that can be listened.
 * <p>
 * The method {@link #listen(Runnable)} add a new listener while the method {@link #unlisten(Consumer)} remove it.
 * </p>
 * <p>
 * The method {@link #subscribe(Consumer)} add a new listener and returns a {@link Cancellable} to unlisten the listener.
 * </p>
 *
 * @param <T> type of object
 */
public interface ObjectListenable<T> extends Listenable {

	/** Add a new listener.
	 * @param listener listener to add
	 */
	void listen(Consumer<T> listener);

	/** Remove a listener.
	 * @param listener listener to remove
	 */
	void unlisten(Consumer<T> listener);
	
	/** Subscribe a listener.
	 * @param listener listener to add
	 * @return a Cancellable allowing to unlisten.
	 */
	default Cancellable subscribe(Consumer<T> listener) {
		listen(listener);
		return () -> {
			unlisten(listener);
			return true;
		};
	}

}
