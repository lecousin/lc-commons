package net.lecousin.commons.io.bytes.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.bytes.BytesIOTestUtils.RandomContentProvider;
import net.lecousin.commons.io.bytes.memory.ByteArray;

class TestBytesIOToInputStream {

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentProvider.class)
	void testReadAllBytes(String displayName, byte[] content) throws Exception {
		BytesIO.Readable io = new ByteArray(content).asBytesIO();
		try (BytesIOToInputStream in = new BytesIOToInputStream(io)) {
			Assertions.assertArrayEquals(content, in.readAllBytes());
			assertThat(in.read()).isEqualTo(-1);
			assertThat(in.skip(1)).isEqualTo(-1);
		}
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentProvider.class)
	void testSkipAndReadByte(String displayName, byte[] content) throws Exception {
		BytesIO.Readable io = new ByteArray(content).asBytesIO();
		int pos = 0;
		try (BytesIOToInputStream in = new BytesIOToInputStream(io)) {
			while (pos < content.length) {
				assertThat(in.read()).isEqualTo(content[pos++] & 0xFF);
				long n = in.skip(10);
				long expected = Math.min(content.length - pos, 10);
				if (pos == content.length) expected = -1;
				assertThat(n).isEqualTo(expected);
				if (expected > 0) pos += expected;
			}
		}
	}
	
}
