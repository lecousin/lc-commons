package net.lecousin.commons.test;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utilities for parameterized tests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParameterizedTestUtils {

	/**
	 * Provides arguments from the list of {@link TestCase} for a class implementing {@link TestCasesProvider}.
	 */
	public static class TestCasesArgumentsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			TestCasesProvider<?, ?> test = createTestInstance(context);
			return test.getTestCases().stream()
				.map(testCase -> Arguments.of(testCase.getName(), testCase.getArgumentProvider()));
		}
	}
	
	/**
	 * Arguments provider, combining arguments from different providers.<br/>
	 * The first argument is supposed to be always the display name, which will be concatenated.
	 * Other arguments are concatenated in order.
	 */
	public static class CompositeArgumentsProvider implements ArgumentsProvider {
		/** Array of providers. */
		protected ArgumentsProvider[] providers;
		/** List of additional arguments to combine. */
		protected List<List<Arguments>> additionals = new LinkedList<>();
		
		/**
		 * Constructor.
		 * @param providers argument providers to combine
		 */
		protected CompositeArgumentsProvider(ArgumentsProvider... providers) {
			this.providers = providers;
		}
		
		/** Add arguments to combine.
		 * @param add additional arguments
		 */
		protected void add(List<Arguments> add) {
			additionals.add(add);
		}
		
		/** Add arguments to combine.
		 * @param add additional arguments
		 */
		protected void add(Stream<Arguments> add) {
			add(add.toList());
		}
		
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			Stream<? extends Arguments> stream = providers[0].provideArguments(context);
			int i = 1;
			while (i < providers.length) {
				stream = combine(stream, providers[i].provideArguments(context));
				i++;
			}
			for (List<Arguments> additional : additionals)
				stream = combine(stream, additional.stream());
			return stream;
		}
	}
	
	/** Combine arguments, supposing that the first argument is always the display name (a string).
	 * @param args1 first arguments
	 * @param args2 second arguments to combine
	 * @return combined arguments
	 */
	public static Stream<? extends Arguments> combine(Stream<? extends Arguments> args1, Stream<? extends Arguments> args2) {
		List<? extends Arguments> list2 = args2.toList();
		return args1.flatMap(a1 ->
			list2.stream().map(a2 -> () -> {
				Object[] o1 = a1.get();
				Object[] o2 = a2.get();
				Object[] o = new Object[o1.length + o2.length - 1];
				System.arraycopy(o1, 1, o, 1, o1.length - 1);
				System.arraycopy(o2, 1, o, o1.length, o2.length - 1);
				o[0] = ((String) o1[0]) + " " + (String) o2[0];
				return o;
			})
		);
	}
	
	/** Combine arguments, supposing that the first argument is always the display name (a string).
	 * @param args arguments to combine
	 * @return combined arguments
	 */
	@SafeVarargs
	public static Stream<? extends Arguments> combineN(Stream<? extends Arguments>... args) {
		Stream<? extends Arguments> result = args[0];
		for (int i = 1; i < args.length; ++i)
			result = combine(result, args[i]);
		return result;
	}

	/**
	 * Create an instance of the test class being tested.<br/>
	 * This is useful when using parameterized tests, with arguments provided by the class itself (not through static methods).
	 * 
	 * @param <T> test class
	 * @param context junit execution context
	 * @return an instance of the test class
	 */
	@SuppressWarnings({"unchecked", "java:S112"})
	public static <T> T createTestInstance(ExtensionContext context) {
		try {
			return (T) context.getRequiredTestClass().getConstructor().newInstance();
		} catch (Exception e) {
			Optional<ExtensionContext> parent = context.getParent();
			if (parent.isPresent()) {
				Optional<Class<?>> parentClass = parent.get().getTestClass();
				if (parentClass.isPresent()) {
					try {
						Object parentInstance = createTestInstance(parent.get());
						if (context.getRequiredTestClass().isAssignableFrom(parentInstance.getClass()))
							return (T) parentInstance;
						Constructor<?>[] constructors = context.getRequiredTestClass().getDeclaredConstructors();
						Object childInstance = constructors[0].newInstance(parentInstance);
						return (T) childInstance;
					} catch (Exception err2) {
						throw new RuntimeException(err2);
					}
				}
			}
			throw new RuntimeException(e);
		}
	}
}
