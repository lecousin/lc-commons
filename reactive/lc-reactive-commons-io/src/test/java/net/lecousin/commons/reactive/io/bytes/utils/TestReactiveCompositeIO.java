package net.lecousin.commons.reactive.io.bytes.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import net.lecousin.commons.io.bytes.memory.ByteArray;
import net.lecousin.commons.reactive.io.bytes.AbstractReadWriteReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.AbstractReadableReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.AbstractReadableSeekableReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.AbstractWritableReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.AbstractWritableReactiveBytesIOTest.WritableTestCase;
import net.lecousin.commons.reactive.io.bytes.AbstractWritableSeekableReactiveBytesIOTest;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import net.lecousin.commons.reactive.io.bytes.file.ReactiveFileIO;
import net.lecousin.commons.test.TestCase;

// because it can use files, and be quite long
public class TestReactiveCompositeIO {
	
	private static Path createFile(byte[] data, int off, int len) {
		try {
			Path path = Files.createTempFile("test", "lc-reactive-io");
			path.toFile().deleteOnExit();
			try (FileOutputStream out = new FileOutputStream(path.toFile())) {
				out.write(data, off, len);
			}
			return path;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Path createFile(int size) {
		return createFile(new byte[size], 0, size);
	}
	
	private static ReactiveBytesIO.Readable createReadableFile(byte[] data, int off, int len) {
		Path path = createFile(data, off, len);
		return ReactiveFileIO.openReadable(path).block();
	}
	
	private static ReactiveBytesIO.Readable.Seekable createReadableSeekableFile(byte[] data, int off, int len) {
		Path path = createFile(data, off, len);
		return ReactiveFileIO.openReadableSeekable(path).block();
	}

	public static class TestReadable extends AbstractReadableReactiveBytesIOTest {
		
		@Override
		public List<? extends TestCase<byte[], ReactiveBytesIO.Readable>> getTestCases() {
			return List.of(
				new TestCase<>(
					"Single ByteArrayIO",
					data -> ReactiveCompositeBytesIO.fromReadable(List.of(ReactiveBytesIO.fromByteArray(new ByteArray(data))), false).block()
				),
				new TestCase<>(
					"3 ByteArrayIO sharing the buffer",
					data -> ReactiveCompositeBytesIO.fromReadable(List.of(
						ReactiveBytesIO.fromByteArray(new ByteArray(data, 0, data.length / 3)),
						ReactiveBytesIO.fromByteArray(new ByteArray(data, data.length / 3, data.length / 3)),
						ReactiveBytesIO.fromByteArray(new ByteArray(data, (data.length / 3) * 2, data.length - ((data.length / 3) * 2)))
					), false).block()
				),
				new TestCase<>(
					"Single FileIO",
					data -> ReactiveCompositeBytesIO.fromReadable(List.of(createReadableFile(data, 0, data.length)), true).block()
				),
				new TestCase<>(
					"3 FileIO",
					data -> ReactiveCompositeBytesIO.fromReadable(List.of(
						createReadableFile(data, 0, data.length / 3),
						createReadableFile(data, data.length / 3, data.length / 3),
						createReadableFile(data, (data.length / 3) * 2, data.length - ((data.length / 3) * 2))
					), true).block()
				),
				new TestCase<>(
					"Mixed FileIO and ByteArrayIO",
					data -> ReactiveCompositeBytesIO.fromReadable(List.of(
						ReactiveBytesIO.fromByteArray(new ByteArray(data, 0, data.length / 3)),
						createReadableFile(data, data.length / 3, data.length / 3),
						ReactiveBytesIO.fromByteArray(new ByteArray(data, (data.length / 3) * 2, data.length - ((data.length / 3) * 2)))
					), true).block()
				)
			);
		}
		
	}
	
	public static class TestReadableSeekable extends AbstractReadableSeekableReactiveBytesIOTest {
		
		@Override
		public List<? extends TestCase<byte[], ReactiveBytesIO.Readable.Seekable>> getTestCases() {
			return List.of(
				new TestCase<>(
					"Single ByteArrayIO",
					data -> ReactiveCompositeBytesIO.fromReadableSeekable(List.of(ReactiveBytesIO.fromByteArray(new ByteArray(data))), false).block()
				),
				new TestCase<>(
					"3 ByteArrayIO sharing the buffer",
					data -> ReactiveCompositeBytesIO.fromReadableSeekable(List.of(
						ReactiveBytesIO.fromByteArray(new ByteArray(data, 0, data.length / 3)),
						ReactiveBytesIO.fromByteArray(new ByteArray(data, data.length / 3, data.length / 3)),
						ReactiveBytesIO.fromByteArray(new ByteArray(data, (data.length / 3) * 2, data.length - ((data.length / 3) * 2)))
					), false).block()
				),
				new TestCase<>(
					"Single FileIO",
					data -> ReactiveCompositeBytesIO.fromReadableSeekable(List.of(createReadableSeekableFile(data, 0, data.length)), true).block()
				),
				new TestCase<>(
					"3 FileIO",
					data -> ReactiveCompositeBytesIO.fromReadableSeekable(List.of(
						createReadableSeekableFile(data, 0, data.length / 3),
						createReadableSeekableFile(data, data.length / 3, data.length / 3),
						createReadableSeekableFile(data, (data.length / 3) * 2, data.length - ((data.length / 3) * 2))
					), true).block()
				),
				new TestCase<>(
					"Mixed FileIO and ByteArrayIO",
					data -> ReactiveCompositeBytesIO.fromReadableSeekable(List.of(
						ReactiveBytesIO.fromByteArray(new ByteArray(data, 0, data.length / 3)),
						createReadableSeekableFile(data, data.length / 3, data.length / 3),
						ReactiveBytesIO.fromByteArray(new ByteArray(data, (data.length / 3) * 2, data.length - ((data.length / 3) * 2)))
					), true).block()
				)
			);
		}
		
	}
	
	private static void checkData(ReactiveBytesIO.Writable io, Object generatedData, byte[] expected) {
		List<?> list = (List<?>) generatedData;
		int i = 0;
		int partSize = expected.length / list.size();
		while (i < list.size() - 1) {
			checkPart(list.get(i), expected, partSize * i, partSize);
			i++;
		}
		checkPart(list.get(i), expected, partSize * i, expected.length - (partSize * i));
	}

	private static void checkPart(Object part, byte[] data, int off, int len) {
		byte[] written = new byte[len];
		if (part instanceof Path path) {
			try (FileInputStream input = new FileInputStream(path.toFile())) {
				int pos = 0;
				while (pos < written.length) {
					int nb = input.read(written, pos, written.length - pos);
					if (nb <= 0) throw new EOFException();
					pos += nb;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			System.arraycopy((byte[])part, 0, written, 0, len);
		}
		byte[] expected;
		if (off == 0 && len == data.length)
			expected = data;
		else {
			expected = new byte[len];
			System.arraycopy(data, off, expected, 0, len);
		}
		assertThat(written).containsExactly(expected);
	}

	
	public static class TestWritable extends AbstractWritableReactiveBytesIOTest {
		
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<?, ?>>> getTestCases() {
			return List.of(
				new TestCase<>(
					"Single ByteArrayIO",
					expectedWriteSize -> {
						byte[] buffer = new byte[expectedWriteSize];
						ReactiveBytesIO.Writable bio = ReactiveBytesIO.fromByteArray(new ByteArray(buffer));
						ReactiveBytesIO.Writable cio = ReactiveCompositeBytesIO.fromWritable(List.of(bio), false).block();
						return new WritableTestCase<>(cio, List.of(buffer));
					}
				),
				new TestCase<>(
					"3 ByteArrayIO",
					expectedWriteSize -> {
						byte[] buffer1 = new byte[expectedWriteSize / 3];
						byte[] buffer2 = new byte[expectedWriteSize / 3];
						byte[] buffer3 = new byte[expectedWriteSize - ((expectedWriteSize / 3) * 2)];
						return new WritableTestCase<>(
							ReactiveCompositeBytesIO.fromWritable(List.of(
								ReactiveBytesIO.fromByteArray(new ByteArray(buffer1)),
								ReactiveBytesIO.fromByteArray(new ByteArray(buffer2)),
								ReactiveBytesIO.fromByteArray(new ByteArray(buffer3))
							), false).block(),
							List.of(buffer1, buffer2, buffer3)
						);
					}
				),
				new TestCase<>(
					"Single FileIO",
					expectedWriteSize -> {
						Path path = createFile(expectedWriteSize);
						return new WritableTestCase<>(
							ReactiveCompositeBytesIO.fromWritable(List.of(ReactiveFileIO.openWritable(path).block()), true).block(),
							List.of(path)
						);
					}
				),
				new TestCase<>(
					"3 FileIO",
					expectedWriteSize -> {
						Path path1 = createFile(expectedWriteSize / 3);
						Path path2 = createFile(expectedWriteSize / 3);
						Path path3 = createFile(expectedWriteSize - ((expectedWriteSize / 3) * 2));
						return new WritableTestCase<>(
							ReactiveCompositeBytesIO.fromWritable(List.of(
								ReactiveFileIO.openWritable(path1).block(),
								ReactiveFileIO.openWritable(path2).block(),
								ReactiveFileIO.openWritable(path3).block()
							), true).block(),
							List.of(path1, path2, path3)
						);
					}
				),
				new TestCase<>(
					"Mixed FileIO and ByteArrayIO",
					expectedWriteSize -> {
						byte[] buffer1 = new byte[expectedWriteSize / 3];
						Path path2 = createFile(expectedWriteSize / 3);
						byte[] buffer3 = new byte[expectedWriteSize - ((expectedWriteSize / 3) * 2)];
						return new WritableTestCase<>(
							ReactiveCompositeBytesIO.fromWritable(List.of(
								ReactiveBytesIO.fromByteArray(new ByteArray(buffer1)),
								ReactiveFileIO.openWritable(path2).block(),
								ReactiveBytesIO.fromByteArray(new ByteArray(buffer3))
							), false).block(),
							List.of(buffer1, path2, buffer3)
						);
					}
				)
			);
		}
		
		@Override
		protected void checkWrittenData(ReactiveBytesIO.Writable io, Object object, byte[] expected) throws Exception {
			checkData(io, object, expected);
		}
		
	}

	public static class TestWritableSeekable extends AbstractWritableSeekableReactiveBytesIOTest {
		
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>(
					"Single ByteArrayIO",
					size -> {
						byte[] buffer = new byte[size];
						ReactiveBytesIO.Writable.Seekable bio = ReactiveBytesIO.fromByteArray(new ByteArray(buffer));
						ReactiveBytesIO.Writable.Seekable cio = ReactiveCompositeBytesIO.fromWritableSeekable(List.of(bio), false).block();
						return new WritableTestCase<>(cio, List.of(buffer));
					}
				),
				new TestCase<>(
					"3 ByteArrayIO",
					size -> {
						byte[] buffer1 = new byte[size / 3];
						byte[] buffer2 = new byte[size / 3];
						byte[] buffer3 = new byte[size - ((size / 3) * 2)];
						return new WritableTestCase<>(
							ReactiveCompositeBytesIO.fromWritableSeekable(List.of(
								ReactiveBytesIO.fromByteArray(new ByteArray(buffer1)),
								ReactiveBytesIO.fromByteArray(new ByteArray(buffer2)),
								ReactiveBytesIO.fromByteArray(new ByteArray(buffer3))
							), false).block(),
							List.of(buffer1, buffer2, buffer3)
						);
					}
				),
				new TestCase<>(
					"Single FileIO",
					size -> {
						Path path = createFile(size);
						return new WritableTestCase<>(
							ReactiveCompositeBytesIO.fromWritableSeekable(List.of(ReactiveFileIO.openWritableSeekable(path).block()), true).block(),
							List.of(path)
						);
					}
				),
				new TestCase<>(
					"3 FileIO",
					size -> {
						Path path1 = createFile(size / 3);
						Path path2 = createFile(size / 3);
						Path path3 = createFile(size - ((size / 3) * 2));
						return new WritableTestCase<>(
							ReactiveCompositeBytesIO.fromWritableSeekable(List.of(
								ReactiveFileIO.openWritableSeekable(path1).block(),
								ReactiveFileIO.openWritableSeekable(path2).block(),
								ReactiveFileIO.openWritableSeekable(path3).block()
							), true).block(),
							List.of(path1, path2, path3)
						);
					}
				),
				new TestCase<>(
					"Mixed FileIO and ByteArrayIO",
					size -> {
						byte[] buffer1 = new byte[size / 3];
						Path path2 = createFile(size / 3);
						byte[] buffer3 = new byte[size - ((size / 3) * 2)];
						return new WritableTestCase<>(
							ReactiveCompositeBytesIO.fromWritableSeekable(List.of(
								ReactiveBytesIO.fromByteArray(new ByteArray(buffer1)),
								ReactiveFileIO.openWritableSeekable(path2).block(),
								ReactiveBytesIO.fromByteArray(new ByteArray(buffer3))
							), false).block(),
							List.of(buffer1, path2, buffer3)
						);
					}
				)
			);
		}
		
		@Override
		protected void checkWrittenData(ReactiveBytesIO.Writable.Seekable io, Object generatedData, byte[] expected) {
			checkData(io, generatedData, expected);
		}
		
	}
	
	public static class TestReadWrite extends AbstractReadWriteReactiveBytesIOTest<ReactiveBytesIO.ReadWrite> {
		
		@Override
		public List<? extends TestCase<Integer, ReactiveBytesIO.ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>(
					"Single ByteArrayIO",
					size -> {
						byte[] buffer = new byte[size];
						ReactiveBytesIO.ReadWrite bio = ReactiveBytesIO.fromByteArray(new ByteArray(buffer));
						return ReactiveCompositeBytesIO.fromReadWrite(List.of(bio), false).block();
					}
				),
				new TestCase<>(
					"3 ByteArrayIO",
					size -> {
						byte[] buffer1 = new byte[size / 3];
						byte[] buffer2 = new byte[size / 3];
						byte[] buffer3 = new byte[size - ((size / 3) * 2)];
						return ReactiveCompositeBytesIO.fromReadWrite(List.of(
							ReactiveBytesIO.fromByteArray(new ByteArray(buffer1)),
							ReactiveBytesIO.fromByteArray(new ByteArray(buffer2)),
							ReactiveBytesIO.fromByteArray(new ByteArray(buffer3))
						), false).block();
					}
				),
				new TestCase<>(
					"Single FileIO",
					size -> {
						Path path = createFile(size);
						return ReactiveCompositeBytesIO.fromReadWrite(List.of(ReactiveFileIO.openReadWrite(path).block()), true).block();
					}
				),
				new TestCase<>(
					"3 FileIO",
					size -> {
						Path path1 = createFile(size / 3);
						Path path2 = createFile(size / 3);
						Path path3 = createFile(size - ((size / 3) * 2));
						return ReactiveCompositeBytesIO.fromReadWrite(List.of(
							ReactiveFileIO.openReadWrite(path1).block(),
							ReactiveFileIO.openReadWrite(path2).block(),
							ReactiveFileIO.openReadWrite(path3).block()
						), true).block();
					}
				),
				new TestCase<>(
					"Mixed FileIO and ByteArrayIO",
					size -> {
						byte[] buffer1 = new byte[size / 3];
						Path path2 = createFile(size / 3);
						byte[] buffer3 = new byte[size - ((size / 3) * 2)];
						return ReactiveCompositeBytesIO.fromReadWrite(List.of(
							ReactiveBytesIO.fromByteArray(new ByteArray(buffer1)),
							ReactiveFileIO.openReadWrite(path2).block(),
							ReactiveBytesIO.fromByteArray(new ByteArray(buffer3))
						), false).block();
					}
				)
			);
		}
	}
	
}
