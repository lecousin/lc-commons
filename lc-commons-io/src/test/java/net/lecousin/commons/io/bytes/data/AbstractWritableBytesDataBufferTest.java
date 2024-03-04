package net.lecousin.commons.io.bytes.data;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteOrder;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.lecousin.commons.io.bytes.data.AbstractReadableBytesDataBufferTest.ByteArrayDataBufferArgumentsProvider;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractWritableBytesDataBufferTest implements TestCasesProvider<Integer, AbstractWritableBytesDataBufferTest.WritableTestCase<?, ?>> {

	protected abstract void checkWrittenData(BytesDataBuffer.Writable buffer, Object object, byte[] expected);
	
	@Data
	@AllArgsConstructor
	public static class WritableTestCase<T extends BytesDataBuffer.Writable, O> {
		private T buffer;
		private O object;
		
		public WritableTestCase(T buffer) {
			this(buffer, null);
		}
	}
	
	private Consumer<Long> getBufferWriter(BytesDataBuffer.Writable buffer, int nbBytes, boolean signed) {
		switch (nbBytes) {
		case 1: return signed ? v -> buffer.writeSignedByte(v.byteValue()) : v -> buffer.writeUnsignedByte(v.intValue());
		case 2: return signed ? v -> buffer.writeSigned2Bytes(v.shortValue()) : v -> buffer.writeUnsigned2Bytes(v.intValue());
		case 3: return signed ? v -> buffer.writeSigned3Bytes(v.intValue()) : v -> buffer.writeUnsigned3Bytes(v.intValue());
		case 4: return signed ? v -> buffer.writeSigned4Bytes(v.intValue()) : buffer::writeUnsigned4Bytes;
		case 5: return signed ? buffer::writeSigned5Bytes : buffer::writeUnsigned5Bytes;
		case 6: return signed ? buffer::writeSigned6Bytes : buffer::writeUnsigned6Bytes;
		case 7: return signed ? buffer::writeSigned7Bytes : buffer::writeUnsigned7Bytes;
		case 8: return buffer::writeSigned8Bytes;
		}
		return null;
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(ByteArrayDataBufferArgumentsProvider.class)
	void test(String displayName, Function<Integer, WritableTestCase<?, ?>> bufferProvider, int nbBytes, ByteOrder order, boolean signed, byte[] content) {
		WritableTestCase<?, ?> tc = bufferProvider.apply(content.length);
		BytesDataBuffer.Writable buffer = tc.getBuffer();
		if (!order.equals(buffer.getByteOrder()))
			buffer.setByteOrder(order);
		buffer.setByteOrder(order);
		
		BiFunction<byte[], Integer, Long> dataReader = signed ? (b,o) -> BytesData.of(order).readSignedBytes(nbBytes, b, o) : (b,o) -> BytesData.of(order).readUnsignedBytes(nbBytes, b, o);
		
		Consumer<Long> bufferWriter = getBufferWriter(buffer, nbBytes, signed);
		Consumer<Long> bufferWriter2 = bufferWriter;
		if (signed) {
			switch (nbBytes) {
			case 2: bufferWriter2 = v -> buffer.writeShort(v.shortValue()); break;
			case 4: bufferWriter2 = v -> buffer.writeInteger(v.intValue()); break;
			case 8: bufferWriter2 = buffer::writeLong; break;
			}
		}
		
		boolean useWriter2 = false;
		for (int i = 0; i < content.length; i += nbBytes) {
			(useWriter2 ? bufferWriter2 : bufferWriter).accept(dataReader.apply(content, i));
			useWriter2 = !useWriter2;
		}
		
		checkWrittenData(buffer, tc.getObject(), content);
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(ByteArrayDataBufferArgumentsProvider.class)
	void test2(String displayName, Function<Integer, WritableTestCase<?, ?>> bufferProvider, int nbBytes, ByteOrder order, boolean signed, byte[] content) {
		WritableTestCase<?, ?> tc = bufferProvider.apply(content.length);
		BytesDataBuffer.Writable buffer = tc.getBuffer();
		if (!order.equals(buffer.getByteOrder()))
			buffer.setByteOrder(order);
		buffer.setByteOrder(order);
		
		BiFunction<byte[], Integer, Long> dataReader = signed ? (b,o) -> BytesData.of(order).readSignedBytes(nbBytes, b, o) : (b,o) -> BytesData.of(order).readUnsignedBytes(nbBytes, b, o);
		
		assertThrows(IllegalArgumentException.class, () -> buffer.writeSignedBytes(0, 0));
		assertThrows(IllegalArgumentException.class, () -> buffer.writeSignedBytes(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> buffer.writeSignedBytes(9, 0));
		assertThrows(IllegalArgumentException.class, () -> buffer.writeUnsignedBytes(0, 0));
		assertThrows(IllegalArgumentException.class, () -> buffer.writeUnsignedBytes(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> buffer.writeUnsignedBytes(8, 0));
		
		for (int i = 0; i < content.length; i += nbBytes) {
			long value = dataReader.apply(content, i);
			if (signed)
				buffer.writeSignedBytes(nbBytes, value);
			else
				buffer.writeUnsignedBytes(nbBytes, value);
		}
		
		checkWrittenData(buffer, tc.getObject(), content);
	}
	
}
