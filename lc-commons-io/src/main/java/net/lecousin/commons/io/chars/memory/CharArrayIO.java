package net.lecousin.commons.io.chars.memory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntBinaryOperator;

import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.bytes.memory.ByteArrayIO;
import net.lecousin.commons.io.chars.CharsIO;

/**
 * CharsIO based on a CharArray.
 */
public class CharArrayIO extends AbstractIO implements CharsIO.ReadWrite.Resizable {

	protected CharArray chars;
	private Optional<IntBinaryOperator> extensionStrategy;
	
	protected CharArrayIO(CharArray chars, Optional<IntBinaryOperator> extensionStrategy) {
		this.chars = chars;
		this.extensionStrategy = extensionStrategy;
	}

	/**
	 * Constructor.
	 * @param chars char array
	 */
	public CharArrayIO(CharArray chars) {
		this(chars, Optional.empty());
	}
	
	@Override
	protected void closeInternal() throws IOException {
		chars = null;
	}
	
	@Override
	public long position() throws IOException {
		if (chars == null) throw new ClosedChannelException();
		return chars.getPosition();
	}
	
	@Override
	public long size() throws IOException {
		if (chars == null) throw new ClosedChannelException();
		return chars.getSize();
	}
	
	@Override
	public void setSize(long newSize) throws IOException {
		if (chars == null) throw new ClosedChannelException();
		LimitExceededException.check(newSize, Integer.MAX_VALUE, "newSize", "Integer.MAX_VALUE");
		chars.setSize((int) newSize);
	}
	
	protected boolean extendCapacity(long newSize) {
		if (extensionStrategy.isEmpty()) return false;
		LimitExceededException.check(newSize, Integer.MAX_VALUE, "newSize", "Integer.MAX_VALUE");
		if (chars.getArrayStartOffset() + newSize <= chars.getArray().length) {
			chars.setSize((int) newSize);
			return true;
		}
		IntBinaryOperator strategy = extensionStrategy.get();
		int current = chars.getSize();
		int newValue = strategy.applyAsInt(current, (int) (newSize - current));
		chars.setSize(newValue);
		chars.setSize((int) newSize);
		return true;
	}
	
	@Override
	public long seek(SeekFrom from, long offset) throws IOException {
		if (chars == null) throw new ClosedChannelException();
		long p;
		switch (Objects.requireNonNull(from, "from")) {
		case CURRENT: p = chars.getPosition() + offset; break;
		case END: p = chars.getSize() - offset; break;
		case START: default: p = offset; break;
		}
		if (p < 0) throw new IllegalArgumentException("Cannot seek beyond the start: " + p);
		if (p > chars.getSize() && !extendCapacity(p)) throw new EOFException(); 
		chars.setPosition((int) p);
		return p;
	}
	
	// --- Readable ---

	@Override
	public Optional<CharBuffer> readBuffer() throws IOException {
		if (chars == null) throw new ClosedChannelException();
		int len = chars.remaining();
		if (len == 0) return Optional.empty();
		CharBuffer buffer = CharBuffer.wrap(chars.getArray(), chars.getArrayStartOffset() + chars.getPosition(), len);
		chars.moveForward(len);
		return Optional.of(buffer);
	}
	
	@Override
	public int readChars(CharBuffer buffer) throws IOException {
		if (chars == null) throw new ClosedChannelException();
		Objects.requireNonNull(buffer, IOChecks.FIELD_BUFFER);
		int r1 = buffer.remaining();
		if (r1 == 0) return 0;
		int r2 = chars.remaining();
		if (r2 == 0) return -1;
		int len = Math.min(r1, r2);
		buffer.put(chars.getArray(), chars.getArrayStartOffset() + chars.getPosition(), len);
		chars.moveForward(len);
		return len;
	}
	
	@Override
	public int readChars(char[] buf, int off, int len) throws IOException {
		if (chars == null) throw new ClosedChannelException();
		IOChecks.checkArray(buf, off, len);
		if (len == 0) return 0;
		int r = chars.remaining();
		if (r == 0) return -1;
		len = Math.min(len, r);
		System.arraycopy(chars.getArray(), chars.getArrayStartOffset() + chars.getPosition(), buf, off, len);
		chars.moveForward(len);
		return len;
	}
	
