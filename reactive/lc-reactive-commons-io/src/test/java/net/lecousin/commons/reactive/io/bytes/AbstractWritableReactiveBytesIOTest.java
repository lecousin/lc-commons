package net.lecousin.commons.reactive.io.bytes;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.lecousin.commons.collections.LcArrayUtils;
import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.LcByteBufferUtils;
import net.lecousin.commons.io.bytes.BytesIOTestUtils;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.RandomContentTestCasesProvider;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.RandomContentWithBufferSizeTestCasesProvider;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.SmallRandomContentTestCasesProvider;
import net.lecousin.commons.reactive.io.AbstractWritableReactiveIOTest;
import net.lecousin.commons.reactive.io.AbstractWritableResizableReactiveIOTest;
import net.lecousin.commons.reactive.io.ReactiveIO;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public abstract class AbstractWritableReactiveBytesIOTest implements TestCasesProvider<Integer, AbstractWritableReactiveBytesIOTest.WritableTestCase<?, ?>> {

	@Data
	@AllArgsConstructor
	public static class WritableTestCase<T extends ReactiveBytesIO.Writable, O> {
		private T io;
		private O object;
		
		public WritableTestCase(T io) {
			this(io, null);
		}
	}
	
	protected abstract void checkWrittenData(ReactiveBytesIO.Writable io, Object object, byte[] expected) throws Exception;
	
	@Nested
	public class AsWritableIO extends AbstractWritableReactiveIOTest {
		@Override
		public List<? extends TestCase<Void, ReactiveIO.Writable>> getTestCases() {
			return AbstractWritableReactiveBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), (Supplier<ReactiveIO.Writable>) () -> tc.getArgumentProvider().apply(1).getIo()))
				.toList();
		}
	}
	
	@Nested
	public class AsResizableIO extends AbstractWritableResizableReactiveIOTest {
		@Override
		public List<? extends TestCase<Integer, ReactiveIO.Writable.Resizable>> getTestCases() {
			return AbstractWritableReactiveBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, ReactiveIO.Writable.Resizable>) (initialSize) -> {
					WritableTestCase<?,?> wtc = tc.getArgumentProvider().apply(initialSize);
					Assumptions.assumeTrue(wtc.getIo() instanceof ReactiveIO.Writable.Resizable);
					return (ReactiveIO.Writable.Resizable) wtc.getIo();
				}))
				.toList();
		}
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyByteArrayOneShot(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable io = ioTuple.getIo();
		
		StepVerifier.create(io.writeBytesFully((byte[]) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[0])).verifyComplete();

		io.writeBytesFully(toWrite).block();
		
		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytesFully(new byte[1])).expectError(EOFException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[0])).verifyComplete();
		
		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			io.writeBytesFully(additional).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + 69).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				io.writeBytesFully(additional2).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof ReactiveIO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71).block();
			byte[] additional = BytesIOTestUtils.generateContent(71);
			io.writeBytesFully(additional).block();
			StepVerifier.create(io.writeBytesFully(new byte[1])).expectError(EOFException.class).verify();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close().block();
		StepVerifier.create(io.writeBytesFully((byte[]) null)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[0])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[1])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.flush()).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyByteArrayWithOffset(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable io = ioTuple.getIo();
		
		StepVerifier.create(io.writeBytesFully((byte[]) null, 0, 1)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[10], -1, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[10], 3, -1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[10], 0, 11)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[10], 11, 1)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[10], 7, 0)).verifyComplete();

		int step = toWrite.length > 10000 ? 1111 : 3;
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			io.writeBytesFully(toWrite, i, l).block();
		}
		
		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytesFully(new byte[1], 0, 1)).expectError(EOFException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[1], 0, 0)).verifyComplete();
		
		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			io.writeBytesFully(additional, 7, 13).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 7, 13)));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length + 13 + 32).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				io.writeBytesFully(additional2, 17, 32).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 7, 13), ByteBuffer.wrap(additional2, 17, 32)));
				r.setSize(toWrite.length).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof ReactiveIO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71).block();
			byte[] additional = BytesIOTestUtils.generateContent(100);
			io.writeBytesFully(additional, 16, 71).block();
			StepVerifier.create(io.writeBytesFully(new byte[10], 3, 1)).expectError(EOFException.class).verify();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 16, 71)));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close().block();
		StepVerifier.create(io.writeBytesFully(toWrite, 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully((byte[]) null, 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[10], -1, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[10], 3, -1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[10], 0, 11)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[10], 11, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(new byte[10], 7, 0)).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyByteBufferOneShot(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable io = ioTuple.getIo();

		StepVerifier.create(io.writeBytesFully((ByteBuffer) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytesFully(ByteBuffer.allocate(0))).verifyComplete();
		
		io.writeBytesFully(ByteBuffer.wrap(toWrite)).block();
		
		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytesFully(ByteBuffer.allocate(1))).expectError(EOFException.class).verify();
		StepVerifier.create(io.writeBytesFully(ByteBuffer.allocate(0))).verifyComplete();
		
		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			io.writeBytesFully(ByteBuffer.wrap(additional)).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + 69).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				io.writeBytesFully(ByteBuffer.wrap(additional2)).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof ReactiveIO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71).block();
			byte[] additional = BytesIOTestUtils.generateContent(71);
			io.writeBytesFully(ByteBuffer.wrap(additional)).block();
			StepVerifier.create(io.writeBytesFully(ByteBuffer.allocate(1))).expectError(EOFException.class).verify();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close().block();
		StepVerifier.create(io.writeBytesFully((ByteBuffer) null)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(ByteBuffer.allocate(0))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyByteBufferSmallParts(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable io = ioTuple.getIo();

		int step = toWrite.length > 10000 ? 1111 : 3;
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			io.writeBytesFully(ByteBuffer.wrap(toWrite, i, l)).block();
		}
		
		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);

		io.close().block();
		StepVerifier.create(io.writeBytesFully(ByteBuffer.allocate(0))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeFullyByteBufferList(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable io = ioTuple.getIo();

		StepVerifier.create(io.writeBytesFully((List<ByteBuffer>) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytesFully(List.of())).verifyComplete();
		StepVerifier.create(io.writeBytesFully(List.of(ByteBuffer.allocate(0)))).verifyComplete();
		StepVerifier.create(io.writeBytesFully(List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0)))).verifyComplete();

		List<ByteBuffer> buffers = new LinkedList<>();
		int step = toWrite.length > 10000 ? 1111 : 3;
		buffers.add(ByteBuffer.allocate(0));
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			buffers.add(ByteBuffer.wrap(toWrite, i, l));
			buffers.add(ByteBuffer.allocate(0));
		}
		io.writeBytesFully(buffers).block();

		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytesFully(List.of(ByteBuffer.allocate(1)))).expectError(EOFException.class).verify();
		StepVerifier.create(io.writeBytesFully(List.of())).verifyComplete();
		StepVerifier.create(io.writeBytesFully(List.of(ByteBuffer.allocate(0)))).verifyComplete();
		StepVerifier.create(io.writeBytesFully(List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0)))).verifyComplete();

		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		io.close().block();
		StepVerifier.create(io.writeBytesFully(List.of(ByteBuffer.allocate(1)))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(List.of())).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(List.of(ByteBuffer.allocate(0)))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0)))).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeFullyByteBufferFlux(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable io = ioTuple.getIo();

		StepVerifier.create(io.writeBytesFully((Flux<ByteBuffer>) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytesFully(Flux.empty())).verifyComplete();
		StepVerifier.create(io.writeBytesFully(Flux.just(ByteBuffer.allocate(0)))).verifyComplete();
		StepVerifier.create(io.writeBytesFully(Flux.fromIterable(List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0))))).verifyComplete();

		Flux<ByteBuffer> flux = Flux.create(sink -> {
			int step = toWrite.length > 10000 ? 1111 : 3;
			sink.next(ByteBuffer.allocate(0));
			for (int i = 0; i < toWrite.length; i += step) {
				int l = Math.min(step, toWrite.length - i);
				sink.next(ByteBuffer.wrap(toWrite, i, l));
				sink.next(ByteBuffer.allocate(0));
			}
			sink.complete();
		});
		io.writeBytesFully(flux).block();
		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);

		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytesFully(Flux.just(ByteBuffer.allocate(1)))).expectError(EOFException.class).verify();
		StepVerifier.create(io.writeBytesFully(Flux.empty())).verifyComplete();
		StepVerifier.create(io.writeBytesFully(Flux.just(ByteBuffer.allocate(0)))).verifyComplete();
		StepVerifier.create(io.writeBytesFully(Flux.fromIterable(List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0))))).verifyComplete();
		
		io.close().block();
		StepVerifier.create(io.writeBytesFully(Flux.just(ByteBuffer.allocate(1)))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(Flux.empty())).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(Flux.just(ByteBuffer.allocate(0)))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFully(Flux.fromIterable(List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0))))).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void writeByteBuffer(String displayName, byte[] toWrite, int bufferSize, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable io = ioTuple.getIo();

		StepVerifier.create(io.writeBytes((ByteBuffer) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytes(ByteBuffer.allocate(0))).expectNext(0).verifyComplete();

		byte[] b;
		if (io instanceof ReactiveIO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new byte[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		boolean smallStep = true;
		for (int i = 0; i < toWrite.length;) {
			int step = smallStep ? 3 : bufferSize;
			smallStep = !smallStep;
			int l = Math.min(step, b.length - i);
			int nb = io.writeBytes(ByteBuffer.wrap(b, i, l)).block();
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytes(ByteBuffer.allocate(1))).expectNext(-1).verifyComplete();
		StepVerifier.create(io.writeBytes(ByteBuffer.allocate(0))).expectNext(0).verifyComplete();

		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int nb = io.writeBytes(ByteBuffer.wrap(additional)).block();
			assertThat(nb).isPositive();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length + nb + 69).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				int nb2 = io.writeBytes(ByteBuffer.wrap(additional2)).block();
				assertThat(nb2).isPositive();
				r.setSize(toWrite.length + nb + nb2).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb), ByteBuffer.wrap(additional2, 0, nb2)));
				r.setSize(toWrite.length).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof ReactiveIO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71).block();
			byte[] additional = BytesIOTestUtils.generateContent(71);
			int nb = io.writeBytes(ByteBuffer.wrap(additional)).block();
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}

		io.close().block();
		StepVerifier.create(io.writeBytes(ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytes(ByteBuffer.allocate(0))).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void writeByteBufferDirect(String displayName, byte[] toWrite, int bufferSize, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable io = ioTuple.getIo();

		StepVerifier.create(io.writeBytes(ByteBuffer.allocateDirect(0))).expectNext(0).verifyComplete();

		byte[] b;
		if (io instanceof ReactiveIO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new byte[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		boolean smallStep = true;
		for (int i = 0; i < toWrite.length;) {
			int step = smallStep ? 3 : bufferSize;
			smallStep = !smallStep;
			int l = Math.min(step, b.length - i);
			ByteBuffer bb = ByteBuffer.allocateDirect(l);
			bb.put(b, i, l);
			int nb = io.writeBytes(bb.flip()).block();
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytes(ByteBuffer.allocateDirect(1))).expectNext(-1).verifyComplete();
		StepVerifier.create(io.writeBytes(ByteBuffer.allocateDirect(0))).expectNext(0).verifyComplete();

		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		io.close().block();
		StepVerifier.create(io.writeBytes(ByteBuffer.allocateDirect(1))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytes(ByteBuffer.allocateDirect(0))).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void writeByteArray(String displayName, byte[] toWrite, int bufferSize, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable io = ioTuple.getIo();

		StepVerifier.create(io.writeBytes((byte[]) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[0])).expectNext(0).verifyComplete();

		byte[] b;
		if (io instanceof ReactiveIO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new byte[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		boolean smallStep = true;
		for (int i = 0; i < toWrite.length;) {
			int step = smallStep ? 3 : bufferSize;
			smallStep = !smallStep;
			int l = Math.min(step, b.length - i);
			byte[] bb = new byte[l];
			System.arraycopy(b, i, bb, 0, l);
			int nb = io.writeBytes(bb).block();
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytes(new byte[1])).expectNext(-1).verifyComplete();
		StepVerifier.create(io.writeBytes(new byte[0])).expectNext(0).verifyComplete();

		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int nb = io.writeBytes(additional).block();
			assertThat(nb).isPositive();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length + nb + 69).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				int nb2 = io.writeBytes(additional2).block();
				assertThat(nb2).isPositive();
				r.setSize(toWrite.length + nb + nb2).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb), ByteBuffer.wrap(additional2, 0, nb2)));
				r.setSize(toWrite.length).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof ReactiveIO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71).block();
			byte[] additional = BytesIOTestUtils.generateContent(71);
			int nb = io.writeBytes(additional).block();
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}

		io.close().block();
		StepVerifier.create(io.writeBytes(new byte[0])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[1])).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void writeByteArrayWithOffset(String displayName, byte[] toWrite, int bufferSize, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable io = ioTuple.getIo();

		StepVerifier.create(io.writeBytes((byte[]) null, 0, 1)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[10], -1, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[10], 4, -2)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[10], 0, 11)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[10], 11, 1)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[10], 7, 0)).expectNext(0).verifyComplete();

		byte[] b;
		if (io instanceof ReactiveIO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new byte[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		boolean smallStep = true;
		for (int i = 0; i < toWrite.length;) {
			int step = smallStep ? 3 : bufferSize;
			smallStep = !smallStep;
			int l = Math.min(step, b.length - i);
			int nb = io.writeBytes(b, i, l).block();
			assertThat(nb).as("Write up to " + l + " at " + i + "/" + toWrite.length).isPositive();
			i += nb;
		}

		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytes(new byte[1], 0, 1)).expectNext(-1).verifyComplete();
		StepVerifier.create(io.writeBytes(new byte[0], 0, 0)).expectNext(0).verifyComplete();
		StepVerifier.create(io.writeBytes(new byte[1], 0, 0)).expectNext(0).verifyComplete();

		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int nb = io.writeBytes(additional, 3, 19).block();
			assertThat(nb).isPositive();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 3, nb)));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length + nb + 69).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(100);
				int nb2 = io.writeBytes(ByteBuffer.wrap(additional2, 6, 69)).block();
				assertThat(nb2).isPositive();
				r.setSize(toWrite.length + nb + nb2).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 3, nb), ByteBuffer.wrap(additional2, 6, nb2)));
				r.setSize(toWrite.length).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof ReactiveIO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71).block();
			byte[] additional = BytesIOTestUtils.generateContent(100);
			int nb = io.writeBytes(additional, 20, 71).block();
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 20, nb)));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}

		io.close().block();
		StepVerifier.create(io.writeBytes((byte[]) null, 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[10], -1, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[10], 2, -1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[10], 0, 11)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[10], 11, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[10], 7, 0)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[1], 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[0], 0, 0)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytes(new byte[1], 0, 0)).expectError(ClosedChannelException.class).verify();
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(SmallRandomContentTestCasesProvider.class)
	void writeByte(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable io = ioTuple.getIo();

		for (int i = 0; i < toWrite.length; ++i)
			io.writeByte(toWrite[i]).block();
		
		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeByte((byte) 0)).expectError(EOFException.class).verify();

		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			for (int i = 0; i < additional.length; ++i)
				io.writeByte(additional[i]).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + 69).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				for (int i = 0; i < additional2.length; ++i)
					io.writeByte(additional2[i]).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof ReactiveIO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71).block();
			byte[] additional = BytesIOTestUtils.generateContent(71);
			for (int i = 0; i < additional.length; ++i)
				io.writeByte(additional[i]).block();
			StepVerifier.create(io.writeByte((byte) 0)).expectError(EOFException.class).verify();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close().block();
		StepVerifier.create(io.writeByte((byte) 0)).expectError(ClosedChannelException.class).verify();
	}
	
}
