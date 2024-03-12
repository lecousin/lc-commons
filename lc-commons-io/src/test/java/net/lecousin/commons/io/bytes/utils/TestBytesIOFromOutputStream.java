package net.lecousin.commons.io.bytes.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.junit.jupiter.api.Assertions;

import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.stream.ByteArrayOutputStreamAccessible;
import net.lecousin.commons.test.TestCase;

public class TestBytesIOFromOutputStream extends AbstractWritableBytesIOTest {

	@Override
	public List<? extends TestCase<Integer, WritableTestCase<? extends BytesIO.Writable, ?>>> getTestCases() {
		return List.of(
			new TestCase<>(
				"ByteArrayOutputStream",
				initialSize -> new WritableTestCase<>(new BytesIOFromOutputStream(new ByteArrayOutputStreamAccessible(initialSize), false), 1)
			),
			new TestCase<>(
				"FileOutputStream",
				initialSize -> {
					try {
						Path path = Files.createTempFile("test-lc-reactive-io", "os-as-writable");
						path.toFile().deleteOnExit();
						SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE);
						if (initialSize > 0) {
							channel.position(initialSize - 1);
							channel.write(ByteBuffer.wrap(new byte[] { (byte) 0 }));
						}
						channel.close();
						FileOutputStream output = new FileOutputStream(path.toFile());
						BytesIOFromOutputStream io = new BytesIOFromOutputStream(output, true);
						return new WritableTestCase<>(io, path);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			)
		);
	}
	
	@Override
	protected void checkWrittenData(BytesIO.Writable io, Object object, byte[] expected) throws Exception {
		if (object instanceof Number) {
			ByteArrayOutputStreamAccessible s = (ByteArrayOutputStreamAccessible) ((BytesIOFromOutputStream) io).getUnderlyingStream();
			for (int i = 0; i < expected.length; ++i)
				assertEquals(expected[i], s.getArray()[i]);
		} else {
			try {
				Path path = (Path) object;
				byte[] found = Files.readAllBytes(path);
				Assertions.assertArrayEquals(expected, found);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
