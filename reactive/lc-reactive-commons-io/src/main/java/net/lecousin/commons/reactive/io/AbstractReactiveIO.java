package net.lecousin.commons.reactive.io;

import net.lecousin.commons.reactive.events.ReactiveSingleEvent;
import reactor.core.publisher.Mono;

/**
 * Base class for a ReactiveIO, implementing the close event.
 */
public abstract class AbstractReactiveIO implements ReactiveIO {

	protected ReactiveSingleEvent<Void> closeEvent = new ReactiveSingleEvent<>();
	
	@Override
	public Mono<Void> close() {
		return closeEvent.emit(this.closeInternal());
	}
	
	protected abstract Mono<Void> closeInternal();
	
	@Override
	public boolean isClosed() {
		return closeEvent.isEmitted();
	}
	
	@Override
	public void onClose(Mono<Void> listener) {
		closeEvent.listen(() -> listener);
	}
	
}
