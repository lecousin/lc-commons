package net.lecousin.commons.function;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;

/**
 * Convert a functional interface into another.<br/>
 * A wrapper is considered as equal to its wrapped object, so calling equals on the wrapper with the wrapped object returns true (but the opposite is false).
 * 
 * @param <T> type of wrapped object
 */
@RequiredArgsConstructor
public class FunctionWrapper<T> {
	
	/** The wrapped object. */
	protected final T inner;
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj instanceof FunctionWrapper<?> w) return w.inner.equals(inner);
		return inner.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return inner.hashCode();
	}
	
	@Override
	public String toString() {
		return inner.toString();
	}
	
	/**
	 * Convert a Runnable into a Consumer.
	 * @param <T> type of element consumed
	 * @param runnable the runnable to convert
	 * @return a consumer that calls the runnable
	 */
	public static <T> Consumer<T> asConsumer(Runnable runnable) {
		return new RunnableAsConsumer<>(runnable);
	}
	
	/**
	 * Convert a Supplier into a Function.
	 * @param <I> input type of the function
	 * @param <O> output type of the supplier and function
	 * @param supplier the supplier to convert
	 * @return a function that calls the supplier and returns its result
	 */
	public static <I, O> Function<I, O> asFunction(Supplier<O> supplier) {
		return new SupplierAsFunction<>(supplier);
	}

	/**
	 * Convert a Runnable into a Consumer.
	 * @param <T> type of element consumed
	 */
	public static class RunnableAsConsumer<T> extends FunctionWrapper<Runnable> implements Consumer<T> {
		/**
		 * Constructor.
		 * @param runnable the runnable to convert
		 */
		public RunnableAsConsumer(Runnable runnable) {
			super(runnable);
		}
		
		@Override
		public void accept(T t) {
			inner.run();
		}
	}
	
	/**
	 * Convert a Supplier into a Function.
	 * @param <I> input type of the function
	 * @param <O> output type of the supplier and function
	 */
	public static class SupplierAsFunction<I, O> extends FunctionWrapper<Supplier<O>> implements Function<I, O> {
		/** Constructor.
		 * 
		 * @param supplier the supplier to convert
		 */
		public SupplierAsFunction(Supplier<O> supplier) {
			super(supplier);
		}
		
		@Override
		public O apply(I t) {
			return inner.get();
		}
	}
	
}
