package net.lecousin.commons.reactive.io;

import java.util.List;

import org.junit.jupiter.api.Nested;

import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadableReactiveIOTest implements TestCasesProvider<Void, ReactiveIO.Readable> {

	@Nested
	public class AsIO extends AbstractReactiveIOTest {
		
		@Override
		public List<? extends TestCase<Void, ReactiveIO>> getTestCases() {
			return AbstractReadableReactiveIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), () -> (ReactiveIO) tc.getArgumentProvider().apply(null)))
				.toList();
		}
		
	}
	
}
