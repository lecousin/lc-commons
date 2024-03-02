package net.lecousin.commons.io.bytes.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.function.FailableSupplier;
import org.apache.commons.lang3.function.TriFunction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.io.bytes.AbstractReadableBytesIOTest;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.bytes.data.BytesDataIOTestUtils.DataTestCasesProvider;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadableBytesDataIOTest implements TestCasesProvider<byte[], BytesDataIO.Readable> {

	@Nested
	public class AsReadableBytesIO extends AbstractReadableBytesIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesIO.Readable>> getTestCases() {
			return AbstractReadableBytesDataIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<byte[], BytesIO.Readable>) (content) -> tc.getArgumentProvider().apply(content)))
				.toList();
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataTestCasesProvider.class)
	void readData(String displayName, byte[] expected, int nbBytes, boolean signed, Function<byte[], BytesDataIO.Readable> ioSupplier) throws Exception {
		BytesDataIO.Readable io = ioSupplier.apply(expected);

		BytesData data = BytesData.of(io.getByteOrder());
		BiFunction<byte[], Integer, Long> dataReader = signed ? (b,o) -> data.readSignedBytes(nbBytes, b, o) : (b,o) -> data.readUnsignedBytes(nbBytes, b, o);

		FailableSupplier<? extends Number, IOException> ioReader = null;
		FailableSupplier<? extends Number, IOException> ioReader2 = null;
		switch (nbBytes) {
		case 1: ioReader = signed ? io::readByte : io::readUnsignedByte; break;
		case 2: ioReader = signed ? io::readSigned2Bytes : io::readUnsigned2Bytes; break;
		case 3: ioReader = signed ? io::readSigned3Bytes : io::readUnsigned3Bytes; break;
		case 4: ioReader = signed ? io::readSigned4Bytes : io::readUnsigned4Bytes; break;
		case 5: ioReader = signed ? io::readSigned5Bytes : io::readUnsigned5Bytes; break;
		case 6: ioReader = signed ? io::readSigned6Bytes : io::readUnsigned6Bytes; break;
		case 7: ioReader = signed ? io::readSigned7Bytes : io::readUnsigned7Bytes; break;
		case 8: ioReader = io::readSigned8Bytes; break;
		}
		if (signed) {
			switch (nbBytes) {
			case 2: ioReader2 = io::readShort; break;
			case 4: ioReader2 = io::readInteger; break;
			case 8: ioReader2 = io::readLong; break;
			}
		}
		if (ioReader2 == null) ioReader2 = ioReader;
		
		boolean useReader2 = false;
		for (int i = 0; i < expected.length - (nbBytes - 1); i += nbBytes) {
			Number n = (useReader2 ? ioReader2 : ioReader).get();
			assertThat(n.longValue()).as("Read at " + i + "/" + expected.length).isEqualTo(dataReader.apply(expected, i));
			useReader2 = !useReader2;
		}
		
		FailableSupplier<? extends Number, IOException> r = ioReader;
		FailableSupplier<? extends Number, IOException> r2 = ioReader2;
		assertThrows(EOFException.class, () -> r.get());
		assertThrows(EOFException.class, () -> r2.get());
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> r.get());
		assertThrows(ClosedChannelException.class, () -> r2.get());
	}

	@ParameterizedTest(name = "Generic data read: {0}")
	@ArgumentsSource(DataTestCasesProvider.class)
	void readData2(String displayName, byte[] expected, int nbBytes, boolean signed, Function<byte[], BytesDataIO.Readable> ioSupplier) throws Exception {
		BytesDataIO.Readable io = ioSupplier.apply(expected);
		
		assertThrows(IllegalArgumentException.class, () -> io.readSignedBytes(9));
		assertThrows(IllegalArgumentException.class, () -> io.readSignedBytes(-1));
		assertThrows(IllegalArgumentException.class, () -> io.readSignedBytes(0));
		assertThrows(IllegalArgumentException.class, () -> io.readUnsignedBytes(8));
		assertThrows(IllegalArgumentException.class, () -> io.readUnsignedBytes(-1));
		assertThrows(IllegalArgumentException.class, () -> io.readUnsignedBytes(0));
		
		BytesData data = BytesData.of(io.getByteOrder());
		BiFunction<byte[], Integer, Long> dataReader = signed ? (b,o) -> data.readSignedBytes(nbBytes, b, o) : (b,o) -> data.readUnsignedBytes(nbBytes, b, o);

		FailableSupplier<? extends Number, IOException> ioReader = signed ? () -> io.readSignedBytes(nbBytes) : () -> io.readUnsignedBytes(nbBytes);

		for (int i = 0; i < expected.length - (nbBytes - 1); i += nbBytes) {
			Number n = ioReader.get();
			assertThat(n).as("Read at " + i + "/" + expected.length).isEqualTo(dataReader.apply(expected, i));
		}
		assertThrows(EOFException.class, () -> ioReader.get());
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> ioReader.get());
	}
	

	@ParameterizedTest(name = "Generic data read: {0}")
	@ArgumentsSource(DataTestCasesProvider.class)
	void readDataSwitchByteOrder(String displayName, byte[] expected, int nbBytes, boolean signed, Function<byte[], BytesDataIO.Readable> ioSupplier) throws Exception {
		BytesDataIO.Readable io = ioSupplier.apply(expected);
		
		TriFunction<BytesData, byte[], Integer, Long> dataReader = signed ? (data,b,o) -> data.readSignedBytes(nbBytes, b, o) : (data,b,o) -> data.readUnsignedBytes(nbBytes, b, o);
		FailableSupplier<? extends Number, IOException> ioReader = signed ? () -> io.readSignedBytes(nbBytes) : () -> io.readUnsignedBytes(nbBytes);

		ByteOrder[] order = new ByteOrder[] { ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN };
		for (int i = 0, j = 0; i < expected.length - (nbBytes - 1); i += nbBytes, j++) {
			io.setByteOrder(order[j % 2]);
			Number n = ioReader.get();
			assertThat(n).as("Read at " + i + "/" + expected.length).isEqualTo(dataReader.apply(BytesData.of(order[j % 2]), expected, i));
		}
		io.setByteOrder(ByteOrder.LITTLE_ENDIAN);
		assertThrows(EOFException.class, () -> ioReader.get());
		io.setByteOrder(ByteOrder.BIG_ENDIAN);
		assertThrows(EOFException.class, () -> ioReader.get());
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> ioReader.get());
	}
	
}
