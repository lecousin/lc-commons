package net.lecousin.commons.io.bytes.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteOrder;
import java.util.List;
import java.util.function.IntBinaryOperator;

import net.lecousin.commons.io.bytes.AbstractReadWriteBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractReadableSeekableBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
import net.lecousin.commons.io.bytes.AbstractWritableSeekableBytesIOTest;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.bytes.data.AbstractReadWriteBytesDataIOTest;
import net.lecousin.commons.io.bytes.data.AbstractReadableSeekableBytesDataIOTest;
import net.lecousin.commons.io.bytes.data.AbstractWritableSeekableBytesDataIOTest;
import net.lecousin.commons.io.bytes.data.BytesDataIO;
import net.lecousin.commons.test.TestCase;

public class TestByteArrayIO {

	public static class TestReadableSeekableBytesIO extends AbstractReadableSeekableBytesIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesIO.Readable.Seekable>> getTestCases() {
			return List.of(
				new TestCase<>("Exact buffer", data -> new ByteArray(data).asBytesIO().asReadableSeekableBytesIO()),
				new TestCase<>("Larger buffer starting at 0", data -> {
					byte[] b = new byte[data.length + 129];
					System.arraycopy(data, 0, b, 0, data.length);
					return new ByteArray(b, 0, data.length).asBytesIO().asReadableSeekableBytesIO();
				}),
				new TestCase<>("Larger buffer with start offset", data -> {
					byte[] b = new byte[data.length + 329];
					System.arraycopy(data, 0, b, 111, data.length);
					return new ByteArray(b, 111, data.length).asBytesIO().asReadableSeekableBytesIO();
				})
			);
		}
	}

	public static class TestReadableSeekableBytesDataIO extends AbstractReadableSeekableBytesDataIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesDataIO.Readable.Seekable>> getTestCases() {
			return List.of(
				new TestCase<>("LE Exact buffer", data -> new ByteArray(data).asBytesDataIO(ByteOrder.LITTLE_ENDIAN).asReadableSeekableBytesDataIO()),
				new TestCase<>("LE Larger buffer starting at 0", data -> {
					byte[] b = new byte[data.length + 129];
					System.arraycopy(data, 0, b, 0, data.length);
					return new ByteArray(b, 0, data.length).asBytesDataIO(ByteOrder.LITTLE_ENDIAN).asReadableSeekableBytesDataIO();
				}),
				new TestCase<>("LE Larger buffer with start offset", data -> {
					byte[] b = new byte[data.length + 329];
					System.arraycopy(data, 0, b, 111, data.length);
					return new ByteArray(b, 111, data.length).asBytesDataIO(ByteOrder.LITTLE_ENDIAN).asReadableSeekableBytesDataIO();
				}),
				new TestCase<>("BE Exact buffer", data -> new ByteArray(data).asBytesDataIO(ByteOrder.BIG_ENDIAN).asReadableSeekableBytesDataIO()),
				new TestCase<>("BE Larger buffer starting at 0", data -> {
					byte[] b = new byte[data.length + 129];
					System.arraycopy(data, 0, b, 0, data.length);
					return new ByteArray(b, 0, data.length).asBytesDataIO(ByteOrder.BIG_ENDIAN).asReadableSeekableBytesDataIO();
				}),
				new TestCase<>("BE Larger buffer with start offset", data -> {
					byte[] b = new byte[data.length + 329];
					System.arraycopy(data, 0, b, 111, data.length);
					return new ByteArray(b, 111, data.length).asBytesDataIO(ByteOrder.BIG_ENDIAN).asReadableSeekableBytesDataIO();
				})
			);
		}
	}

	public static class TestWritableSeekableBytesIO extends AbstractWritableSeekableBytesIOTest {
		IntBinaryOperator minimumStrategy = (current,add) -> current + add;
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("Exact buffer", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asBytesIO().asWritableSeekableBytesIO(), b);
				}),
				new TestCase<>("Larger buffer starting at 0", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asBytesIO().asWritableSeekableBytesIO(), b);
				}),
				new TestCase<>("Larger buffer with start offset", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asBytesIO().asWritableSeekableBytesIO(), b);
				}),
				new TestCase<>("Exact buffer appendable", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asAppendableBytesIO().asNonResizableWritableSeekableBytesIO(), b);
				}),
				new TestCase<>("Larger buffer starting at 0 appendable", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asAppendableBytesIO().asNonResizableWritableSeekableBytesIO(), b);
				}),
				new TestCase<>("Larger buffer with start offset appendable", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asAppendableBytesIO().asNonResizableWritableSeekableBytesIO(), b);
				}),
				new TestCase<>("Exact buffer appendable minimumAppendSize=50", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asAppendableBytesIO(50).asNonResizableWritableSeekableBytesIO(), b);
				}),
				new TestCase<>("Larger buffer starting at 0 appendable minimumAppendSize=50", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asAppendableBytesIO(50).asNonResizableWritableSeekableBytesIO(), b);
				}),
				new TestCase<>("Larger buffer with start offset appendable minimumAppendSize=50", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asAppendableBytesIO(50).asNonResizableWritableSeekableBytesIO(), b);
				}),
				new TestCase<>("Exact buffer appendable strategy=minimum", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asAppendableBytesIO(minimumStrategy).asNonResizableWritableSeekableBytesIO(), b);
				}),
				new TestCase<>("Larger buffer starting at 0 appendable strategy=minimum", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asAppendableBytesIO(minimumStrategy).asNonResizableWritableSeekableBytesIO(), b);
				}),
				new TestCase<>("Larger buffer with start offset appendable strategy=minimum", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asAppendableBytesIO(minimumStrategy).asNonResizableWritableSeekableBytesIO(), b);
				}),
				new TestCase<>("Exact buffer resizable", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asBytesIO(), b);
				}),
				new TestCase<>("Larger buffer starting at 0 resizable", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asBytesIO(), b);
				}),
				new TestCase<>("Larger buffer with start offset resizable", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asBytesIO(), b);
				}),
				new TestCase<>("Exact buffer appendable resizable", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asAppendableBytesIO(), b);
				}),
				new TestCase<>("Larger buffer starting at 0 appendable resizable", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asAppendableBytesIO(), b);
				}),
				new TestCase<>("Larger buffer with start offset appendable resizable", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asAppendableBytesIO(), b);
				})
			);
		}
		
		@Override
		protected void checkWrittenData(BytesIO.Writable.Seekable io, Object object, byte[] expected) throws Exception {
			ByteArray ba = (ByteArray) object;
			assertThat(ba.getSize()).isEqualTo(expected.length);
			ba.trim();
			assertThat(ba.getSize()).isEqualTo(expected.length);
			assertThat(ba.getArray()).containsExactly(expected);
			assertThat(ba.getArrayStartOffset()).isZero();
		}
	}

	@SuppressWarnings("rawtypes")
	public static class TestReadWriteBytesIO extends AbstractReadWriteBytesIOTest {
		@Override
		public List<? extends TestCase<Integer, ?>> getTestCases() {
			return List.of(
				new TestCase<>("Exact buffer", size -> new ByteArray(new byte[size]).asBytesIO().asNonResizableReadWriteBytesIO()),
				new TestCase<>("Larger buffer starting at 0", size -> new ByteArray(new byte[size + 129], 0, size).asBytesIO().asNonResizableReadWriteBytesIO()),
				new TestCase<>("Larger buffer with start offset", size -> new ByteArray(new byte[size + 329], 111, size).asBytesIO().asNonResizableReadWriteBytesIO()),
				new TestCase<>("Exact buffer appendable", size -> new ByteArray(new byte[size]).asAppendableBytesIO().asNonResizableReadWriteBytesIO()),
				new TestCase<>("Larger buffer starting at 0 appendable", size -> new ByteArray(new byte[size + 129], 0, size).asAppendableBytesIO().asNonResizableReadWriteBytesIO()),
				new TestCase<>("Larger buffer with start offset appendable", size -> new ByteArray(new byte[size + 329], 111, size).asAppendableBytesIO().asNonResizableReadWriteBytesIO()),
				new TestCase<>("Exact buffer resizable", size -> new ByteArray(new byte[size]).asBytesIO()),
				new TestCase<>("Larger buffer starting at 0 resizable", size -> new ByteArray(new byte[size + 129], 0, size).asBytesIO()),
				new TestCase<>("Larger buffer with start offset resizable", size -> new ByteArray(new byte[size + 329], 111, size).asBytesIO()),
				new TestCase<>("Exact buffer appendable resizable", size -> new ByteArray(new byte[size]).asAppendableBytesIO()),
				new TestCase<>("Larger buffer starting at 0 appendable resizable", size -> new ByteArray(new byte[size + 129], 0, size).asAppendableBytesIO()),
				new TestCase<>("Larger buffer with start offset appendable resizable", size -> new ByteArray(new byte[size + 329], 111, size).asAppendableBytesIO())
			);
		}
	}

	
	public static class TestWritableSeekableBytesDataIO extends AbstractWritableSeekableBytesDataIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends BytesDataIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("LE Exact buffer", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asBytesDataIO(ByteOrder.LITTLE_ENDIAN).asNonResizableWritableSeekableBytesDataIO(), b);
				}),
				new TestCase<>("LE Larger buffer starting at 0", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asBytesDataIO(ByteOrder.LITTLE_ENDIAN).asNonResizableWritableSeekableBytesDataIO(), b);
				}),
				new TestCase<>("LE Larger buffer with start offset", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asBytesDataIO(ByteOrder.LITTLE_ENDIAN).asNonResizableWritableSeekableBytesDataIO(), b);
				}),
				new TestCase<>("BE Exact buffer", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asBytesDataIO(ByteOrder.BIG_ENDIAN).asNonResizableWritableSeekableBytesDataIO(), b);
				}),
				new TestCase<>("BE Larger buffer starting at 0", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asBytesDataIO(ByteOrder.BIG_ENDIAN).asNonResizableWritableSeekableBytesDataIO(), b);
				}),
				new TestCase<>("BE Larger buffer with start offset", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asBytesDataIO(ByteOrder.BIG_ENDIAN).asNonResizableWritableSeekableBytesDataIO(), b);
				}),
				// Appendable
				new TestCase<>("LE Exact buffer appendable", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN).asNonResizableWritableSeekableBytesDataIO(), b);
				}),
				new TestCase<>("LE Larger buffer starting at 0 appendable", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN).asNonResizableWritableSeekableBytesDataIO(), b);
				}),
				new TestCase<>("LE Larger buffer with start offset appendable", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN).asNonResizableWritableSeekableBytesDataIO(), b);
				}),
				new TestCase<>("BE Exact buffer appendable", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asAppendableBytesDataIO(ByteOrder.BIG_ENDIAN).asNonResizableWritableSeekableBytesDataIO(), b);
				}),
				new TestCase<>("BE Larger buffer starting at 0 appendable", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asAppendableBytesDataIO(ByteOrder.BIG_ENDIAN).asNonResizableWritableSeekableBytesDataIO(), b);
				}),
				new TestCase<>("BE Larger buffer with start offset appendable", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asAppendableBytesDataIO(ByteOrder.BIG_ENDIAN).asNonResizableWritableSeekableBytesDataIO(), b);
				}),
				// Resizable
				new TestCase<>("LE Exact buffer resizable", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asBytesDataIO(ByteOrder.LITTLE_ENDIAN), b);
				}),
				new TestCase<>("LE Larger buffer starting at 0 resizable", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asBytesDataIO(ByteOrder.LITTLE_ENDIAN), b);
				}),
				new TestCase<>("LE Larger buffer with start offset resizable", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asBytesDataIO(ByteOrder.LITTLE_ENDIAN), b);
				}),
				new TestCase<>("BE Exact buffer resizable", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asBytesDataIO(ByteOrder.BIG_ENDIAN), b);
				}),
				new TestCase<>("BE Larger buffer starting at 0 resizable", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asBytesDataIO(ByteOrder.BIG_ENDIAN), b);
				}),
				new TestCase<>("BE Larger buffer with start offset resizable", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asBytesDataIO(ByteOrder.BIG_ENDIAN), b);
				}),
				// Appendable Resizable
				new TestCase<>("LE Exact buffer appendable resizable", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN), b);
				}),
				new TestCase<>("LE Larger buffer starting at 0 appendable resizable", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN), b);
				}),
				new TestCase<>("LE Larger buffer with start offset appendable resizable", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN), b);
				}),
				new TestCase<>("BE Exact buffer appendable resizable", size -> {
					ByteArray b = new ByteArray(new byte[size]);
					return new WritableTestCase<>(b.asAppendableBytesDataIO(ByteOrder.BIG_ENDIAN), b);
				}),
				new TestCase<>("BE Larger buffer starting at 0 appendable resizable", size -> {
					ByteArray b = new ByteArray(new byte[size + 129], 0, size);
					return new WritableTestCase<>(b.asAppendableBytesDataIO(ByteOrder.BIG_ENDIAN), b);
				}),
				new TestCase<>("BE Larger buffer with start offset appendable resizable", size -> {
					ByteArray b = new ByteArray(new byte[size + 329], 111, size);
					return new WritableTestCase<>(b.asAppendableBytesDataIO(ByteOrder.BIG_ENDIAN), b);
				})
			);
		}
		
		@Override
		protected void checkWrittenData(BytesDataIO.Writable.Seekable io, Object object, byte[] expected) throws Exception {
			ByteArray ba = (ByteArray) object;
			assertThat(ba.getSize()).isEqualTo(expected.length);
			ba.trim();
			assertThat(ba.getSize()).isEqualTo(expected.length);
			assertThat(ba.getArray()).containsExactly(expected);
		}
	}


	@SuppressWarnings("rawtypes")
	public static class TestReadWriteBytesDataIO extends AbstractReadWriteBytesDataIOTest {
		@Override
		public List<? extends TestCase<Integer, ?>> getTestCases() {
			IntBinaryOperator minimumStrategy = (current,add) -> current + add;
			return List.of(
				new TestCase<>("LE Exact buffer", size -> new ByteArray(new byte[size]).asBytesDataIO(ByteOrder.LITTLE_ENDIAN).asNonResizableReadWriteBytesDataIO()),
				new TestCase<>("LE Larger buffer starting at 0", size -> new ByteArray(new byte[size + 129], 0, size).asBytesDataIO(ByteOrder.LITTLE_ENDIAN).asNonResizableReadWriteBytesDataIO()),
				new TestCase<>("LE Larger buffer with start offset", size -> new ByteArray(new byte[size + 329], 111, size).asBytesDataIO(ByteOrder.LITTLE_ENDIAN).asNonResizableReadWriteBytesDataIO()),
				
				new TestCase<>("LE Exact buffer appendable", size -> new ByteArray(new byte[size]).asAppendableBytesDataIO(/* default LE */).asNonResizableReadWriteBytesDataIO()),
				new TestCase<>("LE Larger buffer starting at 0 appendable", size -> new ByteArray(new byte[size + 129], 0, size).asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN).asNonResizableReadWriteBytesDataIO()),
				new TestCase<>("LE Larger buffer with start offset appendable", size -> new ByteArray(new byte[size + 329], 111, size).asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN).asNonResizableReadWriteBytesDataIO()),
				
				new TestCase<>("LE Exact buffer appendable minimumSize=64", size -> new ByteArray(new byte[size]).asAppendableBytesDataIO(/* default LE */64).asNonResizableReadWriteBytesDataIO()),
				new TestCase<>("LE Larger buffer starting at 0 appendable minimumSize=64", size -> new ByteArray(new byte[size + 129], 0, size).asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN, 64).asNonResizableReadWriteBytesDataIO()),
				new TestCase<>("LE Larger buffer with start offset appendable minimumSize=64", size -> new ByteArray(new byte[size + 329], 111, size).asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN, 64).asNonResizableReadWriteBytesDataIO()),
				
				new TestCase<>("LE Exact buffer appendable strategy=minimum", size -> new ByteArray(new byte[size]).asAppendableBytesDataIO(/* default LE */minimumStrategy).asNonResizableReadWriteBytesDataIO()),
				new TestCase<>("LE Larger buffer starting at 0 appendable strategy=minimum", size -> new ByteArray(new byte[size + 129], 0, size).asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN, minimumStrategy).asNonResizableReadWriteBytesDataIO()),
				new TestCase<>("LE Larger buffer with start offset appendable strategy=minimum", size -> new ByteArray(new byte[size + 329], 111, size).asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN, minimumStrategy).asNonResizableReadWriteBytesDataIO()),
				
				new TestCase<>("LE Exact buffer resizable", size -> new ByteArray(new byte[size]).asBytesDataIO(/* default LE */)),
				new TestCase<>("LE Larger buffer starting at 0 resizable", size -> new ByteArray(new byte[size + 129], 0, size).asBytesDataIO(ByteOrder.LITTLE_ENDIAN)),
				new TestCase<>("LE Larger buffer with start offset resizable", size -> new ByteArray(new byte[size + 329], 111, size).asBytesDataIO(ByteOrder.LITTLE_ENDIAN)),
				
				new TestCase<>("LE Exact buffer appendable resizable", size -> new ByteArray(new byte[size]).asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN)),
				new TestCase<>("LE Larger buffer starting at 0 appendable resizable", size -> new ByteArray(new byte[size + 129], 0, size).asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN)),
				new TestCase<>("LE Larger buffer with start offset appendable resizable", size -> new ByteArray(new byte[size + 329], 111, size).asAppendableBytesDataIO(ByteOrder.LITTLE_ENDIAN)),
				
				new TestCase<>("BE Exact buffer", size -> new ByteArray(new byte[size]).asBytesDataIO(ByteOrder.BIG_ENDIAN).asNonResizableReadWriteBytesDataIO()),
				new TestCase<>("BE Larger buffer starting at 0", size -> new ByteArray(new byte[size + 129], 0, size).asBytesDataIO(ByteOrder.BIG_ENDIAN).asNonResizableReadWriteBytesDataIO()),
				new TestCase<>("BE Larger buffer with start offset", size -> new ByteArray(new byte[size + 329], 111, size).asBytesDataIO(ByteOrder.BIG_ENDIAN).asNonResizableReadWriteBytesDataIO()),
				
				new TestCase<>("BE Exact buffer appendable", size -> new ByteArray(new byte[size]).asAppendableBytesDataIO(ByteOrder.BIG_ENDIAN).asNonResizableReadWriteBytesDataIO()),
				new TestCase<>("BE Larger buffer starting at 0 appendable", size -> new ByteArray(new byte[size + 129], 0, size).asAppendableBytesDataIO(ByteOrder.BIG_ENDIAN).asNonResizableReadWriteBytesDataIO()),
				new TestCase<>("BE Larger buffer with start offset appendable", size -> new ByteArray(new byte[size + 329], 111, size).asAppendableBytesDataIO(ByteOrder.BIG_ENDIAN).asNonResizableReadWriteBytesDataIO()),
				
				new TestCase<>("BE Exact buffer resizable", size -> new ByteArray(new byte[size]).asBytesDataIO(ByteOrder.BIG_ENDIAN)),
				new TestCase<>("BE Larger buffer starting at 0 resizable", size -> new ByteArray(new byte[size + 129], 0, size).asBytesDataIO(ByteOrder.BIG_ENDIAN)),
				new TestCase<>("BE Larger buffer with start offset resizable", size -> new ByteArray(new byte[size + 329], 111, size).asBytesDataIO(ByteOrder.BIG_ENDIAN)),
				
				new TestCase<>("BE Exact buffer appendable resizable", size -> new ByteArray(new byte[size]).asAppendableBytesDataIO(ByteOrder.BIG_ENDIAN)),
				new TestCase<>("BE Larger buffer starting at 0 appendable resizable", size -> new ByteArray(new byte[size + 129], 0, size).asAppendableBytesDataIO(ByteOrder.BIG_ENDIAN)),
				new TestCase<>("BE Larger buffer with start offset appendable resizable", size -> new ByteArray(new byte[size + 329], 111, size).asAppendableBytesDataIO(ByteOrder.BIG_ENDIAN))
			);
		}
	}

}
