package net.lecousin.commons.io.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.EOFException;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.function.BiFunctionThrows;
import net.lecousin.commons.function.FunctionThrows;
import net.lecousin.commons.io.data.AbstractReadableBytesDataIOTest.DataArguments;
import net.lecousin.commons.test.ParameterizedTestUtils;
import net.lecousin.commons.test.ParameterizedTestUtils.CompositeArgumentsProvider;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadableSeekableBytesDataIOTest implements TestCasesProvider<byte[], BytesDataIO.Readable.Seekable> {

	@Nested
	public class AsReadable extends AbstractReadableBytesDataIOTest {
		
		@Override
		public List<? extends TestCase<byte[], BytesDataIO.Readable>> getTestCases() {
			return AbstractReadableSeekableBytesDataIOTest.this.getTestCases()
				.stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<byte[], BytesDataIO.Readable>) (content) -> (BytesDataIO.Readable) tc.getArgumentProvider().apply(content)))
				.toList();
		}
		
	}
	
	@Nested
	public class AsSeekable extends AbstractSeekableBytesDataIOTest {

		@Override
		public List<? extends TestCase<Integer, BytesDataIO.Seekable>> getTestCases() {
			return AbstractReadableSeekableBytesDataIOTest.this.getTestCases()
				.stream()
				.map(tc -> new TestCase<>(tc.getName(),
					(Function<Integer, BytesDataIO.Seekable>) (size) -> (BytesDataIO.Seekable) tc.getArgumentProvider().apply(new byte[size])))
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
	void testReadFullyAt(String displayName, byte[] data, Function<byte[], BytesDataIO.Readable.Seekable> streamSupplier) throws Exception {
		BytesDataIO.Readable.Seekable stream = streamSupplier.apply(data);
		stream.readFullyAt(0, new byte[0]);
		byte[] found = new byte[data.length];
		stream.readFullyAt(data.length - data.length / 3, found, data.length - data.length / 3, data.length / 3);
		stream.readFullyAt(0, found, 0, data.length - data.length / 3);
		assertThat(found).containsExactly(data);
		stream.readFullyAt(data.length, new byte[0]);
		assertThrows(EOFException.class, () -> stream.readFullyAt(data.length, new byte[1]));
		assertThrows(NullPointerException.class, () -> stream.readFullyAt(0, null));
		stream.close();
		assertThrows(ClosedChannelException.class, () -> stream.readFullyAt(0, new byte[1]));
		assertThrows(ClosedChannelException.class, () -> stream.readFullyAt(0, new byte[0]));
		assertThrows(ClosedChannelException.class, () -> stream.readFullyAt(0, new byte[1], 0, 0));
		assertThrows(ClosedChannelException.class, () -> stream.readFullyAt(data.length, new byte[1]));
		assertThrows(ClosedChannelException.class, () -> stream.readFullyAt(0, null));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataArguments.class)
	void testDataAt(String displayName, byte[] data, Function<byte[], BytesDataIO.Readable.Seekable> streamSupplier, int nbBytes, boolean signed) throws Exception {
		BytesDataIO.Readable.Seekable stream = streamSupplier.apply(data);
		long initialPos = stream.position();
		FunctionThrows<Long, ? extends Number> ioReader = null;
		FunctionThrows<Long, ? extends Number> ioReader2 = null;
		BytesData endianness = stream.getEndianness().equals(BytesDataIO.Endianness.LITTLE_ENDIAN) ? BytesData.LE : BytesData.BE;
		BiFunction<byte[], Integer, ? extends Number> dataReader = null;
		switch (nbBytes) {
		case 1:
			ioReader = signed ? stream::readByteAt : pos -> stream.readByteAt(pos) & 0xFF;
			dataReader = signed ? (b,o) -> b[o] : (b,o) -> b[o] & 0xFF;
			break;
		case 2:
			ioReader = signed ? stream::readSigned2BytesAt : stream::readUnsigned2BytesAt;
			dataReader = signed ? endianness::readSigned2Bytes : endianness::readUnsigned2Bytes;
			break;
		case 3:
			ioReader = signed ? stream::readSigned3BytesAt : stream::readUnsigned3BytesAt;
			dataReader = signed ? endianness::readSigned3Bytes : endianness::readUnsigned3Bytes;
			break;
		case 4:
			ioReader = signed ? stream::readSigned4BytesAt : stream::readUnsigned4BytesAt;
			dataReader = signed ? endianness::readSigned4Bytes : endianness::readUnsigned4Bytes;
			break;
		case 5:
			ioReader = signed ? stream::readSigned5BytesAt : stream::readUnsigned5BytesAt;
			dataReader = signed ? endianness::readSigned5Bytes : endianness::readUnsigned5Bytes;
			break;
		case 6:
			ioReader = signed ? stream::readSigned6BytesAt : stream::readUnsigned6BytesAt;
			dataReader = signed ? endianness::readSigned6Bytes : endianness::readUnsigned6Bytes;
			break;
		case 7:
			ioReader = signed ? stream::readSigned7BytesAt : stream::readUnsigned7BytesAt;
			dataReader = signed ? endianness::readSigned7Bytes : endianness::readUnsigned7Bytes;
			break;
		case 8:
			ioReader = stream::readSigned8BytesAt;
			dataReader = endianness::readSigned8Bytes;
			break;
		}
		if (signed) {
			switch (nbBytes) {
			case 2: ioReader2 = stream::readShortAt; break;
			case 4: ioReader2 = stream::readIntegerAt; break;
			case 8: ioReader2 = stream::readLongAt; break;
			}
		}

		for (int i = 0; i < data.length - nbBytes; i++) {
			Assertions.assertEquals(dataReader.apply(data, i), ioReader.apply((long) i));
			Assertions.assertEquals(initialPos, stream.position());
			if (ioReader2 != null) {
				Assertions.assertEquals(dataReader.apply(data, i), ioReader2.apply((long) i));
				Assertions.assertEquals(initialPos, stream.position());
			}
		}
		
		FunctionThrows<Long, ? extends Number> r = ioReader;
		FunctionThrows<Long, ? extends Number> r2 = ioReader2;
		if (data.length > 0)
			for (int i = 1; i < nbBytes && i <= data.length; ++i) {
				int j = i;
				Assertions.assertThrows(EOFException.class, () -> r.apply((long) (data.length - j)));
				if (r2 != null)
					Assertions.assertThrows(EOFException.class, () -> r2.apply((long) (data.length - j)));
			}
		Assertions.assertThrows(EOFException.class, () -> r.apply((long) data.length));
		Assertions.assertThrows(NegativeValueException.class, () -> r.apply(-1L));
		Assertions.assertEquals(initialPos, stream.position());

		assertFalse(stream.isClosed());
		stream.close();
		assertTrue(stream.isClosed());
		assertThrows(ClosedChannelException.class, () -> r.apply(0L));
		if (r2 != null)
			assertThrows(ClosedChannelException.class, () -> r2.apply(0L));
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataArguments.class)
	void testData2At(String displayName, byte[] data, Function<byte[], BytesDataIO.Readable.Seekable> streamSupplier, int nbBytes, boolean signed) throws Exception {
		BytesDataIO.Readable.Seekable stream = streamSupplier.apply(data);
		long initialPos = stream.position();
		BiFunctionThrows<Long, Integer, ? extends Number> ioReader =
			signed ? stream::readSignedBytesAt : stream::readUnsignedBytesAt;
		BytesData endianness = stream.getEndianness().equals(BytesDataIO.Endianness.LITTLE_ENDIAN) ? BytesData.LE : BytesData.BE;
		BiFunction<byte[], Integer, ? extends Number> dataReader =
			signed ? (b,o) -> endianness.readSignedBytes(nbBytes, b, o) : (b,o) -> endianness.readUnsignedBytes(nbBytes, b, o);

		for (int i = 0; i < data.length - nbBytes; i++) {
			Assertions.assertEquals(dataReader.apply(data, i), ioReader.apply((long) i, nbBytes));
			Assertions.assertEquals(initialPos, stream.position());
		}
		
		BiFunctionThrows<Long, Integer, ? extends Number> r = ioReader;
		if (data.length > 0)
			for (int i = 1; i < nbBytes && i <= data.length; ++i) {
				int j = i;
				Assertions.assertThrows(EOFException.class, () -> r.apply((long) (data.length - j), nbBytes));
			}
		Assertions.assertThrows(EOFException.class, () -> r.apply((long) data.length, nbBytes));
		Assertions.assertThrows(NegativeValueException.class, () -> r.apply(-1L, nbBytes));
		Assertions.assertEquals(initialPos, stream.position());

		assertThrows(IllegalArgumentException.class, () -> stream.readSignedBytesAt(0, -1));
		assertThrows(IllegalArgumentException.class, () -> stream.readSignedBytesAt(0, 9));
		assertThrows(IllegalArgumentException.class, () -> stream.readUnsignedBytesAt(0, -1));
		assertThrows(IllegalArgumentException.class, () -> stream.readUnsignedBytesAt(0, 8));

		assertFalse(stream.isClosed());
		stream.close();
		assertTrue(stream.isClosed());
		assertThrows(ClosedChannelException.class, () -> r.apply(0L, nbBytes));
	}
	
}
