package net.lecousin.commons.io.bytes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import net.lecousin.commons.io.AbstractWritableIOTest;
import net.lecousin.commons.io.AbstractWritableResizableIOTest;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.LcByteBufferUtils;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.RandomContentTestCasesProvider;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.SmallRandomContentTestCasesProvider;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractWritableBytesIOTest implements TestCasesProvider<Integer, AbstractWritableBytesIOTest.WritableTestCase<?, ?>> {

	@Data
	@AllArgsConstructor
	public static class WritableTestCase<T extends BytesIO.Writable, O> {
		private T io;
		private O object;
		
		public WritableTestCase(T io) {
			this(io, null);
		}
	}
	
	protected abstract void checkWrittenData(BytesIO.Writable io, Object object, byte[] expected) throws Exception;
	
	@Nested
	public class AsWritableIO extends AbstractWritableIOTest {
		@Override
		public List<? extends TestCase<Void, IO.Writable>> getTestCases() {
			return AbstractWritableBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), (Supplier<IO.Writable>) () -> tc.getArgumentProvider().apply(1).getIo()))
				.toList();
		}
	}
	
	@Nested
	public class AsResizableIO extends AbstractWritableResizableIOTest {
		@Override
		public List<? extends TestCase<Integer, IO.Writable.Resizable>> getTestCases() {
			return AbstractWritableBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, IO.Writable.Resizable>) (initialSize) -> {
					WritableTestCase<?,?> wtc = tc.getArgumentProvider().apply(initialSize);
					Assumptions.assumeTrue(wtc.getIo() instanceof IO.Writable.Resizable);
					return (IO.Writable.Resizable) wtc.getIo();
				}))
				.toList();
		}
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyByteArrayOneShot(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable io = ioTuple.getIo();
		
		assertThrows(NullPointerException.class, () -> io.writeBytesFully((byte[]) null));
		io.writeBytesFully(new byte[0]);

		io.writeBytesFully(toWrite);
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeBytesFully(new byte[1]));
		io.writeBytesFully(new byte[0]);
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			io.writeBytesFully(additional);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + 69);
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				io.writeBytesFully(additional2);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			byte[] additional = BytesIOTestUtils.generateContent(71);
			io.writeBytesFully(additional);
			assertThrows(EOFException.class, () -> io.writeBytesFully(new byte[1]));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully((byte[]) null));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(new byte[0]));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(new byte[1]));
		assertThrows(ClosedChannelException.class, () -> io.flush());
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyByteArrayWithOffset(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable io = ioTuple.getIo();
		
		assertThrows(NullPointerException.class, () -> io.writeBytesFully((byte[]) null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.writeBytesFully(new byte[10], -1, 1));
		assertThrows(NegativeValueException.class, () -> io.writeBytesFully(new byte[10], 3, -1));
		assertThrows(LimitExceededException.class, () -> io.writeBytesFully(new byte[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.writeBytesFully(new byte[10], 11, 1));
		io.writeBytesFully(new byte[10], 7, 0);

		int step = toWrite.length > 10000 ? 1111 : 3;
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			io.writeBytesFully(toWrite, i, l);
		}
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeBytesFully(new byte[1], 0, 1));
		io.writeBytesFully(new byte[1], 0, 0);
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			io.writeBytesFully(additional, 7, 13);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 7, 13)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + 13 + 32);
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				io.writeBytesFully(additional2, 17, 32);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 7, 13), ByteBuffer.wrap(additional2, 17, 32)));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			byte[] additional = BytesIOTestUtils.generateContent(100);
			io.writeBytesFully(additional, 16, 71);
			assertThrows(EOFException.class, () -> io.writeBytesFully(new byte[10], 3, 1));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 16, 71)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(toWrite, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully((byte[]) null, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(new byte[10], -1, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(new byte[10], 3, -1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(new byte[10], 0, 11));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(new byte[10], 11, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(new byte[10], 7, 0));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyByteBufferOneShot(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeBytesFully((ByteBuffer) null));
		io.writeBytesFully(ByteBuffer.allocate(0));
		
		io.writeBytesFully(ByteBuffer.wrap(toWrite));
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeBytesFully(ByteBuffer.allocate(1)));
		io.writeBytesFully(ByteBuffer.allocate(0));
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			io.writeBytesFully(ByteBuffer.wrap(additional));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + 69);
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				io.writeBytesFully(ByteBuffer.wrap(additional2));
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			byte[] additional = BytesIOTestUtils.generateContent(71);
			io.writeBytesFully(ByteBuffer.wrap(additional));
			assertThrows(EOFException.class, () -> io.writeBytesFully(ByteBuffer.allocate(1)));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully((ByteBuffer) null));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(ByteBuffer.allocate(0)));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(ByteBuffer.allocate(1)));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeBytesFullyByteBufferSmallParts(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable io = ioTuple.getIo();

		int step = toWrite.length > 10000 ? 1111 : 3;
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			io.writeBytesFully(ByteBuffer.wrap(toWrite, i, l));
		}
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(ByteBuffer.allocate(0)));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(ByteBuffer.allocate(1)));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeFullyByteBufferList(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeBytesFully((List<ByteBuffer>) null));
		io.writeBytesFully(List.of());
		io.writeBytesFully(List.of(ByteBuffer.allocate(0)));
		io.writeBytesFully(List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0)));

		List<ByteBuffer> buffers = new LinkedList<>();
		int step = toWrite.length > 10000 ? 1111 : 3;
		buffers.add(ByteBuffer.allocate(0));
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			buffers.add(ByteBuffer.wrap(toWrite, i, l));
			buffers.add(ByteBuffer.allocate(0));
		}
		io.writeBytesFully(buffers);

		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeBytesFully(List.of(ByteBuffer.allocate(1))));
		io.writeBytesFully(List.of());
		io.writeBytesFully(List.of(ByteBuffer.allocate(0)));
		io.writeBytesFully(List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0)));

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(List.of(ByteBuffer.allocate(1))));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(List.of()));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(List.of(ByteBuffer.allocate(0))));
		assertThrows(ClosedChannelException.class, () -> io.writeBytesFully(List.of(ByteBuffer.allocate(0), ByteBuffer.allocate(0))));
	}
	
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeByteBuffer(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeBytes((ByteBuffer) null));
		assertThat(io.writeBytes(ByteBuffer.allocate(0))).isZero();

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
			int nb = io.writeBytes(ByteBuffer.wrap(b, i, l));
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeBytes(ByteBuffer.allocate(1))).isEqualTo(-1);
		assertThat(io.writeBytes(ByteBuffer.allocate(0))).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int nb = io.writeBytes(ByteBuffer.wrap(additional));
			assertThat(nb).isPositive();
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + nb + 69);
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				int nb2 = io.writeBytes(ByteBuffer.wrap(additional2));
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
			int nb = io.writeBytes(ByteBuffer.wrap(additional));
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytes(ByteBuffer.allocate(1)));
		assertThrows(ClosedChannelException.class, () -> io.writeBytes(ByteBuffer.allocate(0)));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeByteArray(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeBytes((byte[]) null));
		assertThat(io.writeBytes(new byte[0])).isZero();

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
			int nb = io.writeBytes(bb);
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeBytes(new byte[1])).isEqualTo(-1);
		assertThat(io.writeBytes(new byte[0])).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int nb = io.writeBytes(additional);
			assertThat(nb).isPositive();
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + nb + 69);
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				int nb2 = io.writeBytes(additional2);
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
			int nb = io.writeBytes(additional);
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 0, nb)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytes(new byte[0]));
		assertThrows(ClosedChannelException.class, () -> io.writeBytes(new byte[1]));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeByteArrayWithOffset(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeBytes((byte[]) null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.writeBytes(new byte[10], -1, 1));
		assertThrows(NegativeValueException.class, () -> io.writeBytes(new byte[10], 4, -2));
		assertThrows(LimitExceededException.class, () -> io.writeBytes(new byte[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.writeBytes(new byte[10], 11, 1));
		assertThat(io.writeBytes(new byte[10], 7, 0)).isZero();

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
			int nb = io.writeBytes(b, i, l);
			assertThat(nb).as("Write up to " + l + " at " + i + "/" + toWrite.length).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeBytes(new byte[1], 0, 1)).isEqualTo(-1);
		assertThat(io.writeBytes(new byte[0], 0, 0)).isZero();
		assertThat(io.writeBytes(new byte[1], 0, 0)).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			int nb = io.writeBytes(additional, 3, 19);
			assertThat(nb).isPositive();
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 3, nb)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + nb + 69);
				byte[] additional2 = BytesIOTestUtils.generateContent(100);
				int nb2 = io.writeBytes(ByteBuffer.wrap(additional2, 6, 69));
				assertThat(nb2).isPositive();
				r.setSize(toWrite.length + nb + nb2);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 3, nb), ByteBuffer.wrap(additional2, 6, nb2)));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			byte[] additional = BytesIOTestUtils.generateContent(100);
			int nb = io.writeBytes(additional, 20, 71);
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcByteBufferUtils.concat(ByteBuffer.wrap(toWrite), ByteBuffer.wrap(additional, 20, nb)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeBytes((byte[]) null, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytes(new byte[10], -1, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytes(new byte[10], 2, -1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytes(new byte[10], 0, 11));
		assertThrows(ClosedChannelException.class, () -> io.writeBytes(new byte[10], 11, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytes(new byte[10], 7, 0));
		assertThrows(ClosedChannelException.class, () -> io.writeBytes(new byte[1], 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeBytes(new byte[0], 0, 0));
		assertThrows(ClosedChannelException.class, () -> io.writeBytes(new byte[1], 0, 0));
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(SmallRandomContentTestCasesProvider.class)
	void writeByte(String displayName, byte[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesIO.Writable io = ioTuple.getIo();

		for (int i = 0; i < toWrite.length; ++i)
			io.writeByte(toWrite[i]);
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeByte((byte) 0));

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(23);
			for (int i = 0; i < additional.length; ++i)
				io.writeByte(additional[i]);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + 69);
				byte[] additional2 = BytesIOTestUtils.generateContent(69);
				for (int i = 0; i < additional2.length; ++i)
					io.writeByte(additional2[i]);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			byte[] additional = BytesIOTestUtils.generateContent(71);
			for (int i = 0; i < additional.length; ++i)
				io.writeByte(additional[i]);
			assertThrows(EOFException.class, () -> io.writeByte((byte) 0));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeByte((byte) 0));
	}
	
}
