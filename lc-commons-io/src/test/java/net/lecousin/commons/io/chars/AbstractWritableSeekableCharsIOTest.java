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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.collections.LcArrayUtils;
import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractSeekableIOTest;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.chars.AbstractWritableCharsIOTest.WritableTestCase;
import net.lecousin.commons.io.chars.CharsIOTestUtils.RandomContentTestCasesProvider;
import net.lecousin.commons.io.chars.CharsIOTestUtils.SmallRandomContentTestCasesProvider;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractWritableSeekableCharsIOTest implements TestCasesProvider<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>> {

	protected abstract void checkWrittenData(CharsIO.Writable.Seekable io, Object object, char[] expected) throws Exception;
	
	@Nested
	public class AsWritableCharsIO extends AbstractWritableCharsIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<?,?>>> getTestCases() {
			return AbstractWritableSeekableCharsIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, WritableTestCase<?,?>>) (size) -> {
					WritableTestCase<? extends CharsIO.Writable.Seekable, ?> wtc = tc.getArgumentProvider().apply(size);
					CharsIO.Writable.Seekable seekable = wtc.getIo();
					CharsIO.Writable writable = convert(seekable);
					return new WritableTestCase<>(writable, Pair.of(seekable, wtc.getObject()));
				}))
				.toList();
		}
		
		@SuppressWarnings("unchecked")
		private <U extends CharsIO.Writable.Seekable & IO.Writable.Resizable> CharsIO.Writable convert(CharsIO.Writable.Seekable seekable) {
			if (seekable instanceof IO.Writable.Resizable) {
				return CharsIOView.Writable.Seekable.Resizable.of((U) seekable);
			}
			return seekable.asWritableCharsIO();
		}
		
		@Override
		protected void checkWrittenData(CharsIO.Writable io, Object object, char[] expected) throws Exception {
			@SuppressWarnings("unchecked")
			Pair<CharsIO.Writable.Seekable, Object> pair = (Pair<CharsIO.Writable.Seekable, Object>) object;
			AbstractWritableSeekableCharsIOTest.this.checkWrittenData(pair.getLeft(), pair.getRight(), expected);
		}
	}
	
	@Nested
	public class AsSeekableIO extends AbstractSeekableIOTest {
		@Override
		public List<? extends TestCase<Integer, IO.Seekable>> getTestCases() {
			return AbstractWritableSeekableCharsIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, IO.Seekable>) (size) -> tc.getArgumentProvider().apply(size).getIo()))
				.toList();
		}
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeCharsFullyAtCharArrayOneShot(String displayName, char[] toWrite, Function<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends CharsIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable.Seekable io = ioTuple.getIo();
		
		assertThrows(NullPointerException.class, () -> io.writeCharsFullyAt(0, (char[]) null));
		assertThrows(NegativeValueException.class, () -> io.writeCharsFullyAt(-1, new char[0]));
		io.writeCharsFullyAt(0, new char[0]);

		io.writeCharsFullyAt(0, toWrite);
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeCharsFullyAt(toWrite.length, new char[1]));
		io.writeCharsFullyAt(toWrite.length, new char[0]);
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			io.writeCharsFullyAt(toWrite.length - before, additional);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite, 0, toWrite.length - before), CharBuffer.wrap(additional)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + additional.length + 69);
				char[] additional2 = CharsIOTestUtils.generateContent(69);
				io.writeCharsFullyAt(toWrite.length - before + additional.length, additional2);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite, 0, toWrite.length - before), CharBuffer.wrap(additional), CharBuffer.wrap(additional2)));
				r.setSize(toWrite.length);
				io.writeCharsFullyAt(toWrite.length - before, toWrite, toWrite.length - before, before);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			char[] additional = CharsIOTestUtils.generateContent(71);
			io.writeCharsFullyAt(toWrite.length, additional);
			assertThrows(EOFException.class, () -> io.writeCharsFullyAt(toWrite.length + additional.length, new char[1]));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, (char[]) null));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, new char[0]));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, new char[1]));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(-1, new char[1]));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeCharsFullyAtCharArrayWithOffset(String displayName, char[] toWrite, Function<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends CharsIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable.Seekable io = ioTuple.getIo();
		
		assertThrows(NullPointerException.class, () -> io.writeCharsFullyAt(0, (char[]) null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.writeCharsFullyAt(0, new char[10], -1, 1));
		assertThrows(NegativeValueException.class, () -> io.writeCharsFullyAt(0, new char[10], 3, -1));
		assertThrows(LimitExceededException.class, () -> io.writeCharsFullyAt(0, new char[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.writeCharsFullyAt(0, new char[10], 11, 1));
		assertThrows(NegativeValueException.class, () -> io.writeCharsFullyAt(-1, new char[10], 0, 1));
		io.writeCharsFullyAt(0, new char[10], 7, 0);

		int step = toWrite.length > 10000 ? 1111 : 3;
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			io.writeCharsFullyAt(i, toWrite, i, l);
		}
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeCharsFullyAt(toWrite.length, new char[1], 0, 1));
		io.writeCharsFullyAt(toWrite.length, new char[1], 0, 0);
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			io.writeCharsFullyAt(toWrite.length - before, additional, 7, 13);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite, 0, toWrite.length - before), CharBuffer.wrap(additional, 7, 13)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + 13 + 32);
				char[] additional2 = CharsIOTestUtils.generateContent(69);
				io.writeCharsFullyAt(toWrite.length - before + 13, additional2, 17, 32);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite, 0, toWrite.length - before), CharBuffer.wrap(additional, 7, 13), CharBuffer.wrap(additional2, 17, 32)));
				r.setSize(toWrite.length);
				io.writeCharsFullyAt(toWrite.length - before, toWrite, toWrite.length - before, before);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			char[] additional = CharsIOTestUtils.generateContent(100);
			io.writeCharsFullyAt(toWrite.length, additional, 16, 71);
			assertThrows(EOFException.class, () -> io.writeCharsFullyAt(toWrite.length + additional.length, new char[10], 3, 1));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 16, 71)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, toWrite, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, (char[]) null, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, new char[10], -1, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, new char[10], 3, -1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, new char[10], 0, 11));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, new char[10], 11, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, new char[10], 7, 0));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(-1, new char[10], 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, new char[10], 0, 1));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeCharsFullyAtCharBufferOneShot(String displayName, char[] toWrite, Function<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends CharsIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable.Seekable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeCharsFullyAt(0, (CharBuffer) null));
		assertThrows(NegativeValueException.class, () -> io.writeCharsFullyAt(-1, CharBuffer.allocate(0)));
		io.writeCharsFullyAt(0, CharBuffer.allocate(0));
		
		io.writeCharsFullyAt(0, CharBuffer.wrap(toWrite));
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeCharsFullyAt(toWrite.length, CharBuffer.allocate(1)));
		io.writeCharsFullyAt(toWrite.length, CharBuffer.allocate(0));
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			io.writeCharsFullyAt(toWrite.length - before, CharBuffer.wrap(additional));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite, 0, toWrite.length - before), CharBuffer.wrap(additional)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + additional.length + 69);
				char[] additional2 = CharsIOTestUtils.generateContent(69);
				io.writeCharsFullyAt(toWrite.length - before + additional.length, CharBuffer.wrap(additional2));
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite, 0, toWrite.length - before), CharBuffer.wrap(additional), CharBuffer.wrap(additional2)));
				r.setSize(toWrite.length);
				io.writeCharsFullyAt(toWrite.length - before, CharBuffer.wrap(toWrite, toWrite.length - before, before));
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			char[] additional = CharsIOTestUtils.generateContent(71);
			io.writeCharsFullyAt(toWrite.length, CharBuffer.wrap(additional));
			assertThrows(EOFException.class, () -> io.writeCharsFullyAt(toWrite.length + additional.length, CharBuffer.allocate(1)));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, (CharBuffer) null));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, CharBuffer.allocate(0)));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, CharBuffer.allocate(1)));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(-1, CharBuffer.allocate(1)));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeCharsFullyAtCharBufferSmallParts(String displayName, char[] toWrite, Function<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends CharsIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable.Seekable io = ioTuple.getIo();

		int step = toWrite.length > 10000 ? 1111 : 3;
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			io.writeCharsFullyAt(i, CharBuffer.wrap(toWrite, i, l));
		}
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, CharBuffer.allocate(0)));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, CharBuffer.allocate(1)));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeCharsFullyAtBufferList(String displayName, char[] toWrite, Function<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends CharsIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable.Seekable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeCharsFullyAt(0, (List<CharBuffer>) null));
		assertThrows(NegativeValueException.class, () -> io.writeCharsFullyAt(-1, List.of()));
		io.writeCharsFullyAt(0, List.of());
		io.writeCharsFullyAt(0, List.of(CharBuffer.allocate(0)));
		io.writeCharsFullyAt(0, List.of(CharBuffer.allocate(0), CharBuffer.allocate(0)));

		List<CharBuffer> buffers = new LinkedList<>();
		int step = toWrite.length > 10000 ? 1111 : 3;
		buffers.add(CharBuffer.allocate(0));
		for (int i = 0; i < toWrite.length; i += step) {
			int l = Math.min(step, toWrite.length - i);
			buffers.add(CharBuffer.wrap(toWrite, i, l));
			buffers.add(CharBuffer.allocate(0));
		}
		io.writeCharsFullyAt(0, buffers);

		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeCharsFullyAt(toWrite.length, List.of(CharBuffer.allocate(1))));
		io.writeCharsFullyAt(toWrite.length, List.of());
		io.writeCharsFullyAt(toWrite.length, List.of(CharBuffer.allocate(0)));
		io.writeCharsFullyAt(toWrite.length, List.of(CharBuffer.allocate(0), CharBuffer.allocate(0)));

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, List.of(CharBuffer.allocate(1))));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, List.of()));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, List.of(CharBuffer.allocate(0))));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsFullyAt(0, List.of(CharBuffer.allocate(0), CharBuffer.allocate(0))));
	}
	
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeCharBufferAt(String displayName, char[] toWrite, Function<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends CharsIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable.Seekable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeCharsAt(0, (CharBuffer) null));
		assertThrows(NegativeValueException.class, () -> io.writeCharsAt(-1, CharBuffer.allocate(1)));
		assertThat(io.writeCharsAt(0, CharBuffer.allocate(0))).isZero();

		int step = toWrite.length > 10000 ? 1111 : 3;
		char[] b;
		if (io instanceof IO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new char[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		for (int i = 0; i < toWrite.length;) {
			int l = Math.min(step, b.length - i);
			int nb = io.writeCharsAt(i, CharBuffer.wrap(b, i, l));
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeCharsAt(toWrite.length, CharBuffer.allocate(1))).isEqualTo(-1);
		assertThat(io.writeCharsAt(toWrite.length, CharBuffer.allocate(0))).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			int done = 0;
			while (done < additional.length) {
				int nb = io.writeCharsAt(toWrite.length - before + done, CharBuffer.wrap(additional, done, additional.length - done));
				assertThat(nb).isPositive();
				done += nb;
			}
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite, 0, toWrite.length - before), CharBuffer.wrap(additional)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + additional.length + 69);
				char[] additional2 = CharsIOTestUtils.generateContent(69);
				done = 0;
				while (done < additional2.length) {
					int nb = io.writeCharsAt(toWrite.length - before + additional.length + done, CharBuffer.wrap(additional2, done, additional2.length - done));
					assertThat(nb).isPositive();
					done += nb;
				}
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite, 0, toWrite.length - before), CharBuffer.wrap(additional), CharBuffer.wrap(additional2)));
				r.setSize(toWrite.length);
				io.writeCharsFullyAt(toWrite.length - before, toWrite, toWrite.length - before, before);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			char[] additional = CharsIOTestUtils.generateContent(71);
			int nb = io.writeCharsAt(toWrite.length, CharBuffer.wrap(additional));
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 0, nb)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, CharBuffer.allocate(1)));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, CharBuffer.allocate(0)));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeCharBufferDirectAt(String displayName, char[] toWrite, Function<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends CharsIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable.Seekable io = ioTuple.getIo();

		assertThrows(NegativeValueException.class, () -> io.writeCharsAt(-1, ByteBuffer.allocateDirect(2).asCharBuffer()));
		assertThat(io.writeCharsAt(0, ByteBuffer.allocateDirect(0).asCharBuffer())).isZero();

		int step = toWrite.length > 10000 ? 1111 : 3;
		char[] b;
		if (io instanceof IO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new char[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		for (int i = 0; i < toWrite.length;) {
			int l = Math.min(step, b.length - i);
			CharBuffer bb = ByteBuffer.allocateDirect(l * 2).asCharBuffer();
			bb.put(b, i, l);
			int nb = io.writeCharsAt(i, bb.flip());
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeCharsAt(toWrite.length, ByteBuffer.allocateDirect(2).asCharBuffer())).isEqualTo(-1);
		assertThat(io.writeCharsAt(toWrite.length, ByteBuffer.allocateDirect(0).asCharBuffer())).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);

		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, ByteBuffer.allocateDirect(2).asCharBuffer()));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, ByteBuffer.allocateDirect(0).asCharBuffer()));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeCharArrayAt(String displayName, char[] toWrite, Function<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends CharsIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable.Seekable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeCharsAt(0, (char[]) null));
		assertThrows(NegativeValueException.class, () -> io.writeCharsAt(-1, new char[1]));
		assertThat(io.writeCharsAt(0, new char[0])).isZero();

		int step = toWrite.length > 10000 ? 1111 : 3;
		char[] b;
		if (io instanceof IO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new char[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		for (int i = 0; i < toWrite.length;) {
			int l = Math.min(step, b.length - i);
			char[] bb = new char[l];
			System.arraycopy(b, i, bb, 0, l);
			int nb = io.writeCharsAt(i, bb);
			assertThat(nb).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeCharsAt(toWrite.length, new char[1])).isEqualTo(-1);
		assertThat(io.writeCharsAt(toWrite.length, new char[0])).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			int nb = io.writeCharsAt(toWrite.length, additional);
			assertThat(nb).isPositive();
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 0, nb)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + nb + 69);
				char[] additional2 = CharsIOTestUtils.generateContent(69);
				int nb2 = io.writeCharsAt(toWrite.length + nb, additional2);
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
			int nb = io.writeCharsAt(toWrite.length, additional);
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 0, nb)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, new char[0]));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, new char[1]));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentTestCasesProvider.class)
	void writeCharArrayAtWithOffset(String displayName, char[] toWrite, Function<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends CharsIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable.Seekable io = ioTuple.getIo();

		assertThrows(NullPointerException.class, () -> io.writeCharsAt(0, (char[]) null, 0, 1));
		assertThrows(NegativeValueException.class, () -> io.writeCharsAt(0, new char[10], -1, 1));
		assertThrows(NegativeValueException.class, () -> io.writeCharsAt(0, new char[10], 4, -2));
		assertThrows(LimitExceededException.class, () -> io.writeCharsAt(0, new char[10], 0, 11));
		assertThrows(LimitExceededException.class, () -> io.writeCharsAt(0, new char[10], 11, 1));
		assertThrows(NegativeValueException.class, () -> io.writeCharsAt(-1, new char[10], 1, 1));
		assertThat(io.writeCharsAt(0, new char[10], 7, 0)).isZero();

		int step = toWrite.length > 10000 ? 1111 : 3;
		char[] b;
		if (io instanceof IO.Writable.Appendable)
			b = toWrite;
		else {
			// we generate a bigger array, so at one point we try to write more than the size
			b = new char[toWrite.length + 20];
			System.arraycopy(toWrite, 0, b, 0, toWrite.length);
		}
		for (int i = 0; i < toWrite.length;) {
			int l = Math.min(step, b.length - i);
			int nb = io.writeCharsAt(i, b, i, l);
			assertThat(nb).as("Write up to " + l + " at " + i + "/" + toWrite.length).isPositive();
			i += nb;
		}

		if (!(io instanceof IO.Writable.Appendable))
			assertThat(io.writeCharsAt(toWrite.length, new char[1], 0, 1)).isEqualTo(-1);
		assertThat(io.writeCharsAt(toWrite.length, new char[0], 0, 0)).isZero();
		assertThat(io.writeCharsAt(toWrite.length, new char[1], 0, 0)).isZero();

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			int before = Math.min(5, toWrite.length);
			int done = 0;
			while (done < additional.length) {
				int nb = io.writeCharsAt(toWrite.length - before + done, additional, done, additional.length - done);
				assertThat(nb).isPositive();
				done += nb;
			}
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite, 0, toWrite.length - before), CharBuffer.wrap(additional)));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length - before + additional.length + 69);
				char[] additional2 = CharsIOTestUtils.generateContent(69);
				done = 0;
				while (done < additional2.length) {
					int nb = io.writeCharsAt(toWrite.length - before + additional.length + done, additional2, done, additional2.length - done);
					assertThat(nb).isPositive();
					done += nb;
				}
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite, 0, toWrite.length - before), CharBuffer.wrap(additional), CharBuffer.wrap(additional2)));
				r.setSize(toWrite.length);
				io.writeCharsFullyAt(toWrite.length - before, toWrite, toWrite.length - before, before);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			char[] additional = CharsIOTestUtils.generateContent(100);
			int nb = io.writeCharsAt(toWrite.length, additional, 20, 71);
			assertThat(nb).isPositive();
			r.setSize(toWrite.length + nb);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcCharBufferUtils.concat(CharBuffer.wrap(toWrite), CharBuffer.wrap(additional, 20, nb)));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, (char[]) null, 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, new char[10], -1, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, new char[10], 2, -1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, new char[10], 0, 11));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, new char[10], 11, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, new char[10], 7, 0));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, new char[1], 0, 1));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, new char[0], 0, 0));
		assertThrows(ClosedChannelException.class, () -> io.writeCharsAt(0, new char[1], 0, 0));
	}

	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(SmallRandomContentTestCasesProvider.class)
	void writeCharAt(String displayName, char[] toWrite, Function<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends CharsIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		CharsIO.Writable.Seekable io = ioTuple.getIo();

		for (int i = 0; i < toWrite.length; ++i)
			io.writeCharAt(i, toWrite[i]);
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> io.writeCharAt(toWrite.length, (char) 0));

		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			char[] additional = CharsIOTestUtils.generateContent(23);
			for (int i = additional.length - 1; i >= 0; --i)
				io.writeCharAt(toWrite.length + i, additional[i]);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + 69);
				char[] additional2 = CharsIOTestUtils.generateContent(69);
				for (int i = additional2.length - 1; i >= 0; --i)
					io.writeCharAt(toWrite.length + additional.length + i, additional2[i]);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + 71);
			char[] additional = CharsIOTestUtils.generateContent(71);
			for (int i = additional.length - 1; i >= 0; --i)
				io.writeCharAt(toWrite.length + i, additional[i]);
			assertThrows(EOFException.class, () -> io.writeCharAt(toWrite.length + additional.length, (char) 0));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.writeCharAt(0, (char) 0));
	}
	
}
