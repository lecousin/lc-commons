package net.lecousin.commons.reactive.io.compress;

import java.nio.ByteBuffer;
import java.util.zip.Inflater;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Inflater (uncompress), the reactive way.
 */
@RequiredArgsConstructor
public class ReactiveInflater {

	private final boolean nowrap;
	private final int outputBufferSize;
	private Inflater inflater = null;
	
	/**
	 * Uncompress the given source.
	 * @param source compressed data
	 * @return uncompressed data
	 */
	public Flux<ByteBuffer> inflate(Flux<ByteBuffer> source) {
		return source
			.concatMap(this::consume)
			.concatWith(Flux.defer(this::end));
	}
	
	private void init() {
		if (inflater == null) inflater = new Inflater(nowrap);
	}
	
	private Flux<ByteBuffer> consume(ByteBuffer source) {
		return Mono.fromCallable(() -> {
			init();
			inflater.setInput(source);
			return inflater;
		})
		.subscribeOn(Schedulers.parallel())
		.flatMapMany(inf -> !source.hasRemaining() ? Flux.empty() : 
			Flux.<ByteBuffer>create(sink -> sink.onRequest(requested -> Schedulers.parallel().schedule(() -> {
				long n = requested;
				while (n-- > 0) {
					ByteBuffer out = ByteBuffer.allocate(outputBufferSize);
					int nb;
					try {
						nb = inflater.inflate(out);
					} catch (Exception e) {
						sink.error(e);
						return;
					}
					if (nb == 0) {
						sink.complete();
						break;
					}
					out.flip();
					sink.next(out);
				}
			})))
		).publishOn(Schedulers.parallel());
	}
	
	private Flux<ByteBuffer> end() {
		return Flux.defer(() -> {
			init();
			inflater.end();
			return Flux.<ByteBuffer>empty();
		}).subscribeOn(Schedulers.parallel());
	}
	
}
