package net.lecousin.commons.function;

/**
 * Operation taking no argument and returning a value, that may throw a checked exception.
 * 
 * @param <T> type of argument
 * @param <E> type of exception
 */
public interface SupplierThrows<T, E extends Exception> {

	/**
	 * Performs the operation.
	 * @return result
	 * @throws E in case of error
	 */
	@SuppressWarnings("java:S112")
	T get() throws E;
	
}
