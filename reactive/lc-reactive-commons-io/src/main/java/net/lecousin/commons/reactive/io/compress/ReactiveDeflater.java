package net.lecousin.commons.reactive.io.compress;

import java.nio.ByteBuffer;
import java.util.zip.Deflater;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Delfater (compress), the reactive way.
 */
public class ReactiveDeflater {
	
	/** Default buffer size. */
	public static final int DEFAULT_BUFFER_SIZE = 4096;

	private final int level;
	private final boolean nowrap;
	private int outputBufferSize;
	private Deflater deflater = null;
	
	/**
	 * Constructor.
	 * @param level compression level
	 * @param nowrap nowrap
	 * @param outputBufferSize buffer size used to produce output
	 * @see Deflater
	 */
	public ReactiveDeflater(int level, boolean nowrap, int outputBufferSize) {
		this.level = level;
		this.nowrap = nowrap;
		this.outputBufferSize = outputBufferSize;
	}
	
	/**
	 * Constructor with default buffer size.
	 * @param level compression level
	 * @param nowrap nowrap
	 * @see Deflater
	 */
	public ReactiveDeflater(int level, boolean nowrap) {
		this(level, nowrap, DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * Compress data.
	 * @param source source data
	 * @return compressed data
	 */
	public Flux<ByteBuffer> deflate(Flux<ByteBuffer> source) {
		return source
			.concatMap(this::consume)
			.concatWith(Flux.defer(this::end));
	}
	
	private void init() {
		if (deflater == null)
			deflater = new Deflater(level, nowrap);
	}
	
	private Flux<ByteBuffer> consume(ByteBuffer source) {
		return Mono.fromCallable(() -> {
			init();
			deflater.setInput(source);
			return deflater;
		})
		.subscribeOn(Schedulers.parallel())
		.flatMapMany(def -> !source.hasRemaining() ? Flux.empty() : 
			Flux.<ByteBuffer>create(sink -> sink.onRequest(requested -> Schedulers.parallel().schedule(() -> {
				long n = requested;
				while (n-- > 0) {
					ByteBuffer out = ByteBuffer.allocate(outputBufferSize);
					if (deflater.deflate(out) == 0) {
						if (!deflater.needsInput()) {
							outputBufferSize *= 2;
							n++;
							continue;
						}
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
		return Mono.fromCallable(() -> {
			init();
			deflater.finish();
			return deflater;
		})
		.subscribeOn(Schedulers.parallel())
		.flatMapMany(def -> Flux.<ByteBuffer>create(sink -> sink.onRequest(requested -> Schedulers.parallel().schedule(() -> {
				long n = requested;
				while (n-- > 0) {
					ByteBuffer out = ByteBuffer.allocate(outputBufferSize);
					if (deflater.deflate(out) == 0) {
						sink.complete();
						break;
					}
					out.flip();
					sink.next(out);
				}
			})))
		).publishOn(Schedulers.parallel());
	}
	
}
