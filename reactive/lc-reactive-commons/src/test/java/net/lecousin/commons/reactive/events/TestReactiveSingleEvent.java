package net.lecousin.commons.reactive.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import net.lecousin.commons.events.Cancellable;
import reactor.core.publisher.Mono;

class TestReactiveSingleEvent {

	@Test
	void testNoListeners() {
		ReactiveSingleEvent<Integer> event = new ReactiveSingleEvent<>();
		assertThat(event.isEmitted()).isFalse();
		assertThat(event.hasListeners()).isFalse();
		event.emit(Mono.just(1)).block();
		assertThat(event.isEmitted()).isTrue();
		assertThat(event.hasListeners()).isFalse();
	}
	
	@Test
	void testSubscribeButUnsubscribeBeforeEvent() {
		ReactiveSingleEvent<Integer> event = new ReactiveSingleEvent<>();
		assertThat(event.isEmitted()).isFalse();
		assertThat(event.hasListeners()).isFalse();
		AtomicInteger called = new AtomicInteger(0);
		Cancellable subscription = event.subscribe(value -> {
			called.set(value);
			return Mono.empty();
		});
		AtomicBoolean called2 = new AtomicBoolean(false);
		Cancellable subscription2 = event.subscribe(() -> {
			called2.set(true);
			return Mono.empty();
		});
		
		assertThat(event.isEmitted()).isFalse();
		assertThat(event.hasListeners()).isTrue();
		subscription.cancel();
		subscription2.cancel();
		assertThat(event.isEmitted()).isFalse();
		assertThat(event.hasListeners()).isFalse();
		assertThat(called.get()).isZero();
		assertThat(called2.get()).isFalse();
		
		event.emit(Mono.just(1)).block();
		assertThat(called.get()).isZero();
		assertThat(called2.get()).isFalse();
	}
	
	@Test
	void testOneListener() {
		ReactiveSingleEvent<Integer> event = new ReactiveSingleEvent<>();
		AtomicInteger called = new AtomicInteger(0);
		Cancellable subscription = event.subscribe(value -> {
			called.set(value);
			return Mono.empty();
		});
		Mono<Void> result = event.emit(Mono.just(10).delayElement(Duration.ofSeconds(1)));
		assertThat(called.get()).isZero();
		assertThat(event.isEmitted()).isTrue();
		result.block();
		assertThat(called.get()).isEqualTo(10);
		assertThat(event.hasListeners()).isFalse();
		subscription.cancel();
	}
	
	@Test
	void test2Listeners() {
		ReactiveSingleEvent<Integer> event = new ReactiveSingleEvent<>();
		AtomicInteger called1 = new AtomicInteger(0);
		Cancellable subscription1 = event.subscribe(value -> {
			called1.set(value);
			return Mono.empty();
		});
		AtomicBoolean called2 = new AtomicBoolean(false);
		Cancellable subscription2 = event.subscribe(() -> {
			called2.set(true);
			return Mono.empty();
		});
		assertThat(event.hasListeners()).isTrue();
		Mono<Void> result = event.emit(Mono.just(10).delayElement(Duration.ofSeconds(1)));
		assertThat(called1.get()).isZero();
		assertThat(called2.get()).isFalse();
		assertThat(event.isEmitted()).isTrue();
		result.block();
		assertThat(called1.get()).isEqualTo(10);
		assertThat(called2.get()).isTrue();
		assertThat(event.hasListeners()).isFalse();
		subscription1.cancel();
		subscription2.cancel();
	}
	
	@Test
	void testSubscribeAfterEmission() {
		ReactiveSingleEvent<Integer> event = new ReactiveSingleEvent<>();
		event.emit(Mono.just(10)).block();
		assertThat(event.isEmitted()).isTrue();
		
		AtomicInteger called = new AtomicInteger(0);
		Cancellable subscription = event.subscribe(value -> {
			called.set(value);
			return Mono.empty();
		});
		assertThat(called.get()).isEqualTo(10);
		assertThat(event.hasListeners()).isFalse();
		subscription.cancel();
		
		called.set(0);
		
		subscription = event.subscribe(() -> {
			called.set(1);
			return Mono.empty();
		});
		assertThat(called.get()).isEqualTo(1);
		assertThat(event.hasListeners()).isFalse();
		subscription.cancel();
	}
	
}
