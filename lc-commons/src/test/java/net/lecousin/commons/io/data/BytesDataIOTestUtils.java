package net.lecousin.commons.io.data;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public final class BytesDataIOTestUtils {

	private BytesDataIOTestUtils() {
		// no instance
	}
	
	public static byte[] generateContent(int size) {
		byte[] data = new byte[size];
		new Random().nextBytes(data);
		return data;
	}
	
	private static final Integer[] TEST_CASE_SIZES = new Integer[] { 0, 1, 2, 10, 4000, 66666, 1234567 };
	
	private static List<byte[]> RANDOM_CONTENT = Arrays.stream(TEST_CASE_SIZES).map(size -> generateContent(size)).toList();
	
	public static class SizeProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Arrays.stream(TEST_CASE_SIZES)
				.map(size -> Arguments.of("size " + size, size));
		}
	}
	
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
}
