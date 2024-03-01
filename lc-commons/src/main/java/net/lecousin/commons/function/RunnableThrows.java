package net.lecousin.commons.function;

/**
 * Operation without argument, that may throw a checked exception.
 * @param <E> type of exception
 */
public interface RunnableThrows<E extends Exception> {

	/**
	 * Performs the operation.
	 * 
	 * @throws E in case of error
	 */
	@SuppressWarnings("java:S112")
	void run() throws E;
	
}
