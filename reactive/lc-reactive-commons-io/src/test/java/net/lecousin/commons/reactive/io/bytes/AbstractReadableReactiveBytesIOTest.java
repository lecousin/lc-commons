package net.lecousin.commons.reactive.io.bytes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.RandomContentTestCasesProvider;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.RandomContentWithBufferSizeTestCasesProvider;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.SmallRandomContentTestCasesProvider;
import net.lecousin.commons.reactive.io.AbstractKnownSizeReactiveIOTest;
import net.lecousin.commons.reactive.io.AbstractReadableReactiveIOTest;
import net.lecousin.commons.reactive.io.ReactiveIO;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public abstract class AbstractReadableReactiveBytesIOTest implements TestCasesProvider<byte[], ReactiveBytesIO.Readable> {

	@Nested
	public class AsReadableIO extends AbstractReadableReactiveIOTest {
		@Override
		public List<? extends TestCase<Void, ReactiveIO.Readable>> getTestCases() {
			return AbstractReadableReactiveBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), (Supplier<ReactiveIO.Readable>) () -> tc.getArgumentProvider().apply(new byte[1])))
				.toList();
		}
	}
	
	@Nested
	public class AsKnownSizeIO extends AbstractKnownSizeReactiveIOTest {
		@Override
		public List<? extends TestCase<Integer, ReactiveIO.KnownSize>> getTestCases() {
			return AbstractReadableReactiveBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(),
					(Function<Integer, ReactiveIO.KnownSize>) (size) -> {
						ReactiveBytesIO.Readable io = tc.getArgumentProvider().apply(new byte[size]);
						assumeThat(io).isInstanceOf(ReactiveIO.KnownSize.class);
						return (ReactiveIO.KnownSize) io;
					}
				))
				.toList();
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void readBuffer(String displayName, byte[] expected, Function<byte[], ReactiveBytesIO.Readable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable io = ioSupplier.apply(expected);
		int pos = 0;
		while (pos < expected.length) {
			ByteBuffer b = io.readBuffer().block();
			assertThat(b).isNotNull();
			assertThat(b.remaining()).isPositive();
			while (b.hasRemaining())
				assertThat(b.get()).isEqualTo(expected[pos++]);
		}
		assertThat(io.readBuffer().block()).isNull();

		io.close().block();
		StepVerifier.create(io.readBuffer()).expectError(ClosedChannelException.class).verify();
	}
	
	static class FluxArgs extends RandomContentTestCasesProvider {
		public FluxArgs() {
			add(List.of(0, 1, 2, 3).stream().map(nbBuffers -> Arguments.of("with " + nbBuffers + " advanced buffers", nbBuffers)));
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(FluxArgs.class)
	void toFlux(String displayName, byte[] expected, Function<byte[], ReactiveBytesIO.Readable> ioSupplier, int nbBuffers) throws Exception {
		ReactiveBytesIO.Readable io = ioSupplier.apply(expected);
		List<ByteBuffer> buffers = io.toFlux(nbBuffers).collectList().block();
		Iterator<ByteBuffer> it = buffers.iterator();
		int done = 0;
		while (done < expected.length) {
			ByteBuffer b = it.next();
			assertThat(b.hasRemaining()).isTrue();
			while (b.hasRemaining()) {
				assertThat(b.get()).as("Byte " + done + "/" + expected.length + " (with " + nbBuffers + " advanced buffers)").isEqualTo(expected[done]);
				done++;
			}
		}
		assertThat(it.hasNext()).isFalse();

		io.close().block();
		StepVerifier.create(io.toFlux(nbBuffers)).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(SmallRandomContentTestCasesProvider.class)
	void readByte(String displayName, byte[] expected, Function<byte[], ReactiveBytesIO.Readable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable io = ioSupplier.apply(expected);
		for (int i = 0; i < expected.length; i++) {
			StepVerifier.create(io.readByte()).expectNext(expected[i]).as("Read byte " + i + "/" + expected.length).verifyComplete();
		}
		StepVerifier.create(io.readByte()).expectError(EOFException.class).verify();
		io.close().block();
		StepVerifier.create(io.readByte()).expectError(ClosedChannelException.class).verify();
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readByteArray(String displayName, byte[] expected, int bufferSize, Function<byte[], ReactiveBytesIO.Readable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable io = ioSupplier.apply(expected);
		
		StepVerifier.create(io.readBytes((byte[]) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.readBytes(new byte[10], -1, 5)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytes(new byte[10], 3, -5)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytes(new byte[10], 3, 10)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytes(new byte[10], 12, 1)).expectError(LimitExceededException.class).verify();
		
		StepVerifier.create(io.readBytes(new byte[0])).expectNext(0).verifyComplete();
		StepVerifier.create(io.readBytes(new byte[10], 3, 0)).expectNext(0).verifyComplete();
		

		byte[] buffer = new byte[bufferSize];
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readBytes(buffer).block();
			assertThat(nb).isPositive();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer[i]);
			pos += nb;
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readBytes(buffer).block());
		assertEquals(-1, io.readBytes(buffer).block());
		assertEquals(-1, io.readBytes(new byte[10], 1, 2).block());
		
		StepVerifier.create(io.readBytes(new byte[0])).expectNext(0).verifyComplete();
		StepVerifier.create(io.readBytes(new byte[10], 3, 0)).expectNext(0).verifyComplete();
		
		StepVerifier.create(io.readBytes((byte[]) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.readBytes(new byte[10], -1, 5)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytes(new byte[10], 3, -5)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytes(new byte[10], 3, 10)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytes(new byte[10], 12, 1)).expectError(LimitExceededException.class).verify();
		
		io.close().block();
		StepVerifier.create(io.readBytes(buffer)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytes((byte[]) null)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytes(new byte[10], -1, 5)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytes(new byte[10], 3, -5)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytes(new byte[10], 3, 10)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytes(new byte[10], 12, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytes(new byte[0])).expectError(ClosedChannelException.class).verify();
	}


	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readByteArrayWithOffset(String displayName, byte[] expected, int bufferSize, Function<byte[], ReactiveBytesIO.Readable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable io = ioSupplier.apply(expected);
		
		byte[] buffer = new byte[bufferSize + 127];
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readBytes(buffer, 13, bufferSize).block();
			assertThat(nb).isPositive().isLessThanOrEqualTo(bufferSize);
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer[i + 13]);
			pos += nb;
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readBytes(buffer, 13, 1).block());
		assertEquals(-1, io.readBytes(buffer, 13, bufferSize).block());
		
		StepVerifier.create(io.readBytes(new byte[0])).expectNext(0).verifyComplete();
		StepVerifier.create(io.readBytes(new byte[10], 3, 0)).expectNext(0).verifyComplete();
		
		io.close().block();
	}
	

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readByteBuffer(String displayName, byte[] expected, int bufferSize, Function<byte[], ReactiveBytesIO.Readable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable io = ioSupplier.apply(expected);
		
		StepVerifier.create(io.readBytes((ByteBuffer) null)).expectError(NullPointerException.class).verify();
		
		StepVerifier.create(io.readBytes(ByteBuffer.allocate(0))).expectNext(0).verifyComplete();
		StepVerifier.create(io.readBytes(ByteBuffer.wrap(new byte[10], 3, 0))).expectNext(0).verifyComplete();

		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		int pos = 0;
		while (pos < expected.length) {
			int nb = io.readBytes(buffer).block();
			assertThat(nb).isPositive();
			buffer.flip();
			for (int i = 0; i < nb; ++i)
				assertEquals(expected[pos + i], buffer.get());
			pos += nb;
			buffer.position(0);
			buffer.limit(bufferSize);
		}
		assertEquals(expected.length, pos);
		assertEquals(-1, io.readBytes(ByteBuffer.allocate(1)).block());
		
		StepVerifier.create(io.readBytes(ByteBuffer.allocate(0))).expectNext(0).verifyComplete();
		StepVerifier.create(io.readBytes(ByteBuffer.wrap(new byte[10], 3, 0))).expectNext(0).verifyComplete();
		
		StepVerifier.create(io.readBytes((ByteBuffer) null)).expectError(NullPointerException.class).verify();
		
		io.close().block();
		StepVerifier.create(io.readBytes(buffer)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytes((ByteBuffer) null)).expectError(ClosedChannelException.class).verify();
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readBytesFullyByteArray(String displayName, byte[] expected, int bufferSize, Function<byte[], ReactiveBytesIO.Readable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable io = ioSupplier.apply(expected);
		
		byte[] empty = new byte[0];
		StepVerifier.create(io.readBytesFully(empty)).expectNext(empty).verifyComplete();
		
		StepVerifier.create(io.readBytesFully(null, 0, 1)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], -1, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 10, 1)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 11, 1)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 0, 11)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 5, 6)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 0, -1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesFully((byte[]) null)).expectError(NullPointerException.class).verify();
		
		byte[] buffer = new byte[bufferSize];
		int pos = 0;
		while (pos < expected.length) {
			if (pos + bufferSize <= expected.length) {
				io.readBytesFully(buffer).block();
				for (int i = 0; i < buffer.length; ++i)
					assertEquals(expected[pos + i], buffer[i]);
				pos += buffer.length;
			} else {
				io.readBytesFully(buffer, 1, expected.length - pos).block();
				for (int i = 0; i < expected.length - pos; ++i)
					assertEquals(expected[pos + i], buffer[i + 1]);
				pos = expected.length;
			}
		}
		assertEquals(expected.length, pos);
		
		StepVerifier.create(io.readBytesFully(new byte[1])).expectError(EOFException.class).verify();
		StepVerifier.create(io.readBytesFully(buffer)).expectError(EOFException.class).verify();
		
		StepVerifier.create(io.readBytesFully(null, 0, 1)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], -1, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 10, 1)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 11, 1)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 0, 11)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 5, 6)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 0, -1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.readBytesFully((byte[]) null)).expectError(NullPointerException.class).verify();
		
		io.close().block();
		StepVerifier.create(io.readBytesFully(buffer)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFully(null, 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], -1, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 10, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 11, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 0, 11)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 5, 6)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFully(new byte[10], 0, -1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFully((byte[]) null)).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void readBytesFullyByteBuffer(String displayName, byte[] expected, int bufferSize, Function<byte[], ReactiveBytesIO.Readable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable io = ioSupplier.apply(expected);
		
		ByteBuffer empty = ByteBuffer.allocate(0);
		StepVerifier.create(io.readBytesFully(empty)).expectNext(empty).verifyComplete();
		
		StepVerifier.create(io.readBytesFully((ByteBuffer) null)).expectError(NullPointerException.class).verify();
		
		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		int pos = 0;
		while (pos < expected.length) {
			if (pos + bufferSize <= expected.length) {
				buffer.position(0);
				buffer.limit(bufferSize);
				io.readBytesFully(buffer).block();
				assertThat(buffer.hasRemaining()).isFalse();
				buffer.flip();
				for (int i = 0; i < bufferSize; ++i)
					assertEquals(expected[pos + i], buffer.get());
				pos += bufferSize;
			} else {
				buffer.position(0);
				buffer.limit(expected.length - pos);
				io.readBytesFully(buffer).block();
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
		StepVerifier.create(io.readBytesFully(buffer)).expectError(EOFException.class).verify();
		StepVerifier.create(io.readBytesFully(ByteBuffer.allocate(1))).expectError(EOFException.class).verify();
		
		StepVerifier.create(io.readBytesFully((ByteBuffer) null)).expectError(NullPointerException.class).verify();

		io.close().block();
		StepVerifier.create(io.readBytesFully(buffer)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.readBytesFully((ByteBuffer) null)).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void skipUpTo(String displayName, byte[] expected, int skipSize, Function<byte[], ReactiveBytesIO.Readable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable io = ioSupplier.apply(expected);
		
		StepVerifier.create(io.skipUpTo(0)).expectNext(0L).verifyComplete();
		StepVerifier.create(io.skipUpTo(-1)).expectError(NegativeValueException.class).verify();
		
		int pos = 0;
		do {
			long skipped = io.skipUpTo(skipSize).block();
			if (pos == expected.length) {
				assertEquals(-1L, skipped);
				break;
			} else
				assertThat(skipped).isPositive();
			pos += skipped;
			assertThat(pos).isLessThanOrEqualTo(expected.length);
		} while (true);

		StepVerifier.create(io.skipUpTo(0)).expectNext(0L).verifyComplete();
		StepVerifier.create(io.skipUpTo(1)).expectNext(-1L).verifyComplete();
		StepVerifier.create(io.skipUpTo(-1)).expectError(NegativeValueException.class).verify();
		
		io.close().block();
		StepVerifier.create(io.skipUpTo(0)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.skipUpTo(1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.skipUpTo(-1)).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void skipFully(String displayName, byte[] expected, int skipSize, Function<byte[], ReactiveBytesIO.Readable> ioSupplier) throws Exception {
		ReactiveBytesIO.Readable io = ioSupplier.apply(expected);
		
		StepVerifier.create(io.skipFully(0)).verifyComplete();
		StepVerifier.create(io.skipFully(-1)).expectError(NegativeValueException.class).verify();
		
		int pos = 0;
		do {
			Mono<Void> skip = io.skipFully(skipSize);
			if (pos + skipSize > expected.length) {
				StepVerifier.create(skip).expectError(EOFException.class).verify();
				int remaining = expected.length - pos;
				// make sure we are at the end
				io.skipUpTo(remaining).block();
				break;
			} else
				StepVerifier.create(skip).verifyComplete();
			pos += skipSize;
		} while (true);

		StepVerifier.create(io.skipFully(0)).verifyComplete();
		StepVerifier.create(io.skipFully(1)).expectError(EOFException.class).verify();
		StepVerifier.create(io.skipFully(-1)).expectError(NegativeValueException.class).verify();
		
		io.close().block();
		StepVerifier.create(io.skipFully(0)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.skipFully(1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.skipFully(-1)).expectError(ClosedChannelException.class).verify();
	}
	
	
}
