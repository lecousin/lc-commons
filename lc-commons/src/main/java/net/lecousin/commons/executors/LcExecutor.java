package net.lecousin.commons.executors;

import java.time.Duration;

import net.lecousin.commons.events.Cancellable;

/** Task executor. */
public interface LcExecutor {

	/** Execute the given task as soon as possible.
	 * 
	 * @param task task
	 */
	void execute(Runnable task);
	
	/** Schedule the given task to be executed after <code>delay</code> milliseconds.
	 * 
	 * @param task task
	 * @param delay delay in milliseconds
	 * @return a Cancellable to cancel the schedule if not yet started
	 */
	Cancellable schedule(Runnable task, long delay);
	
	/** Schedule the given task to be executed after <code>delay</code>.
	 * 
	 * @param task task
	 * @param delay delay
	 * @return a Cancellable to cancel the schedule if not yet started
	 */
	default Cancellable schedule(Runnable task, Duration delay) {
		return schedule(task, delay.toMillis());
	}
	
	/** Submits a periodic action that becomes enabled first after the
     * given period, and subsequently with the given period;
     * that is, executions will commence after
     * {@code period}, then {@code 2 * period}, and so on.<br/>
     * The execution will be scheduled until it is canceled.
     * <p>
     * If any execution of this task takes longer than its period, then
     * subsequent executions may start late, but will not concurrently
     * execute.
     * <p>
     * If an execution throws an exception, it will be silently ignored.
     * <p>
     * Compared to {@link #scheduleWithFixedDelay(Runnable, Duration)},
     * the next execution is evaluated as {@code execution_start + period},
     * the method scheduleWithFixedDelay uses {@code execution_end + delay}.
	 * 
	 * @param task task
	 * @param period delay between 2 executions 
	 * @return a Cancellable to cancel the schedule
	 */
	default Cancellable scheduleAtFixedRate(Runnable task, Duration period) {
		return scheduleAtFixedRate(task, period, period);
	}
	
	/** Submits a periodic action that becomes enabled first after the
     * given initial delay, and subsequently with the given period;
     * that is, executions will commence after
     * {@code initialDelay}, then {@code initialDelay + period}, then
     * {@code initialDelay + 2 * period}, and so on.<br/>
     * The execution will be scheduled until it is canceled.
     * <p>
     * If any execution of this task takes longer than its period, then
     * subsequent executions may start late, but will not concurrently
     * execute.
     * <p>
     * If an execution throws an exception, it will be silently ignored.
     * <p>
     * Compared to {@link #scheduleWithFixedDelay(Runnable, Duration, Duration)},
     * the next execution is evaluated as {@code execution_start + period},
     * the method scheduleWithFixedDelay uses {@code execution_end + delay}.
	 * 
	 * @param task task
	 * @param initialDelay initial delay before the first execution
	 * @param period delay between 2 executions 
	 * @return a Cancellable to cancel the schedule
	 */
	Cancellable scheduleAtFixedRate(Runnable task, Duration initialDelay, Duration period);
	
	/**
	 * Submits a periodic action that becomes enabled first after the
     * given delay, and subsequently with the given delay
     * between the termination of one execution and the commencement of
     * the next.<br/>
     * The execution will be scheduled until it is canceled.
     * <p>
     * If an execution throws an exception, it will be silently ignored.
     * 
	 * @param task task
	 * @param delay delay between the end of the previous execution and the next
	 * @return a Cancellable to cancel the schedule
	 */
	default Cancellable scheduleWithFixedDelay(Runnable task, Duration delay) {
		return scheduleWithFixedDelay(task, delay, delay);
	}
	
	/**
	 * Submits a periodic action that becomes enabled first after the
     * given initial delay, and subsequently with the given delay
     * between the termination of one execution and the commencement of
     * the next.<br/>
     * The execution will be scheduled until it is canceled.
     * <p>
     * If an execution throws an exception, it will be silently ignored.
     * 
	 * @param task task
	 * @param initialDelay initial delay before the first execution
	 * @param delay delay between the end of the previous execution and the next
	 * @return a Cancellable to cancel the schedule
	 */
	Cancellable scheduleWithFixedDelay(Runnable task, Duration initialDelay, Duration delay);
	
}
