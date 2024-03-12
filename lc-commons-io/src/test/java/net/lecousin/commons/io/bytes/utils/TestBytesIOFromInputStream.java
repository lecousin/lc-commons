package net.lecousin.commons.io.bytes.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import net.lecousin.commons.io.bytes.AbstractReadableBytesIOTest;
import net.lecousin.commons.io.bytes.BytesIO.Readable;
import net.lecousin.commons.test.TestCase;

public class TestBytesIOFromInputStream extends AbstractReadableBytesIOTest {

	@Override
	public List<? extends TestCase<byte[], Readable>> getTestCases() {
		return List.of(
			new TestCase<>(
				"ByteArrayInputStream",
				data -> new BytesIOFromInputStream(new ByteArrayInputStream(data), false)
			),
			new TestCase<>(
				"FileInputStream",
				data -> {
					try {
						Path path = Files.createTempFile("test-lc-reactive-io", "is-as-readable");
						path.toFile().deleteOnExit();
						try (FileOutputStream out = new FileOutputStream(path.toFile())) {
							out.write(data);
						}
						FileInputStream input = new FileInputStream(path.toFile());
						BytesIOFromInputStream io = new BytesIOFromInputStream(input, true);
						assertThat(io.getUnderlyingStream()).isSameAs(input);
						return io;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			)
		);
	}
	
}
