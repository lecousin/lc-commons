package net.lecousin.commons.reactive.io.compress;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.io.bytes.BytesIOTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

class TestDeflate {

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(BytesIOTestUtils.RandomContentProvider.class)
	void testDeflateInflate(String displayName, byte[] data) {
		Flux<ByteBuffer> source = Flux.just(ByteBuffer.wrap(data));
		List<ByteBuffer> result = 
			new ReactiveInflater(true, 128).inflate(
				new ReactiveDeflater(Deflater.DEFAULT_COMPRESSION, true, 128)
				.deflate(source).publishOn(Schedulers.parallel(), 1)
			)
			.collectList().block();
		int pos = 0;
		for (ByteBuffer b : result) {
			byte[] buf = new byte[b.remaining()];
			byte[] expected = new byte[buf.length];
			System.arraycopy(data, pos, expected, 0, buf.length);
			b.get(buf);
			assertThat(buf).containsExactly(expected);
			pos += buf.length;
		}
		assertThat(pos).isEqualTo(data.length);
	}

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(BytesIOTestUtils.RandomContentProvider.class)
	void testDeflateInflateDefaultBufferSize(String displayName, byte[] data) {
		Flux<ByteBuffer> source = Flux.just(ByteBuffer.wrap(data));
		List<ByteBuffer> result = 
			new ReactiveInflater(true).inflate(
				new ReactiveDeflater(Deflater.DEFAULT_COMPRESSION, true)
				.deflate(source)
			)
			.collectList().block();
		int pos = 0;
		for (ByteBuffer b : result) {
			byte[] buf = new byte[b.remaining()];
			byte[] expected = new byte[buf.length];
			System.arraycopy(data, pos, expected, 0, buf.length);
			b.get(buf);
			assertThat(buf).containsExactly(expected);
			pos += buf.length;
		}
		assertThat(pos).isEqualTo(data.length);
	}
	
	@Test
	void testInflaterError() {
		var result = new ReactiveInflater(true).inflate(Flux.concat(Mono.just(ByteBuffer.allocate(0)), Mono.just(ByteBuffer.allocate(100))))
			.collectList();
		StepVerifier.create(result).expectError(DataFormatException.class).verify();
	}

}
