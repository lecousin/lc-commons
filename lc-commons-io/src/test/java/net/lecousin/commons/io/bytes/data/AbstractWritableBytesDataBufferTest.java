package net.lecousin.commons.io.bytes.data;

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
		buffer.setByteOrder(order);
		
		BiFunction<byte[], Integer, Long> dataReader = signed ? (b,o) -> BytesData.of(order).readSignedBytes(nbBytes, b, o) : (b,o) -> BytesData.of(order).readUnsignedBytes(nbBytes, b, o);
		
		Consumer<Long> bufferWriter = getBufferWriter(buffer, nbBytes, signed);
		
		for (int i = 0; i < content.length; i += nbBytes) {
			bufferWriter.accept(dataReader.apply(content, i));
		}
		
		checkWrittenData(buffer, tc.getObject(), content);
	}
	
}
