package net.lecousin.commons.io.bytes.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

class TestByteArray {

	@Test
	void testFromByteBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(100);
		buffer.put((byte) 1);
		buffer.put((byte) 2);
		ByteArray a = new ByteArray(buffer);
		assertThat(a.getArrayStartOffset()).isZero();
		assertThat(a.getPosition()).isEqualTo(2);
		assertThat(a.getSize()).isEqualTo(100);
		a.setPosition(0);
		assertThat(a.readByte()).isEqualTo((byte) 1);
		assertThat(a.readByte()).isEqualTo((byte) 2);
		
		buffer = ByteBuffer.allocateDirect(100);
		buffer.put((byte) 1);
		buffer.put((byte) 2);
		buffer.flip();
		a = new ByteArray(buffer);
		assertThat(a.getArrayStartOffset()).isZero();
		assertThat(a.getPosition()).isZero();
		assertThat(a.getSize()).isEqualTo(2);
		assertThat(a.readByte()).isEqualTo((byte) 1);
		assertThat(a.readByte()).isEqualTo((byte) 2);
	}
	
	@Test
	void testToByteBuffer() {
		ByteArray a = new ByteArray(new byte[10], 3, 4);
		ByteBuffer b = a.toByteBuffer();
		assertThat(b.remaining()).isEqualTo(4);
	}
	
}
