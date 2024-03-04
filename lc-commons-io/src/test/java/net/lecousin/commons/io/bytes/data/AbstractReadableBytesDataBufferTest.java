package net.lecousin.commons.io.bytes.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.io.bytes.BytesIOTestUtils;
import net.lecousin.commons.test.ParameterizedTestUtils;
import net.lecousin.commons.test.ParameterizedTestUtils.CompositeArgumentsProvider;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadableBytesDataBufferTest implements TestCasesProvider<byte[], BytesDataBuffer.Readable> {

	public static class ByteArrayDataBufferArgumentsProvider extends CompositeArgumentsProvider {
		public ByteArrayDataBufferArgumentsProvider() {
			super(new ParameterizedTestUtils.TestCasesArgumentsProvider());
			List<Arguments> args = new LinkedList<>();
			for (int nbBytes = 1; nbBytes <= 8; ++nbBytes) {
				for (int size = nbBytes; size <= nbBytes * 10; size += nbBytes) {
					byte[] content = BytesIOTestUtils.generateContent(size);
					args.add(Arguments.of("Signed " + nbBytes + "-bytes using Little-Endian order on content of " + content.length + " byte(s)", nbBytes, ByteOrder.LITTLE_ENDIAN, true, content));
					args.add(Arguments.of("Signed " + nbBytes + "-bytes using Big-Endian order on content of " + content.length + " byte(s)", nbBytes, ByteOrder.BIG_ENDIAN, true, content));
					if (nbBytes != 8) {
						args.add(Arguments.of("Unsigned " + nbBytes + "-bytes using Little-Endian order on content of " + content.length + " byte(s)", nbBytes, ByteOrder.LITTLE_ENDIAN, false, content));
						args.add(Arguments.of("Unsigned " + nbBytes + "-bytes using Big-Endian order on content of " + content.length + " byte(s)", nbBytes, ByteOrder.BIG_ENDIAN, false, content));
					}
				}
			}
			add(args);
		}
	}
	
	private Supplier<? extends Number> getBufferReader(BytesDataBuffer.Readable buffer, int nbBytes, boolean signed) {
		switch (nbBytes) {
		case 1: return signed ? buffer::readSignedByte : buffer::readUnsignedByte;
		case 2: return signed ? buffer::readSigned2Bytes : buffer::readUnsigned2Bytes;
		case 3: return signed ? buffer::readSigned3Bytes : buffer::readUnsigned3Bytes;
		case 4: return signed ? buffer::readSigned4Bytes : buffer::readUnsigned4Bytes;
		case 5: return signed ? buffer::readSigned5Bytes : buffer::readUnsigned5Bytes;
		case 6: return signed ? buffer::readSigned6Bytes : buffer::readUnsigned6Bytes;
		case 7: return signed ? buffer::readSigned7Bytes : buffer::readUnsigned7Bytes;
		case 8: return buffer::readSigned8Bytes;
		}
		return null;
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(ByteArrayDataBufferArgumentsProvider.class)
	void test(String displayName, Function<byte[], BytesDataBuffer.Readable> bufferProvider, int nbBytes, ByteOrder order, boolean signed, byte[] content) {
		BytesDataBuffer.Readable buffer = bufferProvider.apply(content);
		if (!order.equals(buffer.getByteOrder()))
			buffer.setByteOrder(order);
		assertThat(buffer.remaining()).isEqualTo(content.length);
		
		BiFunction<byte[], Integer, Long> dataReader = signed ? (b,o) -> BytesData.of(order).readSignedBytes(nbBytes, b, o) : (b,o) -> BytesData.of(order).readUnsignedBytes(nbBytes, b, o);
		
		Supplier<? extends Number> bufferReader = getBufferReader(buffer, nbBytes, signed);
		Supplier<? extends Number> bufferReader2 = bufferReader;
		if (signed) {
			switch (nbBytes) {
			case 2: bufferReader2 = buffer::readShort; break;
			case 4: bufferReader2 = buffer::readInteger; break;
			case 8: bufferReader2 = buffer::readLong; break;
			}
		}
		
		boolean useReader2 = false;
		for (int i = 0; i < content.length; i += nbBytes) {
			assertThat((useReader2 ? bufferReader2 : bufferReader).get().longValue()).as("Byte " + i + "/" + content.length).isEqualTo(dataReader.apply(content, i));
			useReader2 = !useReader2;
		}
	}
	

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(ByteArrayDataBufferArgumentsProvider.class)
	void test2(String displayName, Function<byte[], BytesDataBuffer.Readable> bufferProvider, int nbBytes, ByteOrder order, boolean signed, byte[] content) {
		BytesDataBuffer.Readable buffer = bufferProvider.apply(content);
		if (!order.equals(buffer.getByteOrder()))
			buffer.setByteOrder(order);
		assertThat(buffer.remaining()).isEqualTo(content.length);
		
		BiFunction<byte[], Integer, Long> dataReader = signed ? (b,o) -> BytesData.of(order).readSignedBytes(nbBytes, b, o) : (b,o) -> BytesData.of(order).readUnsignedBytes(nbBytes, b, o);

		assertThrows(IllegalArgumentException.class, () -> buffer.readSignedBytes(0));
		assertThrows(IllegalArgumentException.class, () -> buffer.readSignedBytes(-1));
		assertThrows(IllegalArgumentException.class, () -> buffer.readSignedBytes(9));
		assertThrows(IllegalArgumentException.class, () -> buffer.readUnsignedBytes(0));
		assertThrows(IllegalArgumentException.class, () -> buffer.readUnsignedBytes(-1));
		assertThrows(IllegalArgumentException.class, () -> buffer.readUnsignedBytes(8));

		for (int i = 0; i < content.length; i += nbBytes) {
			long value = signed ? buffer.readSignedBytes(nbBytes) : buffer.readUnsignedBytes(nbBytes);
			assertThat(value).as("Byte " + i + "/" + content.length).isEqualTo(dataReader.apply(content, i));
		}
	}
	
}
