package net.lecousin.commons.reactive.io.bytes.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.io.bytes.BytesIOTestUtils.RandomContentWithBufferSizeProvider;
import reactor.core.publisher.Flux;

class TestOutputStreamAsFlux {

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(RandomContentWithBufferSizeProvider.class)
	void test(String displayName, byte[] content, int bufferSize) throws Exception {
		OutputStreamAsFlux converter = new OutputStreamAsFlux(bufferSize);
		Flux<ByteBuffer> flux = converter.createFlux();
		byte[] found = new byte[content.length];
		AtomicInteger pos = new AtomicInteger(0);
		CompletableFuture<Void> done = new CompletableFuture<>();
		flux.subscribe(
			next -> {
				int nb = next.remaining();
				next.get(found, pos.get(), nb);
				pos.addAndGet(nb);
			},
			error -> done.completeExceptionally(error),
			() -> {
				assertThat(pos.get()).isEqualTo(content.length);
				assertThat(found).containsExactly(content);
				done.complete(null);
			}
		);
		for (int i = 0; i < content.length && i < 10; i++)
			converter.write(content[i]);
		if (content.length >= 10)
			converter.write(content, 10, content.length - 10);
		converter.close();
		done.get(10, TimeUnit.SECONDS);
	}
	
}
