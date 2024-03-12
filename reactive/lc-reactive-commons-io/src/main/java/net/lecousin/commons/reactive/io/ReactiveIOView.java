package net.lecousin.commons.reactive.io;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

/**
* Sub-view of a ReactiveIO, wrapping the original IO. 
* @param <T> type of IO
*/
public abstract class ReactiveIOView<T extends ReactiveIO> implements ReactiveIO {

	protected T io;
	
	protected ReactiveIOView(T io) {
		this.io = io;
	}

	@Override
	public Mono<Void> close() {
		return io.close();
	}

	@Override
	public boolean isClosed() {
		return io.isClosed();
	}

	@Override
	public void onClose(Mono<Void> listener) {
		io.onClose(listener);
	}
	
	@Override
	public Scheduler getScheduler() {
		return io.getScheduler();
	}
	
}
