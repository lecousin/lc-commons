package net.lecousin.commons.io;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import net.lecousin.commons.test.ParameterizedTestUtils.CompositeArgumentsProvider;
import net.lecousin.commons.test.ParameterizedTestUtils.TestCasesArgumentsProvider;

public final class IOTestUtils {
	
	private IOTestUtils() {
		// no instance
	}
	
	private static final Integer[] TEST_CASE_SIZES = new Integer[] { 0, 1, 2, 10, 4000, 66666, 1234567 };
	
	public static Stream<Integer> sizes() { return Arrays.stream(TEST_CASE_SIZES); }
	
	public static class SizeProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return sizes().map(size -> Arguments.of("size " + size, size));
		}
	}
	
	public static class TestCasesWithSizeProvider extends CompositeArgumentsProvider {
		public TestCasesWithSizeProvider() {
			super(new SizeProvider(), new TestCasesArgumentsProvider());
		}
	}

}
