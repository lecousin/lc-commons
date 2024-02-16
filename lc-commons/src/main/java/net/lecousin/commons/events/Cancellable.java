package net.lecousin.commons.events;

/** Interface for cancellable process. */
public interface Cancellable {

	/** @return true if the process has been cancelled, false if it was not possible or cannot be determined. */
	boolean cancel();
	
	/** Chain of Cancellable.
	 * <p>
	 * When the chain is cancelled, each Cancellable is cancelled, and the Chain is considered as cancelled if all cancellables
	 * have been successfully cancelled.
	 * </p>
	 * <p>
	 * A null Cancellable is allowed, and will be considered as successfully cancelled.
	 * </p>
	 */
	class Chain implements Cancellable {
		
		private Cancellable[] array;
		
		/** Constructor.
		 * @param chain cancellable objects
		 */
		public Chain(Cancellable... chain) {
			this.array = chain;
		}
		
		@Override
		public boolean cancel() {
			boolean result = true;
			for (int i = 0; i < array.length; ++i)
				result &= array[i] == null || array[i].cancel();
			return result;
		}
		
	}
	
	/** Create a {@link Chain} of Cancellable.
	 * @param chain cancellables
	 * @return Chain
	 */
	static Chain of(Cancellable... chain) {
		return new Chain(chain);
	}
	
}
