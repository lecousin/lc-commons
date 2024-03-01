package net.lecousin.commons.io.bytes.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import net.lecousin.commons.io.bytes.AbstractReadWriteBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractReadableSeekableBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
import net.lecousin.commons.io.bytes.AbstractWritableSeekableBytesIOTest;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.test.TestCase;

public class TestFileIO {

	public static class TestReadable extends AbstractReadableSeekableBytesIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesIO.Readable.Seekable>> getTestCases() {
			return List.of(
				new TestCase<>("File", content -> {
					try {
						Path path = Files.createTempFile("test-lc-commons-io-file", "-readable");
						Files.copy(new ByteArrayInputStream(content), path, StandardCopyOption.REPLACE_EXISTING);
						path.toFile().deleteOnExit();
						return new FileIO.Readable(path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
	}
	
	public static class TestWritable extends AbstractWritableSeekableBytesIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("File", size -> {
					try {
						Path path = Files.createTempFile("test-lc-commons-io-file", "-writable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new WritableTestCase<>(new FileIO.Writable(path), path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("File resizable", size -> {
					try {
						Path path = Files.createTempFile("test-lc-commons-io-file", "-resizable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new WritableTestCase<>(new FileIO.Writable.Resizable(path), path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("File appendable", size -> {
					try {
						Path path = Files.createTempFile("test-lc-commons-io-file", "-appendable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new WritableTestCase<>(new FileIO.Writable.Appendable(path), path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("File appendable resizable", size -> {
					try {
						Path path = Files.createTempFile("test-lc-commons-io-file", "-appendable-resizable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new WritableTestCase<>(new FileIO.Writable.AppendableResizable(path), path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
		
		@Override
		protected void checkWrittenData(BytesIO.Writable.Seekable io, Object object, byte[] expected) throws Exception {
			Path path = (Path) object;
			byte[] found = Files.readAllBytes(path);
			assertThat(found).containsExactly(expected);
		}
	}
	
	public static class TestReadWrite extends AbstractReadWriteBytesIOTest<FileIO.ReadWrite> {
		@Override
		public List<? extends TestCase<Integer, FileIO.ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>("File", size -> {
					try {
						Path path = Files.createTempFile("test-lc-commons-io-file", "-writable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new FileIO.ReadWrite(path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("File resizable", size -> {
					try {
						Path path = Files.createTempFile("test-lc-commons-io-file", "-resizable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new FileIO.ReadWrite.Resizable(path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("File appendable", size -> {
					try {
						Path path = Files.createTempFile("test-lc-commons-io-file", "-appendable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new FileIO.ReadWrite.Appendable(path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("File appendable resizable", size -> {
					try {
						Path path = Files.createTempFile("test-lc-commons-io-file", "-appendable-resizable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new FileIO.ReadWrite.AppendableResizable(path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
	}
	
}
