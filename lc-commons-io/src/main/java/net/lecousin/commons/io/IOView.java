package net.lecousin.commons.io;

import java.io.IOException;

/**
 * Sub-view of an IO, wrapping the original IO. 
 * @param <T> type of IO
 */
public abstract class IOView<T extends IO> implements IO {
	
	protected T io;
	
	protected IOView(T io) {
		this.io = io;
	}

	@Override
	public boolean isClosed() {
		return io.isClosed();
	}

	@Override
	public void onClose(Runnable listener) {
		io.onClose(listener);
	}

	@Override
	public void close() throws IOException {
		io.close();
	}
	
}
