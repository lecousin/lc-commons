package net.lecousin.commons.io.bytes;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import net.lecousin.commons.io.IOTestUtils;
import net.lecousin.commons.test.ParameterizedTestUtils;
import net.lecousin.commons.test.ParameterizedTestUtils.CompositeArgumentsProvider;

public final class BytesIOTestUtils {

	private BytesIOTestUtils() {
		// no instance
	}
	
	public static byte[] generateContent(int size) {
		byte[] data = new byte[size];
		new Random().nextBytes(data);
		return data;
	}
	
	private static List<byte[]> RANDOM_CONTENT = IOTestUtils.sizes().map(size -> generateContent(size)).toList();
	
	public static class RandomContentProvider implements ArgumentsProvider {
		private int maxSize;
		
		public RandomContentProvider(int maxSize) {
			this.maxSize = maxSize;
		}
		
		public RandomContentProvider() {
			this(Integer.MAX_VALUE);
		}
		
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return RANDOM_CONTENT.stream()
				.filter(content -> content.length <= maxSize)
				.map(content -> Arguments.of("Random content of size " + content.length, content));
		}
	}
	
	public static class RandomContentTestCasesProvider extends CompositeArgumentsProvider {
		public RandomContentTestCasesProvider() {
			super(new RandomContentProvider(), new ParameterizedTestUtils.TestCasesArgumentsProvider());
		}
	}
	
	public static class SmallRandomContentTestCasesProvider extends CompositeArgumentsProvider {
		public SmallRandomContentTestCasesProvider() {
			super(new RandomContentProvider(100000), new ParameterizedTestUtils.TestCasesArgumentsProvider());
		}
	}

	private static final Integer[] TEST_CASE_BUFFER_SIZE = new Integer[] { 1, 3, 666, 1024, 8192, 128 * 1024 };
	
	public static Stream<Integer> bufferSizes() { return Arrays.stream(TEST_CASE_BUFFER_SIZE); }

	public static class BufferSizeProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return bufferSizes().map(size -> Arguments.of("with buffer size=" + size, size));
		}
	}
	
	public static class RandomContentWithBufferSizeProvider extends CompositeArgumentsProvider {
		public RandomContentWithBufferSizeProvider() {
			super(new RandomContentProvider(), new BufferSizeProvider());
		}
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return super.provideArguments(context)
				.filter(args -> {
					byte[] content = (byte[]) args.get()[1];
					int bufferSize = (int) args.get()[2];
					if (bufferSize > content.length) {
						int i = ArrayUtils.indexOf(TEST_CASE_BUFFER_SIZE, bufferSize);
						if (i > 0 && TEST_CASE_BUFFER_SIZE[i - 1] > content.length) return false;
					}
					if (bufferSize < 100 && content.length > 10000) return false;
					return true;
				});
		}
	}
	
	public static class RandomContentWithBufferSizeTestCasesProvider extends CompositeArgumentsProvider {
		public RandomContentWithBufferSizeTestCasesProvider() {
			super(new RandomContentWithBufferSizeProvider(), new ParameterizedTestUtils.TestCasesArgumentsProvider());
		}
	}
}
