package net.lecousin.commons.io.bytes.data;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.collections.LcArrayUtils;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.function.BiConsumerThrows;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.bytes.BytesIOTestUtils;
import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
import net.lecousin.commons.io.bytes.data.BytesDataIOTestUtils.DataTestCasesProvider;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractWritableSeekableBytesDataIOTest implements TestCasesProvider<Integer, WritableTestCase<? extends BytesDataIO.Writable.Seekable, ?>> {

	protected abstract void checkWrittenData(BytesDataIO.Writable.Seekable io, Object object, byte[] expected) throws Exception;

	
	@Nested
	public class AsWritableBytesDataIO extends AbstractWritableBytesDataIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends BytesDataIO.Writable, ?>>> getTestCases() {
			return AbstractWritableSeekableBytesDataIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(),
					(Function<Integer, WritableTestCase<? extends BytesDataIO.Writable, ?>>) (size) -> {
						WritableTestCase<? extends BytesDataIO.Writable.Seekable, ?> wtc = tc.getArgumentProvider().apply(size);
						return new WritableTestCase<>((BytesDataIO.Writable) wtc.getIo(), wtc.getObject());
					}))
				.toList();
		}

		@Override
		protected void checkWrittenData(BytesDataIO.Writable io, Object object, byte[] expected) throws Exception {
			AbstractWritableSeekableBytesDataIOTest.this.checkWrittenData((BytesDataIO.Writable.Seekable) io, object, expected);
		}
	}
	
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataTestCasesProvider.class)
	void writeDataAt(String displayName, byte[] toWrite, int nbBytes, boolean signed, Function<Integer, WritableTestCase<? extends BytesDataIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesDataIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesDataIO.Writable.Seekable io = ioTuple.getIo();

		BytesData data = BytesData.of(io.getByteOrder());
		BiFunction<byte[], Integer, Long> dataReader = signed ? (b,o) -> data.readSignedBytes(nbBytes, b, o) : (b,o) -> data.readUnsignedBytes(nbBytes, b, o);

		BiConsumerThrows<Long, Number, IOException> ioWriter = null;
		BiConsumerThrows<Long, Number, IOException> ioWriter2 = null;
		
		switch (nbBytes) {
		case 1: ioWriter = signed ? (p,v) -> io.writeByteAt(p, v.byteValue()) : (p,v) -> io.writeUnsignedByteAt(p, v.intValue()); break;
		case 2: ioWriter = signed ? (p,v) -> io.writeSigned2BytesAt(p, v.shortValue()) : (p,v) -> io.writeUnsigned2BytesAt(p, v.intValue()); break;
		case 3: ioWriter = signed ? (p,v) -> io.writeSigned3BytesAt(p, v.intValue()) : (p,v) -> io.writeUnsigned3BytesAt(p, v.intValue()); break;
		case 4: ioWriter = signed ? (p,v) -> io.writeSigned4BytesAt(p, v.intValue()) : (p,v) -> io.writeUnsigned4BytesAt(p, v.longValue()); break;
		case 5: ioWriter = signed ? (p,v) -> io.writeSigned5BytesAt(p, v.longValue()) : (p,v) -> io.writeUnsigned5BytesAt(p, v.longValue()); break;
		case 6: ioWriter = signed ? (p,v) -> io.writeSigned6BytesAt(p, v.longValue()) : (p,v) -> io.writeUnsigned6BytesAt(p, v.longValue()); break;
		case 7: ioWriter = signed ? (p,v) -> io.writeSigned7BytesAt(p, v.longValue()) : (p,v) -> io.writeUnsigned7BytesAt(p, v.longValue()); break;
		case 8: ioWriter = (p,v) -> io.writeSigned8BytesAt(p, v.longValue()); break;
		}
		if (signed) {
			switch (nbBytes) {
			case 2: ioWriter2 = (p,v) -> io.writeShortAt(p, v.shortValue()); break;
			case 4: ioWriter2 = (p,v) -> io.writeIntegerAt(p, v.intValue()); break;
			case 8: ioWriter2 = (p,v) -> io.writeLongAt(p, v.longValue()); break;
			}
		}
		if (ioWriter2 == null) ioWriter2 = ioWriter;

		boolean useWriter2 = false;
		int pos = 0;
		for (; pos < toWrite.length - (nbBytes - 1); pos += nbBytes) {
			(useWriter2 ? ioWriter2 : ioWriter).accept((long) pos, dataReader.apply(toWrite, pos));
			useWriter2 = !useWriter2;
		}
		// finish last bytes
		for (; pos < toWrite.length; ++pos)
			io.writeByteAt(pos, toWrite[pos]);
		
		BiConsumerThrows<Long, Number, IOException> w = ioWriter;
		BiConsumerThrows<Long, Number, IOException> w2 = ioWriter2;
		
		if (!(io instanceof IO.Writable.Appendable)) {
			assertThrows(EOFException.class, () -> w.accept((long) toWrite.length, 0L));
			assertThrows(EOFException.class, () -> w2.accept((long) toWrite.length, 0L));
		}
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(nbBytes * 7);
			for (int i = 0; i < additional.length; i += nbBytes) {
				(useWriter2 ? ioWriter2 : ioWriter).accept((long) toWrite.length + i, dataReader.apply(additional, i));
				useWriter2 = !useWriter2;
			}
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + nbBytes * 5);
				byte[] additional2 = BytesIOTestUtils.generateContent(nbBytes * 5);
				for (int i = 0; i < additional2.length; i += nbBytes) {
					(useWriter2 ? ioWriter2 : ioWriter).accept((long) toWrite.length + additional.length + i, dataReader.apply(additional2, i));
					useWriter2 = !useWriter2;
				}
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + nbBytes * 7);
			byte[] additional = BytesIOTestUtils.generateContent(nbBytes * 7);
			for (int i = 0; i < additional.length; i += nbBytes) {
				(useWriter2 ? ioWriter2 : ioWriter).accept((long) toWrite.length + i, dataReader.apply(additional, i));
				useWriter2 = !useWriter2;
			}
			assertThrows(EOFException.class, () -> w.accept((long) toWrite.length + additional.length, 0L));
			assertThrows(EOFException.class, () -> w2.accept((long) toWrite.length + additional.length, 0L));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> w.accept(0L, 0L));
		assertThrows(ClosedChannelException.class, () -> w2.accept(0L, 0L));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataTestCasesProvider.class)
	void writeDataAt2(String displayName, byte[] toWrite, int nbBytes, boolean signed, Function<Integer, WritableTestCase<? extends BytesDataIO.Writable.Seekable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesDataIO.Writable.Seekable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesDataIO.Writable.Seekable io = ioTuple.getIo();

		assertThrows(IllegalArgumentException.class, () -> io.writeSignedBytesAt(0, 9, 0));
		assertThrows(IllegalArgumentException.class, () -> io.writeSignedBytesAt(0, -1, 0));
		assertThrows(IllegalArgumentException.class, () -> io.writeSignedBytesAt(0, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> io.writeUnsignedBytesAt(0, 8, 0));
		assertThrows(IllegalArgumentException.class, () -> io.writeUnsignedBytesAt(0, -1, 0));
		assertThrows(IllegalArgumentException.class, () -> io.writeUnsignedBytesAt(0, 0, 0));
		assertThrows(NegativeValueException.class, () -> io.writeUnsignedBytesAt(-1, 1, 0));
		
		BytesData data = BytesData.of(io.getByteOrder());
		BiFunction<byte[], Integer, Long> dataReader = signed ? (b,o) -> data.readSignedBytes(nbBytes, b, o) : (b,o) -> data.readUnsignedBytes(nbBytes, b, o);

		BiConsumerThrows<Long, Number, IOException> ioWriter = signed ? (p,v) -> io.writeSignedBytesAt(p, nbBytes, v.longValue()) : (p,v) -> io.writeUnsignedBytesAt(p, nbBytes, v.longValue());
		
		int pos = 0;
		for (; pos < toWrite.length - (nbBytes - 1); pos += nbBytes) {
			ioWriter.accept((long) pos, dataReader.apply(toWrite, pos));
		}
		// finish last bytes
		for (; pos < toWrite.length; ++pos)
			io.writeByteAt(pos, toWrite[pos]);
		
		BiConsumerThrows<Long, Number, IOException> w = ioWriter;
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> w.accept((long) toWrite.length, 0L));
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(nbBytes * 7);
			for (int i = 0; i < additional.length; i += nbBytes)
				ioWriter.accept((long) toWrite.length + i, dataReader.apply(additional, i));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + nbBytes * 5);
				byte[] additional2 = BytesIOTestUtils.generateContent(nbBytes * 5);
				for (int i = 0; i < additional2.length; i += nbBytes)
					ioWriter.accept((long) toWrite.length + additional.length + i, dataReader.apply(additional2, i));
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional, additional2));
				r.setSize(toWrite.length);
				io.flush();
				checkWrittenData(io, ioTuple.getObject(), toWrite);
			}
		} else if (io instanceof IO.Writable.Resizable r) {
			r.setSize(toWrite.length + nbBytes * 7);
			byte[] additional = BytesIOTestUtils.generateContent(nbBytes * 7);
			for (int i = 0; i < additional.length; i += nbBytes)
				ioWriter.accept((long) toWrite.length + i, dataReader.apply(additional, i));
			assertThrows(EOFException.class, () -> w.accept((long) toWrite.length + additional.length, 0L));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}

		io.close();
		assertThrows(ClosedChannelException.class, () -> w.accept(0L, 0L));
	}

}
