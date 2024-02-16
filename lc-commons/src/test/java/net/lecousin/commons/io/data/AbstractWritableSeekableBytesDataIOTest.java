package net.lecousin.commons.io.data;

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

import lombok.AllArgsConstructor;
import lombok.Data;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.function.BiConsumerThrows;
import net.lecousin.commons.io.data.AbstractReadableBytesDataIOTest.DataArguments;
import net.lecousin.commons.test.ParameterizedTestUtils;
import net.lecousin.commons.test.ParameterizedTestUtils.CompositeArgumentsProvider;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractWritableSeekableBytesDataIOTest implements TestCasesProvider<Integer, AbstractWritableSeekableBytesDataIOTest.WritableSeekableTestCase> {

	@Data
	@AllArgsConstructor
	public static class WritableSeekableTestCase {
		private BytesDataIO.Writable.Seekable stream;
		private Object data;
	}
	
	protected abstract void checkWrittenData(BytesDataIO.Writable.Seekable stream, Object generatedData, byte[] expected);
	
	@Nested
	public class AsWritable extends AbstractWritableBytesDataIOTest {
		
		@Override
		public List<? extends TestCase<Integer, AbstractWritableBytesDataIOTest.WritableTestCase>> getTestCases() {
			return AbstractWritableSeekableBytesDataIOTest.this.getTestCases()
				.stream()
				.map(tc -> new TestCase<>(
					tc.getName(),
					(Function<Integer, AbstractWritableBytesDataIOTest.WritableTestCase>)
					(size) -> {
						WritableSeekableTestCase stc = tc.getArgumentProvider().apply(size);
						return new AbstractWritableBytesDataIOTest.WritableTestCase((BytesDataIO.Writable) stc.getStream(), stc.getData());
					}
				))
				.toList();
		}
		
		@Override
		protected void checkWrittenData(BytesDataIO.Writable stream, Object generatedData, byte[] expected) {
			AbstractWritableSeekableBytesDataIOTest.this.checkWrittenData((BytesDataIO.Writable.Seekable) stream, generatedData, expected);
		}
		
	}
	
	@Nested
	public class AsSeekable extends AbstractSeekableBytesDataIOTest {

		@Override
		public List<? extends TestCase<Integer, BytesDataIO.Seekable>> getTestCases() {
			return AbstractWritableSeekableBytesDataIOTest.this.getTestCases()
				.stream()
				.map(tc -> new TestCase<>(tc.getName(),
					(Function<Integer, BytesDataIO.Seekable>) (size) -> (BytesDataIO.Seekable) tc.getArgumentProvider().apply(size).getStream()))
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
	void testWriteFullyAt(String displayName, byte[] data, Function<Integer, WritableSeekableTestCase> streamSupplier) throws Exception {
		WritableSeekableTestCase tc = streamSupplier.apply(data.length);
		BytesDataIO.Writable.Seekable stream = tc.getStream();
		stream.writeFullyAt(0, new byte[0]);
		stream.writeFullyAt(data.length, new byte[0]);
		stream.writeFullyAt(data.length / 3, data, data.length / 3, data.length - data.length / 3);
		stream.writeFullyAt(0, new byte[0]);
		stream.writeFullyAt(0, data, 0, data.length / 3);
		stream.writeFullyAt(0, new byte[0]);
		assertThrows(EOFException.class, () -> stream.writeFullyAt(data.length, new byte[1]));
		stream.writeFullyAt(data.length, new byte[0]);
		checkWrittenData(stream, tc.getData(), data);
		assertThrows(NullPointerException.class, () -> stream.writeFullyAt(0, null));
		stream.close();
		assertThrows(ClosedChannelException.class, () -> stream.writeFullyAt(0, new byte[1]));
		assertThrows(ClosedChannelException.class, () -> stream.writeFullyAt(0, new byte[1], 0, 0));
		assertThrows(ClosedChannelException.class, () -> stream.writeFullyAt(data.length, new byte[0]));
		assertThrows(ClosedChannelException.class, () -> stream.writeFullyAt(data.length, new byte[1]));
		assertThrows(ClosedChannelException.class, () -> stream.writeFullyAt(0, null));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataArguments.class)
	void testDataAt(String displayName, byte[] data, Function<Integer, WritableSeekableTestCase> streamSupplier, int nbBytes, boolean signed) throws Exception {
		WritableSeekableTestCase tc = streamSupplier.apply(data.length);
		BytesDataIO.Writable.Seekable stream = tc.getStream();
		long initialPos = stream.position();
		BiConsumerThrows<Long, Number> ioWriter = null;
		BiConsumerThrows<Long, Number> ioWriter2 = null;
		BytesData endianness = stream.getEndianness().equals(BytesDataIO.Endianness.LITTLE_ENDIAN) ? BytesData.LE : BytesData.BE;
		BiFunction<byte[], Integer, ? extends Number> dataReader = null;
		switch (nbBytes) {
		case 1:
			ioWriter = signed ? (p,v) -> stream.writeByteAt(p,v.byteValue()) : (p,v) -> stream.writeUnsignedByteAt(p, v.intValue());
			dataReader = signed ? (b,o) -> b[o] : (b,o) -> b[o] & 0xFF;
			break;
		case 2:
			ioWriter = signed ? (p,v) -> stream.writeSigned2BytesAt(p,v.shortValue()) : (p,v) -> stream.writeUnsigned2BytesAt(p,v.intValue());
			dataReader = signed ? endianness::readSigned2Bytes : endianness::readUnsigned2Bytes;
			break;
		case 3:
			ioWriter = signed ? (p,v) -> stream.writeSigned3BytesAt(p,v.intValue()) : (p,v) -> stream.writeUnsigned3BytesAt(p,v.intValue());
			dataReader = signed ? endianness::readSigned3Bytes : endianness::readUnsigned3Bytes;
			break;
		case 4:
			ioWriter = signed ? (p,v) -> stream.writeSigned4BytesAt(p,v.intValue()) : (p,v) -> stream.writeUnsigned4BytesAt(p,v.longValue());
			dataReader = signed ? endianness::readSigned4Bytes : endianness::readUnsigned4Bytes;
			break;
		case 5:
			ioWriter = signed ? (p,v) -> stream.writeSigned5BytesAt(p,v.longValue()) : (p,v) -> stream.writeUnsigned5BytesAt(p,v.longValue());
			dataReader = signed ? endianness::readSigned5Bytes : endianness::readUnsigned5Bytes;
			break;
		case 6:
			ioWriter = signed ? (p,v) -> stream.writeSigned6BytesAt(p,v.longValue()) : (p,v) -> stream.writeUnsigned6BytesAt(p,v.longValue());
			dataReader = signed ? endianness::readSigned6Bytes : endianness::readUnsigned6Bytes;
			break;
		case 7:
			ioWriter = signed ? (p,v) -> stream.writeSigned7BytesAt(p,v.longValue()) : (p,v) -> stream.writeUnsigned7BytesAt(p,v.longValue());
			dataReader = signed ? endianness::readSigned7Bytes : endianness::readUnsigned7Bytes;
			break;
		case 8:
			ioWriter = (p,v) -> stream.writeSigned8BytesAt(p,v.longValue());
			dataReader = endianness::readSigned8Bytes;
			break;
		}
		if (signed) {
			switch (nbBytes) {
			case 2: ioWriter2 = (p,v) -> stream.writeShortAt(p,v.shortValue()); break;
			case 4: ioWriter2 = (p,v) -> stream.writeIntegerAt(p,v.intValue()); break;
			case 8: ioWriter2 = (p,v) -> stream.writeLongAt(p,v.longValue()); break;
			}
		}
		if (ioWriter2 == null) ioWriter2 = ioWriter;

		boolean useWriter2 = false;
		int i;
		for (i = 0; i < data.length - nbBytes + 1; i += nbBytes) {
			Number value = dataReader.apply(data, i);
			(useWriter2 ? ioWriter2 : ioWriter).accept((long) i, value);
			useWriter2 = !useWriter2;
			Assertions.assertEquals(initialPos, stream.position());
		}
		// finish
		if (i < data.length) {
			stream.writeFullyAt(i, data, i, data.length - i);
			Assertions.assertEquals(initialPos, stream.position());
		}
		
		BiConsumerThrows<Long, Number> w = ioWriter;
		BiConsumerThrows<Long, Number> w2 = ioWriter2;
		if (data.length > 0)
			for (i = 1; i < nbBytes && i <= data.length; ++i) {
				int j = i;
				Assertions.assertThrows(EOFException.class, () -> w.accept((long) (data.length - j), 0L), "Write at " + (data.length - j));
				Assertions.assertThrows(EOFException.class, () -> w2.accept((long) (data.length - j), 0L), "Write at " + (data.length - j));
			}
		Assertions.assertThrows(EOFException.class, () -> w.accept((long) data.length, 0L));
		Assertions.assertThrows(NegativeValueException.class, () -> w.accept(-1L, 0L));
		Assertions.assertEquals(initialPos, stream.position());
		
		checkWrittenData(stream, tc.getData(), data);

		assertFalse(stream.isClosed());
		stream.close();
		assertTrue(stream.isClosed());
		assertThrows(ClosedChannelException.class, () -> w.accept(0L, 0L));
		assertThrows(ClosedChannelException.class, () -> w2.accept(0L, 0L));
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(DataArguments.class)
	void testData2At(String displayName, byte[] data, Function<Integer, WritableSeekableTestCase> streamSupplier, int nbBytes, boolean signed) throws Exception {
		WritableSeekableTestCase tc = streamSupplier.apply(data.length);
		BytesDataIO.Writable.Seekable stream = tc.getStream();
		long initialPos = stream.position();
		BiConsumerThrows<Long, Long> ioWriter =
			signed ? (p,v) -> stream.writeSignedBytesAt(p, nbBytes, v) : (p,v) -> stream.writeUnsignedBytesAt(p, nbBytes, v);
		BytesData endianness = stream.getEndianness().equals(BytesDataIO.Endianness.LITTLE_ENDIAN) ? BytesData.LE : BytesData.BE;
		BiFunction<byte[], Integer, ? extends Number> dataReader =
			signed ? (b,o) -> endianness.readSignedBytes(nbBytes, b, o) : (b,o) -> endianness.readUnsignedBytes(nbBytes, b, o);

		int i;
		for (i = 0; i < data.length - nbBytes + 1; i += nbBytes) {
			Number value = dataReader.apply(data, i);
			ioWriter.accept((long) i, value.longValue());
			Assertions.assertEquals(initialPos, stream.position());
		}
		// finish
		if (i < data.length) {
			stream.writeFullyAt(i, data, i, data.length - i);
			Assertions.assertEquals(initialPos, stream.position());
		}
		
		if (data.length > 0)
			for (i = 1; i < nbBytes && i <= data.length; ++i) {
				int j = i;
				Assertions.assertThrows(EOFException.class, () -> ioWriter.accept((long) (data.length - j), 0L));
			}
		Assertions.assertThrows(EOFException.class, () -> ioWriter.accept((long) data.length, 0L));
		Assertions.assertThrows(NegativeValueException.class, () -> ioWriter.accept(-1L, 0L));
		Assertions.assertEquals(initialPos, stream.position());
		
		checkWrittenData(stream, tc.getData(), data);
		
		assertThrows(IllegalArgumentException.class, () -> stream.writeSignedBytesAt(0L, -1, 0L));
		assertThrows(IllegalArgumentException.class, () -> stream.writeSignedBytesAt(0L, 9, 0L));
		assertThrows(IllegalArgumentException.class, () -> stream.writeUnsignedBytesAt(0L, -1, 0L));
		assertThrows(IllegalArgumentException.class, () -> stream.writeUnsignedBytesAt(0L, 8, 0L));

		assertFalse(stream.isClosed());
		stream.close();
		assertTrue(stream.isClosed());
		assertThrows(ClosedChannelException.class, () -> ioWriter.accept(0L, 0L));
	}

}
