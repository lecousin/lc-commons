package net.lecousin.commons.io.bytes.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;

import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadWriteBytesDataBufferTest implements TestCasesProvider<Integer, BytesDataBuffer.ReadWrite> {

	@Nested
	public class AsReadable extends AbstractReadableBytesDataBufferTest {
		@Override
		public List<? extends TestCase<byte[], BytesDataBuffer.Readable>> getTestCases() {
			return AbstractReadWriteBytesDataBufferTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<byte[], BytesDataBuffer.Readable>) (content) -> {
					BytesDataBuffer.ReadWrite rw = tc.getArgumentProvider().apply(content.length);
					for (int i = 0; i < content.length; ++i) {
						rw.writeSignedByte(content[i]);
					}
					rw.setPosition(0);
					return rw;
				}))
				.toList();
		}
	}
	
	@Nested
	public class AsWritable extends AbstractWritableBytesDataBufferTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<?, ?>>> getTestCases() {
			return AbstractReadWriteBytesDataBufferTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, WritableTestCase<?, ?>>) (size) -> {
					BytesDataBuffer.ReadWrite rw = tc.getArgumentProvider().apply(size);
					return new WritableTestCase<>(rw, rw);
				}))
				.toList();
		}
		
		@Override
		protected void checkWrittenData(BytesDataBuffer.Writable buffer, Object object, byte[] expected) {
			BytesDataBuffer.ReadWrite rw = (BytesDataBuffer.ReadWrite) buffer;
			rw.setPosition(0);
			for (int i = 0; i < expected.length; ++i)
				assertThat(rw.readSignedByte()).isEqualTo(expected[i]);
		}
	}
	
}
