package net.lecousin.commons.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractKnownSizeIOTest implements TestCasesProvider<Integer, IO.KnownSize> {

	@Nested
	public class AsIO extends AbstractIOTest {
		
		@Override
		public List<? extends TestCase<Void, IO>> getTestCases() {
			return AbstractKnownSizeIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), () -> (IO) tc.getArgumentProvider().apply(1)))
				.toList();
		}
		
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(IOTestUtils.TestCasesWithSizeProvider.class)
	void testSize(String displayName, int size, Function<Integer, IO.KnownSize> ioSupplier) throws Exception {
		IO.KnownSize io = ioSupplier.apply(size);
		assertThat(io.size()).isEqualTo(size);
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.size());
	}
	
}
