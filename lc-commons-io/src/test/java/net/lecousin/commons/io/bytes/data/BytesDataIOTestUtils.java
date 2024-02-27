package net.lecousin.commons.io.bytes.data;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import net.lecousin.commons.io.bytes.BytesIOTestUtils;
import net.lecousin.commons.test.ParameterizedTestUtils;
import net.lecousin.commons.test.ParameterizedTestUtils.CompositeArgumentsProvider;

public final class BytesDataIOTestUtils {

	private BytesDataIOTestUtils() {
		// no instance
	}
	
	public static class DataArgumentsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			List<Arguments> list = new LinkedList<>();
			for (int nbBytes = 1; nbBytes <= 8; nbBytes++) {
				if (nbBytes != 8) {
					list.add(Arguments.of("Unsigned " + nbBytes + " bytes", nbBytes, false));
				}
				list.add(Arguments.of("Signed " + nbBytes + " bytes", nbBytes, true));
			}
			return list.stream();
		}
	}
	
	public static class DataTestCasesProvider extends CompositeArgumentsProvider {
		public DataTestCasesProvider() {
			super(new BytesIOTestUtils.RandomContentProvider(100000), new DataArgumentsProvider(), new ParameterizedTestUtils.TestCasesArgumentsProvider());
		}
	}
	
}
