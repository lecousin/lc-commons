package net.lecousin.commons.events;

/**
 * An object that can be listened.
 * <p>
 * The method {@link #listen(Runnable)} add a new listener while the method {@link #unlisten(Runnable)} remove it.
 * </p>
 * <p>
 * The method {@link #subscribe(Runnable)} add a new listener and returns a {@link Cancellable} to unlisten the listener.
 * </p>
 */
public interface Listenable {

	/** Add a new listener.
	 * @param listener listener to add
	 */
	void listen(Runnable listener);
	
	/** Remove a listener.
	 * @param listener listener to remove
	 */
	void unlisten(Runnable listener);
	
	/** Subscribe a listener.
	 * @param listener listener to add
	 * @return a Cancellable allowing to unlisten.
	 */
	default Cancellable subscribe(Runnable listener) {
		listen(listener);
		return () -> {
			unlisten(listener);
			return true;
		};
	}

	/** @return true if at least one listener is present. */
	boolean hasListeners();

}
