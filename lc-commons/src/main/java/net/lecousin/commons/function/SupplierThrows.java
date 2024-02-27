package net.lecousin.commons.function;

/**
 * Operation taking no argument and returning a value, that may throw a checked exception.
 * 
 * @param <T> type of argument
 */
public interface SupplierThrows<T> {

	/**
	 * Performs the operation.
	 * @return result
	 * @throws Exception in case of error
	 */
	@SuppressWarnings("java:S112")
	T get() throws Exception;
	
}
