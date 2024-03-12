package net.lecousin.commons.io.chars;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.io.chars.CharsIOTestUtils.RandomContentWithBufferSizeTestCasesProvider;
import net.lecousin.commons.io.chars.CharsIOTestUtils.SmallRandomContentTestCasesProvider;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadableSeekableCharsIOTest implements TestCasesProvider<char[], CharsIO.Readable.Seekable> {

	@Nested
	public class AsReadableCharsIO extends AbstractReadableCharsIOTest {
		@Override
		public List<? extends TestCase<char[], CharsIO.Readable>> getTestCases() {
			return AbstractReadableSeekableCharsIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<char[], CharsIO.Readable>) (content) -> tc.getArgumentProvider().apply(content).asReadableCharsIO()))
				.toList();
		}
	}
	
	@Nested
	public class AsSeekableIO extends AbstractSeekableIOTest {
		@Override
		public List<? extends TestCase<Integer, IO.Seekable>> getTestCases() {
			return AbstractReadableSeekableCharsIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, IO.Seekable>) (size) -> tc.getArgumentProvider().apply(CharsIOTestUtils.generateContent(size))))
				.toList();
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(SmallRandomContentTestCasesProvider.class)
	void readCharAt(String displayName, char[] expected, Function<char[], CharsIO.Readable.Seekable> ioSupplier) throws Exception {
		CharsIO.Readable.Seekable io = ioSupplier.apply(expected);
		long initialPos = io.position();
		for (int i = 0; i < expected.length; i++) {
			assertThat(io.readCharAt(i)).as("Read char " + i + "/" + expected.length).isEqualTo(expected[i]);
			assertThat(io.position()).isEqualTo(initialPos);
		}
		assertThrows(EOFException.class, () -> io.readCharAt(expected.length));
		assertThrows(EOFException.class, () -> io.readCharAt(expected.length + 1));
		assertThrows(NegativeValueException.class, () -> io.readCharAt(-1));
		assertThat(io.position()).isEqualTo(initialPos);
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readCharAt(0));
		assertThrows(ClosedChannelException.class, () -> io.readCharAt(expected.length));
		assertThrows(ClosedChannelException.class, () -> io.readCharAt(expected.length + 1));
		assertThrows(ClosedChannelException.class, () -> io.readCharAt(-1));
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readCharArrayAt(String displayName, char[] expected, int bufferSize, Function<char[], CharsIO.Readable.Seekable> ioSupplier) throws Exception {
		CharsIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		long initialPos = io.position();
				
		assertThrows(NullPointerException.class, () -> io.readCharsAt(0, (char[]) null));
		assertThrows(NegativeValueException.class, () -> io.readCharsAt(-1, new char[10], 0, 1));
		assertThrows(NegativeValueException.class, () -> io.readCharsAt(0, new char[10], -1, 5));
		assertThrows(NegativeValueException.class, () -> io.readCharsAt(0, new char[10], 3, -5));
		assertThrows(LimitExceededException.class, () -> io.readCharsAt(0, new char[10], 3, 10));
		assertThrows(LimitExceededException.class, () -> io.readCharsAt(0, new char[10], 12, 1));
		
		assertThat(io.readCharsAt(0, new char[0])).isZero();
		assertThat(io.readCharsAt(0, new char[10], 3, 0)).isZero();
		
		assertThat(io.position()).isEqualTo(initialPos);

		char[] buffer = new char[bufferSize];
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readCharsAt(pos, buffer);
			assertThat(nb).isPositive();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer[i]);
			pos += nb;
			assertThat(io.position()).isEqualTo(initialPos);
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readCharsAt(expected.length, buffer));
		assertEquals(-1, io.readCharsAt(expected.length + 1, buffer));
		assertEquals(-1, io.readCharsAt(expected.length, new char[10], 1, 2));
		
		assertThat(io.readCharsAt(expected.length, new char[0])).isZero();
		assertThat(io.readCharsAt(expected.length, new char[10], 3, 0)).isZero();
		
		assertThrows(NullPointerException.class, () -> io.readCharsAt(0, (char[]) null));
		assertThrows(NegativeValueException.class, () -> io.readCharsAt(-1, new char[10], 0, 1));
		assertThrows(NegativeValueException.class, () -> io.readCharsAt(0, new char[10], -1, 5));
		assertThrows(NegativeValueException.class, () -> io.readCharsAt(0, new char[10], 3, -5));
		assertThrows(LimitExceededException.class, () -> io.readCharsAt(0, new char[10], 3, 10));
		assertThrows(LimitExceededException.class, () -> io.readCharsAt(0, new char[10], 12, 1));
		
		assertThat(io.position()).isEqualTo(initialPos);
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readCharsAt(0, buffer));
		assertThrows(ClosedChannelException.class, () -> io.readCharsAt(0, (char[]) null));
		assertThrows(ClosedChannelException.class, () -> io.readCharsAt(0, new char[10], -1, 5));
		assertThrows(ClosedChannelException.class, () -> io.readCharsAt(0, new char[10], 3, -5));
		assertThrows(ClosedChannelException.class, () -> io.readCharsAt(0, new char[10], 3, 10));
		assertThrows(ClosedChannelException.class, () -> io.readCharsAt(0, new char[10], 12, 1));
		assertThrows(ClosedChannelException.class, () -> io.readCharsAt(0, new char[0]));
		assertThrows(ClosedChannelException.class, () -> io.readCharsAt(-1, new char[0]));
	}


	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readCharArrayAtWithOffset(String displayName, char[] expected, int bufferSize, Function<char[], CharsIO.Readable.Seekable> ioSupplier) throws Exception {
		CharsIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		long initialPos = io.position();
		
		char[] buffer = new char[bufferSize + 127];
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readCharsAt(pos, buffer, 13, bufferSize);
			assertThat(nb).isPositive().isLessThanOrEqualTo(bufferSize);
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer[i + 13]);
			pos += nb;
		}
		assertEquals(expected.length, pos);
		assertThat(io.position()).isEqualTo(initialPos);
		assertEquals(-1, io.readCharsAt(expected.length, buffer, 13, 1));
		assertEquals(-1, io.readCharsAt(expected.length, buffer, 13, bufferSize));
		
		assertThat(io.readCharsAt(0, new char[0])).isZero();
		assertThat(io.readCharsAt(0, new char[10], 3, 0)).isZero();
		assertThat(io.readCharsAt(expected.length, new char[0])).isZero();
		assertThat(io.readCharsAt(expected.length, new char[10], 3, 0)).isZero();
		assertThat(io.position()).isEqualTo(initialPos);
		
		io.close();
	}
	

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readCharBufferAt(String displayName, char[] expected, int bufferSize, Function<char[], CharsIO.Readable.Seekable> ioSupplier) throws Exception {
		CharsIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		long initialPos = io.position();
		
		assertThrows(NullPointerException.class, () -> io.readCharsAt(0, (CharBuffer) null));
		
		assertThat(io.readCharsAt(0, CharBuffer.allocate(0))).isZero();
		assertThat(io.readCharsAt(0, CharBuffer.wrap(new char[10], 3, 0))).isZero();
		assertThat(io.position()).isEqualTo(initialPos);

		CharBuffer buffer = CharBuffer.allocate(bufferSize);
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readCharsAt(pos, buffer);
			assertThat(nb).isPositive();
			buffer.flip();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer.get());
			pos += nb;
			buffer.position(0);
			buffer.limit(bufferSize);
		}
		assertEquals(expected.length, pos);
		assertThat(io.position()).isEqualTo(initialPos);
		assertEquals(-1, io.readCharsAt(expected.length, CharBuffer.allocate(1)));
		
		assertThat(io.readCharsAt(0, CharBuffer.allocate(0))).isZero();
		assertThat(io.readCharsAt(0, CharBuffer.wrap(new char[10], 3, 0))).isZero();
		
		assertThrows(NullPointerException.class, () -> io.readCharsAt(0, (CharBuffer) null));
		assertThrows(NegativeValueException.class, () -> io.readCharsAt(-1, CharBuffer.allocate(1)));
		assertThat(io.position()).isEqualTo(initialPos);
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readCharsAt(0, buffer));
		assertThrows(ClosedChannelException.class, () -> io.readCharsAt(0, (CharBuffer) null));
		assertThrows(ClosedChannelException.class, () -> io.readCharsAt(-1, CharBuffer.allocate(1)));
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readCharBufferDirectAt(String displayName, char[] expected, int bufferSize, Function<char[], CharsIO.Readable.Seekable> ioSupplier) throws Exception {
		CharsIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		long initialPos = io.position();
		
		assertThat(io.readCharsAt(0, ByteBuffer.allocateDirect(0).asCharBuffer())).isZero();
		assertThat(io.position()).isEqualTo(initialPos);

		CharBuffer buffer = ByteBuffer.allocateDirect(bufferSize * 2).asCharBuffer();
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readCharsAt(pos, buffer);
			assertThat(nb).isPositive();
			buffer.flip();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer.get());
			pos += nb;
			buffer.position(0);
			buffer.limit(bufferSize);
		}
		assertEquals(expected.length, pos);
		assertThat(io.position()).isEqualTo(initialPos);
		assertEquals(-1, io.readCharsAt(expected.length, ByteBuffer.allocateDirect(2).asCharBuffer()));
		
		assertThat(io.readCharsAt(0, ByteBuffer.allocateDirect(0).asCharBuffer())).isZero();
		
		assertThrows(NegativeValueException.class, () -> io.readCharsAt(-1, ByteBuffer.allocateDirect(2).asCharBuffer()));
		assertThat(io.position()).isEqualTo(initialPos);
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readCharsAt(0, buffer));
		assertThrows(ClosedChannelException.class, () -> io.readCharsAt(-1, ByteBuffer.allocateDirect(2).asCharBuffer()));
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readCharsFullyAtCharArray(String displayName, char[] expected, int bufferSize, Function<char[], CharsIO.Readable.Seekable> ioSupplier) throws Exception {
		CharsIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		long initialPos = io.position();
		
		char[] empty = new char[0];
		io.readCharsFullyAt(0, empty);
		io.readCharsFullyAt(expected.length, empty);
		
		assertThrows(NullPointerException.class, () -> io.readCharsFullyAt(0, (char[]) null));
		assertThrows(NullPointerException.class, () -> io.readCharsFullyAt(0, null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.readCharsFullyAt(0, new char[10], -1, 1));
		assertThrows(LimitExceededException.class, () -> io.readCharsFullyAt(0, new char[10], 10, 1));
		assertThrows(LimitExceededException.class, () -> io.readCharsFullyAt(0, new char[10], 11, 1));
		assertThrows(LimitExceededException.class, () -> io.readCharsFullyAt(0, new char[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.readCharsFullyAt(0, new char[10], 5, 6));
		assertThrows(NegativeValueException.class, () -> io.readCharsFullyAt(0, new char[10], 0, -1));
		assertThrows(NegativeValueException.class, () -> io.readCharsFullyAt(-1, new char[1], 0, 1));
		assertThrows(NegativeValueException.class, () -> io.readCharsFullyAt(-1, new char[1]));
		assertThat(io.position()).isEqualTo(initialPos);
		
		char[] buffer = new char[bufferSize];
		int pos = 0;
		while (pos < expected.length) {
			if (pos + bufferSize <= expected.length) {
				io.readCharsFullyAt(pos, buffer);
				for (int i = 0; i < buffer.length; ++i)
					assertEquals(expected[pos + i], buffer[i]);
				pos += buffer.length;
			} else {
				io.readCharsFullyAt(pos, buffer, 1, expected.length - pos);
				for (int i = 0; i < expected.length - pos; ++i)
					assertEquals(expected[pos + i], buffer[i + 1]);
				pos = expected.length;
			}
		}
		assertEquals(expected.length, pos);
		assertThat(io.position()).isEqualTo(initialPos);
		
		assertThrows(EOFException.class, () -> io.readCharsFullyAt(expected.length, new char[1]));
		assertThrows(EOFException.class, () -> io.readCharsFullyAt(expected.length, buffer));
		
		assertThrows(NullPointerException.class, () -> io.readCharsFullyAt(0, null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.readCharsFullyAt(0, new char[10], -1, 1));
		assertThrows(LimitExceededException.class, () -> io.readCharsFullyAt(0, new char[10], 10, 1));
		assertThrows(LimitExceededException.class, () -> io.readCharsFullyAt(0, new char[10], 11, 1));
		assertThrows(LimitExceededException.class, () -> io.readCharsFullyAt(0, new char[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.readCharsFullyAt(0, new char[10], 5, 6));
		assertThrows(NegativeValueException.class, () -> io.readCharsFullyAt(0, new char[10], 0, -1));
		assertThrows(NegativeValueException.class, () -> io.readCharsFullyAt(-1, new char[1], 0, 1));
		assertThrows(NullPointerException.class, () -> io.readCharsFullyAt(0, (char[]) null));
		assertThat(io.position()).isEqualTo(initialPos);
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readCharsFullyAt(0, buffer));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFullyAt(-1, buffer));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFullyAt(0, null, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFullyAt(0, new char[10], -1, 1));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFullyAt(0, new char[10], 10, 1));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFullyAt(0, new char[10], 11, 1));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFullyAt(0, new char[10], 0, 11));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFullyAt(0, new char[10], 5, 6));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFullyAt(0, new char[10], 0, -1));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFullyAt(0, (char[]) null));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readCharsFullyAtCharBuffer(String displayName, char[] expected, int bufferSize, Function<char[], CharsIO.Readable.Seekable> ioSupplier) throws Exception {
		CharsIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		long initialPos = io.position();
		
		CharBuffer empty = CharBuffer.allocate(0);
		io.readCharsFullyAt(0, empty);
		io.readCharsFullyAt(expected.length, empty);
		
		assertThrows(NullPointerException.class, () -> io.readCharsFullyAt(0, (CharBuffer) null));
		assertThat(io.position()).isEqualTo(initialPos);
		
		CharBuffer buffer = CharBuffer.allocate(bufferSize);
		int pos = 0;
		while (pos < expected.length) {
			if (pos + bufferSize <= expected.length) {
				buffer.position(0);
				buffer.limit(bufferSize);
				io.readCharsFullyAt(pos, buffer);
				assertThat(buffer.hasRemaining()).isFalse();
				buffer.flip();
				for (int i = 0; i < bufferSize; ++i)
					assertEquals(expected[pos + i], buffer.get());
				pos += bufferSize;
			} else {
				buffer.position(0);
				buffer.limit(expected.length - pos);
				io.readCharsFullyAt(pos, buffer);
				assertThat(buffer.hasRemaining()).isFalse();
				buffer.flip();
				for (int i = 0; i < expected.length - pos; ++i)
					assertEquals(expected[pos + i], buffer.get());
				pos = expected.length;
			}
		}
		assertEquals(expected.length, pos);
		assertThat(io.position()).isEqualTo(initialPos);
		
		buffer.position(0);
		buffer.limit(bufferSize);
		assertThrows(EOFException.class, () -> io.readCharsFullyAt(expected.length, buffer));
		assertThrows(EOFException.class, () -> io.readCharsFullyAt(expected.length, CharBuffer.allocate(1)));
		assertThrows(EOFException.class, () -> io.readCharsFullyAt(expected.length + 1, CharBuffer.allocate(1)));
		
		assertThrows(NullPointerException.class, () -> io.readCharsFullyAt(0, (CharBuffer) null));
		assertThrows(NegativeValueException.class, () -> io.readCharsFullyAt(-1, CharBuffer.allocate(1)));
		assertThat(io.position()).isEqualTo(initialPos);

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.readCharsFullyAt(0, buffer));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFullyAt(0, (CharBuffer) null));
		assertThrows(ClosedChannelException.class, () -> io.readCharsFullyAt(-1, CharBuffer.allocate(1)));
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void seekAndReadFullyCharArray(String displayName, char[] expected, int bufferSize, Function<char[], CharsIO.Readable.Seekable> ioSupplier) throws Exception {
		CharsIO.Readable.Seekable io = ioSupplier.apply(expected);

		char[] buffer = new char[bufferSize];
		int step = 1;
		if (expected.length > 10000)
			step = 7899;
		else if (expected.length > 1000)
			step = 111;
		
		// SeekFrom.START
		
		// read forward
		for (int i = 0; i < expected.length - bufferSize; i += step) {
			assertThat(io.seek(SeekFrom.START, i)).isEqualTo(i);
			assertThat(io.position()).isEqualTo(i);
			io.readCharsFully(buffer);
			for (int j = 0; j < bufferSize; j++)
				assertEquals(expected[i + j], buffer[j]);
		}
		// read backward
		for (int i = expected.length - bufferSize; i >= 0; i -= step) {
			assertThat(io.seek(SeekFrom.START, i)).isEqualTo(i);
			assertThat(io.position()).isEqualTo(i);
			io.readCharsFully(buffer);
			for (int j = 0; j < bufferSize; j++)
				assertEquals(expected[i + j], buffer[j]);
		}
		
		// SeekFrom.END
		
		// read forward
		for (int i = 0; i < expected.length - bufferSize; i += step) {
			assertThat(io.seek(SeekFrom.END, expected.length - i)).isEqualTo(i);
			assertThat(io.position()).isEqualTo(i);
			io.readCharsFully(buffer);
			for (int j = 0; j < bufferSize; j++)
				assertEquals(expected[i + j], buffer[j]);
		}
		// read backward
		for (int i = expected.length - bufferSize; i >= 0; i -= step) {
			assertThat(io.seek(SeekFrom.END, expected.length - i)).isEqualTo(i);
			assertThat(io.position()).isEqualTo(i);
			io.readCharsFully(buffer);
			for (int j = 0; j < bufferSize; j++)
				assertEquals(expected[i + j], buffer[j]);
		}
		
		// SeekFrom.CURRENT

		assertThat(io.seek(SeekFrom.CURRENT, -io.position())).isZero();
		for (int i = 0; i < expected.length - bufferSize; i += step) {
			assertThat(io.position()).isEqualTo(i);
			io.readCharsFully(buffer);
			for (int j = 0; j < bufferSize; j++)
				assertEquals(expected[i + j], buffer[j]);
			assertThat(io.position()).isEqualTo(i + bufferSize);
			if (i + bufferSize + step <= expected.length) {
				assertThat(io.seek(SeekFrom.CURRENT, -bufferSize + step)).isEqualTo(i + step);
				assertThat(io.position()).isEqualTo(i + step);
			}
		}
		
		io.close();
	}
	
}
