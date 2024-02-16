package net.lecousin.commons.function;

/**
 * Operation without argument, that may throw a checked exception.
 */
public interface RunnableThrows {

	/**
	 * Performs the operation.
	 * 
	 * @throws Exception in case of error
	 */
	@SuppressWarnings("java:S112")
	void run() throws Exception;
	
}
