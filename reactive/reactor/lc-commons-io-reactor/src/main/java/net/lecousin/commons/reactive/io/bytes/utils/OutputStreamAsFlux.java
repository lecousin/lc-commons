package net.lecousin.commons.reactive.io.bytes.utils;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/** An OutputStream that can be converted into Flux of ByteBuffer.
 * <p>
 * Before any write is performed, the method {@link #createFlux()} must be called
 * to create a Flux, and the Flux must be subscribed.
 * </p>
 * <p>
 * Each write operation is buffered, each time a buffer is full it is emitted to the Flux.
 * </p>
 * <p>
 * At any time, the method {@link #flushAndCompleteCurrentFlux()} can be called, to
 * flush (emit any buffered data on the current Flux) and complete the Flux. Once called,
 * it is forbidden to write any data, until the method {@link #createFlux()} is called again.
 * </p>
 * <p>
 * When this output stream is closed, it is flushed and the current Flux is completed.
 * </p>
 */
public class OutputStreamAsFlux extends OutputStream {

	private int bufferSize;
	private FluxSink<ByteBuffer> sink;
	private ByteBuffer currentBuffer;
	
	/**
	 * Constructor.
	 * @param bufferSize size of the buffer: written data are buffered before to be emitted on the Flux
	 */
	public OutputStreamAsFlux(int bufferSize) {
		this.bufferSize = bufferSize;
		this.currentBuffer = ByteBuffer.allocate(bufferSize);
	}
	
	/**
	 * Create a Flux so data can be written to this output stream.
	 * @return the Flux
	 */
	public Flux<ByteBuffer> createFlux() {
		if (sink != null) throw new IllegalStateException();
		return Flux.create(s -> this.sink = s);
	}
	
	/**
	 * flush (emit any buffered data on the current Flux) and complete the Flux.
	 */
	public void flushAndCompleteCurrentFlux() {
		flush();
		if (sink != null) {
			sink.complete();
			sink = null;
		}
	}

	@Override
	public void write(int b) {
		currentBuffer.put((byte) b);
		if (!currentBuffer.hasRemaining()) {
			if (sink == null) throw new IllegalStateException();
			sink.next(currentBuffer.flip());
			currentBuffer = ByteBuffer.allocate(bufferSize);
		}
	}
	
	@Override
	public void write(byte[] b, int off, int len) {
		while (len > 0) {
			if (len < currentBuffer.remaining()) {
				currentBuffer.put(b, off, len);
				return;
			}
			int l = currentBuffer.remaining();
			currentBuffer.put(b, off, l);
			if (sink == null) throw new IllegalStateException();
			sink.next(currentBuffer.flip());
			currentBuffer = ByteBuffer.allocate(bufferSize);
			len -= l;
			off += l;
		}
	}
	
	@Override
	public void flush() {
		if (currentBuffer.position() > 0 && sink != null) {
			sink.next(currentBuffer.flip());
			currentBuffer = ByteBuffer.allocate(bufferSize);
		}
	}
	
	@Override
	public void close() {
		flushAndCompleteCurrentFlux();
	}
	
}
