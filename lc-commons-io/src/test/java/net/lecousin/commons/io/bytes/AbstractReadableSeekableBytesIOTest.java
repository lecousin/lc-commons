package net.lecousin.commons.io.bytes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractSeekableIOTest;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.RandomContentWithBufferSizeTestCasesProvider;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.SmallRandomContentTestCasesProvider;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadableSeekableBytesIOTest implements TestCasesProvider<byte[], BytesIO.Readable.Seekable> {

	@Nested
	public class AsReadableBytesIO extends AbstractReadableBytesIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesIO.Readable>> getTestCases() {
			return AbstractReadableSeekableBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<byte[], BytesIO.Readable>) (content) -> tc.getArgumentProvider().apply(content).asReadableBytesIO()))
				.toList();
		}
	}
	
	@Nested
	public class AsSeekableIO extends AbstractSeekableIOTest {
		@Override
		public List<? extends TestCase<Integer, IO.Seekable>> getTestCases() {
			return AbstractReadableSeekableBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, IO.Seekable>) (size) -> tc.getArgumentProvider().apply(BytesIOTestUtils.generateContent(size))))
				.toList();
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(SmallRandomContentTestCasesProvider.class)
	void readByteAt(String displayName, byte[] expected, Function<byte[], BytesIO.Readable.Seekable> ioSupplier) throws Exception {
		BytesIO.Readable.Seekable io = ioSupplier.apply(expected);
		for (int i = 0; i < expected.length; i++) {
			assertThat(io.readByteAt(i)).as("Read byte " + i + "/" + expected.length).isEqualTo(expected[i]);
		}
		assertThrows(EOFException.class, () -> io.readByteAt(expected.length));
		assertThrows(EOFException.class, () -> io.readByteAt(expected.length + 1));
		assertThrows(NegativeValueException.class, () -> io.readByteAt(-1));
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readByteAt(0));
		assertThrows(ClosedChannelException.class, () -> io.readByteAt(expected.length));
		assertThrows(ClosedChannelException.class, () -> io.readByteAt(-1));
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readByteArrayAt(String displayName, byte[] expected, int bufferSize, Function<byte[], BytesIO.Readable.Seekable> ioSupplier) throws Exception {
		BytesIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		assertThrows(NullPointerException.class, () -> io.readBytesAt(0, (byte[]) null));
		assertThrows(NegativeValueException.class, () -> io.readBytesAt(-1, new byte[10], 0, 1));
		assertThrows(NegativeValueException.class, () -> io.readBytesAt(0, new byte[10], -1, 5));
		assertThrows(NegativeValueException.class, () -> io.readBytesAt(0, new byte[10], 3, -5));
		assertThrows(LimitExceededException.class, () -> io.readBytesAt(0, new byte[10], 3, 10));
		assertThrows(LimitExceededException.class, () -> io.readBytesAt(0, new byte[10], 12, 1));
		
		assertThat(io.readBytesAt(0, new byte[0])).isZero();
		assertThat(io.readBytesAt(0, new byte[10], 3, 0)).isZero();

		byte[] buffer = new byte[bufferSize];
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readBytesAt(pos, buffer);
			assertThat(nb).isPositive();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer[i]);
			pos += nb;
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readBytesAt(expected.length, buffer));
		assertEquals(-1, io.readBytesAt(expected.length + 1, buffer));
		assertEquals(-1, io.readBytesAt(expected.length, new byte[10], 1, 2));
		
		assertThat(io.readBytesAt(expected.length, new byte[0])).isZero();
		assertThat(io.readBytesAt(expected.length, new byte[10], 3, 0)).isZero();
		
		assertThrows(NullPointerException.class, () -> io.readBytesAt(0, (byte[]) null));
		assertThrows(NegativeValueException.class, () -> io.readBytesAt(-1, new byte[10], 0, 1));
		assertThrows(NegativeValueException.class, () -> io.readBytesAt(0, new byte[10], -1, 5));
		assertThrows(NegativeValueException.class, () -> io.readBytesAt(0, new byte[10], 3, -5));
		assertThrows(LimitExceededException.class, () -> io.readBytesAt(0, new byte[10], 3, 10));
		assertThrows(LimitExceededException.class, () -> io.readBytesAt(0, new byte[10], 12, 1));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readBytesAt(0, buffer));
		assertThrows(ClosedChannelException.class, () -> io.readBytesAt(0, (byte[]) null));
		assertThrows(ClosedChannelException.class, () -> io.readBytesAt(0, new byte[10], -1, 5));
		assertThrows(ClosedChannelException.class, () -> io.readBytesAt(0, new byte[10], 3, -5));
		assertThrows(ClosedChannelException.class, () -> io.readBytesAt(0, new byte[10], 3, 10));
		assertThrows(ClosedChannelException.class, () -> io.readBytesAt(0, new byte[10], 12, 1));
		assertThrows(ClosedChannelException.class, () -> io.readBytesAt(0, new byte[0]));
		assertThrows(ClosedChannelException.class, () -> io.readBytesAt(-1, new byte[0]));
	}


	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readByteArrayAtWithOffset(String displayName, byte[] expected, int bufferSize, Function<byte[], BytesIO.Readable.Seekable> ioSupplier) throws Exception {
		BytesIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		byte[] buffer = new byte[bufferSize + 127];
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readBytesAt(pos, buffer, 13, bufferSize);
			assertThat(nb).isPositive().isLessThanOrEqualTo(bufferSize);
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer[i + 13]);
			pos += nb;
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readBytesAt(expected.length, buffer, 13, 1));
		assertEquals(-1, io.readBytesAt(expected.length, buffer, 13, bufferSize));
		
		assertThat(io.readBytesAt(0, new byte[0])).isZero();
		assertThat(io.readBytesAt(0, new byte[10], 3, 0)).isZero();
		assertThat(io.readBytesAt(expected.length, new byte[0])).isZero();
		assertThat(io.readBytesAt(expected.length, new byte[10], 3, 0)).isZero();
		
		io.close();
	}
	

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readByteBufferAt(String displayName, byte[] expected, int bufferSize, Function<byte[], BytesIO.Readable.Seekable> ioSupplier) throws Exception {
		BytesIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		assertThrows(NullPointerException.class, () -> io.readBytesAt(0, (ByteBuffer) null));
		
		assertThat(io.readBytesAt(0, ByteBuffer.allocate(0))).isZero();
		assertThat(io.readBytesAt(0, ByteBuffer.wrap(new byte[10], 3, 0))).isZero();

		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readBytesAt(pos, buffer);
			assertThat(nb).isPositive();
			buffer.flip();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer.get());
			pos += nb;
			buffer.position(0);
			buffer.limit(bufferSize);
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readBytesAt(expected.length, ByteBuffer.allocate(1)));
		
		assertThat(io.readBytesAt(0, ByteBuffer.allocate(0))).isZero();
		assertThat(io.readBytesAt(0, ByteBuffer.wrap(new byte[10], 3, 0))).isZero();
		
		assertThrows(NullPointerException.class, () -> io.readBytesAt(0, (ByteBuffer) null));
		assertThrows(NegativeValueException.class, () -> io.readBytesAt(-1, ByteBuffer.allocate(1)));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readBytesAt(0, buffer));
		assertThrows(ClosedChannelException.class, () -> io.readBytesAt(0, (ByteBuffer) null));
		assertThrows(ClosedChannelException.class, () -> io.readBytesAt(-1, ByteBuffer.allocate(1)));
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readBytesFullyAtByteArray(String displayName, byte[] expected, int bufferSize, Function<byte[], BytesIO.Readable.Seekable> ioSupplier) throws Exception {
		BytesIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		byte[] empty = new byte[0];
		io.readBytesFullyAt(0, empty);
		io.readBytesFullyAt(expected.length, empty);
		
		assertThrows(NullPointerException.class, () -> io.readBytesFullyAt(0, null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.readBytesFullyAt(0, new byte[10], -1, 1));
		assertThrows(LimitExceededException.class, () -> io.readBytesFullyAt(0, new byte[10], 10, 1));
		assertThrows(LimitExceededException.class, () -> io.readBytesFullyAt(0, new byte[10], 11, 1));
		assertThrows(LimitExceededException.class, () -> io.readBytesFullyAt(0, new byte[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.readBytesFullyAt(0, new byte[10], 5, 6));
		assertThrows(NegativeValueException.class, () -> io.readBytesFullyAt(0, new byte[10], 0, -1));
		assertThrows(NegativeValueException.class, () -> io.readBytesFullyAt(-1, new byte[1], 0, 1));
		assertThrows(NullPointerException.class, () -> io.readBytesFullyAt(0, (byte[]) null));
		
		byte[] buffer = new byte[bufferSize];
		int pos = 0;
		while (pos < expected.length) {
			if (pos + bufferSize <= expected.length) {
				io.readBytesFullyAt(pos, buffer);
				for (int i = 0; i < buffer.length; ++i)
					assertEquals(expected[pos + i], buffer[i]);
				pos += buffer.length;
			} else {
				io.readBytesFullyAt(pos, buffer, 1, expected.length - pos);
				for (int i = 0; i < expected.length - pos; ++i)
					assertEquals(expected[pos + i], buffer[i + 1]);
				pos = expected.length;
			}
		}
		assertEquals(expected.length, pos);
		
		assertThrows(EOFException.class, () -> io.readBytesFullyAt(expected.length, new byte[1]));
		assertThrows(EOFException.class, () -> io.readBytesFullyAt(expected.length, buffer));
		
		assertThrows(NullPointerException.class, () -> io.readBytesFullyAt(0, null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.readBytesFullyAt(0, new byte[10], -1, 1));
		assertThrows(LimitExceededException.class, () -> io.readBytesFullyAt(0, new byte[10], 10, 1));
		assertThrows(LimitExceededException.class, () -> io.readBytesFullyAt(0, new byte[10], 11, 1));
		assertThrows(LimitExceededException.class, () -> io.readBytesFullyAt(0, new byte[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.readBytesFullyAt(0, new byte[10], 5, 6));
		assertThrows(NegativeValueException.class, () -> io.readBytesFullyAt(0, new byte[10], 0, -1));
		assertThrows(NegativeValueException.class, () -> io.readBytesFullyAt(-1, new byte[1], 0, 1));
		assertThrows(NullPointerException.class, () -> io.readBytesFullyAt(0, (byte[]) null));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readBytesFullyAt(0, buffer));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFullyAt(-1, buffer));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFullyAt(0, null, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFullyAt(0, new byte[10], -1, 1));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFullyAt(0, new byte[10], 10, 1));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFullyAt(0, new byte[10], 11, 1));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFullyAt(0, new byte[10], 0, 11));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFullyAt(0, new byte[10], 5, 6));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFullyAt(0, new byte[10], 0, -1));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFullyAt(0, (byte[]) null));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readBytesFullyAtByteBuffer(String displayName, byte[] expected, int bufferSize, Function<byte[], BytesIO.Readable.Seekable> ioSupplier) throws Exception {
		BytesIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		ByteBuffer empty = ByteBuffer.allocate(0);
		io.readBytesFullyAt(0, empty);
		io.readBytesFullyAt(expected.length, empty);
		
		assertThrows(NullPointerException.class, () -> io.readBytesFullyAt(0, (ByteBuffer) null));
		
		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		int pos = 0;
		while (pos < expected.length) {
			if (pos + bufferSize <= expected.length) {
				buffer.position(0);
				buffer.limit(bufferSize);
				io.readBytesFullyAt(pos, buffer);
				assertThat(buffer.hasRemaining()).isFalse();
				buffer.flip();
				for (int i = 0; i < bufferSize; ++i)
					assertEquals(expected[pos + i], buffer.get());
				pos += bufferSize;
			} else {
				buffer.position(0);
				buffer.limit(expected.length - pos);
				io.readBytesFullyAt(pos, buffer);
				assertThat(buffer.hasRemaining()).isFalse();
				buffer.flip();
				for (int i = 0; i < expected.length - pos; ++i)
					assertEquals(expected[pos + i], buffer.get());
				pos = expected.length;
			}
		}
		assertEquals(expected.length, pos);
		
		buffer.position(0);
		buffer.limit(bufferSize);
		assertThrows(EOFException.class, () -> io.readBytesFullyAt(expected.length, buffer));
		assertThrows(EOFException.class, () -> io.readBytesFullyAt(expected.length, ByteBuffer.allocate(1)));
		assertThrows(EOFException.class, () -> io.readBytesFullyAt(expected.length + 1, ByteBuffer.allocate(1)));
		
		assertThrows(NullPointerException.class, () -> io.readBytesFullyAt(0, (ByteBuffer) null));
		assertThrows(NegativeValueException.class, () -> io.readBytesFullyAt(-1, ByteBuffer.allocate(1)));

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readBytesFullyAt(0, buffer));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFullyAt(0, (ByteBuffer) null));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFullyAt(-1, ByteBuffer.allocate(1)));
	}

}
