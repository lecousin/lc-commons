package net.lecousin.commons.io.stream;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestSkipHeaderOutputStream {

	@Test
	void test() throws Exception {
		ByteArrayOutputStreamAccessible out2 = new ByteArrayOutputStreamAccessible();
		SkipHeaderOutputStream out = new SkipHeaderOutputStream(out2, 4);
		out.write(new byte[0]);
		assertThat(out2.getArrayCount()).isZero();
		out.write(1);
		assertThat(out2.getArrayCount()).isZero();
		out.write(new byte[2]);
		assertThat(out2.getArrayCount()).isZero();
		out.write(new byte[5]);
		assertThat(out2.getArrayCount()).isEqualTo(4);
		out.write(1);
		assertThat(out2.getArrayCount()).isEqualTo(5);
		out.write(new byte[5]);
		assertThat(out2.getArrayCount()).isEqualTo(10);
		out.write(new byte[0]);
		assertThat(out2.getArrayCount()).isEqualTo(10);
		out.close();
	}
	
}
