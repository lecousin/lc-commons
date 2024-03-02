package net.lecousin.commons.io.bytes.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.bytes.data.AbstractWritableBytesDataIOTest;
import net.lecousin.commons.io.bytes.data.BytesDataIO;
import net.lecousin.commons.io.bytes.file.FileIO;
import net.lecousin.commons.test.TestCase;

public class TestBufferedWritableBytesDataIO extends AbstractWritableBytesDataIOTest {

	@Override
	public List<? extends TestCase<Integer, WritableTestCase<? extends BytesDataIO.Writable, ?>>> getTestCases() {
		return List.of(
			new TestCase<>("Using FileIO, default buffer size and Little-Endian", size -> {
				try {
					Path path = Files.createTempFile("test-lc-commons-io-buffered-writable", "");
					path.toFile().deleteOnExit();
					try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
						f.setLength(size);
					}
					BytesIO.Writable io = new FileIO.Writable.Appendable(path);
					BufferedWritableBytesDataIO buffered = new BufferedWritableBytesDataIO(io, true);
					return new WritableTestCase<>(buffered, path);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			})
		);
	}
	
	@Override
	protected void checkWrittenData(BytesDataIO.Writable io, Object object, byte[] expected) throws Exception {
		byte[] found = null;
		if (object instanceof Path path) {
			found = Files.readAllBytes(path);
		}
		assertThat(found).containsExactly(expected);
	}
	
}
