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

public class TestReactiveReadableBytesIOFromFlux extends AbstractReadableReactiveBytesIOTest {

	@Override
	public List<TestCase<byte[], ReactiveBytesIO.Readable>> getTestCases() {
		return List.of(new TestCase<>("Using file", data -> {
			try {
				Path path = Files.createTempFile("test", "lc-reactive-io");
				path.toFile().deleteOnExit();
				try (FileOutputStream out = new FileOutputStream(path.toFile())) {
					out.write(data);
				}
				ReactiveBytesIO.Readable file = ReactiveFileIO.openReadable(path).block();
				var io = ReactiveBytesIO.from(file.toFlux());
				io.onClose(file.close());
				return io;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}));
	}
	
}
