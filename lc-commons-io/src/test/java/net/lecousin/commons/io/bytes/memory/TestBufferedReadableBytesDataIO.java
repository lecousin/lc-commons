package net.lecousin.commons.io.bytes.memory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.commons.io.bytes.data.AbstractReadableBytesDataIOTest;
import net.lecousin.commons.io.bytes.data.BytesDataIO;
import net.lecousin.commons.io.bytes.file.FileIO;
import net.lecousin.commons.io.bytes.utils.CompositeBytesDataIO;
import net.lecousin.commons.test.TestCase;

public class TestBufferedReadableBytesDataIO extends AbstractReadableBytesDataIOTest {

	@Override
	public List<? extends TestCase<byte[], BytesDataIO.Readable>> getTestCases() {
		return List.of(
			new TestCase<>("Using FileIO and default Little-Endian order", content -> {
				try {
					Path path = Files.createTempFile("test-lc-commons-io-buffered-readable", "");
					Files.copy(new ByteArrayInputStream(content), path, StandardCopyOption.REPLACE_EXISTING);
					path.toFile().deleteOnExit();
					return new BufferedReadableBytesDataIO(new FileIO.Readable(path), true);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}),
			new TestCase<>("Using FileIO and Big-Endian order", content -> {
				try {
					Path path = Files.createTempFile("test-lc-commons-io-buffered-readable", "");
					Files.copy(new ByteArrayInputStream(content), path, StandardCopyOption.REPLACE_EXISTING);
					path.toFile().deleteOnExit();
					return new BufferedReadableBytesDataIO(new FileIO.Readable(path), ByteOrder.BIG_ENDIAN, true);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}),
			new TestCase<>("Using ByteArrayIO and default Little-Endian order", content -> new BufferedReadableBytesDataIO(new ByteArray(content).asBytesDataIO(), false)),
			new TestCase<>("Using Composite of ByteArray of 3 bytes", content -> {
				List<BytesDataIO.Readable> list = new LinkedList<>();
				int pos = 0;
				while (pos + 3 <= content.length) {
					list.add(new ByteArray(content, pos, 3).asBytesDataIO());
					pos += 3;
				}
				if (pos < content.length)
					list.add(new ByteArray(content, pos, content.length - pos).asBytesDataIO());
				try {
					return new BufferedReadableBytesDataIO(CompositeBytesDataIO.fromReadable(list, ByteOrder.BIG_ENDIAN, true, true), true);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
		);
	}
	
}
