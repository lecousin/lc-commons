package net.lecousin.commons.io.bytes.data;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.TriFunction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.collections.LcArrayUtils;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.bytes.BytesIOTestUtils;
import net.lecousin.commons.io.bytes.data.BytesDataIOTestUtils.DataTestCasesProvider;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractWritableBytesDataIOTest implements TestCasesProvider<Integer, WritableTestCase<? extends BytesDataIO.Writable, ?>> {

	protected abstract void checkWrittenData(BytesDataIO.Writable io, Object object, byte[] expected) throws Exception;

	
	@Nested
	public class AsWritableBytesIO extends AbstractWritableBytesIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<?, ?>>> getTestCases() {
			return AbstractWritableBytesDataIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(),
					(Function<Integer, WritableTestCase<?, ?>>) (size) -> {
						WritableTestCase<? extends BytesDataIO.Writable, ?> wtc = tc.getArgumentProvider().apply(size);
						return new WritableTestCase<>((BytesIO.Writable) wtc.getIo(), wtc.getObject());
					}))
				.toList();
		}

		@Override
		protected void checkWrittenData(BytesIO.Writable io, Object object, byte[] expected) throws Exception {
			AbstractWritableBytesDataIOTest.this.checkWrittenData((BytesDataIO.Writable) io, object, expected);
		}
	}
	
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataTestCasesProvider.class)
	void writeData(String displayName, byte[] toWrite, int nbBytes, boolean signed, Function<Integer, WritableTestCase<? extends BytesDataIO.Writable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesDataIO.Writable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesDataIO.Writable io = ioTuple.getIo();

		BytesData data = BytesData.of(io.getByteOrder());
		BiFunction<byte[], Integer, Long> dataReader = signed ? (b,o) -> data.readSignedBytes(nbBytes, b, o) : (b,o) -> data.readUnsignedBytes(nbBytes, b, o);

		FailableConsumer<Number, IOException> ioWriter = null;
		FailableConsumer<Number, IOException> ioWriter2 = null;
		
		switch (nbBytes) {
		case 1: ioWriter = signed ? v -> io.writeByte(v.byteValue()) : v -> io.writeUnsignedByte(v.intValue()); break;
		case 2: ioWriter = signed ? v -> io.writeSigned2Bytes(v.shortValue()) : v -> io.writeUnsigned2Bytes(v.intValue()); break;
		case 3: ioWriter = signed ? v -> io.writeSigned3Bytes(v.intValue()) : v -> io.writeUnsigned3Bytes(v.intValue()); break;
		case 4: ioWriter = signed ? v -> io.writeSigned4Bytes(v.intValue()) : v -> io.writeUnsigned4Bytes(v.longValue()); break;
		case 5: ioWriter = signed ? v -> io.writeSigned5Bytes(v.longValue()) : v -> io.writeUnsigned5Bytes(v.longValue()); break;
		case 6: ioWriter = signed ? v -> io.writeSigned6Bytes(v.longValue()) : v -> io.writeUnsigned6Bytes(v.longValue()); break;
		case 7: ioWriter = signed ? v -> io.writeSigned7Bytes(v.longValue()) : v -> io.writeUnsigned7Bytes(v.longValue()); break;
		case 8: ioWriter = v -> io.writeSigned8Bytes(v.longValue()); break;
		}
		if (signed) {
			switch (nbBytes) {
			case 2: ioWriter2 = v -> io.writeShort(v.shortValue()); break;
			case 4: ioWriter2 = v -> io.writeInteger(v.intValue()); break;
			case 8: ioWriter2 = v -> io.writeLong(v.longValue()); break;
			}
		}
		if (ioWriter2 == null) ioWriter2 = ioWriter;

		boolean useWriter2 = false;
		int pos = 0;
		for (; pos < toWrite.length - (nbBytes - 1); pos += nbBytes) {
			(useWriter2 ? ioWriter2 : ioWriter).accept(dataReader.apply(toWrite, pos));
			useWriter2 = !useWriter2;
		}
		// finish last bytes
		for (; pos < toWrite.length; ++pos)
			io.writeByte(toWrite[pos]);
		
		FailableConsumer<Number, IOException> w = ioWriter;
		FailableConsumer<Number, IOException> w2 = ioWriter2;
		
		if (!(io instanceof IO.Writable.Appendable)) {
			assertThrows(EOFException.class, () -> w.accept(0L));
			assertThrows(EOFException.class, () -> w2.accept(0L));
		}
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(nbBytes * 7);
			for (int i = 0; i < additional.length; i += nbBytes) {
				(useWriter2 ? ioWriter2 : ioWriter).accept(dataReader.apply(additional, i));
				useWriter2 = !useWriter2;
			}
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + nbBytes * 5);
				byte[] additional2 = BytesIOTestUtils.generateContent(nbBytes * 5);
				for (int i = 0; i < additional2.length; i += nbBytes) {
					(useWriter2 ? ioWriter2 : ioWriter).accept(dataReader.apply(additional2, i));
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
				(useWriter2 ? ioWriter2 : ioWriter).accept(dataReader.apply(additional, i));
				useWriter2 = !useWriter2;
			}
			assertThrows(EOFException.class, () -> w.accept(0L));
			assertThrows(EOFException.class, () -> w2.accept(0L));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> w.accept(0));
		assertThrows(ClosedChannelException.class, () -> w2.accept(0));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataTestCasesProvider.class)
	void writeData2(String displayName, byte[] toWrite, int nbBytes, boolean signed, Function<Integer, WritableTestCase<? extends BytesDataIO.Writable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesDataIO.Writable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesDataIO.Writable io = ioTuple.getIo();
		
		assertThrows(IllegalArgumentException.class, () -> io.writeSignedBytes(9, 0));
		assertThrows(IllegalArgumentException.class, () -> io.writeSignedBytes(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> io.writeSignedBytes(0, 0));
		assertThrows(IllegalArgumentException.class, () -> io.writeUnsignedBytes(8, 0));
		assertThrows(IllegalArgumentException.class, () -> io.writeUnsignedBytes(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> io.writeUnsignedBytes(0, 0));

		BytesData data = BytesData.of(io.getByteOrder());
		BiFunction<byte[], Integer, Long> dataReader = signed ? (b,o) -> data.readSignedBytes(nbBytes, b, o) : (b,o) -> data.readUnsignedBytes(nbBytes, b, o);

		FailableConsumer<Number, IOException> ioWriter = signed ? v -> io.writeSignedBytes(nbBytes, v.longValue()) : v -> io.writeUnsignedBytes(nbBytes, v.longValue());
		
		int pos = 0;
		for (; pos < toWrite.length - (nbBytes - 1); pos += nbBytes) {
			ioWriter.accept(dataReader.apply(toWrite, pos));
		}
		// finish last bytes
		for (; pos < toWrite.length; ++pos)
			io.writeByte(toWrite[pos]);
		
		FailableConsumer<Number, IOException> w = ioWriter;
		
		if (!(io instanceof IO.Writable.Appendable))
			assertThrows(EOFException.class, () -> w.accept(0L));
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		
		if (io instanceof IO.Writable.Appendable) {
			byte[] additional = BytesIOTestUtils.generateContent(nbBytes * 7);
			for (int i = 0; i < additional.length; i += nbBytes)
				ioWriter.accept(dataReader.apply(additional, i));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			if (io instanceof IO.Writable.Resizable r) {
				r.setSize(toWrite.length + additional.length + nbBytes * 5);
				byte[] additional2 = BytesIOTestUtils.generateContent(nbBytes * 5);
				for (int i = 0; i < additional2.length; i += nbBytes)
					ioWriter.accept(dataReader.apply(additional2, i));
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
				ioWriter.accept(dataReader.apply(additional, i));
			assertThrows(EOFException.class, () -> w.accept(0L));
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), LcArrayUtils.concat(toWrite, additional));
			r.setSize(toWrite.length);
			io.flush();
			checkWrittenData(io, ioTuple.getObject(), toWrite);
		}
		io.close();
		assertThrows(ClosedChannelException.class, () -> w.accept(0));
	}


	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataTestCasesProvider.class)
	void writeDataSwitchByteOrder(String displayName, byte[] toWrite, int nbBytes, boolean signed, Function<Integer, WritableTestCase<? extends BytesDataIO.Writable, ?>> ioSupplier) throws Exception {
		WritableTestCase<? extends BytesDataIO.Writable, ?> ioTuple = ioSupplier.apply(toWrite.length);
		BytesDataIO.Writable io = ioTuple.getIo();
		
		TriFunction<BytesData, byte[], Integer, Long> dataReader = signed ? (data,b,o) -> data.readSignedBytes(nbBytes, b, o) : (data,b,o) -> data.readUnsignedBytes(nbBytes, b, o);
		FailableConsumer<Number, IOException> ioWriter = signed ? v -> io.writeSignedBytes(nbBytes, v.longValue()) : v -> io.writeUnsignedBytes(nbBytes, v.longValue());
		
		ByteOrder[] order = new ByteOrder[] { ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN };
		int pos = 0;
		for (int j = 0; pos < toWrite.length - (nbBytes - 1); pos += nbBytes, j++) {
			io.setByteOrder(order[j % 2]);
			ioWriter.accept(dataReader.apply(BytesData.of(order[j % 2]), toWrite, pos));
		}
		// finish last bytes
		for (; pos < toWrite.length; ++pos)
			io.writeByte(toWrite[pos]);
		
		io.flush();
		checkWrittenData(io, ioTuple.getObject(), toWrite);
		io.close();
	}

}