	@Override
	public int readCharsAt(long pos, CharBuffer buffer) throws IOException {
		IOChecks.checkBufferOperation(this, pos, buffer);
		int r = buffer.remaining();
		if (r == 0) return 0;
		if (pos >= chars.getSize()) return -1;
		int len = Math.min(r, chars.getSize() - (int) pos);
		buffer.put(chars.getArray(), chars.getArrayStartOffset() + (int) pos, len);
		return len;
	}
	
	@Override
	public int readCharsAt(long pos, char[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, pos, buf, off, len);
		if (len == 0) return 0;
		if (pos >= chars.getSize()) return -1;
		len = Math.min(len, chars.getSize() - (int) pos);
		System.arraycopy(chars.getArray(), chars.getArrayStartOffset() + (int) pos, buf, off, len);
		return len;
	}
	
	@Override
	public void readCharsFully(CharBuffer buffer) throws IOException {
		if (chars == null) throw new ClosedChannelException();
		Objects.requireNonNull(buffer, IOChecks.FIELD_BUFFER);
		int r = buffer.remaining();
		if (r == 0) return;
		if (r > chars.remaining()) throw new EOFException();
		buffer.put(chars.getArray(), chars.getArrayStartOffset() + chars.getPosition(), r);
		chars.moveForward(r);
	}
	
	@Override
	public void readCharsFully(char[] buf, int off, int len) throws IOException {
		if (chars == null) throw new ClosedChannelException();
		IOChecks.checkArray(buf, off, len);
		if (len == 0) return;
		if (len > chars.remaining()) throw new EOFException();
		System.arraycopy(chars.getArray(), chars.getArrayStartOffset() + chars.getPosition(), buf, off, len);
		chars.moveForward(len);
	}
	
	@Override
	public void readCharsFullyAt(long pos, CharBuffer buffer) throws IOException {
		IOChecks.checkBufferOperation(this, pos, buffer);
		int r = buffer.remaining();
		if (r == 0) return;
		if (pos + r > chars.getSize()) throw new EOFException();
		buffer.put(chars.getArray(), chars.getArrayStartOffset() + (int) pos, r);
	}
	
	@Override
	public void readCharsFullyAt(long pos, char[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, pos, buf, off, len);
		if (len == 0) return;
		if (pos + len > chars.getSize()) throw new EOFException();
		System.arraycopy(chars.getArray(), chars.getArrayStartOffset() + (int) pos, buf, off, len);
	}
	
	@Override
	public char readChar() throws IOException {
		if (chars == null) throw new ClosedChannelException();
		if (chars.remaining() == 0) throw new EOFException();
		return chars.readChar();
	}
	
	@Override
	public char readCharAt(long pos) throws IOException {
		if (chars == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, IOChecks.FIELD_POS);
		if (pos >= chars.getSize()) throw new EOFException();
		return chars.getArray()[chars.getArrayStartOffset() + (int) pos];
	}
	
	@Override
	public long skipUpTo(long toSkip) throws IOException {
		if (chars == null) throw new ClosedChannelException();
		NegativeValueException.check(toSkip, "toSkip");
		if (toSkip == 0) return 0;
		int r = chars.remaining();
		if (r == 0) return -1;
		long nb = Math.min(toSkip, r);
		chars.moveForward((int) nb);
		return nb;
	}
	
	@Override
	public void skipFully(long toSkip) throws IOException {
		if (chars == null) throw new ClosedChannelException();
		NegativeValueException.check(toSkip, "toSkip");
		if (chars.getPosition() + toSkip > chars.getSize()) throw new EOFException();
		chars.moveForward((int) toSkip);
	}
	
	
	// --- Writable ---
	
	@Override
	public int writeChars(CharBuffer buffer) throws IOException {
		if (chars == null) throw new ClosedChannelException();
		Objects.requireNonNull(buffer, IOChecks.FIELD_BUFFER);
		int r = buffer.remaining();
		if (r == 0) return 0;
		int len = chars.remaining();
		if (len == 0) {
			if (!extendCapacity((long) chars.getPosition() + r)) return -1;
			len = r;
		}
		len = Math.min(r, len);
		buffer.get(chars.getArray(), chars.getArrayStartOffset() + chars.getPosition(), len);
		chars.moveForward(len);
		return len;
	}
	
	@Override
	public int writeChars(char[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, buf, off, len);
		if (len == 0) return 0;
		int r = chars.remaining();
		if (r == 0) {
			if (!extendCapacity((long) chars.getPosition() + len)) return -1;
			r = len;
		}
		len = Math.min(r, len);
		chars.write(buf, off, len);
		return len;
	}
	
