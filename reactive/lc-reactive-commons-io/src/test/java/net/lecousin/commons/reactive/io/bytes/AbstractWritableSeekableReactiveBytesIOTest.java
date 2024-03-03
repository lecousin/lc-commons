package net.lecousin.commons.reactive.io.bytes;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.collections.LcArrayUtils;
import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.LcByteBufferUtils;
import net.lecousin.commons.io.bytes.BytesIOTestUtils;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.RandomContentTestCasesProvider;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.SmallRandomContentTestCasesProvider;
import net.lecousin.commons.reactive.io.AbstractSeekableReactiveIOTest;
import net.lecousin.commons.reactive.io.ReactiveIO;
import net.lecousin.commons.reactive.io.bytes.AbstractWritableReactiveBytesIOTest.WritableTestCase;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;
import reactor.test.StepVerifier;

public abstract class AbstractWritableSeekableReactiveBytesIOTest implements TestCasesProvider<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>> {

	protected abstract void checkWrittenData(ReactiveBytesIO.Writable.Seekable io, Object object, byte[] expected) throws Exception;
	
	@Nested
	public class AsWritableBytesIO extends AbstractWritableReactiveBytesIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<?,?>>> getTestCases() {
			return AbstractWritableSeekableReactiveBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, WritableTestCase<?,?>>) (size) -> {
					WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?> wtc = tc.getArgumentProvider().apply(size);
					ReactiveBytesIO.Writable.Seekable seekable = wtc.getIo();
					ReactiveBytesIO.Writable writable = seekable.asWritableBytesIO();
					return new WritableTestCase<>(writable, Pair.of(seekable, wtc.getObject()));
				}))
				.toList();
		}
		
		@Override
		protected void checkWrittenData(ReactiveBytesIO.Writable io, Object object, byte[] expected) throws Exception {
			@SuppressWarnings("unchecked")
			Pair<ReactiveBytesIO.Writable.Seekable, Object> pair = (Pair<ReactiveBytesIO.Writable.Seekable, Object>) object;
			AbstractWritableSeekableReactiveBytesIOTest.this.checkWrittenData(pair.getLeft(), pair.getRight(), expected);
		}
	}
	
	@Nested
	public class AsSeekableIO extends AbstractSeekableReactiveIOTest {
		@Override
		public List<? extends TestCase<Integer, ReactiveIO.Seekable>> getTestCases() {
			return AbstractWritableSeekableReactiveBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, ReactiveIO.Seekable>) (size) -> tc.getArgumentProvider().apply(size).getIo()))
				.toList();
		}
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyAtByteArrayOneShot(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable.Seekable io = ioTuple.getIo();
		
		StepVerifier.create(io.writeBytesFullyAt(0, (byte[]) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(-1, new byte[1])).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[0])).verifyComplete();

		io.writeBytesFullyAt(0, toWrite).block();
		
		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytesFullyAt(toWrite.length, new byte[1])).expectError(EOFException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(toWrite.length, new byte[0])).verifyComplete();
		
		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			io.writeBytesFullyAt(toWrite.length - before, additional).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional)));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + additional.length + 69).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				io.writeBytesFullyAt(toWrite.length - before + additional.length, additional2).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional), ByteBuffer.wrap(additional2)));
				r.setSize(toWrite.length).block();
				io.writeBytesFullyAt(toWrite.length - before, toWrite, toWrite.length - before, before).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof ReactiveIO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71).block();
			byte[] additional = BytesIOTestUtils.generateContent(71);
			io.writeBytesFullyAt(toWrite.length, additional).block();
			StepVerifier.create(io.writeBytesFullyAt(toWrite.length + additional.length, new byte[1])).expectError(EOFException.class).verify();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}

		io.close().block();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[0])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[1])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, (byte[]) null)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(-1, new byte[1])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(toWrite.length + 1, new byte[1])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(toWrite.length, new byte[1])).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyAtByteArrayWithOffset(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable.Seekable io = ioTuple.getIo();
		
		StepVerifier.create(io.writeBytesFullyAt(0, (byte[]) null, 0, 0)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(-1, new byte[1], 0, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[10], -1, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[10], 2, -1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[10], 11, 1)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[10], 0, 11)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[10], 7, 5)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[10], 7, 0)).verifyComplete();

		int step = toWrite.length > 10000 ? 1111 : 3;
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			io.writeBytesFullyAt(i, toWrite, i, l).block();
		}
		
		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytesFullyAt(toWrite.length, new byte[1], 0, 1)).expectError(EOFException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(toWrite.length, new byte[1], 0, 0)).verifyComplete();
		
		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			io.writeBytesFullyAt(toWrite.length - before, additional, 7, 13).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional, 7, 13)));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + 13 + 32).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				io.writeBytesFullyAt(toWrite.length - before + 13, additional2, 17, 32).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional, 7, 13), ByteBuffer.wrap(additional2, 17, 32)));
				r.setSize(toWrite.length).block();
				io.writeBytesFullyAt(toWrite.length - before, toWrite, toWrite.length - before, before).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof ReactiveIO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71).block();
			byte[] additional = BytesIOTestUtils.generateContent(100);
			io.writeBytesFullyAt(toWrite.length, additional, 16, 71).block();
			StepVerifier.create(io.writeBytesFullyAt(toWrite.length + additional.length, new byte[10], 3, 1)).expectError(EOFException.class).verify();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 16, 71)));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close().block();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[0], 0, 0)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[1], 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, (byte[]) null, 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(-1, new byte[1], 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(toWrite.length + 1, new byte[1], 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(toWrite.length, new byte[1], 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[10], -1, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[10], 11, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[10], 0, 11)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, new byte[10], 7, 5)).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyAtByteBufferOneShot(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable.Seekable io = ioTuple.getIo();

		StepVerifier.create(io.writeBytesFullyAt(0, (ByteBuffer) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(-1, ByteBuffer.allocate(1))).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, ByteBuffer.allocate(0))).verifyComplete();
		
		io.writeBytesFullyAt(0, ByteBuffer.wrap(toWrite)).block();
		
		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytesFullyAt(toWrite.length, ByteBuffer.allocate(1))).expectError(EOFException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(toWrite.length, ByteBuffer.allocate(0))).verifyComplete();
		
		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			io.writeBytesFullyAt(toWrite.length - before, ByteBuffer.wrap(additional)).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional)));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + additional.length + 69).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				io.writeBytesFullyAt(toWrite.length - before + additional.length, ByteBuffer.wrap(additional2)).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional), ByteBuffer.wrap(additional2)));
				r.setSize(toWrite.length).block();
				io.writeBytesFullyAt(toWrite.length - before, ByteBuffer.wrap(toWrite, toWrite.length - before, before)).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof ReactiveIO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71).block();
			byte[] additional = BytesIOTestUtils.generateContent(71);
			io.writeBytesFullyAt(toWrite.length, ByteBuffer.wrap(additional)).block();
			StepVerifier.create(io.writeBytesFullyAt(toWrite.length + additional.length, ByteBuffer.allocate(1))).expectError(EOFException.class).verify();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close().block();
		StepVerifier.create(io.writeBytesFullyAt(0, ByteBuffer.allocate(0))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, (ByteBuffer) null)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(-1, ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(toWrite.length + 1, ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(toWrite.length, ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyAtByteBufferSmallParts(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable.Seekable io = ioTuple.getIo();

		int step = toWrite.length > 10000 ? 1111 : 3;
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			io.writeBytesFullyAt(i, ByteBuffer.wrap(toWrite, i, l)).block();
		}
		
		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);

		io.close().block();
		StepVerifier.create(io.writeBytesFullyAt(0, ByteBuffer.allocate(0))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyAtBufferList(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable.Seekable io = ioTuple.getIo();

		StepVerifier.create(io.writeBytesFullyAt(0, (List<ByteBuffer>) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(-1, List.of())).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, List.of())).verifyComplete();
		StepVerifier.create(io.writeBytesFullyAt(0, List.of(ByteBuffer.allocate(0)))).verifyComplete();
		StepVerifier.create(io.writeBytesFullyAt(0, List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0)))).verifyComplete();

		List<ByteBuffer> buffers = new LinkedList<>();
		int step = toWrite.length > 10000 ? 1111 : 3;
		buffers.add(ByteBuffer.allocate(0));
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			buffers.add(ByteBuffer.wrap(toWrite, i, l));
			buffers.add(ByteBuffer.allocate(0));
		}
		io.writeBytesFullyAt(0, buffers).block();

		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytesFullyAt(toWrite.length, List.of(ByteBuffer.allocate(1)))).expectError(EOFException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, List.of())).verifyComplete();
		StepVerifier.create(io.writeBytesFullyAt(0, List.of(ByteBuffer.allocate(0)))).verifyComplete();
		StepVerifier.create(io.writeBytesFullyAt(0, List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0)))).verifyComplete();

		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		io.close().block();
		StepVerifier.create(io.writeBytesFullyAt(0, List.of(ByteBuffer.allocate(1)))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, List.of())).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, List.of(ByteBuffer.allocate(0)))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesFullyAt(0, List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0)))).expectError(ClosedChannelException.class).verify();
	}
	
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeByteBufferAt(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable.Seekable io = ioTuple.getIo();

		StepVerifier.create(io.writeBytesAt(0, (ByteBuffer) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytesAt(-1, ByteBuffer.allocate(1))).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, ByteBuffer.allocate(0))).expectNext(0).verifyComplete();

		int step = toWrite.length > 10000 ? 1111 : 3;
		byte[] b;
		if (io instanceof ReactiveIO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new byte[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		for (int i = 0; i < toWrite.length;) {
			int l = Math.min(step, b.length - i);
			int nb = io.writeBytesAt(i, ByteBuffer.wrap(b, i, l)).block();
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytesAt(toWrite.length, ByteBuffer.allocate(1))).expectNext(-1).verifyComplete();
		StepVerifier.create(io.writeBytesAt(toWrite.length, ByteBuffer.allocate(0))).expectNext(0).verifyComplete();

		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			int done = 0;
			while (done < additional.length) {
				int nb = io.writeBytesAt(toWrite.length - before + done, ByteBuffer.wrap(additional, done, additional.length - done)).block();
				assertThat(nb).isPositive();
				done += nb;
			}
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional)));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + additional.length + 69).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				done = 0;
				while (done < additional2.length) {
					int nb = io.writeBytesAt(toWrite.length - before + additional.length + done, ByteBuffer.wrap(additional2, done, additional2.length - done)).block();
					assertThat(nb).isPositive();
					done += nb;
				}
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional), ByteBuffer.wrap(additional2)));
				r.setSize(toWrite.length).block();
				io.writeBytesFullyAt(toWrite.length - before, toWrite, toWrite.length - before, before).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof ReactiveIO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71).block();
			byte[] additional = BytesIOTestUtils.generateContent(71);
			int nb = io.writeBytesAt(toWrite.length, ByteBuffer.wrap(additional)).block();
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close().block();
		StepVerifier.create(io.writeBytesAt(0, ByteBuffer.allocate(0))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, (ByteBuffer) null)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(-1, ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(toWrite.length + 1, ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(toWrite.length, ByteBuffer.allocate(1))).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeByteArrayAt(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable.Seekable io = ioTuple.getIo();

		StepVerifier.create(io.writeBytesAt(0, (byte[]) null)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytesAt(-1, new byte[1])).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, new byte[0])).expectNext(0).verifyComplete();

		int step = toWrite.length > 10000 ? 1111 : 3;
		byte[] b;
		if (io instanceof ReactiveIO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new byte[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		for (int i = 0; i < toWrite.length;) {
			int l = Math.min(step, b.length - i);
			byte[] bb = new byte[l];
			System.arraycopy(b, i, bb, 0, l);
			int nb = io.writeBytesAt(i, bb).block();
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytesAt(toWrite.length, new byte[1])).expectNext(-1).verifyComplete();
		StepVerifier.create(io.writeBytesAt(0, new byte[0])).expectNext(0).verifyComplete();

		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int nb = io.writeBytesAt(toWrite.length, additional).block();
			assertThat(nb).isPositive();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length + nb + 69).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				int nb2 = io.writeBytesAt(toWrite.length + nb, additional2).block();
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
			int nb = io.writeBytesAt(toWrite.length, additional).block();
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close().block();
		StepVerifier.create(io.writeBytesAt(0, new byte[0])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, new byte[1])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, (byte[]) null)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(-1, new byte[1])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(toWrite.length + 1, new byte[1])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(toWrite.length, new byte[1])).expectError(ClosedChannelException.class).verify();
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeByteArrayAtWithOffset(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable.Seekable io = ioTuple.getIo();

		StepVerifier.create(io.writeBytesAt(0, (byte[]) null, 0, 1)).expectError(NullPointerException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, new byte[10], -1, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, new byte[10], 2, -1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, new byte[10], 11, 1)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, new byte[10], 0, 11)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, new byte[10], 7, 5)).expectError(LimitExceededException.class).verify();
		StepVerifier.create(io.writeBytesAt(-1, new byte[10], 1, 1)).expectError(NegativeValueException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, new byte[10], 7, 0)).expectNext(0).verifyComplete();

		int step = toWrite.length > 10000 ? 1111 : 3;
		byte[] b;
		if (io instanceof ReactiveIO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new byte[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		for (int i = 0; i < toWrite.length;) {
			int l = Math.min(step, b.length - i);
			int nb = io.writeBytesAt(i, b, i, l).block();
			assertThat(nb).as("Write up to " + l + " at " + i + "/" + toWrite.length).isPositive();
			i += nb;
		}

		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeBytesAt(toWrite.length, new byte[1], 0, 1)).expectNext(-1).verifyComplete();
		StepVerifier.create(io.writeBytesAt(toWrite.length, new byte[0], 0, 0)).expectNext(0).verifyComplete();
		StepVerifier.create(io.writeBytesAt(toWrite.length, new byte[1], 0, 0)).expectNext(0).verifyComplete();

		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			int done = 0;
			while (done < additional.length) {
				int nb = io.writeBytesAt(toWrite.length - before + done, additional, done, additional.length - done).block();
				assertThat(nb).isPositive();
				done += nb;
			}
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional)));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + additional.length + 69).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				done = 0;
				while (done < additional2.length) {
					int nb = io.writeBytesAt(toWrite.length - before + additional.length + done, additional2, done, additional2.length - done).block();
					assertThat(nb).isPositive();
					done += nb;
				}
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional), ByteBuffer.wrap(additional2)));
				r.setSize(toWrite.length).block();
				io.writeBytesFullyAt(toWrite.length - before, toWrite, toWrite.length - before, before).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof ReactiveIO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71).block();
			byte[] additional = BytesIOTestUtils.generateContent(100);
			int nb = io.writeBytesAt(toWrite.length, additional, 20, 71).block();
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 20, nb)));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close().block();
		StepVerifier.create(io.writeBytesAt(0, new byte[0], 0, 0)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, new byte[1], 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, (byte[]) null, 0, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, new byte[10], -1, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, new byte[10], 11, 1)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, new byte[10], 0, 11)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(0, new byte[10], 7, 5)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(toWrite.length + 1, new byte[1])).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.writeBytesAt(toWrite.length, new byte[1])).expectError(ClosedChannelException.class).verify();
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(SmallRandomContentTestCasesProvider.class)
	void writeByteAt(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		ReactiveBytesIO.Writable.Seekable io = ioTuple.getIo();

		for (int pos = toWrite.length - 1; pos >= 0; pos--)
			io.writeByteAt(pos, toWrite[pos]).block();
		
		if (!(io instanceof ReactiveIO.Writable.Appendable))
			StepVerifier.create(io.writeByteAt(toWrite.length, (byte) 0)).expectError(EOFException.class).verify();

		io.flush().block();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof ReactiveIO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			for (int i = additional.length - 1; i >= 0; --i)
				io.writeByteAt(toWrite.length + i, additional[i]).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof ReactiveIO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + 69).block();
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				for (int i = additional2.length - 1; i >= 0; --i)
					io.writeByteAt(toWrite.length + additional.length + i, additional2[i]).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length).block();
				io.flush().block();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof ReactiveIO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71).block();
			byte[] additional = BytesIOTestUtils.generateContent(71);
			for (int i = additional.length - 1; i >= 0; --i)
				io.writeByteAt(toWrite.length + i, additional[i]).block();
			StepVerifier.create(io.writeByteAt(toWrite.length + additional.length, (byte) 0)).expectError(EOFException.class).verify();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length).block();
			io.flush().block();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close().block();
		StepVerifier.create(io.writeByteAt(0, (byte) 0)).expectError(ClosedChannelException.class).verify();
	}
	
}
