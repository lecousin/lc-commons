package net.lecousin.commons.io.bytes.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.function.FailableFunction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.bytes.AbstractReadableSeekableBytesIOTest;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.bytes.data.BytesDataIOTestUtils.DataTestCasesProvider;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadableSeekableBytesDataIOTest implements TestCasesProvider<byte[], BytesDataIO.Readable.Seekable> {

	@Nested
	public class AsReadableSeekableBytesIO extends AbstractReadableSeekableBytesIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesIO.Readable.Seekable>> getTestCases() {
			return AbstractReadableSeekableBytesDataIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<byte[], BytesIO.Readable.Seekable>) (content) -> tc.getArgumentProvider().apply(content)))
				.toList();
		}
	}

	@Nested
	public class AsReadableBytesDataIO extends AbstractReadableBytesDataIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesDataIO.Readable>> getTestCases() {
			return AbstractReadableSeekableBytesDataIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<byte[], BytesDataIO.Readable>) (content) -> tc.getArgumentProvider().apply(content).asReadableBytesDataIO()))
				.toList();
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataTestCasesProvider.class)
	void readDataAt(String displayName, byte[] expected, int nbBytes, boolean signed, Function<byte[], BytesDataIO.Readable.Seekable> ioSupplier) throws Exception {
		BytesDataIO.Readable.Seekable io = ioSupplier.apply(expected);

		BytesData data = BytesData.of(io.getByteOrder());
		BiFunction<byte[], Integer, Long> dataReader = signed ? (b,o) -> data.readSignedBytes(nbBytes, b, o) : (b,o) -> data.readUnsignedBytes(nbBytes, b, o);

		FailableFunction<Long, ? extends Number, IOException> ioReader = null;
		FailableFunction<Long, ? extends Number, IOException> ioReader2 = null;
		switch (nbBytes) {
		case 1: ioReader = signed ? io::readByteAt : io::readUnsignedByteAt; break;
		case 2: ioReader = signed ? io::readSigned2BytesAt : io::readUnsigned2BytesAt; break;
		case 3: ioReader = signed ? io::readSigned3BytesAt : io::readUnsigned3BytesAt; break;
		case 4: ioReader = signed ? io::readSigned4BytesAt : io::readUnsigned4BytesAt; break;
		case 5: ioReader = signed ? io::readSigned5BytesAt : io::readUnsigned5BytesAt; break;
		case 6: ioReader = signed ? io::readSigned6BytesAt : io::readUnsigned6BytesAt; break;
		case 7: ioReader = signed ? io::readSigned7BytesAt : io::readUnsigned7BytesAt; break;
		case 8: ioReader = io::readSigned8BytesAt; break;
		}
		if (signed) {
			switch (nbBytes) {
			case 2: ioReader2 = io::readShortAt; break;
			case 4: ioReader2 = io::readIntegerAt; break;
			case 8: ioReader2 = io::readLongAt; break;
			}
		}
		if (ioReader2 == null) ioReader2 = ioReader;
		boolean useReader2 = false;
		for (int i = 0; i < expected.length - (nbBytes - 1); i++) {
			Number n = (useReader2 ? ioReader2 : ioReader).apply((long) i);
			assertThat(n.longValue()).as("Read at " + i + "/" + expected.length).isEqualTo(dataReader.apply(expected, i));
			useReader2 = !useReader2;
		}
		
		FailableFunction<Long, ? extends Number, IOException> r = ioReader;
		FailableFunction<Long, ? extends Number, IOException> r2 = ioReader2;
		assertThrows(EOFException.class, () -> r.apply((long) expected.length));
		assertThrows(EOFException.class, () -> r2.apply((long) expected.length));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> r.apply(0L));
		assertThrows(ClosedChannelException.class, () -> r2.apply(0L));
		assertThrows(ClosedChannelException.class, () -> r.apply((long) expected.length));
		assertThrows(ClosedChannelException.class, () -> r2.apply((long) expected.length));
	}

	@ParameterizedTest(name = "Generic data read: {0}")
	@ArgumentsSource(DataTestCasesProvider.class)
	void readData2(String displayName, byte[] expected, int nbBytes, boolean signed, Function<byte[], BytesDataIO.Readable.Seekable> ioSupplier) throws Exception {
		BytesDataIO.Readable.Seekable io = ioSupplier.apply(expected);
		
		assertThrows(IllegalArgumentException.class, () -> io.readSignedBytesAt(0, 9));
		assertThrows(IllegalArgumentException.class, () -> io.readSignedBytesAt(0, -1));
		assertThrows(IllegalArgumentException.class, () -> io.readSignedBytesAt(0, 0));
		assertThrows(IllegalArgumentException.class, () -> io.readUnsignedBytesAt(0, 8));
		assertThrows(IllegalArgumentException.class, () -> io.readUnsignedBytesAt(0, -1));
		assertThrows(IllegalArgumentException.class, () -> io.readUnsignedBytesAt(0, 0));
		assertThrows(NegativeValueException.class, () -> io.readUnsignedBytesAt(-1, 1));

		BytesData data = BytesData.of(io.getByteOrder());
		BiFunction<byte[], Integer, Long> dataReader = signed ? (b,o) -> data.readSignedBytes(nbBytes, b, o) : (b,o) -> data.readUnsignedBytes(nbBytes, b, o);

		FailableFunction<Long, ? extends Number, IOException> ioReader = signed ? p -> io.readSignedBytesAt(p, nbBytes) : p -> io.readUnsignedBytesAt(p, nbBytes);
		
		for (int i = 0; i < expected.length - (nbBytes - 1); i += nbBytes) {
			Number n = ioReader.apply((long) i);
			assertThat(n).as("Read at " + i + "/" + expected.length).isEqualTo(dataReader.apply(expected, i));
		}
		assertThrows(EOFException.class, () -> ioReader.apply((long) expected.length));
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> ioReader.apply(0L));
	}

}
