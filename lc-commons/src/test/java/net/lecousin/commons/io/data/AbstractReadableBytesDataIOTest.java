package net.lecousin.commons.io.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.EOFException;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.io.AbstractIOTest;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.test.ParameterizedTestUtils;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.ParameterizedTestUtils.CompositeArgumentsProvider;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadableBytesDataIOTest implements TestCasesProvider<byte[], BytesDataIO.Readable> {

	@Nested
	public class TestIO extends AbstractIOTest {
		@Override
		public List<TestCase.NoInput<IO>> getTestCases() {
			return AbstractReadableBytesDataIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), () -> (IO) tc.getArgumentProvider().apply(new byte[10])))
				.toList();
		}
	}
	
	protected static class RandomContent extends CompositeArgumentsProvider {
		public RandomContent() {
			super(new BytesDataIOTestUtils.RandomContentProvider(), new ParameterizedTestUtils.TestCasesArgumentsProvider());
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContent.class)
	void testReadFullyOneShot(String displayName, byte[] data, Function<byte[], BytesDataIO.Readable> streamSupplier) throws Exception {
		BytesDataIO.Readable stream = streamSupplier.apply(data);
		stream.readFully(new byte[0]);
		byte[] found = new byte[data.length];
		stream.readFully(found);
		assertThat(found).containsExactly(data);
		stream.readFully(new byte[0]);
		assertThrows(EOFException.class, () -> stream.readFully(new byte[1]));
		assertThrows(NullPointerException.class, () -> stream.readFully(null));
		stream.close();
		assertThrows(ClosedChannelException.class, () -> stream.readFully(new byte[1]));
		assertThrows(ClosedChannelException.class, () -> stream.readFully(new byte[1], 0, 0));
		assertThrows(ClosedChannelException.class, () -> stream.readFully(null));
	}
	
	protected static class DataArguments extends CompositeArgumentsProvider {
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
	void testData(String displayName, byte[] data, Function<byte[], BytesDataIO.Readable> streamSupplier, int nbBytes, boolean signed) throws Exception {
		BytesDataIO.Readable stream = streamSupplier.apply(data);
		Callable<? extends Number> reader = null;
		Callable<? extends Number> reader2 = null;
		BytesData endianness = stream.getEndianness().equals(BytesDataIO.Endianness.LITTLE_ENDIAN) ? BytesData.LE : BytesData.BE;
		BiFunction<byte[], Integer, ? extends Number> dataReader = null;
		switch (nbBytes) {
		case 1:
			reader = signed ? stream::readByte : stream::readUnsignedByte;
			dataReader = signed ? (b,o) -> b[o] : (b,o) -> b[o] & 0xFF;
			break;
		case 2:
			reader = signed ? stream::readSigned2Bytes : stream::readUnsigned2Bytes;
			dataReader = signed ? endianness::readSigned2Bytes : endianness::readUnsigned2Bytes;
			if (signed) reader2 = stream::readShort;
			break;
		case 3:
			reader = signed ? stream::readSigned3Bytes : stream::readUnsigned3Bytes;
			dataReader = signed ? endianness::readSigned3Bytes : endianness::readUnsigned3Bytes;
			break;
		case 4:
			reader = signed ? stream::readSigned4Bytes : stream::readUnsigned4Bytes;
			dataReader = signed ? endianness::readSigned4Bytes : endianness::readUnsigned4Bytes;
			if (signed) reader2 = stream::readInteger;
			break;
		case 5:
			reader = signed ? stream::readSigned5Bytes : stream::readUnsigned5Bytes;
			dataReader = signed ? endianness::readSigned5Bytes : endianness::readUnsigned5Bytes;
			break;
		case 6:
			reader = signed ? stream::readSigned6Bytes : stream::readUnsigned6Bytes;
			dataReader = signed ? endianness::readSigned6Bytes : endianness::readUnsigned6Bytes;
			break;
		case 7:
			reader = signed ? stream::readSigned7Bytes : stream::readUnsigned7Bytes;
			dataReader = signed ? endianness::readSigned7Bytes : endianness::readUnsigned7Bytes;
			break;
		case 8:
			reader = stream::readSigned8Bytes;
			dataReader = endianness::readSigned8Bytes;
			reader2 = stream::readLong;
			break;
		}
		
		boolean useReader2 = false;
		if (reader2 == null) reader2 = reader;
		for (int pos = 0; pos + nbBytes <= data.length; pos += nbBytes) {
			Number found = (useReader2 ? reader2 : reader).call();
			Number expected = dataReader.apply(data, pos);
			assertThat(found).as("Read at " + pos).isEqualTo(expected);
			useReader2 = !useReader2;
		}
		Callable<? extends Number> r = reader;
		Callable<? extends Number> r2 = reader2;
		assertThrows(EOFException.class, () -> r.call());
		assertThrows(EOFException.class, () -> r2.call());
		
		assertFalse(stream.isClosed());
		stream.close();
		assertTrue(stream.isClosed());
		assertThrows(ClosedChannelException.class, () -> r.call());
		assertThrows(ClosedChannelException.class, () -> r2.call());
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataArguments.class)
	void testData2(String displayName, byte[] data, Function<byte[], BytesDataIO.Readable> streamSupplier, int nbBytes, boolean signed) throws Exception {
		BytesDataIO.Readable stream = streamSupplier.apply(data);
		BytesData endianness = stream.getEndianness().equals(BytesDataIO.Endianness.LITTLE_ENDIAN) ? BytesData.LE : BytesData.BE;
		Callable<? extends Number> reader =
			signed ? () -> stream.readSignedBytes(nbBytes) : () -> stream.readUnsignedBytes(nbBytes);
		BiFunction<byte[], Integer, ? extends Number> dataReader =
			signed ? (b,o) -> endianness.readSignedBytes(nbBytes, b, o) : (b,o) -> endianness.readUnsignedBytes(nbBytes, b, o);

		for (int pos = 0; pos + nbBytes <= data.length; pos += nbBytes) {
			Number found = reader.call();
			Number expected = dataReader.apply(data, pos);
			assertThat(found).as("Read at " + pos).isEqualTo(expected);
		}
		Callable<? extends Number> r = reader;
		assertThrows(EOFException.class, () -> r.call());
		
		assertThrows(IllegalArgumentException.class, () -> stream.readSignedBytes(-1));
		assertThrows(IllegalArgumentException.class, () -> stream.readSignedBytes(9));
		assertThrows(IllegalArgumentException.class, () -> stream.readUnsignedBytes(-1));
		assertThrows(IllegalArgumentException.class, () -> stream.readUnsignedBytes(8));

		assertFalse(stream.isClosed());
		stream.close();
		assertTrue(stream.isClosed());
		assertThrows(ClosedChannelException.class, () -> r.call());
	}
	
}
