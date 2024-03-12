package net.lecousin.commons.collections;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestLcArrayUtils {

	@Test
	void testCountByteArrays() {
		assertThat(LcArrayUtils.count(new byte[0][])).isZero();
		assertThat(LcArrayUtils.count(new byte[10])).isEqualTo(10);
		assertThat(LcArrayUtils.count(new byte[10], new byte[20])).isEqualTo(30);
		assertThat(LcArrayUtils.count(new byte[10], new byte[20], new byte[30])).isEqualTo(60);
	}
	
	@Test
	void testConcatByteArrays() {
		assertThat(LcArrayUtils.concat(new byte[0][]).length).isZero();
		Random random = new Random();
		byte[] b1 = new byte[10];
		random.nextBytes(b1);
		Assertions.assertArrayEquals(b1, LcArrayUtils.concat(b1));
		byte[] b2 = new byte[20];
		random.nextBytes(b2);
		byte[] b1b2 = new byte[30];
		System.arraycopy(b1, 0, b1b2, 0, b1.length);
		System.arraycopy(b2, 0, b1b2, b1.length, b2.length);
		Assertions.assertArrayEquals(b1b2, LcArrayUtils.concat(b1, b2));
		byte[] b3 = new byte[30];
		random.nextBytes(b3);
		Assertions.assertArrayEquals(LcArrayUtils.concat(b1b2, b3), LcArrayUtils.concat(b1, b2, b3));
	}

	@Test
	void testCountCharArrays() {
		assertThat(LcArrayUtils.count(new char[0][])).isZero();
		assertThat(LcArrayUtils.count(new char[10])).isEqualTo(10);
		assertThat(LcArrayUtils.count(new char[10], new char[20])).isEqualTo(30);
		assertThat(LcArrayUtils.count(new char[10], new char[20], new char[30])).isEqualTo(60);
	}
	
	@Test
	void testConcatCharArrays() {
		assertThat(LcArrayUtils.concat(new char[0][]).length).isZero();
		char[] b1 = "abcdefghij".toCharArray();
		Assertions.assertArrayEquals(b1, LcArrayUtils.concat(b1));
		char[] b2 = "²&é\"'(-è_çà)=ù€$¤~#{".toCharArray();
		char[] b1b2 = new char[30];
		System.arraycopy(b1, 0, b1b2, 0, b1.length);
		System.arraycopy(b2, 0, b1b2, b1.length, b2.length);
		Assertions.assertArrayEquals(b1b2, LcArrayUtils.concat(b1, b2));
		char[] b3 = "abcdefghijklmnopqrstuvwxyz0123".toCharArray();
		Assertions.assertArrayEquals(LcArrayUtils.concat(b1b2, b3), LcArrayUtils.concat(b1, b2, b3));
	}
	
}
