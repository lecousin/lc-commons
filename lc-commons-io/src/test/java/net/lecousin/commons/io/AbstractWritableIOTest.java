package net.lecousin.commons.io;

import java.util.List;

import org.junit.jupiter.api.Nested;

import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractWritableIOTest implements TestCasesProvider<Void, IO.Writable> {

	@Nested
	public class AsIO extends AbstractIOTest {
		
		@Override
		public List<? extends TestCase<Void, IO>> getTestCases() {
			return AbstractWritableIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), () -> (IO) tc.getArgumentProvider().apply(null)))
				.toList();
		}
		
	}
	
}
