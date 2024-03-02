package net.lecousin.commons.io;

import java.io.IOException;

import net.lecousin.commons.events.SingleEvent;

/**
 * Abstract class for an IO, implementing the close event.
 */
public abstract class AbstractIO implements IO {
	
	/** Close event. */
	protected SingleEvent<IO> closeEvent = new SingleEvent<>();

	/** Close internal resources.
	 * @throws IOException
	 */
	protected abstract void closeInternal() throws IOException;
	
	@Override
	public final void close() throws IOException {
		if (isClosed()) return;
		closeInternal();
		closeEvent.emit(this);
	}
	
	@Override
	public boolean isClosed() {
		return closeEvent.isEmitted();
	}
	
	@Override
	public void onClose(Runnable listener) {
		closeEvent.listen(listener);
	}
	
}
