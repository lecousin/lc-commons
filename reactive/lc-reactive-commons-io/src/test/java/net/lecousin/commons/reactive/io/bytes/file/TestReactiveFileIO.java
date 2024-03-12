package net.lecousin.commons.reactive.io.bytes.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import net.lecousin.commons.reactive.io.bytes.AbstractReadWriteReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.AbstractReadableReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.AbstractWritableReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.AbstractWritableReactiveBytesIOTest.WritableTestCase;
import net.lecousin.commons.reactive.io.bytes.AbstractWritableSeekableReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import net.lecousin.commons.test.TestCase;

public class TestReactiveFileIO {

	public static class TestReadable extends AbstractReadableReactiveBytesIOTest {
		
		@Override
		public List<? extends TestCase<byte[], ReactiveBytesIO.Readable>> getTestCases() {
			return List.of(
				new TestCase<>("File", content -> {
					try {
						Path path = Files.createTempFile("test-lc-reactive-commons-io-file", "-readable");
						Files.copy(new ByteArrayInputStream(content), path, StandardCopyOption.REPLACE_EXISTING);
						path.toFile().deleteOnExit();
						return ReactiveFileIO.openReadable(path).block();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
		
	}
	
	public static class TestWritable extends AbstractWritableReactiveBytesIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<?, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("File", size -> {
					try {
						Path path = Files.createTempFile("test-lc-reactive-commons-io-file", "-writable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new WritableTestCase<>(ReactiveFileIO.openWritable(path).block(), path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("File appendable", size -> {
					try {
						Path path = Files.createTempFile("test-lc-reactive-commons-io-file", "-writable-appendable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new WritableTestCase<>(ReactiveFileIO.openWritableAppendable(path).block(), path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
		
		@Override
		protected void checkWrittenData(ReactiveBytesIO.Writable io, Object object, byte[] expected) throws Exception {
			Path path = (Path) object;
			byte[] found = Files.readAllBytes(path);
			assertThat(found).containsExactly(expected);
		}
	}
	
	public static class TestWritableSeekable extends AbstractWritableSeekableReactiveBytesIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("File", size -> {
					try {
						Path path = Files.createTempFile("test-lc-reactive-commons-io-file", "-writable-seekable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new WritableTestCase<>(ReactiveFileIO.openWritableSeekable(path).block(), path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("File resizable", size -> {
					try {
						Path path = Files.createTempFile("test-lc-reactive-commons-io-file", "-writable-seekable-resizable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new WritableTestCase<>(ReactiveFileIO.openWritableSeekableResizable(path).block(), path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("File appendable", size -> {
					try {
						Path path = Files.createTempFile("test-lc-reactive-commons-io-file", "-writable-seekable-appendable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new WritableTestCase<>(ReactiveFileIO.openWritableSeekableAppendable(path).block(), path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("File appendable resizable", size -> {
					try {
						Path path = Files.createTempFile("test-lc-reactive-commons-io-file", "-writable-seekable-appendable-resizable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return new WritableTestCase<>(ReactiveFileIO.openWritableSeekableAppendableResizable(path).block(), path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
		
		@Override
		protected void checkWrittenData(ReactiveBytesIO.Writable.Seekable io, Object object, byte[] expected) throws Exception {
			Path path = (Path) object;
			byte[] found = Files.readAllBytes(path);
			assertThat(found).containsExactly(expected);
		}
	}
	
	public static class TestReadWrite extends AbstractReadWriteReactiveBytesIOTest<ReactiveBytesIO.ReadWrite> {
		@Override
		public List<? extends TestCase<Integer, ReactiveBytesIO.ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>("File", size -> {
					try {
						Path path = Files.createTempFile("test-lc-reactive-commons-io-file", "-rw");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return ReactiveFileIO.openReadWrite(path).block();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("File resizable", size -> {
					try {
						Path path = Files.createTempFile("test-lc-reactive-commons-io-file", "-rw-resizable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return ReactiveFileIO.openReadWriteResizable(path).block();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("File appendable", size -> {
					try {
						Path path = Files.createTempFile("test-lc-reactive-commons-io-file", "-rw-appendable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return ReactiveFileIO.openReadWriteAppendable(path).block();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("File appendable resizable", size -> {
					try {
						Path path = Files.createTempFile("test-lc-reactive-commons-io-file", "-rw-appendable-resizable");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size);
						}
						return ReactiveFileIO.openReadWriteAppendableResizable(path).block();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
	}

	
}
