package net.lecousin.commons.reactive;

import java.util.LinkedList;
import java.util.function.Supplier;

import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

class BufferedFlux<T> {
	private final LinkedList<T> buffer = new LinkedList<>();
	private final Supplier<Mono<T>> itemSupplier;
	private final int advanced;
	private final FluxSink<T> sink;
	private boolean generating = false;
	private boolean endReached = false;
	private Throwable error = null;
	private long waiting = 0;
	
	BufferedFlux(FluxSink<T> sink, int advanced, Supplier<Mono<T>> itemSupplier) {
		this.sink = sink;
		this.advanced = advanced;
		this.itemSupplier = itemSupplier;
		if (advanced > 0) {
			generating = true;
			Schedulers.parallel().schedule(this::getNext);
		}
		sink.onRequest(this::provideNext);
	}
	
	private void getNext() {
		itemSupplier.get()
		.doOnSuccess(this::newItemReady)
		.subscribe(i -> { }, err -> {
			this.error = err;
			sink.error(err);
		});
	}
	
	private void newItemReady(T item) {
		boolean provideImmediately = false;
		boolean continueGenerating = true;
		boolean isGenerating;
		synchronized (buffer) {
			if (item == null) {
				endReached = true;
				continueGenerating = false;
				if (waiting > 0)
					provideImmediately = true;
			} else if (waiting > 0) {
				waiting--;
				provideImmediately = true;
			} else {
				buffer.add(item);
				if (buffer.size() >= advanced) {
					continueGenerating = false;
				}
			}
			isGenerating = generating;
			continueGenerating &= generating;
		}
		if (provideImmediately) {
			if (item == null)
				sink.complete();
			else
				sink.next(item);
		}
		if (continueGenerating)
			Schedulers.parallel().schedule(this::getNext);
		else if (isGenerating)
			generating = false;
	}
	
	private void provideNext(long n) {
		do {
			if (provideNextItem(n))
				break;
		} while (--n > 0);
	}
	
	private boolean provideNextItem(long n) {
		T item = null;
		boolean launchGeneration = false;
		boolean callEnd = false;
		synchronized (buffer) {
			if (buffer.isEmpty()) {
				if (endReached)
					callEnd = true;
				waiting += n;
			} else {
				item = buffer.removeFirst();
			}
			if (!generating && !endReached) {
				generating = true;
				launchGeneration = true;
			}
		}
		if (error != null)
			return true;
		if (callEnd) {
			sink.complete();
			return true;
		}
		if (item != null) {
			sink.next(item);
		}
		if (launchGeneration)
			Schedulers.parallel().schedule(this::getNext);
		return item == null;
	}

}
