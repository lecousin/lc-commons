package net.lecousin.commons.test;

import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A test case, with a display name, and a function that provide an object from an input.
 * 
 * @param <I> input
 * @param <O> output
 */
@Data
@AllArgsConstructor
public class TestCase<I, O> {

	private String name;
	private Function<I, O> argumentProvider;
	
	/**
	 * Test case specialization without input (Void).
	 * 
	 * @param <O> output
	 */
	public static class NoInput<O> extends TestCase<Void, O> {
		
		/** Constructor.
		 * @param name test name
		 * @param supplier object supplier
		 */
		public NoInput(String name, Supplier<O> supplier) {
			super(name, v -> supplier.get());
		}
		
		/** Equivalent to <code>getArgumentProvider().apply(null)</code>.
		 * @return the object for this test case
		 */
		public O getArgument() {
			return getArgumentProvider().apply(null);
		}
		
	}
	
}
