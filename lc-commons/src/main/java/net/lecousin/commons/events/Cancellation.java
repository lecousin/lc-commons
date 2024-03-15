package net.lecousin.commons.events;

import java.util.function.Supplier;

import lombok.Getter;

/**
 * A cancellable on which we can change the behaviour on cancellation.<br/>
 * This can be used typically when a task has multiple steps, each steps
 * having its own Cancellable: each time we move to the next step, we can
 * set the new cancellable on this Cancellation object.
 */
public class Cancellation implements Cancellable {

	@Getter
	private boolean cancelled = false;
	private Supplier<Boolean> cancellation;
	
	/**
	 * Constructor.
	 * @param cancellation called when cancelled
	 */
	public Cancellation(Supplier<Boolean> cancellation) {
		this.cancellation = cancellation;
	}
	
	/**
	 * Constructor.
	 * @param initialCancellable the initial cancellable to cancel
	 */
	public Cancellation(Cancellable initialCancellable) {
		this((Supplier<Boolean>) initialCancellable::cancel);
	}
	
	/**
	 * Constructor, with nothing to cancel.
	 */
	public Cancellation() {
		this(() -> false);
	}
	
	@Override
	public boolean cancel() {
		cancelled = true;
		return cancellation.get();
	}
	
	/** Cancel the given Cancellable on cancellation.
	 * @param toCancel to be cancelled
	 */
	public void setCancellation(Cancellable toCancel) {
		setCancellation((Supplier<Boolean>) toCancel::cancel);
	}
	
	/** Change the behaviour on cancellation.
	 * @param canceller called on cancellation
	 */
	public void setCancellation(Supplier<Boolean> canceller) {
		this.cancellation = canceller;
		if (cancelled) canceller.get();
	}
	
}
