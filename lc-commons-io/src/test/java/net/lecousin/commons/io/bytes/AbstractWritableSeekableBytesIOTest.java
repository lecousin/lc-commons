package net.lecousin.commons.io.bytes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import net.lecousin.commons.io.AbstractSeekableIOTest;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.LcByteBufferUtils;
import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.RandomContentTestCasesProvider;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.SmallRandomContentTestCasesProvider;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractWritableSeekableBytesIOTest implements TestCasesProvider<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>> {

	protected abstract void checkWrittenData(BytesIO.Writable.Seekable io, Object object, byte[] expected) throws Exception;
	
	@Nested
	public class AsWritableBytesIO extends AbstractWritableBytesIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<?,?>>> getTestCases() {
			return AbstractWritableSeekableBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, WritableTestCase<?,?>>) (size) -> {
					WritableTestCase<? extends BytesIO.Writable.Seekable, ?> wtc = tc.getArgumentProvider().apply(size);
					BytesIO.Writable.Seekable seekable = wtc.getIo();
					BytesIO.Writable writable = convert(seekable);
					return new WritableTestCase<>(writable, Pair.of(seekable, wtc.getObject()));
				}))
				.toList();
		}
		
		@SuppressWarnings("unchecked")
		private <U extends BytesIO.Writable.Seekable & IO.Writable.Resizable> BytesIO.Writable convert(BytesIO.Writable.Seekable seekable) {
			if (seekable instanceof IO.Writable.Resizable) {
				return BytesIOView.Writable.Seekable.Resizable.of((U) seekable);
			}
			return seekable.asWritableBytesIO();
		}
		
		@Override
		protected void checkWrittenData(BytesIO.Writable io, Object object, byte[] expected) throws Exception {
			@SuppressWarnings("unchecked")
			Pair<BytesIO.Writable.Seekable, Object> pair = (Pair<BytesIO.Writable.Seekable, Object>) object;
			AbstractWritableSeekableBytesIOTest.this.checkWrittenData(pair.getLeft(), pair.getRight(), expected);
		}
	}
	
	@Nested
	public class AsSeekableIO extends AbstractSeekableIOTest {
		@Override
		public List<? extends TestCase<Integer, IO.Seekable>> getTestCases() {
			return AbstractWritableSeekableBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, IO.Seekable>) (size) -> tc.getArgumentProvider().apply(size).getIo()))
				.toList();
		}
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyAtByteArrayOneShot(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable.Seekable io = ioTuple.getIo();
		
		assertThrows(NullPointerException.class, () -> io.writeBytesFullyAt(0, (byte[]) null));
		assertThrows(NegativeValueException.class, () -> io.writeBytesFullyAt(-1, new byte[0]));
		io.writeBytesFullyAt(0, new byte[0]);

		io.writeBytesFullyAt(0, toWrite);
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeBytesFullyAt(toWrite.length, new byte[1]));
		io.writeBytesFullyAt(toWrite.length, new byte[0]);
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			io.writeBytesFullyAt(toWrite.length - before, additional);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + additional.length + 69);
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				io.writeBytesFullyAt(toWrite.length - before + additional.length, additional2);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional), ByteBuffer.wrap(additional2)));
				r.setSize(toWrite.length);
				io.writeBytesFullyAt(toWrite.length - before, toWrite, toWrite.length - before, before);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			byte[] additional = BytesIOTestUtils.generateContent(71);
			io.writeBytesFullyAt(toWrite.length, additional);
			assertThrows(EOFException.class, () -> io.writeBytesFullyAt(toWrite.length + additional.length, new byte[1]));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, (byte[]) null));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, new byte[0]));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, new byte[1]));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(-1, new byte[1]));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyAtByteArrayWithOffset(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable.Seekable io = ioTuple.getIo();
		
		assertThrows(NullPointerException.class, () -> io.writeBytesFullyAt(0, (byte[]) null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.writeBytesFullyAt(0, new byte[10], -1, 1));
		assertThrows(NegativeValueException.class, () -> io.writeBytesFullyAt(0, new byte[10], 3, -1));
		assertThrows(LimitExceededException.class, () -> io.writeBytesFullyAt(0, new byte[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.writeBytesFullyAt(0, new byte[10], 11, 1));
		assertThrows(NegativeValueException.class, () -> io.writeBytesFullyAt(-1, new byte[10], 0, 1));
		io.writeBytesFullyAt(0, new byte[10], 7, 0);

		int step = toWrite.length > 10000 ? 1111 : 3;
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			io.writeBytesFullyAt(i, toWrite, i, l);
		}
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeBytesFullyAt(toWrite.length, new byte[1], 0, 1));
		io.writeBytesFullyAt(toWrite.length, new byte[1], 0, 0);
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			io.writeBytesFullyAt(toWrite.length - before, additional, 7, 13);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional, 7, 13)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + 13 + 32);
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				io.writeBytesFullyAt(toWrite.length - before + 13, additional2, 17, 32);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional, 7, 13), ByteBuffer.wrap(additional2, 17, 32)));
				r.setSize(toWrite.length);
				io.writeBytesFullyAt(toWrite.length - before, toWrite, toWrite.length - before, before);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			byte[] additional = BytesIOTestUtils.generateContent(100);
			io.writeBytesFullyAt(toWrite.length, additional, 16, 71);
			assertThrows(EOFException.class, () -> io.writeBytesFullyAt(toWrite.length + additional.length, new byte[10], 3, 1));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 16, 71)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, toWrite, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, (byte[]) null, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, new byte[10], -1, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, new byte[10], 3, -1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, new byte[10], 0, 11));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, new byte[10], 11, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, new byte[10], 7, 0));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(-1, new byte[10], 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, new byte[10], 0, 1));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyAtByteBufferOneShot(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable.Seekable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeBytesFullyAt(0, (ByteBuffer) null));
		assertThrows(NegativeValueException.class, () -> io.writeBytesFullyAt(-1, ByteBuffer.allocate(0)));
		io.writeBytesFullyAt(0, ByteBuffer.allocate(0));
		
		io.writeBytesFullyAt(0, ByteBuffer.wrap(toWrite));
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeBytesFullyAt(toWrite.length, ByteBuffer.allocate(1)));
		io.writeBytesFullyAt(toWrite.length, ByteBuffer.allocate(0));
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			io.writeBytesFullyAt(toWrite.length - before, ByteBuffer.wrap(additional));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + additional.length + 69);
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				io.writeBytesFullyAt(toWrite.length - before + additional.length, ByteBuffer.wrap(additional2));
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional), ByteBuffer.wrap(additional2)));
				r.setSize(toWrite.length);
				io.writeBytesFullyAt(toWrite.length - before, ByteBuffer.wrap(toWrite, toWrite.length - before, before));
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			byte[] additional = BytesIOTestUtils.generateContent(71);
			io.writeBytesFullyAt(toWrite.length, ByteBuffer.wrap(additional));
			assertThrows(EOFException.class, () -> io.writeBytesFullyAt(toWrite.length + additional.length, ByteBuffer.allocate(1)));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, (ByteBuffer) null));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, ByteBuffer.allocate(0)));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, ByteBuffer.allocate(1)));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(-1, ByteBuffer.allocate(1)));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyAtByteBufferSmallParts(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable.Seekable io = ioTuple.getIo();

		int step = toWrite.length > 10000 ? 1111 : 3;
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			io.writeBytesFullyAt(i, ByteBuffer.wrap(toWrite, i, l));
		}
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, ByteBuffer.allocate(0)));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, ByteBuffer.allocate(1)));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyAtBufferList(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable.Seekable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeBytesFullyAt(0, (List<ByteBuffer>) null));
		assertThrows(NegativeValueException.class, () -> io.writeBytesFullyAt(-1, List.of()));
		io.writeBytesFullyAt(0, List.of());
		io.writeBytesFullyAt(0, List.of(ByteBuffer.allocate(0)));
		io.writeBytesFullyAt(0, List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0)));

		List<ByteBuffer> buffers = new LinkedList<>();
		int step = toWrite.length > 10000 ? 1111 : 3;
		buffers.add(ByteBuffer.allocate(0));
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			buffers.add(ByteBuffer.wrap(toWrite, i, l));
			buffers.add(ByteBuffer.allocate(0));
		}
		io.writeBytesFullyAt(0, buffers);

		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeBytesFullyAt(toWrite.length, List.of(ByteBuffer.allocate(1))));
		io.writeBytesFullyAt(toWrite.length, List.of());
		io.writeBytesFullyAt(toWrite.length, List.of(ByteBuffer.allocate(0)));
		io.writeBytesFullyAt(toWrite.length, List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0)));

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, List.of(ByteBuffer.allocate(1))));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, List.of()));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, List.of(ByteBuffer.allocate(0))));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFullyAt(0, List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0))));
	}
	
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeByteBufferAt(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable.Seekable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeBytesAt(0, (ByteBuffer) null));
		assertThrows(NegativeValueException.class, () -> io.writeBytesAt(-1, ByteBuffer.allocate(1)));
		assertThat(io.writeBytesAt(0, ByteBuffer.allocate(0))).isZero();

		int step = toWrite.length > 10000 ? 1111 : 3;
		byte[] b;
		if (io instanceof IO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new byte[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		for (int i = 0; i < toWrite.length;) {
			int l = Math.min(step, b.length - i);
			int nb = io.writeBytesAt(i, ByteBuffer.wrap(b, i, l));
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeBytesAt(toWrite.length, ByteBuffer.allocate(1))).isEqualTo(-1);
		assertThat(io.writeBytesAt(toWrite.length, ByteBuffer.allocate(0))).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			int done = 0;
			while (done < additional.length) {
				int nb = io.writeBytesAt(toWrite.length - before + done, ByteBuffer.wrap(additional, done, additional.length - done));
				assertThat(nb).isPositive();
				done += nb;
			}
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + additional.length + 69);
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				done = 0;
				while (done < additional2.length) {
					int nb = io.writeBytesAt(toWrite.length - before + additional.length + done, ByteBuffer.wrap(additional2, done, additional2.length - done));
					assertThat(nb).isPositive();
					done += nb;
				}
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional), ByteBuffer.wrap(additional2)));
				r.setSize(toWrite.length);
				io.writeBytesFullyAt(toWrite.length - before, toWrite, toWrite.length - before, before);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			byte[] additional = BytesIOTestUtils.generateContent(71);
			int nb = io.writeBytesAt(toWrite.length, ByteBuffer.wrap(additional));
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, ByteBuffer.allocate(1)));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, ByteBuffer.allocate(0)));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeByteBufferDirectAt(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable.Seekable io = ioTuple.getIo();

		assertThrows(NegativeValueException.class, () -> io.writeBytesAt(-1, ByteBuffer.allocateDirect(1)));
		assertThat(io.writeBytesAt(0, ByteBuffer.allocateDirect(0))).isZero();

		int step = toWrite.length > 10000 ? 1111 : 3;
		byte[] b;
		if (io instanceof IO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new byte[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		for (int i = 0; i < toWrite.length;) {
			int l = Math.min(step, b.length - i);
			ByteBuffer bb = ByteBuffer.allocateDirect(l);
			bb.put(b, i, l);
			int nb = io.writeBytesAt(i, bb.flip());
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeBytesAt(toWrite.length, ByteBuffer.allocateDirect(1))).isEqualTo(-1);
		assertThat(io.writeBytesAt(toWrite.length, ByteBuffer.allocateDirect(0))).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, ByteBuffer.allocateDirect(1)));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, ByteBuffer.allocateDirect(0)));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeByteArrayAt(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable.Seekable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeBytesAt(0, (byte[]) null));
		assertThrows(NegativeValueException.class, () -> io.writeBytesAt(-1, new byte[1]));
		assertThat(io.writeBytesAt(0, new byte[0])).isZero();

		int step = toWrite.length > 10000 ? 1111 : 3;
		byte[] b;
		if (io instanceof IO.Writable.Appendable)
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
			int nb = io.writeBytesAt(i, bb);
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeBytesAt(toWrite.length, new byte[1])).isEqualTo(-1);
		assertThat(io.writeBytesAt(toWrite.length, new byte[0])).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int nb = io.writeBytesAt(toWrite.length, additional);
			assertThat(nb).isPositive();
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + nb + 69);
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				int nb2 = io.writeBytesAt(toWrite.length + nb, additional2);
				assertThat(nb2).isPositive();
				r.setSize(toWrite.length + nb + nb2);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb), ByteBuffer.wrap(additional2, 0, nb2)));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			byte[] additional = BytesIOTestUtils.generateContent(71);
			int nb = io.writeBytesAt(toWrite.length, additional);
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, new byte[0]));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, new byte[1]));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeByteArrayAtWithOffset(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable.Seekable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeBytesAt(0, (byte[]) null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.writeBytesAt(0, new byte[10], -1, 1));
		assertThrows(NegativeValueException.class, () -> io.writeBytesAt(0, new byte[10], 4, -2));
		assertThrows(LimitExceededException.class, () -> io.writeBytesAt(0, new byte[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.writeBytesAt(0, new byte[10], 11, 1));
		assertThrows(NegativeValueException.class, () -> io.writeBytesAt(-1, new byte[10], 1, 1));
		assertThat(io.writeBytesAt(0, new byte[10], 7, 0)).isZero();

		int step = toWrite.length > 10000 ? 1111 : 3;
		byte[] b;
		if (io instanceof IO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new byte[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		for (int i = 0; i < toWrite.length;) {
			int l = Math.min(step, b.length - i);
			int nb = io.writeBytesAt(i, b, i, l);
			assertThat(nb).as("Write up to " + l + " at " + i + "/" + toWrite.length).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeBytesAt(toWrite.length, new byte[1], 0, 1)).isEqualTo(-1);
		assertThat(io.writeBytesAt(toWrite.length, new byte[0], 0, 0)).isZero();
		assertThat(io.writeBytesAt(toWrite.length, new byte[1], 0, 0)).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			int done = 0;
			while (done < additional.length) {
				int nb = io.writeBytesAt(toWrite.length - before + done, additional, done, additional.length - done);
				assertThat(nb).isPositive();
				done += nb;
			}
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + additional.length + 69);
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				done = 0;
				while (done < additional2.length) {
					int nb = io.writeBytesAt(toWrite.length - before + additional.length + done, additional2, done, additional2.length - done);
					assertThat(nb).isPositive();
					done += nb;
				}
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite, 0, toWrite.length - before), ByteBuffer.wrap(additional), ByteBuffer.wrap(additional2)));
				r.setSize(toWrite.length);
				io.writeBytesFullyAt(toWrite.length - before, toWrite, toWrite.length - before, before);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			byte[] additional = BytesIOTestUtils.generateContent(100);
			int nb = io.writeBytesAt(toWrite.length, additional, 20, 71);
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 20, nb)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, (byte[]) null, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, new byte[10], -1, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, new byte[10], 2, -1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, new byte[10], 0, 11));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, new byte[10], 11, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, new byte[10], 7, 0));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, new byte[1], 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, new byte[0], 0, 0));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesAt(0, new byte[1], 0, 0));
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(SmallRandomContentTestCasesProvider.class)
	void writeByteAt(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable.Seekable io = ioTuple.getIo();

		for (int i = 0; i < toWrite.length; ++i)
			io.writeByteAt(i, toWrite[i]);
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeByteAt(toWrite.length, (byte) 0));

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			for (int i = additional.length - 1; i >= 0; --i)
				io.writeByteAt(toWrite.length + i, additional[i]);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + 69);
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				for (int i = additional2.length - 1; i >= 0; --i)
					io.writeByteAt(toWrite.length + additional.length + i, additional2[i]);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			byte[] additional = BytesIOTestUtils.generateContent(71);
			for (int i = additional.length - 1; i >= 0; --i)
				io.writeByteAt(toWrite.length + i, additional[i]);
			assertThrows(EOFException.class, () -> io.writeByteAt(toWrite.length + additional.length, (byte) 0));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeByteAt(0, (byte) 0));
	}
	
}