	@Override
	public int writeCharsAt(long pos, CharBuffer buffer) throws IOException {
		IOChecks.checkBufferOperation(this, pos, buffer);
		int r = buffer.remaining();
		if (r == 0) return 0;
		if (pos >= chars.getSize() && !extendCapacity(pos + r)) return -1;
		r = Math.min(r, chars.getSize() - (int) pos);
		buffer.get(chars.getArray(), chars.getArrayStartOffset() + (int) pos, r);
		return r;
	}
	
	@Override
	public int writeCharsAt(long pos, char[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, pos, buf, off, len);
		if (len == 0) return 0;
		if (pos >= chars.getSize() && !extendCapacity(pos + len)) return -1;
		len = Math.min(len, chars.getSize() - (int) pos);
		System.arraycopy(buf, off, chars.getArray(), chars.getArrayStartOffset() + (int) pos, len);
		return len;
	}
	
	@Override
	public void writeCharsFully(CharBuffer buffer) throws IOException {
		if (chars == null) throw new ClosedChannelException();
		Objects.requireNonNull(buffer, IOChecks.FIELD_BUFFER);
		int r = buffer.remaining();
		if (r == 0) return;
		if (r > chars.remaining() && !extendCapacity((long) chars.getPosition() + r)) throw new EOFException();
		buffer.get(chars.getArray(), chars.getArrayStartOffset() + chars.getPosition(), r);
		chars.moveForward(r);
	}
	
	@Override
	public void writeCharsFully(char[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, buf, off, len);
		if (len == 0) return;
		if (len > chars.remaining() && !extendCapacity((long) chars.getPosition() + len)) throw new EOFException();
		chars.write(buf, off, len);
	}
	
	@Override
	public void writeCharsFullyAt(long pos, CharBuffer buffer) throws IOException {
		IOChecks.checkBufferOperation(this, pos, buffer);
		int r = buffer.remaining();
		if (pos + r > chars.getSize() && !extendCapacity(pos + r)) throw new EOFException();
		buffer.get(chars.getArray(), chars.getArrayStartOffset() + (int) pos, r);
	}
	
	@Override
	public void writeCharsFullyAt(long pos, char[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, pos, buf, off, len);
		if (pos + len > chars.getSize() && !extendCapacity(pos + len)) throw new EOFException();
		System.arraycopy(buf, off, chars.getArray(), chars.getArrayStartOffset() + (int) pos, len);
	}
	
	@Override
	public void writeChar(char value) throws IOException {
		if (chars == null) throw new ClosedChannelException();
		if (chars.remaining() == 0 && !extendCapacity(chars.getSize() + 1L)) throw new EOFException();
		chars.writeChar(value);
	}
	
	@Override
	public void writeCharAt(long pos, char value) throws IOException {
		if (chars == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, IOChecks.FIELD_POS);
		if (pos >= chars.getSize() && !extendCapacity(pos + 1)) throw new EOFException();
		chars.getArray()[chars.getArrayStartOffset() + (int) pos] = value;
	}
	
	@Override
	public void flush() throws IOException {
		if (chars == null) throw new ClosedChannelException();
	}

	/** Appendable CharArrayIO. */
	public static class Appendable extends CharArrayIO implements CharsIO.ReadWrite.AppendableResizable {
		
		/**
		 * Constructor.
		 * @param chars char array
		 * @param extensionStrategy take the current size and the additional requested size in parameter
		 *   and returns the new size to be allocated.
		 */
		public Appendable(CharArray chars, IntBinaryOperator extensionStrategy) {
			super(chars, Optional.of(extensionStrategy));
		}
		
		/**
		 * Constructor.
		 * @param chars char array
		 * @param minimumAppendSize when appending chars beyond the char array size, the char array is extended
		 *   of at least <code>appendMinimum</code> chars, or the requested additional chars if greater than
		 *   <code>appendMinimum</code>.
		 */
		public Appendable(CharArray chars, int minimumAppendSize) {
			this(chars, ByteArrayIO.Appendable.extensionStrategyWithMinimumAppendSize(minimumAppendSize));
		}
		
		/**
		 * Constructor with default extension strategy.
		 * @param chars char array
		 */
		public Appendable(CharArray chars) {
			this(chars, ByteArrayIO.Appendable.DEFAULT_EXTENSION_STRATEGY);
		}
		
	}
}
