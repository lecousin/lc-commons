package net.lecousin.commons.function;

/**
 * Operation taking 2 arguments and returning a value, that may throw a checked exception.
 * @param <A> type of first argument
 * @param <B> type of second argument
 * @param <R> return type
 */
public interface BiFunctionThrows<A, B, R> {

	/**
	 * Performs the operation.
	 * 
	 * @param a first argument
	 * @param b second argument
	 * @return value
	 * @throws Exception in case of error
	 */
	@SuppressWarnings("java:S112")
	R apply(A a, B b) throws Exception;
	
}
