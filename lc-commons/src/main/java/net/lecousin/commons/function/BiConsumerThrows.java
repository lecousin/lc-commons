package net.lecousin.commons.function;

/** Operation taking 2 arguments, that may throw a checked exception.
 * @param <A> type of first argument
 * @param <B> type of second argument
 */
public interface BiConsumerThrows<A, B> {

	/** Performs the operation with the 2 arguments.
	 * 
	 * @param a first value
	 * @param b second value
	 * @throws Exception in case of error
	 */
	@SuppressWarnings("java:S112")
	void accept(A a, B b) throws Exception;
	
}
