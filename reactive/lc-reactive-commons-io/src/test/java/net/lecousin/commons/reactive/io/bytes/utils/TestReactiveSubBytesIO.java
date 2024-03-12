package net.lecousin.commons.reactive.io.bytes.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assertions;

import net.lecousin.commons.io.bytes.memory.ByteArray;
import net.lecousin.commons.reactive.io.bytes.AbstractReadWriteReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.AbstractReadableSeekableReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.AbstractWritableReactiveBytesIOTest.WritableTestCase;
import net.lecousin.commons.reactive.io.bytes.AbstractWritableSeekableReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import net.lecousin.commons.reactive.io.bytes.file.ReactiveFileIO;
import net.lecousin.commons.test.TestCase;

public class TestReactiveSubBytesIO {
	
	public static class TestReadableSeekable extends AbstractReadableSeekableReactiveBytesIOTest {
		@Override
		public List<? extends TestCase<byte[], ReactiveBytesIO.Readable.Seekable>> getTestCases() {
			return List.of(
				new TestCase<>("ByteArrayIO", data -> {
					byte[] buf = new byte[data.length + 777];
					System.arraycopy(data, 0, buf, 111, data.length);
					return ReactiveBytesIO.subOf(ReactiveBytesIO.fromByteArray(new ByteArray(buf)).asReadableSeekableBytesIO(), 111, 111 + data.length, false);
				}),
				new TestCase<>("FileIO", data -> {
					try {
						Path path = Files.createTempFile("test", "lc-reactive-io");
						path.toFile().deleteOnExit();
						try (FileOutputStream out = new FileOutputStream(path.toFile())) {
							out.write(new byte[111]);
							out.write(data);
							out.write(new byte[666]);
						}
						ReactiveBytesIO.Readable.Seekable fio = ReactiveFileIO.openReadableSeekable(path).block();
						return ReactiveBytesIO.subOf(fio, 111, 111 + data.length, true);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
	}
	
	private static void checkData(ReactiveBytesIO.Writable io, Object generatedData, byte[] expected) {
		ReactiveBytesIO.Readable reader;
		if (generatedData instanceof Path) {
			reader = ReactiveFileIO.openReadable((Path)generatedData).block();
		} else {
			reader = ReactiveBytesIO.fromByteArray((ByteArray) generatedData);
		}
		reader.skipFully(111).block();
		byte[] found = new byte[expected.length];
		reader.readBytesFully(found).block();
		reader.close().block();
		Assertions.assertArrayEquals(expected, found);
	}
	
	public static class TestWritableSeekable extends AbstractWritableSeekableReactiveBytesIOTest {
		
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("ByteArrayIO", size -> {
					ByteArray ba = new ByteArray(new byte[size + 777]);
					return new WritableTestCase<>(ReactiveBytesIO.subOf(ReactiveBytesIO.fromByteArray(ba).asWritableSeekableBytesIO(), 111, 111 + size, false), ba);
				}),
				new TestCase<>("FileIO", size -> {
					try {
						Path path = Files.createTempFile("test", "lc-reactive-io");
						path.toFile().deleteOnExit();
						try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
							f.setLength(size + 777);
						}
						ReactiveBytesIO.Writable.Seekable fio = ReactiveFileIO.openWritableSeekable(path).block();
						return new WritableTestCase<>(ReactiveBytesIO.subOf(fio, 111, 111 + size, true), path);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
		
		@Override
		protected void checkWrittenData(ReactiveBytesIO.Writable.Seekable io, Object object, byte[] expected) throws Exception {
			checkData(io, object, expected);
		}
		
	}
	
	public static class TestReadWrite extends AbstractReadWriteReactiveBytesIOTest<ReactiveBytesIO.ReadWrite> {
		
		@Override
		public List<TestCase<Integer, ReactiveBytesIO.ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>(
					"ByteArrayIO",
					initialSize -> ReactiveBytesIO.subOfReadWrite(
						ReactiveBytesIO.fromByteArray(new ByteArray(new byte[initialSize + 777])),
						111, 111 + initialSize, false
					)
				),
				new TestCase<>(
					"FileIO",
					initialSize -> {
						try {
							Path path = Files.createTempFile("test", "lc-reactive-io");
							path.toFile().deleteOnExit();
							try (RandomAccessFile f = new RandomAccessFile(path.toFile(), "rw")) {
								f.setLength(initialSize + 777);
							}
							ReactiveBytesIO.ReadWrite fio = ReactiveFileIO.openReadWrite(path).block();
							return ReactiveBytesIO.subOfReadWrite(fio, 111, 111 + initialSize, true);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				)
			);
		}
		
	}
	
}
