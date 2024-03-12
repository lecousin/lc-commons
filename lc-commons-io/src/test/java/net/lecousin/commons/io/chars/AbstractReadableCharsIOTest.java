package net.lecousin.commons.io.chars;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractKnownSizeIOTest;
import net.lecousin.commons.io.AbstractReadableIOTest;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.chars.CharsIOTestUtils.RandomContentTestCasesProvider;
import net.lecousin.commons.io.chars.CharsIOTestUtils.RandomContentWithBufferSizeTestCasesProvider;
import net.lecousin.commons.io.chars.CharsIOTestUtils.SmallRandomContentTestCasesProvider;
import net.lecousin.commons.io.chars.memory.CharArray;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadableCharsIOTest implements TestCasesProvider<char[], CharsIO.Readable> {

	@Nested
	public class AsReadableIO extends AbstractReadableIOTest {
		@Override
		public List<? extends TestCase<Void, IO.Readable>> getTestCases() {
			return AbstractReadableCharsIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), (Supplier<IO.Readable>) () -> tc.getArgumentProvider().apply(new char[1])))
				.toList();
		}
	}
	
	@Nested
	public class AsKnownSizeIO extends AbstractKnownSizeIOTest {
		@Override
		public List<? extends TestCase<Integer, IO.KnownSize>> getTestCases() {
			return AbstractReadableCharsIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(),
					(Function<Integer, IO.KnownSize>) (size) -> {
						CharsIO.Readable io = tc.getArgumentProvider().apply(new char[size]);
						assumeThat(io).isInstanceOf(IO.KnownSize.class);
						return (IO.KnownSize) io;
					}
				))
				.toList();
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void readBuffer(String displayName, char[] expected, Function<char[], CharsIO.Readable> ioSupplier) throws Exception {
		CharsIO.Readable io = ioSupplier.apply(expected);
		int pos = 0;
		while (pos < expected.length) {
			Optional<CharBuffer> ob = io.readBuffer();
			assertThat(ob).isPresent();
			CharBuffer b = ob.get();
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
	void readChar(String displayName, char[] expected, Function<char[], CharsIO.Readable> ioSupplier) throws Exception {
		CharsIO.Readable io = ioSupplier.apply(expected);
		for (int i = 0; i < expected.length; i++) {
			assertThat(io.readChar()).as("Read char " + i + "/" + expected.length).isEqualTo(expected[i]);
		}
		assertThrows(EOFException.class, () -> io.readChar());
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readChar());
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readCharArray(String displayName, char[] expected, int bufferSize, Function<char[], CharsIO.Readable> ioSupplier) throws Exception {
		CharsIO.Readable io = ioSupplier.apply(expected);
		
		assertThrows(NullPointerException.class, () -> io.readChars((char[]) null));
		assertThrows(NegativeValueException.class, () -> io.readChars(new char[10], -1, 5));
		assertThrows(NegativeValueException.class, () -> io.readChars(new char[10], 3, -5));
		assertThrows(LimitExceededException.class, () -> io.readChars(new char[10], 3, 10));
		assertThrows(LimitExceededException.class, () -> io.readChars(new char[10], 12, 1));
		
		assertThat(io.readChars(new char[0])).isZero();
		assertThat(io.readChars(new char[10], 3, 0)).isZero();

		char[] buffer = new char[bufferSize];
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readChars(buffer);
			assertThat(nb).isPositive();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer[i]);
			pos += nb;
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readChars(buffer));
		assertEquals(-1, io.readChars(buffer));
		assertEquals(-1, io.readChars(new char[10], 1, 2));
		
		assertThat(io.readChars(new char[0])).isZero();
		assertThat(io.readChars(new char[10], 3, 0)).isZero();
		
		assertThrows(NullPointerException.class, () -> io.readChars((char[]) null));
		assertThrows(NegativeValueException.class, () -> io.readChars(new char[10], -1, 5));
		assertThrows(NegativeValueException.class, () -> io.readChars(new char[10], 3, -5));
		assertThrows(LimitExceededException.class, () -> io.readChars(new char[10], 3, 10));
		assertThrows(LimitExceededException.class, () -> io.readChars(new char[10], 12, 1));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readChars(buffer));
		assertThrows(ClosedChannelException.class, () -> io.readChars((char[]) null));
		assertThrows(ClosedChannelException.class, () -> io.readChars(new char[10], -1, 5));
		assertThrows(ClosedChannelException.class, () -> io.readChars(new char[10], 3, -5));
		assertThrows(ClosedChannelException.class, () -> io.readChars(new char[10], 3, 10));
		assertThrows(ClosedChannelException.class, () -> io.readChars(new char[10], 12, 1));
		assertThrows(ClosedChannelException.class, () -> io.readChars(new char[0]));
	}


	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readCharArrayWithOffset(String displayName, char[] expected, int bufferSize, Function<char[], CharsIO.Readable> ioSupplier) throws Exception {
		CharsIO.Readable io = ioSupplier.apply(expected);
		
		char[] buffer = new char[bufferSize + 127];
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readChars(buffer, 13, bufferSize);
			assertThat(nb).isPositive().isLessThanOrEqualTo(bufferSize);
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer[i + 13]);
			pos += nb;
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readChars(buffer, 13, 1));
		assertEquals(-1, io.readChars(buffer, 13, bufferSize));
		
		assertThat(io.readChars(new char[0])).isZero();
		assertThat(io.readChars(new char[10], 3, 0)).isZero();
		
		io.close();
	}
	

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readCharBuffer(String displayName, char[] expected, int bufferSize, Function<char[], CharsIO.Readable> ioSupplier) throws Exception {
		CharsIO.Readable io = ioSupplier.apply(expected);
		
		assertThrows(NullPointerException.class, () -> io.readChars((CharBuffer) null));
		
		assertThat(io.readChars(CharBuffer.allocate(0))).isZero();
		assertThat(io.readChars(CharBuffer.wrap(new char[10], 3, 0))).isZero();

		CharBuffer buffer = CharBuffer.allocate(bufferSize);
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readChars(buffer);
			assertThat(nb).isPositive();
			buffer.flip();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer.get());
			pos += nb;
			buffer.position(0);
			buffer.limit(bufferSize);
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readChars(CharBuffer.allocate(1)));
		
		assertThat(io.readChars(CharBuffer.allocate(0))).isZero();
		assertThat(io.readChars(CharBuffer.wrap(new char[10], 3, 0))).isZero();
		
		assertThrows(NullPointerException.class, () -> io.readChars((CharBuffer) null));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readChars(buffer));
		assertThrows(ClosedChannelException.class, () -> io.readChars((CharBuffer) null));
	}


	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readCharBufferDirect(String displayName, char[] expected, int bufferSize, Function<char[], CharsIO.Readable> ioSupplier) throws Exception {
		CharsIO.Readable io = ioSupplier.apply(expected);
		
		assertThrows(NullPointerException.class, () -> io.readChars((CharBuffer) null));
		
		assertThat(io.readChars(ByteBuffer.allocateDirect(0).asCharBuffer())).isZero();

		CharBuffer buffer = ByteBuffer.allocateDirect(bufferSize * 2).asCharBuffer();
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readChars(buffer);
			assertThat(nb).isPositive();
			buffer.flip();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer.get());
			pos += nb;
			buffer.position(0);
			buffer.limit(bufferSize);
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readChars(ByteBuffer.allocateDirect(2).asCharBuffer()));
		
		assertThat(io.readChars(ByteBuffer.allocateDirect(0).asCharBuffer())).isZero();
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readChars(buffer));
	}
	
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readCharsFullyCharArray(String displayName, char[] expected, int bufferSize, Function<char[], CharsIO.Readable> ioSupplier) throws Exception {
		CharsIO.Readable io = ioSupplier.apply(expected);
		
		char[] empty = new char[0];
		io.readCharsFully(empty);
		
		assertThrows(NullPointerException.class, () -> io.readCharsFully(null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.readCharsFully(new char[10], -1, 1));
		assertThrows(LimitExceededException.class, () -> io.readCharsFully(new char[10], 10, 1));
		assertThrows(LimitExceededException.class, () -> io.readCharsFully(new char[10], 11, 1));
		assertThrows(LimitExceededException.class, () -> io.readCharsFully(new char[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.readCharsFully(new char[10], 5, 6));
		assertThrows(NegativeValueException.class, () -> io.readCharsFully(new char[10], 0, -1));
		assertThrows(NullPointerException.class, () -> io.readCharsFully((char[]) null));
		
		char[] buffer = new char[bufferSize];
		int pos = 0;
		while (pos < expected.length) {
			if (pos + bufferSize <= expected.length) {
				io.readCharsFully(buffer);
				for (int i = 0; i < buffer.length; ++i)
					assertEquals(expected[pos + i], buffer[i]);
				pos += buffer.length;
			} else {
				io.readCharsFully(buffer, 1, expected.length - pos);
				for (int i = 0; i < expected.length - pos; ++i)
					assertEquals(expected[pos + i], buffer[i + 1]);
				pos = expected.length;
			}
		}
		assertEquals(expected.length, pos);
		
		assertThrows(EOFException.class, () -> io.readCharsFully(new char[1]));
		assertThrows(EOFException.class, () -> io.readCharsFully(buffer));
		
		assertThrows(NullPointerException.class, () -> io.readCharsFully(null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.readCharsFully(new char[10], -1, 1));
		assertThrows(LimitExceededException.class, () -> io.readCharsFully(new char[10], 10, 1));
		assertThrows(LimitExceededException.class, () -> io.readCharsFully(new char[10], 11, 1));
		assertThrows(LimitExceededException.class, () -> io.readCharsFully(new char[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.readCharsFully(new char[10], 5, 6));
		assertThrows(NegativeValueException.class, () -> io.readCharsFully(new char[10], 0, -1));
		assertThrows(NullPointerException.class, () -> io.readCharsFully((char[]) null));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readCharsFully(buffer));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFully(null, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFully(new char[10], -1, 1));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFully(new char[10], 10, 1));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFully(new char[10], 11, 1));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFully(new char[10], 0, 11));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFully(new char[10], 5, 6));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFully(new char[10], 0, -1));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFully((char[]) null));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readCharsFullyCharBuffer(String displayName, char[] expected, int bufferSize, Function<char[], CharsIO.Readable> ioSupplier) throws Exception {
		CharsIO.Readable io = ioSupplier.apply(expected);
		
		CharBuffer empty = CharBuffer.allocate(0);
		io.readCharsFully(empty);
		
		assertThrows(NullPointerException.class, () -> io.readCharsFully((CharBuffer) null));
		
		CharBuffer buffer = CharBuffer.allocate(bufferSize);
		int pos = 0;
		while (pos < expected.length) {
			if (pos + bufferSize <= expected.length) {
				buffer.position(0);
				buffer.limit(bufferSize);
				io.readCharsFully(buffer);
				assertThat(buffer.hasRemaining()).isFalse();
				buffer.flip();
				for (int i = 0; i < bufferSize; ++i)
					assertEquals(expected[pos + i], buffer.get());
				pos += bufferSize;
			} else {
				buffer.position(0);
				buffer.limit(expected.length - pos);
				io.readCharsFully(buffer);
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
		assertThrows(EOFException.class, () -> io.readCharsFully(buffer));
		assertThrows(EOFException.class, () -> io.readCharsFully(CharBuffer.allocate(1)));
		
		assertThrows(NullPointerException.class, () -> io.readCharsFully((CharBuffer) null));

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readCharsFully(buffer));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFully((CharBuffer) null));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void skipUpTo(String displayName, char[] expected, int skipSize, Function<char[], CharsIO.Readable> ioSupplier) throws Exception {
		CharsIO.Readable io = ioSupplier.apply(expected);
		
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
	void skipFully(String displayName, char[] expected, int skipSize, Function<char[], CharsIO.Readable> ioSupplier) throws Exception {
		CharsIO.Readable io = ioSupplier.apply(expected);
		
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
	void transferFully(String displayName, char[] expected, Function<char[], CharsIO.Readable> ioSupplier) throws Exception {
		CharsIO.Readable io = ioSupplier.apply(expected);
		CharArray ba = new CharArray(new char[expected.length]);
		io.transferFully(ba.asCharsIO());
		assertThat(io.readBuffer()).isEmpty();
		Assertions.assertArrayEquals(expected, ba.getArray());
		io.close();
	}

	
}
