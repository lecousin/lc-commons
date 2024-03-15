package net.lecousin.commons.io.chars.memory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Optional;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.chars.CharsIO;

/**
 * Readable and Seekable CharsIO from a CharSequence.
 */
public class ReadableSeekableCharsIOFromCharSequence extends AbstractIO implements CharsIO.Readable.Seekable {

	private final CharSequence chars;
	private int position = 0;
	
	/**
	 * Constructor.
	 * @param chars char sequence
	 */
	public ReadableSeekableCharsIOFromCharSequence(CharSequence chars) {
		this.chars = chars;
	}
	
	@Override
	protected void closeInternal() throws IOException {
		// nothing to close
	}
	
	@Override
	public long position() throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		return position;
	}
	
	@Override
	public long size() throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		return chars.length();
	}
	
	@Override
	public long seek(SeekFrom from, long offset) throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		long p;
		switch (Objects.requireNonNull(from, "from")) {
		case CURRENT: p = position + offset; break;
		case END: p = chars.length() - offset; break;
		case START: default: p = offset; break;
		}
		if (p < 0) throw new IllegalArgumentException("Cannot seek beyond the start: " + p);
		if (p > chars.length()) throw new EOFException(); 
		position = (int) p;
		return p;
	}
	
	@Override
	public long skipUpTo(long toSkip) throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		NegativeValueException.check(toSkip, "toSkip");
		if (toSkip == 0) return 0;
		int r = chars.length() - position;
		if (r == 0) return -1;
		long nb = Math.min(toSkip, r);
		position += (int) nb;
		return nb;
	}
	
	@Override
	public char readChar() throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		if (position == chars.length()) throw new EOFException();
		return chars.charAt(position++);
	}
	
	@Override
	public char readCharAt(long pos) throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		NegativeValueException.check(pos, IOChecks.FIELD_POS);
		if (pos >= chars.length()) throw new EOFException();
		return chars.charAt((int) pos);
	}
	
	@Override
	public Optional<CharBuffer> readBuffer() throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		if (position == chars.length()) return Optional.empty();
		CharBuffer b = CharBuffer.wrap(chars, position, chars.length());
		position = chars.length();
		return Optional.of(b);
	}
	
	@Override
	public int readChars(CharBuffer buffer) throws IOException {
		if (isClosed()) throw new ClosedChannelException();
		int r = buffer.remaining();
		if (r == 0) return 0;
		int l = chars.length() - position;
		if (l == 0) return -1;
		l = Math.min(l, r);
		for (int i = 0; i < l; ++i) buffer.put(chars.charAt(position++));
		return l;
	}
	
	@Override
	public int readChars(char[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, buf, off, len);
		if (len == 0) return 0;
		int l = chars.length() - position;
		if (l == 0) return -1;
		l = Math.min(l, len);
		for (int i = 0; i < l; ++i) buf[off + i] = chars.charAt(position++);
		return l;
	}
	
	@Override
	public int readCharsAt(long pos, CharBuffer buffer) throws IOException {
		IOChecks.checkBufferOperation(this, pos, buffer);
		int r = buffer.remaining();
		if (r == 0) return 0;
		int l = chars.length();
		if (pos >= l) return -1;
		l = Math.min(l - (int) pos, r);
		for (int i = 0; i < l; ++i) buffer.put(chars.charAt((int) pos + i));
		return l;
	}
	
	@Override
	public int readCharsAt(long pos, char[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, pos, buf, off, len);
		if (len == 0) return 0;
		int l = chars.length();
		if (pos >= l) return -1;
		l = Math.min(l - (int) pos, len);
		for (int i = 0; i < l; ++i) buf[off + i] = chars.charAt((int) pos + i);
		return l;
	}
	
}
