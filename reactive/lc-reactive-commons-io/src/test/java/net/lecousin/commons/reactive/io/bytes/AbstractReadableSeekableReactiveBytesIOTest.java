package net.lecousin.commons.reactive.io.bytes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.io.bytes.BytesIOTestUtils;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.RandomContentWithBufferSizeTestCasesProvider;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.SmallRandomContentTestCasesProvider;
import net.lecousin.commons.reactive.io.AbstractSeekableReactiveIOTest;
import net.lecousin.commons.reactive.io.ReactiveIO;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;
import reactor.test.StepVerifier;

public abstract class AbstractReadableSeekableReactiveBytesIOTest implements TestCasesProvider<byte[], ReactiveBytesIO.Readable.Seekable> {

	@Nested
	public class AsReadableBytesIO extends AbstractReadableReactiveBytesIOTest {
		@Override
		public List<? extends TestCase<byte[], ReactiveBytesIO.Readable>> getTestCases() {
			return AbstractReadableSeekableReactiveBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<byte[], ReactiveBytesIO.Readable>) (content) -> tc.getArgumentProvider().apply(content).asReadableBytesIO()))
				.toList();
		}
	}
	
	@Nested
	public class AsSeekableIO extends AbstractSeekableReactiveIOTest {
		@Override
		public List<? extends TestCase<Integer, ReactiveIO.Seekable>> getTestCases() {
			return AbstractReadableSeekableReactiveBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, ReactiveIO.Seekable>) (size) -> tc.getArgumentProvider().apply(BytesIOTestUtils.generateContent(size))))
				.toList();
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(SmallRandomContentTestCasesProvider.class)
	void readByteAt(String displayName, byte[] expected, Function<byte[], ReactiveBytesIO.Readable.Seekable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		long initialPos = io.position().block();
		int step = expected.length > 10000 ? 123 : 1;
		for (int i = 0; i < expected.length; i += step) {
			StepVerifier.create(io.readByteAt(i)).expectNext(expected[i]).as("Read byte " + i + "/" + expected.length).verifyComplete();
			StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();
		}
		StepVerifier.create(io.readByteAt(expected.length)).expectError(EOFException.class).verify();
		StepVerifier.create(io.readByteAt(expected.length + 1)).expectError(EOFException.class).verify();
		StepVerifier.create(io.readByteAt(-1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();
		
		io.close().block();
		StepVerifier.create(io.readByteAt(0)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readByteAt(expected.length)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readByteAt(expected.length + 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readByteAt(-1)).expectError(ClosedChannelException.class).verify();
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readByteArrayAt(String displayName, byte[] expected, int bufferSize, Function<byte[], ReactiveBytesIO.Readable.Seekable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		long initialPos = io.position().block();
		
		StepVerifier.create(io.readBytesAt(0, (byte[]) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.readBytesAt(-1, new byte[10], 0, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesAt(0, new byte[10], -1, 5)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesAt(0, new byte[10], 3, -5)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesAt(0, new byte[10], 3, 10)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesAt(0, new byte[10], 12, 1)).expectError(LimitExceededException.class).verify();
		
		StepVerifier.create(io.readBytesAt(0, new byte[0])).expectNext(0).verifyComplete();
		StepVerifier.create(io.readBytesAt(0, new byte[10], 3, 0)).expectNext(0).verifyComplete();
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();

		byte[] buffer = new byte[bufferSize];
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readBytesAt(pos, buffer).block();
			assertThat(nb).isPositive();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer[i]);
			pos += nb;
		}
		assertEquals(expected.length, pos);
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();
		StepVerifier.create(io.readBytesAt(expected.length, buffer)).expectNext(-1).verifyComplete();
		StepVerifier.create(io.readBytesAt(expected.length + 1, buffer)).expectNext(-1).verifyComplete();
		StepVerifier.create(io.readBytesAt(expected.length, new byte[10], 1, 2)).expectNext(-1).verifyComplete();
		
		StepVerifier.create(io.readBytesAt(expected.length, new byte[0])).expectNext(0).verifyComplete();
		StepVerifier.create(io.readBytesAt(expected.length, new byte[10], 3, 0)).expectNext(0).verifyComplete();
		
		StepVerifier.create(io.readBytesAt(0, (byte[]) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.readBytesAt(-1, new byte[10], 0, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesAt(0, new byte[10], -1, 5)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesAt(0, new byte[10], 3, -5)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesAt(0, new byte[10], 3, 10)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesAt(0, new byte[10], 12, 1)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();
		
		io.close().block();
		StepVerifier.create(io.readBytesAt(0, buffer)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesAt(0, (byte[]) null)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesAt(0, new byte[10], -1, 5)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesAt(0, new byte[10], 3, -5)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesAt(0, new byte[10], 3, 10)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesAt(0, new byte[10], 12, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesAt(0, new byte[0])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesAt(-1, new byte[0])).expectError(ClosedChannelException.class).verify();
	}


	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readByteArrayAtWithOffset(String displayName, byte[] expected, int bufferSize, Function<byte[], ReactiveBytesIO.Readable.Seekable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		long initialPos = io.position().block();
		
		byte[] buffer = new byte[bufferSize + 127];
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readBytesAt(pos, buffer, 13, bufferSize).block();
			assertThat(nb).isPositive().isLessThanOrEqualTo(bufferSize);
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer[i + 13]);
			pos += nb;
		}
		assertEquals(expected.length, pos);
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();
		StepVerifier.create(io.readBytesAt(expected.length, buffer, 13, 1)).expectNext(-1).verifyComplete();
		StepVerifier.create(io.readBytesAt(expected.length, buffer, 13, bufferSize)).expectNext(-1).verifyComplete();
		
		StepVerifier.create(io.readBytesAt(0, new byte[0])).expectNext(0).verifyComplete();
		StepVerifier.create(io.readBytesAt(0, new byte[10], 3, 0)).expectNext(0).verifyComplete();
		StepVerifier.create(io.readBytesAt(expected.length, new byte[0])).expectNext(0).verifyComplete();
		StepVerifier.create(io.readBytesAt(expected.length, new byte[10], 3, 0)).expectNext(0).verifyComplete();
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();
		
		io.close().block();
	}
	

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readByteBufferAt(String displayName, byte[] expected, int bufferSize, Function<byte[], ReactiveBytesIO.Readable.Seekable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		long initialPos = io.position().block();
		
		StepVerifier.create(io.readBytesAt(0, (ByteBuffer) null)).expectError(NullPointerException.class).verify();
		
		StepVerifier.create(io.readBytesAt(0, ByteBuffer.allocate(0))).expectNext(0).verifyComplete();
		StepVerifier.create(io.readBytesAt(0, ByteBuffer.wrap(new byte[10], 3, 0))).expectNext(0).verifyComplete();
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();

		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readBytesAt(pos, buffer).block();
			assertThat(nb).isPositive();
			buffer.flip();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer.get());
			pos += nb;
			buffer.position(0);
			buffer.limit(bufferSize);
		}
		assertEquals(expected.length, pos);
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();
		StepVerifier.create(io.readBytesAt(expected.length, ByteBuffer.allocate(1))).expectNext(-1).verifyComplete();
		
		StepVerifier.create(io.readBytesAt(0, ByteBuffer.allocate(0))).expectNext(0).verifyComplete();
		StepVerifier.create(io.readBytesAt(0, ByteBuffer.wrap(new byte[10], 3, 0))).expectNext(0).verifyComplete();
		
		StepVerifier.create(io.readBytesAt(0, (ByteBuffer) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.readBytesAt(-1, ByteBuffer.allocate(1))).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();
		
		io.close().block();
		StepVerifier.create(io.readBytesAt(0, buffer)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesAt(0, (ByteBuffer) null)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesAt(-1, ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readBytesFullyAtByteArray(String displayName, byte[] expected, int bufferSize, Function<byte[], ReactiveBytesIO.Readable.Seekable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		long initialPos = io.position().block();
		
		byte[] empty = new byte[0];
		StepVerifier.create(io.readBytesFullyAt(0, empty)).expectNext(empty).verifyComplete();
		StepVerifier.create(io.readBytesFullyAt(expected.length, empty)).expectNext(empty).verifyComplete();
		
		StepVerifier.create(io.readBytesFullyAt(0, (byte[]) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, (byte[]) null, 0, 1)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(-1, new byte[1])).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(-1, new byte[1], 0, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[1], -1, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[1], 0, -1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[10], 10, 1)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[10], 11, 1)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[10], 0, 11)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[10], 5, 6)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(expected.length + 1, new byte[1])).expectError(EOFException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(expected.length, new byte[1])).expectError(EOFException.class).verify();
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();
		
		byte[] buffer = new byte[bufferSize];
		int pos = 0;
		while (pos < expected.length) {
			if (pos + bufferSize <= expected.length) {
				io.readBytesFullyAt(pos, buffer).block();
				for (int i = 0; i < buffer.length; ++i)
					assertEquals(expected[pos + i], buffer[i]);
				pos += buffer.length;
			} else {
				io.readBytesFullyAt(pos, buffer, 1, expected.length - pos).block();
				for (int i = 0; i < expected.length - pos; ++i)
					assertEquals(expected[pos + i], buffer[i + 1]);
				pos = expected.length;
			}
		}
		assertEquals(expected.length, pos);
		
		StepVerifier.create(io.readBytesFullyAt(expected.length, new byte[1])).expectError(EOFException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(expected.length, buffer)).expectError(EOFException.class).verify();
		
		io.readBytesFullyAt(0, new byte[0]).block();
		io.readBytesFullyAt(expected.length, new byte[0]).block();
		io.readBytesFullyAt(expected.length, new byte[10], 0, 0).block();
		
		StepVerifier.create(io.readBytesFullyAt(0, (byte[]) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, (byte[]) null, 0, 1)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(-1, new byte[1])).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(-1, new byte[1], 0, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[1], -1, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[1], 0, -1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[10], 10, 1)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[10], 11, 1)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[10], 0, 11)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[10], 5, 6)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(expected.length + 1, new byte[1])).expectError(EOFException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(expected.length, new byte[1])).expectError(EOFException.class).verify();
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();
		
		io.close();
		StepVerifier.create(io.readBytesFullyAt(0, buffer)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(-1, buffer)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, (byte[]) null)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, (byte[]) null, 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(-1, new byte[1])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(-1, new byte[1], 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[1], -1, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[1], 0, -1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[10], 10, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[10], 11, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[10], 0, 11)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, new byte[10], 5, 6)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(expected.length + 1, new byte[1])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(expected.length, new byte[1])).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readBytesFullyAtByteBuffer(String displayName, byte[] expected, int bufferSize, Function<byte[], ReactiveBytesIO.Readable.Seekable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		long initialPos = io.position().block();
		
		ByteBuffer empty = ByteBuffer.allocate(0);
		StepVerifier.create(io.readBytesFullyAt(0, empty)).expectNext(empty).verifyComplete();
		StepVerifier.create(io.readBytesFullyAt(expected.length, empty)).expectNext(empty).verifyComplete();
		
		StepVerifier.create(io.readBytesFullyAt(0, (ByteBuffer) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(-1, ByteBuffer.allocate(1))).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(expected.length + 1, ByteBuffer.allocate(1))).expectError(EOFException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(expected.length, ByteBuffer.allocate(1))).expectError(EOFException.class).verify();
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();
		
		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		int pos = 0;
		while (pos < expected.length) {
			if (pos + bufferSize <= expected.length) {
				buffer.position(0);
				buffer.limit(bufferSize);
				io.readBytesFullyAt(pos, buffer).block();
				assertThat(buffer.hasRemaining()).isFalse();
				buffer.flip();
				for (int i = 0; i < bufferSize; ++i)
					assertEquals(expected[pos + i], buffer.get());
				pos += bufferSize;
			} else {
				buffer.position(0);
				buffer.limit(expected.length - pos);
				io.readBytesFullyAt(pos, buffer).block();
				assertThat(buffer.hasRemaining()).isFalse();
				buffer.flip();
				for (int i = 0; i < expected.length - pos; ++i)
					assertEquals(expected[pos + i], buffer.get());
				pos = expected.length;
			}
		}
		assertEquals(expected.length, pos);
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();
		
		buffer.position(0);
		buffer.limit(bufferSize);
		StepVerifier.create(io.readBytesFullyAt(expected.length, buffer)).expectError(EOFException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(expected.length, ByteBuffer.allocate(1))).expectError(EOFException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(expected.length + 1, ByteBuffer.allocate(1))).expectError(EOFException.class).verify();

		
		StepVerifier.create(io.readBytesFullyAt(0, (ByteBuffer) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(-1, ByteBuffer.allocate(1))).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.position()).expectNext(initialPos).verifyComplete();

		io.close();
		StepVerifier.create(io.readBytesFullyAt(0, buffer)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(0, (ByteBuffer) null)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFullyAt(-1, ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void seekAndReadFullyByteArray(String displayName, byte[] expected, int bufferSize, Function<byte[], ReactiveBytesIO.Readable.Seekable> ioSupplier) {
		ReactiveBytesIO.Readable.Seekable io = ioSupplier.apply(expected);

		byte[] buffer = new byte[bufferSize];
		int step = expected.length > 10000 ? 1123 : 1;
		
		// SeekFrom.START
		
		// read forward
		for (int i = 0; i < expected.length - bufferSize; i += step) {
			assertThat(io.seek(SeekFrom.START, i).block()).isEqualTo(i);
			assertThat(io.position().block()).isEqualTo(i);
			io.readBytesFully(buffer).block();
			for (int j = 0; j < bufferSize; j++)
				assertEquals(expected[i + j], buffer[j]);
		}
		// read backward
		for (int i = expected.length - bufferSize; i >= 0; i -= step) {
			assertThat(io.seek(SeekFrom.START, i).block()).isEqualTo(i);
			assertThat(io.position().block()).isEqualTo(i);
			io.readBytesFully(buffer).block();
			for (int j = 0; j < bufferSize; j++)
				assertEquals(expected[i + j], buffer[j]);
		}
		
		// SeekFrom.END
		
		// read forward
		for (int i = 0; i < expected.length - bufferSize; i += step) {
			assertThat(io.seek(SeekFrom.END, expected.length - i).block()).isEqualTo(i);
			assertThat(io.position().block()).isEqualTo(i);
			io.readBytesFully(buffer).block();
			for (int j = 0; j < bufferSize; j++)
				assertEquals(expected[i + j], buffer[j]);
		}
		// read backward
		for (int i = expected.length - bufferSize; i >= 0; i -= step) {
			assertThat(io.seek(SeekFrom.END, expected.length - i).block()).isEqualTo(i);
			assertThat(io.position().block()).isEqualTo(i);
			io.readBytesFully(buffer).block();
			for (int j = 0; j < bufferSize; j++)
				assertEquals(expected[i + j], buffer[j]);
		}
		
		// SeekFrom.CURRENT

		assertThat(io.seek(SeekFrom.CURRENT, -io.position().block()).block()).isZero();
		for (int i = 0; i < expected.length - bufferSize; i += step) {
			assertThat(io.position().block()).isEqualTo(i);
			io.readBytesFully(buffer).block();
			for (int j = 0; j < bufferSize; j++)
				assertEquals(expected[i + j], buffer[j]);
			assertThat(io.position().block()).isEqualTo(i + bufferSize);
			if (i + bufferSize + step <= expected.length) {
				assertThat(io.seek(SeekFrom.CURRENT, -bufferSize + step).block()).isEqualTo(i + step);
				assertThat(io.position().block()).isEqualTo(i + step);
			}
		}
		
		io.close().block();
	}

}
