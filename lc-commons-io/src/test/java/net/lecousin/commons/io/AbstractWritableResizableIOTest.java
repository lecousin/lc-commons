package net.lecousin.commons.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractWritableResizableIOTest implements TestCasesProvider<Integer, IO.Writable.Resizable> {

	@Nested
	public class AsKnownSize extends AbstractKnownSizeIOTest {
		
		@Override
		public List<? extends TestCase<Integer, IO.KnownSize>> getTestCases() {
			return AbstractWritableResizableIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, IO.KnownSize>) (size) -> (IO.KnownSize) tc.getArgumentProvider().apply(size)))
				.toList();
		}
		
	}
	
	@Nested
	public class AsWritable extends AbstractWritableIOTest {
		
		@Override
		public List<? extends TestCase<Void, IO.Writable>> getTestCases() {
			return AbstractWritableResizableIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), () -> (IO.Writable) tc.getArgumentProvider().apply(1)))
				.toList();
		}
		
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(IOTestUtils.TestCasesWithSizeProvider.class)
	void testResize(String displayName, int initialSize, Function<Integer, IO.Writable.Resizable> ioSupplier) throws Exception {
		IO.Writable.Resizable io = ioSupplier.apply(initialSize);
		assertThat(io.size()).isEqualTo(initialSize);
		
		long newSize = initialSize > 0 ? initialSize * 2 : 10;
		io.setSize(newSize);
		assertThat(io.size()).isEqualTo(newSize);
		
		newSize = newSize / 2;
		io.setSize(newSize);
		assertThat(io.size()).isEqualTo(newSize);
		
		io.setSize(0);
		assertThat(io.size()).isZero();
		
		assertThrows(NegativeValueException.class, () -> io.setSize(-1));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.setSize(1));
	}
	
}
