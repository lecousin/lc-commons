package net.lecousin.commons.function;

/**
 * Operation taking one argument, returning a value, that may throw a checked exception.
 * @param <T> type of argument
 * @param <R> type of result
 */
public interface FunctionThrows<T, R> {

	/**
	 * Performs the operation.
	 * @param t argument
	 * @return result
	 * @throws Exception in case of error
	 */
	@SuppressWarnings("java:S112")
	R apply(T t) throws Exception;
	
}
