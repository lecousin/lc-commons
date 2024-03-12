package net.lecousin.commons.io.chars;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import net.lecousin.commons.io.IOTestUtils;
import net.lecousin.commons.io.bytes.BytesIOTestUtils;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.BufferSizeProvider;
import net.lecousin.commons.io.bytes.data.BytesData;
import net.lecousin.commons.test.ParameterizedTestUtils;
import net.lecousin.commons.test.ParameterizedTestUtils.CompositeArgumentsProvider;

public final class CharsIOTestUtils {

	private CharsIOTestUtils() {
		// no instance
	}
	
	public static char[] generateContent(int size) {
		char[] data = new char[size];
		Random random = new Random();
		for (int i = 0; i < size; ) {
			byte[] bytes = new byte[(size - i) * 4];
			random.nextBytes(bytes);
			for (int j = 0; j < bytes.length && i < size; j += 4) {
				int r = BytesData.LE.readInteger(bytes, j);
				if (Character.isDefined(r)) {
					int nb = Character.charCount(r);
					if (i + nb <= size) {
						Character.toChars(r, data, i);
						i += nb;
					}
				}
			}
			for (int j = 0; j < bytes.length && i < size; j += 32) {
				data[i++] = (char)bytes[j];
			}
		}
		return data;
	}
	
	private static List<char[]> RANDOM_CONTENT = IOTestUtils.sizes().map(size -> generateContent(size)).toList();
	
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

	public static class RandomContentWithBufferSizeProvider extends CompositeArgumentsProvider {
		public RandomContentWithBufferSizeProvider() {
			super(new RandomContentProvider(), new BufferSizeProvider());
		}
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return super.provideArguments(context)
				.filter(args -> {
					char[] content = (char[]) args.get()[1];
					int bufferSize = (int) args.get()[2];
					if (bufferSize > content.length) {
						var sizes = BytesIOTestUtils.bufferSizes().toList();
						int i = sizes.indexOf(bufferSize);
						if (i > 0 && sizes.get(i - 1) > content.length) return false;
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
