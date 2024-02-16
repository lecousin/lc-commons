package net.lecousin.commons.function;

/**
 * Operation taking one argument, that may throw a checked exception.
 * 
 * @param <T> type of argument
 */
public interface ConsumerThrows<T> {

	/**
	 * Performs the operation.
	 * 
	 * @param t argument
	 * @throws Exception in case of error
	 */
	@SuppressWarnings("java:S112")
	void accept(T t) throws Exception;
	
}
