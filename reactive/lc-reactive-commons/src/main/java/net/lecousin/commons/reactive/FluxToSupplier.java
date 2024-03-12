package net.lecousin.commons.reactive;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

/**
 * Create a supplier of elements, from a Flux.<br/>
 * This class subscribe to the Flux immediately, but do not request any element yet.
 * Each time the get method is called, an item is requested to the Flux.<br/>
 * When the Flux is completed, an empty Mono is returned.
 * 
 * @param <T> type of element
 */
public class FluxToSupplier<T> implements Supplier<Mono<T>> {

	private CompletableFuture<Subscription> sub = new CompletableFuture<>();
	private MonoSink<T> sink;
	private boolean end = false;
	private Throwable error = null;
	
	/**
	 * Constructor.
	 * @param input Flux of items
	 */
	public FluxToSupplier(Flux<T> input) {
		input.subscribe(new Subscriber<>() {

			@Override
			public void onSubscribe(Subscription s) {
				sub.complete(s);
			}

			@Override
			public void onNext(T item) {
				sink.success(item);
				sink = null;
			}

			@Override
			public void onError(Throwable t) {
				end = true;
				error = t;
				if (sink != null) {
					sink.error(t);
					sink = null;
				}
			}

			@Override
			public void onComplete() {
				end = true;
				if (sink != null) {
					sink.success();
					sink = null;
				}
			}
			
		});
	}
	
	@Override
	public Mono<T> get() {
		return Mono.create(s -> {
			if (end) {
				if (error != null)
					s.error(error);
				else
					s.success();
				return;
			}
			this.sink = s;
			s.onRequest(n -> Mono.fromFuture(sub).subscribe(subscription -> subscription.request(1)));
		});
	}
	
}
