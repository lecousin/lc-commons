package net.lecousin.commons.io.utils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.IO;

/**
 * Abstract class for Sub-IO.
 * @param <I> type of IO
 */
public abstract class AbstractSubIO<I extends IO> extends AbstractIO implements IO.Seekable, IO.Readable, IO.Writable {

	protected I io;
	protected long start;
	protected long size;
	protected long position = 0;
	private boolean closeIoOnClose;
	
	protected AbstractSubIO(I io, long start, long size, boolean closeIoOnClose) {
		NegativeValueException.check(start, "start");
		NegativeValueException.check(size, "size");
		this.io = io;
		this.start = start;
		this.size = size;
		this.closeIoOnClose = closeIoOnClose;
	}

	@Override
	protected void closeInternal() throws IOException {
		if (closeIoOnClose) io.close();
		io = null;
	}
	
	@Override
	public long position() throws IOException {
		if (io == null) throw new ClosedChannelException();
		return position;
	}
	
	@Override
	public long size() throws IOException {
		if (io == null) throw new ClosedChannelException();
		return size;
	}
	
	@Override
	public long seek(SeekFrom from, long offset) throws IOException {
		if (io == null) throw new ClosedChannelException();
		long p;
		switch (Objects.requireNonNull(from, "from")) {
		case CURRENT: p = position + offset; break;
		case END: p = size - offset; break;
		case START: default: p = offset; break;
		}
		if (p < 0) throw new IllegalArgumentException("Cannot move beyond the start: " + p);
		if (p > size) throw new EOFException();
		this.position = p;
		return p;
	}

	
	@Override
	public long skipUpTo(long toSkip) throws IOException {
		if (io == null) throw new ClosedChannelException();
		if (toSkip == 0) return 0;
		NegativeValueException.check(toSkip, "toSkip");
		if (position == size) return -1;
		long skip = Math.min(toSkip, size - position);
		this.position += skip;
		return skip;
	}
	
	@Override
	public void skipFully(long toSkip) throws IOException {
		if (io == null) throw new ClosedChannelException();
		NegativeValueException.check(toSkip, "toSkip");
		if (toSkip > size - position) throw new EOFException();
		this.position += toSkip;
	}

	@Override
	public void flush() throws IOException {
		if (io == null) throw new ClosedChannelException();
		((IO.Writable) io).flush();
	}
	
}
