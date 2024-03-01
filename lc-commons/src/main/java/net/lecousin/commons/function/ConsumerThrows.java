package net.lecousin.commons.function;

/**
 * Operation taking one argument, that may throw a checked exception.
 * 
 * @param <T> type of argument
 * @param <E> type of exception
 */
public interface ConsumerThrows<T, E extends Exception> {

	/**
	 * Performs the operation.
	 * 
	 * @param t argument
	 * @throws E in case of error
	 */
	@SuppressWarnings("java:S112")
	void accept(T t) throws E;
	
}
