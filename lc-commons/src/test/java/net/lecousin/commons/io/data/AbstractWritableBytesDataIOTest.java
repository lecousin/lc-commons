package net.lecousin.commons.io.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.EOFException;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.lecousin.commons.function.ConsumerThrows;
import net.lecousin.commons.io.AbstractIOTest;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.test.ParameterizedTestUtils;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.ParameterizedTestUtils.CompositeArgumentsProvider;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractWritableBytesDataIOTest implements TestCasesProvider<Integer, AbstractWritableBytesDataIOTest.WritableTestCase> {

	@Nested
	public class TestIO extends AbstractIOTest {
		@Override
		public List<TestCase.NoInput<IO>> getTestCases() {
			return AbstractWritableBytesDataIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), () -> (IO) tc.getArgumentProvider().apply(10).getStream()))
				.toList();
		}
	}
	
	@Data
	@AllArgsConstructor
	public static class WritableTestCase {
		private BytesDataIO.Writable stream;
		private Object data;
	}
	
	protected abstract void checkWrittenData(BytesDataIO.Writable stream, Object generatedData, byte[] expected);
	
	protected static class RandomContent extends CompositeArgumentsProvider {
		public RandomContent() {
			super(new BytesDataIOTestUtils.RandomContentProvider(), new ParameterizedTestUtils.TestCasesArgumentsProvider());
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContent.class)
	void testWriteFullyOneShot(String displayName, byte[] data, Function<Integer, WritableTestCase> streamSupplier) throws Exception {
		WritableTestCase tc = streamSupplier.apply(data.length);
		BytesDataIO.Writable stream = tc.getStream();
		stream.writeFully(new byte[0]);
		stream.writeFully(data);
		stream.writeFully(new byte[0]);
		assertThrows(EOFException.class, () -> stream.writeFully(new byte[1]));
		checkWrittenData(stream, tc.getData(), data);
		assertThrows(NullPointerException.class, () -> stream.writeFully(null));
		stream.close();
		assertThrows(ClosedChannelException.class, () -> stream.writeFully(new byte[1]));
		assertThrows(ClosedChannelException.class, () -> stream.writeFully(new byte[1], 0, 0));
		assertThrows(ClosedChannelException.class, () -> stream.writeFully(null));
	}
	
	static class DataArguments extends CompositeArgumentsProvider {
		public DataArguments() {
			super(new BytesDataIOTestUtils.RandomContentProvider(), new ParameterizedTestUtils.TestCasesArgumentsProvider());
			List<Arguments> list = new LinkedList<>();
			for (int nbBytes = 1; nbBytes <= 8; nbBytes++) {
				if (nbBytes != 8) {
					list.add(Arguments.of("Unsigned " + nbBytes + " bytes", nbBytes, false));
				}
				list.add(Arguments.of("Signed " + nbBytes + " bytes", nbBytes, true));
			}
			add(list);
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataArguments.class)
	void testData(String displayName, byte[] data, Function<Integer, WritableTestCase> streamSupplier, int nbBytes, boolean signed) throws Exception {
		WritableTestCase tuple = streamSupplier.apply(data.length);
		BytesDataIO.Writable stream = tuple.getStream();
		ConsumerThrows<Number> writer = null;
		ConsumerThrows<Number> writer2 = null;
		BytesData endianness = stream.getEndianness().equals(BytesDataIO.Endianness.LITTLE_ENDIAN) ? BytesData.LE : BytesData.BE;
		BiFunction<byte[], Integer, ? extends Number> dataReader = null;
		switch (nbBytes) {
		case 1:
			writer = signed ? v -> stream.writeByte(v.byteValue()) : v -> stream.writeUnsignedByte(v.byteValue());
			dataReader = signed ? (b,o) -> b[o] : (b,o) -> b[o] & 0xFF;
			break;
		case 2:
			writer = signed ? v -> stream.writeSigned2Bytes(v.shortValue()) : v -> stream.writeUnsigned2Bytes(v.intValue());
			dataReader = signed ? endianness::readSigned2Bytes : endianness::readUnsigned2Bytes;
			if (signed) writer2 = v -> stream.writeShort(v.shortValue());
			break;
		case 3:
			writer = signed ? v -> stream.writeSigned3Bytes(v.intValue()) : v -> stream.writeUnsigned3Bytes(v.intValue());
			dataReader = signed ? endianness::readSigned3Bytes : endianness::readUnsigned3Bytes;
			break;
		case 4:
			writer = signed ? v -> stream.writeSigned4Bytes(v.intValue()) : v -> stream.writeUnsigned4Bytes(v.longValue());
			dataReader = signed ? endianness::readSigned4Bytes : endianness::readUnsigned4Bytes;
			if (signed) writer2 = v -> stream.writeInteger(v.intValue());
			break;
		case 5:
			writer = signed ? v -> stream.writeSigned5Bytes(v.longValue()) : v -> stream.writeUnsigned5Bytes(v.longValue());
			dataReader = signed ? endianness::readSigned5Bytes : endianness::readUnsigned5Bytes;
			break;
		case 6:
			writer = signed ? v -> stream.writeSigned6Bytes(v.longValue()) : v -> stream.writeUnsigned6Bytes(v.longValue());
			dataReader = signed ? endianness::readSigned6Bytes : endianness::readUnsigned6Bytes;
			break;
		case 7:
			writer = signed ? v -> stream.writeSigned7Bytes(v.longValue()) : v -> stream.writeUnsigned7Bytes(v.longValue());
			dataReader = signed ? endianness::readSigned7Bytes : endianness::readUnsigned7Bytes;
			break;
		case 8:
			writer = v -> stream.writeSigned8Bytes(v.longValue());
			dataReader = endianness::readSigned8Bytes;
			writer2 = v -> stream.writeLong(v.longValue());
			break;
		}
		
		boolean useWriter2 = false;
		if (writer2 == null) writer2 = writer;
		int pos;
		for (pos = 0; pos + nbBytes <= data.length; pos += nbBytes) {
			Number expected = dataReader.apply(data, pos);
			(useWriter2 ? writer2 : writer).accept(expected);
			useWriter2 = !useWriter2;
		}
		ConsumerThrows<Number> w = writer;
		ConsumerThrows<Number> w2 = writer2;
		assertThrows(EOFException.class, () -> w.accept(0));
		assertThrows(EOFException.class, () -> w2.accept(0));

		// finish with remaining bytes
		if (pos < data.length)
			stream.writeFully(data, pos, data.length - pos);
		checkWrittenData(stream, tuple.getData(), data);

		assertFalse(stream.isClosed());
		stream.close();
		assertTrue(stream.isClosed());
		assertThrows(ClosedChannelException.class, () -> w.accept(0));
		assertThrows(ClosedChannelException.class, () -> w2.accept(0));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataArguments.class)
	void testData2(String displayName, byte[] data, Function<Integer, WritableTestCase> streamSupplier, int nbBytes, boolean signed) throws Exception {
		WritableTestCase tuple = streamSupplier.apply(data.length);
		BytesDataIO.Writable stream = tuple.getStream();
		ConsumerThrows<Long> writer =
			signed ? v -> stream.writeSignedBytes(nbBytes, v) : v -> stream.writeUnsignedBytes(nbBytes, v);
		BytesData endianness = stream.getEndianness().equals(BytesDataIO.Endianness.LITTLE_ENDIAN) ? BytesData.LE : BytesData.BE;
		BiFunction<byte[], Integer, ? extends Number> dataReader =
			signed ? (b,o) -> endianness.readSignedBytes(nbBytes, b, o) : (b,o) -> endianness.readUnsignedBytes(nbBytes, b, o);
		
		int pos;
		for (pos = 0; pos + nbBytes <= data.length; pos += nbBytes) {
			Number expected = dataReader.apply(data, pos);
			writer.accept(expected.longValue());
		}
		assertThrows(EOFException.class, () -> writer.accept(0L));

		// finish with remaining bytes
		if (pos < data.length)
			stream.writeFully(data, pos, data.length - pos);
		checkWrittenData(stream, tuple.getData(), data);

		assertThrows(IllegalArgumentException.class, () -> stream.writeSignedBytes(-1, 0L));
		assertThrows(IllegalArgumentException.class, () -> stream.writeSignedBytes(9, 0L));
		assertThrows(IllegalArgumentException.class, () -> stream.writeUnsignedBytes(-1, 0L));
		assertThrows(IllegalArgumentException.class, () -> stream.writeUnsignedBytes(8, 0L));

		assertFalse(stream.isClosed());
		stream.close();
		assertTrue(stream.isClosed());
		assertThrows(ClosedChannelException.class, () -> writer.accept(0L));
	}
	
}
