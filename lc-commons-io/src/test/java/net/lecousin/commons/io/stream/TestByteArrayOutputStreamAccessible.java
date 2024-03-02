package net.lecousin.commons.io.stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestByteArrayOutputStreamAccessible {
	
	@Test
	void test() {
		ByteArrayOutputStreamAccessible o;
		o = new ByteArrayOutputStreamAccessible();
		Assertions.assertEquals(0, o.getArrayCount());
		
		o = new ByteArrayOutputStreamAccessible(100);
		Assertions.assertEquals(100, o.getArray().length);
		Assertions.assertEquals(0, o.getArrayCount());
		
		byte[] b = new byte[100];
		o = new ByteArrayOutputStreamAccessible(b, 13);
		Assertions.assertSame(b, o.getArray());
		Assertions.assertEquals(13, o.getArrayCount());
	}

}
