package net.lecousin.commons.io.utils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.function.FailableSupplier;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.IO;

/**
 * Abstract class for a CompositeIO.
 * @param <I> type of IO
 */
public abstract class AbstractCompositeIO<I extends IO> extends AbstractIO implements IO.Seekable, IO.Writable, IO.Readable {

	// CHECKSTYLE DISABLE: VisibilityModifier
	@SuppressWarnings({"java:S2156", "java:S1104"}) // non-private members
	protected final class Element {
		public I io;
		public long size;
		public long startPosition;
		public long ioPosition;
		public Element next;
	}
	// CHECKSTYLE ENABLE: VisibilityModifier
	
	protected Element head;
	protected Element cursor;
	private boolean closeIosOnClose;
	private boolean garbageIoOnConsumed;
	protected long position = 0;
	protected long size = 0;
	
	protected AbstractCompositeIO(List<? extends I> ios, boolean closeIosOnClose, boolean garbageIoOnConsumed) throws IOException {
		this.closeIosOnClose = closeIosOnClose;
		this.garbageIoOnConsumed = garbageIoOnConsumed;
		if (ios.isEmpty()) {
			head = cursor = null;
		} else {
			Iterator<? extends I> it = ios.iterator();
			head = cursor = createElement(it.next(), null);
			Element last = head;
			while (it.hasNext()) last = createElement(it.next(), last);
		}
	}
	
	private Element createElement(I io, Element previous) throws IOException {
		Element e = new Element();
		e.io = io;
		if (io instanceof IO.KnownSize ks) {
			e.size = ks.size();
			size += e.size;
		} else {
			e.size = -1;
		}
		if (previous == null) {
			e.startPosition = 0;
		} else {
			e.startPosition = previous.size != -1 && previous.startPosition != -1 ? previous.startPosition + previous.size : -1;
			previous.next = e;
		}
		e.ioPosition = 0;
		return e;
	}
	
	protected void moveNext() throws IOException {
		if (cursor.io instanceof IO.Writable w) w.flush();
		cursor = cursor.next;
		if (garbageIoOnConsumed) {
			if (closeIosOnClose) head.io.close();
			head = cursor;
		}
		if (cursor != null && cursor.io instanceof IO.Seekable s) s.seek(SeekFrom.START, 0L);
	}
	
	protected Element getElementForPosition(long pos) {
		Element e = head;
		while (pos >= e.startPosition + e.size) e = e.next;
		return e;
	}
	
	protected <T> T doOperationOnPosition(FailableSupplier<T, IOException> op, T resultOnEOF) throws IOException {
		do {
			if (cursor == null) {
				if (resultOnEOF != null) return resultOnEOF;
				throw new EOFException();
			}
			try {
				T result = op.get();
				if (cursor.size != -1 && cursor.ioPosition == cursor.size) {
					moveNext();
				}
				return result;
			} catch (EOFException e) {
				moveNext();
			}
		} while (true);
	}
	
	@Override
	protected void closeInternal() throws IOException {
		while (head != null) {
			if (closeIosOnClose) head.io.close();
			head = head.next;
		}
	}
	
	@Override
	public long position() throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		return position;
	}
	
	@Override
	public long size() throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		return size;
	}
	
	@Override
	public long seek(SeekFrom from, long offset) throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		long p;
		switch (Objects.requireNonNull(from, "from")) {
		case CURRENT: p = position + offset; break;
		case END: p = size - offset; break;
		case START: default: p = offset; break;
		}
		if (p < 0) throw new IllegalArgumentException("Cannot move beyond the start: " + p);
		if (p > size) throw new EOFException();
		if (p == size) {
			cursor = null;
		} else {
			cursor = getElementForPosition(p);
			cursor.ioPosition = p - cursor.startPosition;
			((IO.Seekable) cursor.io).seek(SeekFrom.START, cursor.ioPosition);
		}
		position = p;
		return p;
	}
	
	@Override
	public void flush() throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		for (Element e = head; e != null; e = e.next)
			((IO.Writable) e.io).flush();
	}

	
	@Override
	public long skipUpTo(long toSkip) throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		if (toSkip == 0) return 0;
		NegativeValueException.check(toSkip, "toSkip");
		return doOperationOnPosition(() -> {
			long result = ((IO.Readable) cursor.io).skipUpTo(toSkip);
			if (result <= 0) throw new EOFException();
			position += result;
			cursor.ioPosition += result;
			return result;
		}, -1L);
	}
	
}
