package net.lecousin.commons.reactive.io.bytes.utils;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.reactive.io.AbstractReactiveIO;
import net.lecousin.commons.reactive.io.ReactiveIOChecks;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Create a readable I/O from a Flux of ByteBuffer.<br/>
 * This I/O request buffers to the Flux on-demand, and do not buffer data in advance.
 */
public class ReactiveReadableBytesIOFromFlux extends AbstractReactiveIO implements ReactiveBytesIO.Readable {
	
	private CompletableFuture<Subscription> subscription = new CompletableFuture<>();
	private CompletableFuture<ByteBuffer> nextBuffer = null;
	private boolean end = false;
	private Throwable error = null;

	/**
	 * Constructor.
	 * @param flux flux providing buffers
	 */
	public ReactiveReadableBytesIOFromFlux(Flux<ByteBuffer> flux) {
		flux.subscribe(new Subscriber<>() {
			@Override
			public void onSubscribe(Subscription s) {
				if (subscription.isCancelled())
					s.cancel();
				else
					subscription.complete(s);
			}
			
			public void onNext(ByteBuffer buffer) {
				nextBuffer.complete(buffer);
			}
			
			@Override
			public void onError(Throwable t) {
				error = t;
				if (!subscription.isDone())
					subscription.completeExceptionally(t);
				if (nextBuffer != null && !nextBuffer.isDone())
					nextBuffer.completeExceptionally(t);
			}
			
			@Override
			public void onComplete() {
				end = true;
				if (nextBuffer != null)
					nextBuffer.complete(null);
			}
		});
	}

	
	@SuppressWarnings({"java:S1181", "java:S2142"})
	private Mono<ByteBuffer> needData() {
		if (nextBuffer == null) {
			if (end) return Mono.empty();
			if (error != null) return Mono.error(error);
			return Mono.fromFuture(subscription)
				.flatMap(s -> {
					nextBuffer = new CompletableFuture<>();
					s.request(1);
					return Mono.fromFuture(nextBuffer);
				});
		}
		try {
			ByteBuffer b = nextBuffer.get();
			if (b == null)
				return Mono.empty();
			if (!b.hasRemaining()) {
				nextBuffer = null;
				return needData();
			}
			return Mono.just(b);
		} catch (Throwable t) {
			return Mono.error(t);
		}
	}
	
	@Override
	public Scheduler getScheduler() {
		return Schedulers.parallel();
	}

	@SuppressWarnings({"java:S2142"})
	@Override
	public Mono<Void> closeInternal() {
		return Mono.fromRunnable(() -> {
			if (subscription == null) return;
			if (!subscription.isDone())
				subscription.cancel(false);
			try {
				subscription.get().cancel();
			} catch (Exception e) {
				// nothing
			}
			subscription = null;
		});
	}

	
	@Override
	public Mono<ByteBuffer> readBuffer() {
		return ReactiveIOChecks.deferNotClosed(this, () ->
			needData()
			.map(b -> {
				nextBuffer = null; // completely consumed
				return b;
			})
		);
	}
	
	@Override
	public Mono<Byte> readByte() {
		return ReactiveIOChecks.deferNotClosed(this, () ->
		needData()
		.map(b -> {
			byte value = b.get();
			if (!b.hasRemaining())
				nextBuffer = null; // completely consumed
			return value;
		})
		.switchIfEmpty(Mono.error(new EOFException()))
	);
	}
	
	@Override
	public Mono<Integer> readBytes(ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBuffer(this, buffer, () -> {
			if (buffer.remaining() == 0) return Mono.just(0);
			return needData()
			.map(b -> {
				int l = b.remaining();
				int l2 = buffer.remaining();
				if (l > l2) {
					int p = b.limit();
					b.limit(b.position() + l2);
					buffer.put(b);
					b.limit(p);
					return l2;
				}
				buffer.put(b);
				nextBuffer = null; // completely consumed
				return l;
			})
			.switchIfEmpty(Mono.just(-1));
		});
	}

	@Override
	public Mono<Long> skipUpTo(long toSkip) {
		return ReactiveIOChecks.deferNotClosedAnd(this, () -> NegativeValueException.checker(toSkip, "toSkip"), () -> {
			if (toSkip == 0) return Mono.just(0L);
			return needData().map(b -> {
				int r = b.remaining();
				if (toSkip >= r) {
					nextBuffer = null; // completely consumed
					return (long) r;
				}
				b.position(b.position() + (int) toSkip);
				return toSkip;
			}).switchIfEmpty(Mono.just(-1L));
		});
	}

}
