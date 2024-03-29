package net.lecousin.commons.io.bytes.memory;

import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assertions;

import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
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
					FileIO.Writable.Appendable io = new FileIO.Writable.Appendable(path);
					BufferedWritableBytesDataIO<FileIO.Writable.Appendable> buffered = new BufferedWritableBytesDataIO<>(io, true);
					return new WritableTestCase<>(buffered, path);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}),
			new TestCase<>("Using FileIO, minimum buffer size and Little-Endian", size -> {
				try {
					Path path = Files.createTempFile("test-lc-commons-io-buffered-writable", "");
					path.toFile().deleteOnExit();
					try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
						f.setLength(size);
					}
					FileIO.Writable.Appendable io = new FileIO.Writable.Appendable(path);
					BufferedWritableBytesDataIO<FileIO.Writable.Appendable> buffered = new BufferedWritableBytesDataIO<>(io, 0, true);
					return new WritableTestCase<>(buffered, path);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}),
			new TestCase<>("Using FileIO, with default buffer size and Big-Endian", size -> {
				try {
					Path path = Files.createTempFile("test-lc-commons-io-buffered-writable", "");
					path.toFile().deleteOnExit();
					try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
						f.setLength(size);
					}
					FileIO.Writable.Appendable io = new FileIO.Writable.Appendable(path);
					BufferedWritableBytesDataIO<FileIO.Writable.Appendable> buffered = new BufferedWritableBytesDataIO<>(io, ByteOrder.BIG_ENDIAN, false);
					buffered.onClose(() -> {
						try {
							io.close();
						} catch (Exception e) {
							// ignore
						}
					});
					return new WritableTestCase<>(buffered, path);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}),
			new TestCase<>("Using ByteArray, with default buffer size and Big-Endian", size -> {
				ByteArray ba = new ByteArray(new byte[Math.min(size, 1)]);
				BufferedWritableBytesDataIO<ByteArrayIO.Appendable> buffered = new BufferedWritableBytesDataIO<>(ba.asAppendableBytesIO(11), ByteOrder.BIG_ENDIAN, false);
				return new WritableTestCase<>(buffered, ba);
			})
		);
	}
	
	@Override
	protected void checkWrittenData(BytesDataIO.Writable io, Object object, byte[] expected) throws Exception {
		byte[] found = null;
		if (object instanceof Path path) {
			found = Files.readAllBytes(path);
		} else {
			ByteArray ba = (ByteArray) object;
			found = new byte[ba.getSize()];
			System.arraycopy(ba.getArray(), ba.getArrayStartOffset(), found, 0, found.length);
		}
		Assertions.assertArrayEquals(expected, found);
	}
	
}
