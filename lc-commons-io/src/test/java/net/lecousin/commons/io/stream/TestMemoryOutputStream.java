package net.lecousin.commons.io.stream;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestMemoryOutputStream {

	@Test
	void test() throws Exception {
		MemoryOutputStream out = new MemoryOutputStream(100);
		assertThat(out.getAllBuffers()).isEmpty();
		assertThat(out.getAllFilledBuffers()).isEmpty();
		assertThat(out.getNextBytes()).isEmpty();
		
		out.write(12);
		assertThat(out.getAllFilledBuffers()).isEmpty();
		assertThat(out.getNextBytes()).isPresent().hasValueSatisfying(buffer -> {
			assertThat(buffer.remaining()).isEqualTo(1);
			assertThat(buffer.readByte()).isEqualTo((byte) 12);
		});
		assertThat(out.getAllBuffers()).isEmpty();
		assertThat(out.getAllFilledBuffers()).isEmpty();
		assertThat(out.getNextBytes()).isEmpty();
		
		out.write(new byte[150]);
		assertThat(out.getAllFilledBuffers()).hasSize(1).anySatisfy(b -> {
			assertThat(b.remaining()).isEqualTo(150);
		});
		
		for (int i = 0; i < 99; ++i) {
			out.write(i);
			assertThat(out.getAllFilledBuffers()).isEmpty();
		}
		out.write(1);
		out.write(1);
		assertThat(out.getAllFilledBuffers()).hasSize(1).anyMatch(b -> b.remaining() == 100);
		assertThat(out.getAllBuffers()).hasSize(1).anyMatch(b -> b.remaining() == 1);
		
		out.write(1);
		out.write(new byte[25]);
		out.write(new byte[25]);
		out.write(new byte[25]);
		out.write(new byte[25]);
		assertThat(out.getAllFilledBuffers()).hasSize(2).anyMatch(b -> b.remaining() == 76).anyMatch(b -> b.remaining() == 25);
		assertThat(out.getAllBuffers()).isEmpty();
		
		out.close();
	}
	
}
