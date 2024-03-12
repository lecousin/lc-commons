package net.lecousin.commons.io.chars;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
import net.lecousin.commons.io.chars.CharsIOTestUtils.RandomContentTestCasesProvider;
import net.lecousin.commons.io.chars.CharsIOTestUtils.RandomContentWithBufferSizeTestCasesProvider;
import net.lecousin.commons.io.chars.CharsIOTestUtils.SmallRandomContentTestCasesProvider;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractWritableCharsIOTest implements TestCasesProvider<Integer, AbstractWritableCharsIOTest.WritableTestCase<?, ?>> {

	@Data
	@AllArgsConstructor
	public static class WritableTestCase<T extends CharsIO.Writable, O> {
		private T io;
		private O object;
		
		public WritableTestCase(T io) {
			this(io, null);
		}
	}
	
	protected abstract void checkWrittenData(CharsIO.Writable io, Object object, char[] expected) throws Exception;
	
	@Nested
	public class AsWritableIO extends AbstractWritableIOTest {
		@Override
		public List<? extends TestCase<Void, IO.Writable>> getTestCases() {
			return AbstractWritableCharsIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), (Supplier<IO.Writable>) () -> tc.getArgumentProvider().apply(1).getIo()))
				.toList();
		}
	}
	
	@Nested
	public class AsResizableIO extends AbstractWritableResizableIOTest {
		@Override
		public List<? extends TestCase<Integer, IO.Writable.Resizable>> getTestCases() {
			return AbstractWritableCharsIOTest.this.getTestCases().stream()
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
	void writeCharsFullyCharArrayOneShot(String displayName, char[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable io = ioTuple.getIo();
		
		assertThrows(NullPointerException.class, () -> io.writeCharsFully((char[]) null));
		io.writeCharsFully(new char[0]);

		io.writeCharsFully(toWrite);
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeCharsFully(new char[1]));
		io.writeCharsFully(new char[0]);
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			io.writeCharsFully(additional);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + 69);
				char[] additional2 = CharsIOTestUtils.generateContent(69);
				io.writeCharsFully(additional2);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			char[] additional = CharsIOTestUtils.generateContent(71);
			io.writeCharsFully(additional);
			assertThrows(EOFException.class, () -> io.writeCharsFully(new char[1]));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully((char[]) null));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(new char[0]));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(new char[1]));
		assertThrows(ClosedChannelException.class, () -> io.flush());
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeCharsFullyCharArrayWithOffset(String displayName, char[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable io = ioTuple.getIo();
		
		assertThrows(NullPointerException.class, () -> io.writeCharsFully((char[]) null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.writeCharsFully(new char[10], -1, 1));
		assertThrows(NegativeValueException.class, () -> io.writeCharsFully(new char[10], 3, -1));
		assertThrows(LimitExceededException.class, () -> io.writeCharsFully(new char[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.writeCharsFully(new char[10], 11, 1));
		io.writeCharsFully(new char[10], 7, 0);

		int step = toWrite.length > 10000 ? 1111 : 3;
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			io.writeCharsFully(toWrite, i, l);
		}
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeCharsFully(new char[1], 0, 1));
		io.writeCharsFully(new char[1], 0, 0);
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			io.writeCharsFully(additional, 7, 13);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 7, 13)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + 13 + 32);
				char[] additional2 = CharsIOTestUtils.generateContent(69);
				io.writeCharsFully(additional2, 17, 32);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 7, 13), CharBuffer.wrap(additional2, 17, 32)));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			char[] additional = CharsIOTestUtils.generateContent(100);
			io.writeCharsFully(additional, 16, 71);
			assertThrows(EOFException.class, () -> io.writeCharsFully(new char[10], 3, 1));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 16, 71)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(toWrite, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully((char[]) null, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(new char[10], -1, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(new char[10], 3, -1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(new char[10], 0, 11));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(new char[10], 11, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(new char[10], 7, 0));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeCharsFullyCharBufferOneShot(String displayName, char[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeCharsFully((CharBuffer) null));
		io.writeCharsFully(CharBuffer.allocate(0));
		
		io.writeCharsFully(CharBuffer.wrap(toWrite));
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeCharsFully(CharBuffer.allocate(1)));
		io.writeCharsFully(CharBuffer.allocate(0));
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			io.writeCharsFully(CharBuffer.wrap(additional));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + 69);
				char[] additional2 = CharsIOTestUtils.generateContent(69);
				io.writeCharsFully(CharBuffer.wrap(additional2));
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			char[] additional = CharsIOTestUtils.generateContent(71);
			io.writeCharsFully(CharBuffer.wrap(additional));
			assertThrows(EOFException.class, () -> io.writeCharsFully(CharBuffer.allocate(1)));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully((CharBuffer) null));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(CharBuffer.allocate(0)));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(CharBuffer.allocate(1)));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeCharsFullyCharBufferSmallParts(String displayName, char[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable io = ioTuple.getIo();

		int step = toWrite.length > 10000 ? 1111 : 3;
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			io.writeCharsFully(CharBuffer.wrap(toWrite, i, l));
		}
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(CharBuffer.allocate(0)));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(CharBuffer.allocate(1)));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeFullyCharBufferList(String displayName, char[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeCharsFully((List<CharBuffer>) null));
		io.writeCharsFully(List.of());
		io.writeCharsFully(List.of(CharBuffer.allocate(0)));
		io.writeCharsFully(List.of(CharBuffer.allocate(0), CharBuffer.allocate(0)));

		List<CharBuffer> buffers = new LinkedList<>();
		int step = toWrite.length > 10000 ? 1111 : 3;
		buffers.add(CharBuffer.allocate(0));
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			buffers.add(CharBuffer.wrap(toWrite, i, l));
			buffers.add(CharBuffer.allocate(0));
		}
		io.writeCharsFully(buffers);

		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeCharsFully(List.of(CharBuffer.allocate(1))));
		io.writeCharsFully(List.of());
		io.writeCharsFully(List.of(CharBuffer.allocate(0)));
		io.writeCharsFully(List.of(CharBuffer.allocate(0), CharBuffer.allocate(0)));

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(List.of(CharBuffer.allocate(1))));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(List.of()));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(List.of(CharBuffer.allocate(0))));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFully(List.of(CharBuffer.allocate(0), CharBuffer.allocate(0))));
	}
	
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void writeCharBuffer(String displayName, char[] toWrite, int bufferSize, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeChars((CharBuffer) null));
		assertThat(io.writeChars(CharBuffer.allocate(0))).isZero();

		char[] b;
		if (io instanceof IO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new char[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		boolean smallStep = true;
		for (int i = 0; i < toWrite.length;) {
			int step = smallStep ? 3 : bufferSize;
			smallStep = !smallStep;
			int l = Math.min(step, b.length - i);
			int nb = io.writeChars(CharBuffer.wrap(b, i, l));
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeChars(CharBuffer.allocate(1))).isEqualTo(-1);
		assertThat(io.writeChars(CharBuffer.allocate(0))).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			int nb = io.writeChars(CharBuffer.wrap(additional));
			assertThat(nb).isPositive();
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 0, nb)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + nb + 69);
				char[] additional2 = CharsIOTestUtils.generateContent(69);
				int nb2 = io.writeChars(CharBuffer.wrap(additional2));
				assertThat(nb2).isPositive();
				r.setSize(toWrite.length + nb + nb2);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 0, nb), CharBuffer.wrap(additional2, 0, nb2)));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			char[] additional = CharsIOTestUtils.generateContent(71);
			int nb = io.writeChars(CharBuffer.wrap(additional));
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 0, nb)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeChars(CharBuffer.allocate(1)));
		assertThrows(ClosedChannelException.class, () -> io.writeChars(CharBuffer.allocate(0)));
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void writeCharBufferDirect(String displayName, char[] toWrite, int bufferSize, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable io = ioTuple.getIo();

		assertThat(io.writeChars(ByteBuffer.allocateDirect(0).asCharBuffer())).isZero();

		char[] b;
		if (io instanceof IO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new char[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		boolean smallStep = true;
		for (int i = 0; i < toWrite.length;) {
			int step = smallStep ? 3 : bufferSize;
			smallStep = !smallStep;
			int l = Math.min(step, b.length - i);
			CharBuffer bb = ByteBuffer.allocateDirect(l * 2).asCharBuffer();
			bb.put(b, i, l);
			int nb = io.writeChars(bb.flip());
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeChars(ByteBuffer.allocateDirect(2).asCharBuffer())).isEqualTo(-1);
		assertThat(io.writeChars(ByteBuffer.allocateDirect(0).asCharBuffer())).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeChars(ByteBuffer.allocateDirect(2).asCharBuffer()));
		assertThrows(ClosedChannelException.class, () -> io.writeChars(ByteBuffer.allocateDirect(0).asCharBuffer()));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void writeCharArray(String displayName, char[] toWrite, int bufferSize, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeChars((char[]) null));
		assertThat(io.writeChars(new char[0])).isZero();

		char[] b;
		if (io instanceof IO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new char[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		boolean smallStep = true;
		for (int i = 0; i < toWrite.length;) {
			int step = smallStep ? 3 : bufferSize;
			smallStep = !smallStep;
			int l = Math.min(step, b.length - i);
			char[] bb = new char[l];
			System.arraycopy(b, i, bb, 0, l);
			int nb = io.writeChars(bb);
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeChars(new char[1])).isEqualTo(-1);
		assertThat(io.writeChars(new char[0])).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			int nb = io.writeChars(additional);
			assertThat(nb).isPositive();
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 0, nb)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + nb + 69);
				char[] additional2 = CharsIOTestUtils.generateContent(69);
				int nb2 = io.writeChars(additional2);
				assertThat(nb2).isPositive();
				r.setSize(toWrite.length + nb + nb2);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 0, nb), CharBuffer.wrap(additional2, 0, nb2)));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			char[] additional = CharsIOTestUtils.generateContent(71);
			int nb = io.writeChars(additional);
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 0, nb)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeChars(new char[0]));
		assertThrows(ClosedChannelException.class, () -> io.writeChars(new char[1]));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeTestCasesProvider.class)
	void writeCharArrayWithOffset(String displayName, char[] toWrite, int bufferSize, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeChars((char[]) null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.writeChars(new char[10], -1, 1));
		assertThrows(NegativeValueException.class, () -> io.writeChars(new char[10], 4, -2));
		assertThrows(LimitExceededException.class, () -> io.writeChars(new char[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.writeChars(new char[10], 11, 1));
		assertThat(io.writeChars(new char[10], 7, 0)).isZero();

		char[] b;
		if (io instanceof IO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new char[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		boolean smallStep = true;
		for (int i = 0; i < toWrite.length;) {
			int step = smallStep ? 3 : bufferSize;
			smallStep = !smallStep;
			int l = Math.min(step, b.length - i);
			int nb = io.writeChars(b, i, l);
			assertThat(nb).as("Write up to " + l + " at " + i + "/" + toWrite.length).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeChars(new char[1], 0, 1)).isEqualTo(-1);
		assertThat(io.writeChars(new char[0], 0, 0)).isZero();
		assertThat(io.writeChars(new char[1], 0, 0)).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			int nb = io.writeChars(additional, 3, 19);
			assertThat(nb).isPositive();
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 3, nb)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + nb + 69);
				char[] additional2 = CharsIOTestUtils.generateContent(100);
				int nb2 = io.writeChars(CharBuffer.wrap(additional2, 6, 69));
				assertThat(nb2).isPositive();
				r.setSize(toWrite.length + nb + nb2);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 3, nb), CharBuffer.wrap(additional2, 6, nb2)));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			char[] additional = CharsIOTestUtils.generateContent(100);
			int nb = io.writeChars(additional, 20, 71);
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 20, nb)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeChars((char[]) null, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeChars(new char[10], -1, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeChars(new char[10], 2, -1));
		assertThrows(ClosedChannelException.class, () -> io.writeChars(new char[10], 0, 11));
		assertThrows(ClosedChannelException.class, () -> io.writeChars(new char[10], 11, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeChars(new char[10], 7, 0));
		assertThrows(ClosedChannelException.class, () -> io.writeChars(new char[1], 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeChars(new char[0], 0, 0));
		assertThrows(ClosedChannelException.class, () -> io.writeChars(new char[1], 0, 0));
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(SmallRandomContentTestCasesProvider.class)
	void writeChar(String displayName, char[] toWrite, Function<Integer, WritableTestCase<?, ?>> ioSupplier) throws Exception {
		WritableTestCase<?, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable io = ioTuple.getIo();

		for (int i = 0; i < toWrite.length; ++i)
			io.writeChar(toWrite[i]);
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeChar((char) 0));

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			for (int i = 0; i < additional.length; ++i) {
				io.writeChar(additional[i]);
				if (io instanceof IO.KnownSize ks) assertThat(ks.size()).as("Size after adding " + (i+1) + " chars").isEqualTo(toWrite.length + i + 1);
			}
			if (io instanceof IO.Seekable s) assertThat(s.position()).isEqualTo(toWrite.length + additional.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + 69);
				char[] additional2 = CharsIOTestUtils.generateContent(69);
				for (int i = 0; i < additional2.length; ++i)
					io.writeChar(additional2[i]);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			char[] additional = CharsIOTestUtils.generateContent(71);
			for (int i = 0; i < additional.length; ++i)
				io.writeChar(additional[i]);
			assertThrows(EOFException.class, () -> io.writeChar((char) 0));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeChar((char) 0));
	}
	
}
