package net.lecousin.commons.io.bytes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractKnownSizeIOTest;
import net.lecousin.commons.io.AbstractReadableIOTest;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.RandomContentTestCasesProvider;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.RandomContentWithBufferSizeTestCasesProvider;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.SmallRandomContentTestCasesProvider;
import net.lecousin.commons.io.bytes.memory.ByteArray;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadableBytesIOTest implements TestCasesProvider<byte[], BytesIO.Readable> {

	@Nested
	public class AsReadableIO extends AbstractReadableIOTest {
		@Override
		public List<? extends TestCase<Void, IO.Readable>> getTestCases() {
			return AbstractReadableBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), (Supplier<IO.Readable>) () -> tc.getArgumentProvider().apply(new byte[1])))
				.toList();
		}
	}
	
	@Nested
	public class AsKnownSizeIO extends AbstractKnownSizeIOTest {
		@Override
		public List<? extends TestCase<Integer, IO.KnownSize>> getTestCases() {
			return AbstractReadableBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(),
					(Function<Integer, IO.KnownSize>) (size) -> {
						BytesIO.Readable io = tc.getArgumentProvider().apply(new byte[size]);
						assumeThat(io).isInstanceOf(IO.KnownSize.class);
						return (IO.KnownSize) io;
					}
				))
				.toList();
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void readBuffer(String displayName, byte[] expected, Function<byte[], BytesIO.Readable> ioSupplier) throws Exception {
		BytesIO.Readable io = ioSupplier.apply(expected);
		int pos = 0;
		while (pos < expected.length) {
			Optional<ByteBuffer> ob = io.readBuffer();
			assertThat(ob).isPresent();
			ByteBuffer b = ob.get();
			assertThat(b.remaining()).isPositive();
			while (b.hasRemaining())
				assertThat(b.get()).isEqualTo(expected[pos++]);
		}
		assertThat(io.readBuffer()).isEmpty();

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readBuffer());
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(SmallRandomContentTestCasesProvider.class)
	void readByte(String displayName, byte[] expected, Function<byte[], BytesIO.Readable> ioSupplier) throws Exception {
		BytesIO.Readable io = ioSupplier.apply(expected);
		for (int i = 0; i < expected.length; i++) {
			assertThat(io.readByte()).as("Read byte " + i + "/" + expected.length).isEqualTo(expected[i]);
		}
		assertThrows(EOFException.class, () -> io.readByte());
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readByte());
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readByteArray(String displayName, byte[] expected, int bufferSize, Function<byte[], BytesIO.Readable> ioSupplier) throws Exception {
		BytesIO.Readable io = ioSupplier.apply(expected);
		
		assertThrows(NullPointerException.class, () -> io.readBytes((byte[]) null));
		assertThrows(NegativeValueException.class, () -> io.readBytes(new byte[10], -1, 5));
		assertThrows(NegativeValueException.class, () -> io.readBytes(new byte[10], 3, -5));
		assertThrows(LimitExceededException.class, () -> io.readBytes(new byte[10], 3, 10));
		assertThrows(LimitExceededException.class, () -> io.readBytes(new byte[10], 12, 1));
		
		assertThat(io.readBytes(new byte[0])).isZero();
		assertThat(io.readBytes(new byte[10], 3, 0)).isZero();

		byte[] buffer = new byte[bufferSize];
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readBytes(buffer);
			assertThat(nb).isPositive();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer[i]);
			pos += nb;
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readBytes(buffer));
		assertEquals(-1, io.readBytes(buffer));
		assertEquals(-1, io.readBytes(new byte[10], 1, 2));
		
		assertThat(io.readBytes(new byte[0])).isZero();
		assertThat(io.readBytes(new byte[10], 3, 0)).isZero();
		
		assertThrows(NullPointerException.class, () -> io.readBytes((byte[]) null));
		assertThrows(NegativeValueException.class, () -> io.readBytes(new byte[10], -1, 5));
		assertThrows(NegativeValueException.class, () -> io.readBytes(new byte[10], 3, -5));
		assertThrows(LimitExceededException.class, () -> io.readBytes(new byte[10], 3, 10));
		assertThrows(LimitExceededException.class, () -> io.readBytes(new byte[10], 12, 1));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readBytes(buffer));
		assertThrows(ClosedChannelException.class, () -> io.readBytes((byte[]) null));
		assertThrows(ClosedChannelException.class, () -> io.readBytes(new byte[10], -1, 5));
		assertThrows(ClosedChannelException.class, () -> io.readBytes(new byte[10], 3, -5));
		assertThrows(ClosedChannelException.class, () -> io.readBytes(new byte[10], 3, 10));
		assertThrows(ClosedChannelException.class, () -> io.readBytes(new byte[10], 12, 1));
		assertThrows(ClosedChannelException.class, () -> io.readBytes(new byte[0]));
	}


	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readByteArrayWithOffset(String displayName, byte[] expected, int bufferSize, Function<byte[], BytesIO.Readable> ioSupplier) throws Exception {
		BytesIO.Readable io = ioSupplier.apply(expected);
		
		byte[] buffer = new byte[bufferSize + 127];
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readBytes(buffer, 13, bufferSize);
			assertThat(nb).isPositive().isLessThanOrEqualTo(bufferSize);
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer[i + 13]);
			pos += nb;
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readBytes(buffer, 13, 1));
		assertEquals(-1, io.readBytes(buffer, 13, bufferSize));
		
		assertThat(io.readBytes(new byte[0])).isZero();
		assertThat(io.readBytes(new byte[10], 3, 0)).isZero();
		
		io.close();
	}
	

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readByteBuffer(String displayName, byte[] expected, int bufferSize, Function<byte[], BytesIO.Readable> ioSupplier) throws Exception {
		BytesIO.Readable io = ioSupplier.apply(expected);
		
		assertThrows(NullPointerException.class, () -> io.readBytes((ByteBuffer) null));
		
		assertThat(io.readBytes(ByteBuffer.allocate(0))).isZero();
		assertThat(io.readBytes(ByteBuffer.wrap(new byte[10], 3, 0))).isZero();

		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readBytes(buffer);
			assertThat(nb).isPositive();
			buffer.flip();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer.get());
			pos += nb;
			buffer.position(0);
			buffer.limit(bufferSize);
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readBytes(ByteBuffer.allocate(1)));
		
		assertThat(io.readBytes(ByteBuffer.allocate(0))).isZero();
		assertThat(io.readBytes(ByteBuffer.wrap(new byte[10], 3, 0))).isZero();
		
		assertThrows(NullPointerException.class, () -> io.readBytes((ByteBuffer) null));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readBytes(buffer));
		assertThrows(ClosedChannelException.class, () -> io.readBytes((ByteBuffer) null));
	}


	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readByteBufferDirect(String displayName, byte[] expected, int bufferSize, Function<byte[], BytesIO.Readable> ioSupplier) throws Exception {
		BytesIO.Readable io = ioSupplier.apply(expected);
		
		assertThrows(NullPointerException.class, () -> io.readBytes((ByteBuffer) null));
		
		assertThat(io.readBytes(ByteBuffer.allocateDirect(0))).isZero();

		ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readBytes(buffer);
			assertThat(nb).isPositive();
			buffer.flip();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer.get());
			pos += nb;
			buffer.position(0);
			buffer.limit(bufferSize);
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readBytes(ByteBuffer.allocateDirect(1)));
		
		assertThat(io.readBytes(ByteBuffer.allocateDirect(0))).isZero();
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readBytes(buffer));
	}
	
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readBytesFullyByteArray(String displayName, byte[] expected, int bufferSize, Function<byte[], BytesIO.Readable> ioSupplier) throws Exception {
		BytesIO.Readable io = ioSupplier.apply(expected);
		
		byte[] empty = new byte[0];
		io.readBytesFully(empty);
		
		assertThrows(NullPointerException.class, () -> io.readBytesFully(null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.readBytesFully(new byte[10], -1, 1));
		assertThrows(LimitExceededException.class, () -> io.readBytesFully(new byte[10], 10, 1));
		assertThrows(LimitExceededException.class, () -> io.readBytesFully(new byte[10], 11, 1));
		assertThrows(LimitExceededException.class, () -> io.readBytesFully(new byte[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.readBytesFully(new byte[10], 5, 6));
		assertThrows(NegativeValueException.class, () -> io.readBytesFully(new byte[10], 0, -1));
		assertThrows(NullPointerException.class, () -> io.readBytesFully((byte[]) null));
		
		byte[] buffer = new byte[bufferSize];
		int pos = 0;
		while (pos < expected.length) {
			if (pos + bufferSize <= expected.length) {
				io.readBytesFully(buffer);
				for (int i = 0; i < buffer.length; ++i)
					assertEquals(expected[pos + i], buffer[i]);
				pos += buffer.length;
			} else {
				io.readBytesFully(buffer, 1, expected.length - pos);
				for (int i = 0; i < expected.length - pos; ++i)
					assertEquals(expected[pos + i], buffer[i + 1]);
				pos = expected.length;
			}
		}
		assertEquals(expected.length, pos);
		
		assertThrows(EOFException.class, () -> io.readBytesFully(new byte[1]));
		assertThrows(EOFException.class, () -> io.readBytesFully(buffer));
		
		assertThrows(NullPointerException.class, () -> io.readBytesFully(null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.readBytesFully(new byte[10], -1, 1));
		assertThrows(LimitExceededException.class, () -> io.readBytesFully(new byte[10], 10, 1));
		assertThrows(LimitExceededException.class, () -> io.readBytesFully(new byte[10], 11, 1));
		assertThrows(LimitExceededException.class, () -> io.readBytesFully(new byte[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.readBytesFully(new byte[10], 5, 6));
		assertThrows(NegativeValueException.class, () -> io.readBytesFully(new byte[10], 0, -1));
		assertThrows(NullPointerException.class, () -> io.readBytesFully((byte[]) null));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readBytesFully(buffer));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFully(null, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFully(new byte[10], -1, 1));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFully(new byte[10], 10, 1));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFully(new byte[10], 11, 1));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFully(new byte[10], 0, 11));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFully(new byte[10], 5, 6));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFully(new byte[10], 0, -1));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFully((byte[]) null));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readBytesFullyByteBuffer(String displayName, byte[] expected, int bufferSize, Function<byte[], BytesIO.Readable> ioSupplier) throws Exception {
		BytesIO.Readable io = ioSupplier.apply(expected);
		
		ByteBuffer empty = ByteBuffer.allocate(0);
		io.readBytesFully(empty);
		
		assertThrows(NullPointerException.class, () -> io.readBytesFully((ByteBuffer) null));
		
		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		int pos = 0;
		while (pos < expected.length) {
			if (pos + bufferSize <= expected.length) {
				buffer.position(0);
				buffer.limit(bufferSize);
				io.readBytesFully(buffer);
				assertThat(buffer.hasRemaining()).isFalse();
				buffer.flip();
				for (int i = 0; i < bufferSize; ++i)
					assertEquals(expected[pos + i], buffer.get());
				pos += bufferSize;
			} else {
				buffer.position(0);
				buffer.limit(expected.length - pos);
				io.readBytesFully(buffer);
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
		assertThrows(EOFException.class, () -> io.readBytesFully(buffer));
		assertThrows(EOFException.class, () -> io.readBytesFully(ByteBuffer.allocate(1)));
		
		assertThrows(NullPointerException.class, () -> io.readBytesFully((ByteBuffer) null));

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readBytesFully(buffer));
		assertThrows(ClosedChannelException.class, () -> io.readBytesFully((ByteBuffer) null));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void skipUpTo(String displayName, byte[] expected, int skipSize, Function<byte[], BytesIO.Readable> ioSupplier) throws Exception {
		BytesIO.Readable io = ioSupplier.apply(expected);
		
		assertThat(io.skipUpTo(0)).isZero();
		assertThrows(NegativeValueException.class, () -> io.skipUpTo(-1));
		
		int pos = 0;
		do {
			long skipped = io.skipUpTo(skipSize);
			if (pos == expected.length) {
				assertEquals(-1L, skipped);
				break;
			} else
				assertThat(skipped).isPositive();
			pos += skipped;
			assertThat(pos).isLessThanOrEqualTo(expected.length);
		} while (true);

		assertThat(io.skipUpTo(0)).isZero();
		assertThat(io.skipUpTo(1)).isEqualTo(-1L);
		assertThrows(NegativeValueException.class, () -> io.skipUpTo(-1));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.skipUpTo(0));
		assertThrows(ClosedChannelException.class, () -> io.skipUpTo(1));
		assertThrows(ClosedChannelException.class, () -> io.skipUpTo(-1));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void skipFully(String displayName, byte[] expected, int skipSize, Function<byte[], BytesIO.Readable> ioSupplier) throws Exception {
		BytesIO.Readable io = ioSupplier.apply(expected);
		
		io.skipFully(0);
		assertThrows(NegativeValueException.class, () -> io.skipFully(-1));
		
		int pos = 0;
		do {
			if (pos + skipSize > expected.length) {
				assertThrows(EOFException.class, () -> io.skipFully(skipSize));
				int remaining = expected.length - pos;
				// make sure we are at the end
				io.skipUpTo(remaining);
				break;
			} else
				io.skipFully(skipSize);
			pos += skipSize;
		} while (true);

		io.skipFully(0);
		assertThrows(EOFException.class, () -> io.skipFully(1));
		assertThrows(NegativeValueException.class, () -> io.skipFully(-1));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.skipFully(0));
		assertThrows(ClosedChannelException.class, () -> io.skipFully(1));
		assertThrows(ClosedChannelException.class, () -> io.skipFully(-1));
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void transferFully(String displayName, byte[] expected, Function<byte[], BytesIO.Readable> ioSupplier) throws Exception {
		BytesIO.Readable io = ioSupplier.apply(expected);
		ByteArray ba = new ByteArray(new byte[expected.length]);
		io.transferFully(ba.asBytesIO());
		assertThat(io.readBuffer()).isEmpty();
		assertThat(ba.getArray()).containsExactly(expected);
		io.close();
	}

	
}
