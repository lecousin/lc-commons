package net.lecousin.commons.function;

/** Operation taking 2 arguments, that may throw a checked exception.
 * @param <A> type of first argument
 * @param <B> type of second argument
 * @param <E> type of exception
 */
public interface BiConsumerThrows<A, B, E extends Exception> {

	/** Performs the operation with the 2 arguments.
	 * 
	 * @param a first value
	 * @param b second value
	 * @throws E in case of error
	 */
	@SuppressWarnings("java:S112")
	void accept(A a, B b) throws E;
	
}
