package net.lecousin.commons.reactive.io.bytes.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import net.lecousin.commons.reactive.io.bytes.AbstractReadableReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import net.lecousin.commons.reactive.io.bytes.file.ReactiveFileIO;
import net.lecousin.commons.test.TestCase;

public class TestBufferedReadableReactiveBytesIO extends AbstractReadableReactiveBytesIOTest {

	@Override
	public List<? extends TestCase<byte[], ReactiveBytesIO.Readable>> getTestCases() {
		return List.of(
			new TestCase<>(
				"File with 1 buffer",
				data -> {
					try {
						Path path = Files.createTempFile("test", "lc-reactive-io");
						path.toFile().deleteOnExit();
						try (FileOutputStream out = new FileOutputStream(path.toFile())) {
							out.write(data);
						}
						return ReactiveFileIO.openReadable(path)
							.map(io -> new BufferedReadableReactiveBytesIO(io, 1, true))
							.block();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			),
			new TestCase<>(
				"File with 3 buffers",
				data -> {
					try {
						Path path = Files.createTempFile("test", "lc-reactive-io");
						path.toFile().deleteOnExit();
						try (FileOutputStream out = new FileOutputStream(path.toFile())) {
							out.write(data);
						}
						return ReactiveFileIO.openReadable(path)
							.map(io -> {
								BufferedReadableReactiveBytesIO b = new BufferedReadableReactiveBytesIO(io, 3, false);
								b.onClose(io.close());
								return b;
							})
							.block();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			)
		);
	}
	
}
