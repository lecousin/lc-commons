package net.lecousin.commons.reactive.io.bytes.utils;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.reactive.io.AbstractReactiveIO;
import net.lecousin.commons.reactive.io.ReactiveIOChecks;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * A Readable I/O, with <i>in-advance</i> buffering.<br/>
 * This implementation starts immediately reading from the given I/O, and fill up to the given number of buffers.<br/>
 * Once a buffer is read, it will start again reading to fill a new buffer.<br/>
 * If reading on this I/O is faster than buffering, no buffer is used.
 */
@SuppressWarnings({"java:S1181", "java:S2142"})
public class BufferedReadableReactiveBytesIO extends AbstractReactiveIO implements ReactiveBytesIO.Readable {

	private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);
	
	private ReactiveBytesIO.Readable io;
	private boolean closeIo;
	private boolean end = false;
	private Throwable error = null;
	private CompletableFuture<Subscription> subscription = new CompletableFuture<>();
	private CompletableFuture<ByteBuffer> currentBuffer = new CompletableFuture<>();
	
	/**
	 * Constructor.
	 * @param io I/O to buffer
	 * @param advancedBuffers maximum number of buffers to read in advance
	 * @param closeIo if true, the given I/O will be closed together with this I/O
	 */
	public BufferedReadableReactiveBytesIO(ReactiveBytesIO.Readable io, int advancedBuffers, boolean closeIo) {
		this.io = io;
		this.closeIo = closeIo;
		currentBuffer.complete(EMPTY);
		io.toFlux().publishOn(Schedulers.parallel(), advancedBuffers).subscribe(new S());
	}
	
	private final class S implements Subscriber<ByteBuffer> {

		@Override
		public void onSubscribe(Subscription s) {
			subscription.complete(s);
		}

		@Override
		public void onNext(ByteBuffer t) {
			currentBuffer.complete(t);
		}

		@Override
		public void onError(Throwable t) {
			error = t;
			currentBuffer.completeExceptionally(t);
		}

		@Override
		public void onComplete() {
			end = true;
			currentBuffer.cancel(false);
		}
		
	}
	
	@Override
	public Mono<Void> closeInternal() {
		return Mono.defer(() -> {
			ReactiveBytesIO.Readable i = io;
			io = null;
			if (i != null && closeIo)
				return Mono.fromFuture(subscription)
					.flatMap(s -> Mono.fromFuture(currentBuffer))
					.onErrorResume(e -> Mono.just(EMPTY))
					.flatMap(b -> i.close());
			return Mono.empty();
		});
	}
	
	@Override
	public Scheduler getScheduler() {
		return Schedulers.parallel();
	}
	
	private Mono<Void> goNext() {
		return Mono.fromFuture(subscription)
		.flatMap(s -> {
			if (end) return Mono.empty();
			if (error != null) return Mono.error(error);
			currentBuffer = new CompletableFuture<>();
			s.request(1);
			return waitNext();
		});
	}
	
	private Mono<Void> waitNext() {
		return Mono.fromFuture(currentBuffer)
			.onErrorResume(CancellationException.class, e -> Mono.empty())
			.then();
	}
	
	@Override
	public Mono<ByteBuffer> readBuffer() {
		return ReactiveIOChecks.deferNotClosed(this, () -> {
			if (currentBuffer.isDone()) {
				ByteBuffer b;
				try {
					b = currentBuffer.get();
				} catch (CancellationException e) {
					return Mono.empty();
				} catch (Throwable t) {
					return Mono.error(t);
				}
				if (b.hasRemaining()) {
					currentBuffer = new CompletableFuture<>();
					currentBuffer.complete(EMPTY);
					return Mono.just(b);
				}
				if (end) return Mono.empty();
				return goNext().then(readBuffer());
			}
			return waitNext().then(readBuffer());
		});
	}
	
	private Supplier<Mono<Integer>> doRead(ByteBuffer buffer) {
		return () -> {
			if (!buffer.hasRemaining()) return Mono.just(0);
			if (currentBuffer.isDone()) {
				ByteBuffer b;
				try {
					b = currentBuffer.get();
				} catch (CancellationException e) {
					return Mono.just(-1);
				} catch (Throwable t) {
					return Mono.error(t);
				}
				int l = b.remaining();
				if (l > 0) {
					int l2 = buffer.remaining();
					if (l <= l2) {
						buffer.put(b);
						currentBuffer = new CompletableFuture<>();
						currentBuffer.complete(EMPTY);
						return Mono.just(l);
					}
					int p = b.position();
					ByteBuffer slice = b.slice(p, l2);
					buffer.put(slice);
					b.position(p + l2);
					return Mono.just(l2);
				}
				if (end) return Mono.just(-1);
				return goNext().then(Mono.defer(doRead(buffer)));
			}
			return waitNext().then(Mono.defer(doRead(buffer)));
		};
	}
	
	@Override
	public Mono<Integer> readBytes(ByteBuffer buffer) {
		return ReactiveIOChecks.deferByteBuffer(this, buffer, doRead(buffer));
	}
	
	private Supplier<Mono<Byte>> doReadByte() {
		return () -> {
			if (currentBuffer.isDone()) {
				ByteBuffer b;
				try {
					b = currentBuffer.get();
				} catch (CancellationException e) {
					return Mono.error(new EOFException());
				} catch (Throwable t) {
					return Mono.error(t);
				}
				if (!b.hasRemaining()) {
					if (end) return Mono.error(new EOFException());
					return goNext().then(Mono.defer(doReadByte()));
				}
				byte result = b.get();
				if (!b.hasRemaining()) {
					currentBuffer = new CompletableFuture<>();
					currentBuffer.complete(EMPTY);
				}
				return Mono.just(result);
			}
			return waitNext().then(Mono.defer(doReadByte()));
		};
	}
	
	@Override
	public Mono<Byte> readByte() {
		return ReactiveIOChecks.deferNotClosed(this, doReadByte());
	}
	
	private Supplier<Mono<Long>> doSkipUpTo(long nbBytes) {
		return () -> {
			if (nbBytes < 0) return Mono.error(new NegativeValueException(nbBytes, "nbBytes"));
			if (nbBytes == 0) return Mono.just(0L);
			if (currentBuffer.isDone()) {
				ByteBuffer b;
				try {
					b = currentBuffer.get();
				} catch (CancellationException e) {
					return Mono.just(-1L);
				} catch (Throwable t) {
					return Mono.error(t);
				}
				int l = b.remaining();
				if (l > 0) {
					if (l <= nbBytes) {
						currentBuffer = new CompletableFuture<>();
						currentBuffer.complete(EMPTY);
						return Mono.just((long) l);
					}
					b.position(b.position() + (int) nbBytes);
					return Mono.just(nbBytes);
				}
				if (end) return Mono.just(-1L);
				return goNext().then(Mono.defer(doSkipUpTo(nbBytes)));
			}
			return waitNext().then(Mono.defer(doSkipUpTo(nbBytes)));
		};
	}
	
	@Override
	public Mono<Long> skipUpTo(long nbBytes) {
		return ReactiveIOChecks.deferNotClosed(this, doSkipUpTo(nbBytes));
	}
	
}
