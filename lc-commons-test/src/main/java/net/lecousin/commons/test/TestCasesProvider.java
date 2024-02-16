package net.lecousin.commons.test;

import java.util.List;

/**
 * Interface for test classes that generate itself the test cases for parameterized tests.
 * 
 * @param <I> input
 * @param <O> output
 */
public interface TestCasesProvider<I, O> {

	/**
	 * @return the list of test cases for parameterized tests.
	 */
	List<? extends TestCase<I, O>> getTestCases();
	
}
