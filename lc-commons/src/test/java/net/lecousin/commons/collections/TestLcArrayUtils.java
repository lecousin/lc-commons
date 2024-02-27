package net.lecousin.commons.collections;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;

import org.junit.jupiter.api.Test;

class TestLcArrayUtils {

	@Test
	void testCountByteArrays() {
		assertThat(LcArrayUtils.count()).isZero();
		assertThat(LcArrayUtils.count(new byte[10])).isEqualTo(10);
		assertThat(LcArrayUtils.count(new byte[10], new byte[20])).isEqualTo(30);
		assertThat(LcArrayUtils.count(new byte[10], new byte[20], new byte[30])).isEqualTo(60);
	}
	
	@Test
	void testConcatByteArrays() {
		assertThat(LcArrayUtils.concat().length).isZero();
		Random random = new Random();
		byte[] b1 = new byte[10];
		random.nextBytes(b1);
		assertThat(LcArrayUtils.concat(b1)).containsExactly(b1);
		byte[] b2 = new byte[20];
		random.nextBytes(b2);
		byte[] b1b2 = new byte[30];
		System.arraycopy(b1, 0, b1b2, 0, b1.length);
		System.arraycopy(b2, 0, b1b2, b1.length, b2.length);
		assertThat(LcArrayUtils.concat(b1, b2)).containsExactly(b1b2);
		byte[] b3 = new byte[30];
		random.nextBytes(b3);
		assertThat(LcArrayUtils.concat(b1, b2, b3)).containsExactly(LcArrayUtils.concat(b1b2, b3));
	}
	
}
