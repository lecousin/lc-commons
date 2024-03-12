package net.lecousin.commons.io.chars.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.junit.jupiter.api.Test;

class TestCharArray {

	@Test
	void testFromCharBuffer() {
		CharBuffer buffer = CharBuffer.allocate(100);
		buffer.put((char) 1);
		buffer.put((char) 2);
		CharArray a = new CharArray(buffer);
		assertThat(a.getArrayStartOffset()).isZero();
		assertThat(a.getPosition()).isEqualTo(2);
		assertThat(a.getSize()).isEqualTo(100);
		a.setPosition(0);
		assertThat(a.readChar()).isEqualTo((char) 1);
		assertThat(a.readChar()).isEqualTo((char) 2);
		
		buffer = ByteBuffer.allocateDirect(100).asCharBuffer();
		buffer.put((char) 1);
		buffer.put((char) 2);
		buffer.flip();
		a = new CharArray(buffer);
		assertThat(a.getArrayStartOffset()).isZero();
		assertThat(a.getPosition()).isZero();
		assertThat(a.getSize()).isEqualTo(2);
		assertThat(a.readChar()).isEqualTo((char) 1);
		assertThat(a.readChar()).isEqualTo((char) 2);
	}
	
	@Test
	void testToCharBuffer() {
		CharArray a = new CharArray(new char[10], 3, 4);
		CharBuffer b = a.toCharBuffer();
		assertThat(b.remaining()).isEqualTo(4);
	}
	
	@Test
	void testAsCharSequence() {
		CharArray a = new CharArray("abcdefg".toCharArray());
		assertThat(a.length()).isEqualTo(7);
		assertThat(a.charAt(3)).isEqualTo('d');
		assertThat(a.subSequence(2, 5).toString()).isEqualTo("cde");
		assertThat(a.toString()).isEqualTo("abcdefg");
	}
	
}
