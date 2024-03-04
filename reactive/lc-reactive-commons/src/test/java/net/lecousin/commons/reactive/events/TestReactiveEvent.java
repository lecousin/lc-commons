package net.lecousin.commons.reactive.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.lecousin.commons.events.Cancellable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TestReactiveEvent {

	@Test
	void testNoListener() {
		ReactiveEvent<Integer> event = new ReactiveEvent<>();
		
		assertThat(event.hasListeners()).isFalse();
		
		event.emit(Mono.just(1)).block();
		event.emit(Mono.just(2)).block();
	}
	
	private static class Listener {
		
		private List<Integer> received = new LinkedList<>();
		private Cancellable subscription;
	
		private Mono<Void> listenMonoVoid(Integer value) {
			return Mono.defer(() -> {
				synchronized (received) {
					received.add(value);
				}
				return Mono.empty();
			});
		}
		
		private Mono<Integer> listenMonoValue(Integer value) {
			return Mono.fromCallable(() -> {
				synchronized (received) {
					received.add(value);
				}
				return value + 1;
			});
		}
		
		private Flux<Integer> listenFlux(Integer value) {
			return Mono.fromRunnable(() -> {
				synchronized (received) {
					received.add(value);
				}
			}).thenMany(Flux.just(1, 2, 3));
		}
		
		private Mono<Void> listenSimple() {
			return Mono.fromRunnable(() -> {
				synchronized (received) {
					received.add(0);
				}
			});
		}
		
		private Mono<Integer> listenError() {
			return Mono.fromCallable(() -> {
				throw new IOException("test-error");
			});
		}
		
	}
	
	@Test
	void testListeners() {
		ReactiveEvent<Integer> event = new ReactiveEvent<>();
		
		event.emit(Mono.just(1)).block();
		
		assertThat(event.hasListeners()).isFalse();
		
		Listener l1 = new Listener();
		l1.subscription = event.subscribe(l1::listenMonoVoid);
		
		assertThat(event.hasListeners()).isTrue();
		
		event.emit(Mono.just(2)).block();
		
		assertThat(event.hasListeners()).isTrue();
		assertThat(l1.received).containsExactly(2);
		
		Listener l2 = new Listener();
		l2.subscription = event.subscribe(l2::listenMonoValue);
		
		event.emit(Mono.just(3)).block();
		
		assertThat(event.hasListeners()).isTrue();
		assertThat(l1.received).containsExactly(2, 3);
		assertThat(l2.received).containsExactly(3);
		
		Listener l3 = new Listener();
		l3.subscription = event.subscribe(l3::listenFlux);
		
		event.emit(Mono.just(4)).block();
		
		assertThat(event.hasListeners()).isTrue();
		assertThat(l1.received).containsExactly(2, 3, 4);
		assertThat(l2.received).containsExactly(3, 4);
		assertThat(l3.received).containsExactly(4);
		
		Listener l4 = new Listener();
		l4.subscription = event.subscribe(l4::listenSimple);
		
		event.emit(Mono.just(5)).block();
		
		assertThat(event.hasListeners()).isTrue();
		assertThat(l1.received).containsExactly(2, 3, 4, 5);
		assertThat(l2.received).containsExactly(3, 4, 5);
		assertThat(l3.received).containsExactly(4, 5);
		assertThat(l4.received).containsExactly(0);
		
		l1.subscription.cancel();
		
		event.emit(Mono.just(6)).block();
		
		assertThat(event.hasListeners()).isTrue();
		assertThat(l1.received).containsExactly(2, 3, 4, 5);
		assertThat(l2.received).containsExactly(3, 4, 5, 6);
		assertThat(l3.received).containsExactly(4, 5, 6);
		assertThat(l4.received).containsExactly(0, 0);

		l2.subscription.cancel();
		
		event.emit(Mono.just(7)).block();
		
		assertThat(event.hasListeners()).isTrue();
		assertThat(l1.received).containsExactly(2, 3, 4, 5);
		assertThat(l2.received).containsExactly(3, 4, 5, 6);
		assertThat(l3.received).containsExactly(4, 5, 6, 7);
		assertThat(l4.received).containsExactly(0, 0, 0);

		l3.subscription.cancel();
		
		event.emit(Mono.just(8)).block();
		
		assertThat(event.hasListeners()).isTrue();
		assertThat(l1.received).containsExactly(2, 3, 4, 5);
		assertThat(l2.received).containsExactly(3, 4, 5, 6);
		assertThat(l3.received).containsExactly(4, 5, 6, 7);
		assertThat(l4.received).containsExactly(0, 0, 0, 0);
		
		l4.subscription.cancel();
		
		event.emit(Mono.just(9)).block();
		
		assertThat(event.hasListeners()).isFalse();
		assertThat(l1.received).containsExactly(2, 3, 4, 5);
		assertThat(l2.received).containsExactly(3, 4, 5, 6);
		assertThat(l3.received).containsExactly(4, 5, 6, 7);
		assertThat(l4.received).containsExactly(0, 0, 0, 0);
	}
	
	@Test
	void testListenerError() {
		ReactiveEvent<Integer> event = new ReactiveEvent<>();
		Listener l1 = new Listener();
		l1.subscription = event.subscribe(l1::listenMonoVoid);
		Listener l2 = new Listener();
		l2.subscription = event.subscribe(l1::listenError);
		
		StepVerifier.create(event.emit(Mono.just(1))).expectError(IOException.class).verify();
		assertThat(l1.received).containsExactly(1);
		
		Listener l3 = new Listener();
		l3.subscription = event.subscribe(l1::listenError);
		
		StepVerifier.create(event.emit(Mono.just(2))).expectError(RuntimeException.class).verify();
		assertThat(l1.received).containsExactly(1, 2);
	}
	
}
