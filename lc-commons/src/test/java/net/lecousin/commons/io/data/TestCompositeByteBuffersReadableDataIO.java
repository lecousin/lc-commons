package net.lecousin.commons.io.data;

import java.nio.ByteBuffer;
import java.util.List;

import net.lecousin.commons.test.TestCase;

public class TestCompositeByteBuffersReadableDataIO {

	public static class TestReadable extends AbstractReadableBytesDataIOTest {
		
		@Override
		public List<? extends TestCase<byte[], BytesDataIO.Readable>> getTestCases() {
			return List.of(
				new TestCase<>(
					"Single ByteBuffer exact data LittleEndian",
					data -> new CompositeByteBuffersReadableDataIO.LittleEndian(List.of(ByteBuffer.wrap(data)))
				),
				new TestCase<>(
					"Single ByteBuffer exact data BigEndian",
					data -> new CompositeByteBuffersReadableDataIO.BigEndian(List.of(ByteBuffer.wrap(data)))
				),
				new TestCase<>(
					"Single ByteBuffer with offset LittleEndian",
					data -> {
						byte[] b = new byte[data.length + 259];
						System.arraycopy(data, 0, b, 13, data.length);
						return new CompositeByteBuffersReadableDataIO.LittleEndian(List.of(ByteBuffer.wrap(b, 13, data.length)));
					}
				),
				new TestCase<>(
					"Single ByteBuffer with offset BigEndian",
					data -> {
						byte[] b = new byte[data.length + 259];
						System.arraycopy(data, 0, b, 13, data.length);
						return new CompositeByteBuffersReadableDataIO.BigEndian(List.of(ByteBuffer.wrap(b, 13, data.length)));
					}
				),
				new TestCase<>(
					"3 ByteBuffer LittleEndian",
					data -> {
						byte[] b1 = new byte[data.length / 3];
						byte[] b2 = new byte[data.length / 3];
						byte[] b3 = new byte[data.length - (data.length / 3) * 2];
						System.arraycopy(data, 0, b1, 0, b1.length);
						System.arraycopy(data, b1.length, b2, 0, b2.length);
						System.arraycopy(data, b1.length + b2.length, b3, 0, b3.length);
						return new CompositeByteBuffersReadableDataIO.LittleEndian(List.of(ByteBuffer.wrap(b1), ByteBuffer.wrap(b2), ByteBuffer.wrap(b3)));
					}
				),
				new TestCase<>(
					"3 ByteBuffer BigEndian",
					data -> {
						byte[] b1 = new byte[data.length / 3];
						byte[] b2 = new byte[data.length / 3];
						byte[] b3 = new byte[data.length - (data.length / 3) * 2];
						System.arraycopy(data, 0, b1, 0, b1.length);
						System.arraycopy(data, b1.length, b2, 0, b2.length);
						System.arraycopy(data, b1.length + b2.length, b3, 0, b3.length);
						return new CompositeByteBuffersReadableDataIO.BigEndian(List.of(ByteBuffer.wrap(b1), ByteBuffer.wrap(b2), ByteBuffer.wrap(b3)));
					}
				),
				new TestCase<>(
					"LittleEndian buffers of 1",
					data -> {
						CompositeByteBuffersReadableDataIO io = new CompositeByteBuffersReadableDataIO.LittleEndian();
						for (int i = 0; i < data.length; ++i)
							io.add(ByteBuffer.wrap(data, i, 1));
						return io;
					}
				),
				new TestCase<>(
					"BigEndian buffers of 1",
					data -> {
						CompositeByteBuffersReadableDataIO io = new CompositeByteBuffersReadableDataIO.BigEndian();
						for (int i = 0; i < data.length; ++i)
							io.add(ByteBuffer.wrap(data, i, 1));
						return io;
					}
				)
			);
		}
	}
	
}
