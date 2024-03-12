package net.lecousin.commons.reactive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class TestFluxToSupplier {

	@Test
	void test() {
		Flux<Integer> flux = Flux.fromIterable(Arrays.asList(10, 7, 9, 36));
		FluxToSupplier<Integer> test = new FluxToSupplier<>(flux);
		assertThat(test.get().block()).isEqualTo(10);
		assertThat(test.get().block()).isEqualTo(7);
		assertThat(test.get().block()).isEqualTo(9);
		assertThat(test.get().block()).isEqualTo(36);
		assertThat(test.get().block()).isNull();
	}

	@Test
	void testDelayed() {
		Flux<Integer> flux = Flux.fromIterable(Arrays.asList(10, 7, 9, 36)).delayElements(Duration.ofMillis(100));
		FluxToSupplier<Integer> test = new FluxToSupplier<>(flux);
		assertThat(test.get().block()).isEqualTo(10);
		assertThat(test.get().block()).isEqualTo(7);
		assertThat(test.get().block()).isEqualTo(9);
		assertThat(test.get().block()).isEqualTo(36);
		assertThat(test.get().block()).isNull();
	}
	
	@Test
	void testError() {
		Flux<Integer> flux = Flux.concat(Mono.just(10), Mono.just(7), Mono.error(new RuntimeException("error-3")), Mono.just(9));
		FluxToSupplier<Integer> test = new FluxToSupplier<>(flux);
		assertThat(test.get().block()).isEqualTo(10);
		assertThat(test.get().block()).isEqualTo(7);
		assertThrows(RuntimeException.class, () -> test.get().block(), "error-3");
		assertThrows(RuntimeException.class, () -> test.get().block(), "error-3");
	}
	
	@Test
	void testEmpty() {
		Flux<Integer> flux = Flux.empty();
		FluxToSupplier<Integer> test = new FluxToSupplier<>(flux);
		assertThat(test.get().block()).isNull();
	}
	
	@Test
	void testDelayedEnd() {
		Flux<Integer> flux = Flux.concat(Mono.just(10), Mono.defer(() -> Mono.just(0).delayElement(Duration.ofSeconds(1)).flatMap(i -> Mono.empty())));
		FluxToSupplier<Integer> test = new FluxToSupplier<>(flux);
		assertThat(test.get().block()).isEqualTo(10);
		assertThat(test.get().block()).isNull();
	}
	
	@Test
	void testDelayedError() {
		Flux<Integer> flux = Flux.concat(Mono.just(10), Mono.just(7), Mono.defer(() -> Mono.just(0).delayElement(Duration.ofSeconds(1)).flatMap(i -> Mono.error(new RuntimeException("error-3")))), Mono.just(9));
		FluxToSupplier<Integer> test = new FluxToSupplier<>(flux);
		assertThat(test.get().block()).isEqualTo(10);
		assertThat(test.get().block()).isEqualTo(7);
		assertThrows(RuntimeException.class, () -> test.get().block(), "error-3");
		assertThrows(RuntimeException.class, () -> test.get().block(), "error-3");
	}
	
}
