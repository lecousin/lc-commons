package net.lecousin.commons.io.chars;

import java.io.EOFException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.chars.memory.CharArray;
import net.lecousin.commons.io.chars.memory.ReadableSeekableCharsIOFromCharSequence;
import net.lecousin.commons.io.chars.utils.ReadableCharsIOFromBytesIO;

/**
 * IO working on characters.
 */
public interface CharsIO extends IO {
	
	/** Readable characters IO. */
	interface Readable extends CharsIO, IO.Readable {
		
		/**
		 * Read a single character.
		 * @return character read
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if no more character can be read
		 * @throws IOException in case an error occurred while reading
		 */
		char readChar() throws IOException;
		
		/**
		 * Read <i>some</i> characters. At least one character is read, but the buffer is not necessarily
		 * filled, if no more character can be read because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #readCharsFully(CharBuffer)} this operation will read as much characters
		 * as possible in a single operation, but will not fill the buffer if reading more characters
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buffer the buffer to fill
		 * @return number of characters read, or -1 if the end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws IOException in case an error occurred while reading
		 */
		int readChars(CharBuffer buffer) throws IOException;
		
		/**
		 * Read <i>some</i> characters. At least one character is read, but the requested characters are not necessarily
		 * read, if no more character can be read because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #readCharsFully(char[], int, int)} this operation will read as much characters
		 * as possible in a single operation, but will not fill the buffer if reading more characters
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buf the buffer to fill
		 * @param off offset in the buffer
		 * @param len maximum number of characters to read
		 * @return number of characters read, or -1 if the end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws NegativeValueException if off or len is negative
		 * @throws LimitExceededException if off + len &gt; buf.length
		 * @throws IOException in case an error occurred while reading
		 */
		default int readChars(char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, buf, off, len);
			return readChars(CharBuffer.wrap(buf, off, len));
		}
		
		/**
		 * Read <i>some</i> characters. At least one character is read, but the buffer is not necessarily
		 * filled, if no more character can be read because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #readCharsFully(char[])} this operation will read as much characters
		 * as possible in a single operation, but will not fill the buffer if reading more characters
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buf the buffer to fill
		 * @return number of characters read, or -1 if the end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws IOException in case an error occurred while reading
		 */
		default int readChars(char[] buf) throws IOException {
			IOChecks.checkArrayOperation(this, buf);
			return readChars(buf, 0, buf.length);
		}
		
		/**
		 * Read some characters and return the buffer.<br/>
		 * Compared to the method {@link #readChars(CharBuffer)}, this method lets the implementation allocate
		 * a buffer before to read (and potentially make a better decision if it knows in advance the amount of characters that
		 * can be read at once). However the counterpart is that each call will allocate a new buffer, making reuse of buffers impossible.
		 * 
		 * @return a buffer if some characters can be read, or empty in case the end is reached.
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws IOException in case an error occurred while reading
		 */
		Optional<CharBuffer> readBuffer() throws IOException;
		
		/**
		 * Read characters to fill the given buffer.<br/>
		 * Compared to {@link #readChars(CharBuffer)} this method ensures that the buffer is filled, or
		 * EOFException is thrown.
		 * 
		 * @param buffer the buffer to fill
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if the buffer cannot be filled because it would reached the end
		 * @throws IOException in case an error occurred while reading
		 */
		default void readCharsFully(CharBuffer buffer) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			while (buffer.hasRemaining())
				if (readChars(buffer) <= 0) throw new EOFException();
		}
		
		/**
		 * Read <code>len</code> characters to the given buffer.<br/>
		 * Compared to {@link #readChars(char[], int, int)} this method ensures that the requested
		 * number of characters are read, or EOFException is thrown.
		 * 
		 * @param buf the buffer to fill
		 * @param off offset in the buffer
		 * @param len number of characters to read
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if the buffer cannot be filled because it would reached the end
		 * @throws NegativeValueException if off or len is negative
		 * @throws LimitExceededException if off + len &gt; buf.length
		 * @throws IOException in case an error occurred while reading
		 */
		default void readCharsFully(char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, buf, off, len);
			while (len > 0) {
				int nb = readChars(buf, off, len);
				if (nb <= 0) throw new EOFException();
				off += nb;
				len -= nb;
			}
		}
		
		/**
		 * Read characters to fill the given buffer.<br/>
		 * Compared to {@link #readChars(char[])} this method ensures that the buffer is filled, or
		 * EOFException is thrown.
		 * 
		 * @param buf the buffer to fill
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if the buffer cannot be filled because it would reached the end
		 * @throws IOException in case an error occurred while reading
		 */
		default void readCharsFully(char[] buf) throws IOException {
			IOChecks.checkArrayOperation(this, buf);
			readCharsFully(buf, 0, buf.length);
		}
		
		/**
		 * Write all remaining characters from this I/O to the given writable.
		 * @param to output
		 * @throws IOException if an error occurs during the transfer
		 */
		default void transferFully(CharsIO.Writable to) throws IOException {
			Optional<CharBuffer> b;
			while ((b = readBuffer()).isPresent())
				to.writeCharsFully(b.get());
		}

		/**
		 * Readable and Seekable characters IO.
		 */
		interface Seekable extends CharsIO.Readable, IO.Seekable {

			/**
			 * Read a single character at the given position.
			 * @param pos position
			 * @return character read
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if the position is at the end
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while reading
			 */
			char readCharAt(long pos) throws IOException;
			
			/**
			 * Read <i>some</i> characters at the given position. At least one character is read, but the buffer is not necessarily
			 * filled, if no more character can be read because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #readCharsFullyAt(long,CharBuffer)} this operation will read as much characters
			 * as possible in a single operation, but will not fill the buffer if reading more characters
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buffer the buffer to fill
			 * @return number of characters read, or -1 if the end is reached
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while reading
			 */
			int readCharsAt(long pos, CharBuffer buffer) throws IOException;
			
			/**
			 * Read <i>some</i> characters at the given position. At least one character is read, but the requested characters are not necessarily
			 * read, if no more character can be read because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #readCharsFullyAt(long, char[], int, int)} this operation will read as much characters
			 * as possible in a single operation, but will not fill the buffer if reading more characters
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buf the buffer to fill
			 * @param off offset in the buffer
			 * @param len maximum number of characters to read
			 * @return number of characters read, or -1 if the end is reached
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos, off or len is negative
			 * @throws LimitExceededException if off + len &gt; buf.length
			 * @throws IOException in case an error occurred while reading
			 */
			default int readCharsAt(long pos, char[] buf, int off, int len) throws IOException {
				IOChecks.checkArrayOperation(this, pos, buf, off, len);
				return readCharsAt(pos, CharBuffer.wrap(buf, off, len));
			}
			
			/**
			 * Read <i>some</i> characters at the given position. At least one character is read, but the buffer is not necessarily
			 * filled, if no more character can be read because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #readCharsFullyAt(long,char[])} this operation will read as much characters
			 * as possible in a single operation, but will not fill the buffer if reading more characters
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buf the buffer to fill
			 * @return number of characters read, or -1 if the end is reached
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while reading
			 */
			default int readCharsAt(long pos, char[] buf) throws IOException {
				IOChecks.checkArrayOperation(this, pos, buf);
				return readCharsAt(pos, buf, 0, buf.length);
			}
			
			/**
			 * Read characters at the given position to fill the given buffer.<br/>
			 * Compared to {@link #readCharsAt(long,CharBuffer)} this method ensures that the buffer is filled, or
			 * EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buffer the buffer to fill
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if the buffer cannot be filled because it would reached the end
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while reading
			 */
			default void readCharsFullyAt(long pos, CharBuffer buffer) throws IOException {
				if (isClosed()) throw new ClosedChannelException();
				NegativeValueException.check(pos, IOChecks.FIELD_POS);
				int done = 0;
				while (buffer.hasRemaining()) {
					int nb = readCharsAt(pos + done, buffer);
					if (nb <= 0) throw new EOFException();
					done += nb;
				}
			}
			
			/**
			 * Read <code>len</code> characters at the given position into the given buffer.<br/>
			 * Compared to {@link #readCharsAt(long, char[], int, int)} this method ensures that the requested
			 * number of characters are read, or EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buf the buffer to fill
			 * @param off offset in the buffer
			 * @param len number of characters to read
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if the buffer cannot be filled because it would reached the end
			 * @throws NegativeValueException if pos, off or len is negative
			 * @throws LimitExceededException if off + len &gt; buf.length
			 * @throws IOException in case an error occurred while reading
			 */
			default void readCharsFullyAt(long pos, char[] buf, int off, int len) throws IOException {
				IOChecks.checkArrayOperation(this, pos, buf, off, len);
				while (len > 0) {
					int nb = readCharsAt(pos, buf, off, len);
					if (nb <= 0) throw new EOFException();
					off += nb;
					pos += nb;
					len -= nb;
				}
			}
			
			/**
			 * Read characters at the given position to fill the given buffer.<br/>
			 * Compared to {@link #readCharsAt(long,char[])} this method ensures that the buffer is filled, or
			 * EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buf the buffer to fill
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if the buffer cannot be filled because it would reached the end
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while reading
			 */
			default void readCharsFullyAt(long pos, char[] buf) throws IOException {
				IOChecks.checkArrayOperation(this, pos, buf);
				readCharsFullyAt(pos, buf, 0, buf.length);
			}
			
			/** @return a Readable view of this IO. */
			default CharsIO.Readable asReadableCharsIO() {
				return new CharsIOView.Readable(this);
			}
			
		}

		
	}
	
	
	
	/**
	 * Writable characters IO.
	 */
	interface Writable extends CharsIO, IO.Writable {

		/**
		 * Write a single character.
		 * @param value character to write
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if no more character can be written
		 * @throws IOException in case an error occurred while writing
		 */
		void writeChar(char value) throws IOException;
		
		/**
		 * Write <i>some</i> characters. At least one character is written, but the buffer is not necessarily
		 * consumed, if no more character can be written because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #writeCharsFully(CharBuffer)} this operation will write as much characters
		 * as possible in a single operation, but will not write all if writing more characters
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buffer the buffer to write
		 * @return number of characters written, or -1 if the end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws IOException in case an error occurred while writing
		 */
		int writeChars(CharBuffer buffer) throws IOException;
		
		/**
		 * Write up to <code>len</code> characters. At least one character is written, but the requested number of characters
		 * are not necessarily written, if no more character can be written because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #writeCharsFully(char[],int,int)} this operation will write as much characters
		 * as possible in a single operation, but will not write all if writing more characters
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buf the buffer to write
		 * @param off offset in the buffer
		 * @param len maximum number of characters to write
		 * @return number of characters written, or -1 if the end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws NegativeValueException if off or len is negative
		 * @throws LimitExceededException if off + len &gt; buf.length
		 * @throws IOException in case an error occurred while writing
		 */
		default int writeChars(char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, buf, off, len);
			return writeChars(CharBuffer.wrap(buf, off, len));
		}
		
		/**
		 * Write <i>some</i> characters. At least one character is written, but the buffer is not necessarily
		 * consumed, if no more character can be written because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #writeCharsFully(char[])} this operation will write as much characters
		 * as possible in a single operation, but will not write all if writing more characters
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buf the buffer to write
		 * @return number of characters written, or -1 if the end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws IOException in case an error occurred while writing
		 */
		default int writeChars(char[] buf) throws IOException {
			IOChecks.checkArrayOperation(this, buf);
			return writeChars(buf, 0, buf.length);
		}
		
		/**
		 * Write all characters from the given buffer.<br/>
		 * If it cannot write all characters because end is reached, EOFException is thrown.
		 * 
		 * @param buffer the buffer to write
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if all characters cannot be written because end is reached
		 * @throws IOException in case an error occurred while writing
		 */
		default void writeCharsFully(CharBuffer buffer) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			while (buffer.hasRemaining())
				if (writeChars(buffer) <= 0) throw new EOFException();
		}
		
		/**
		 * Write all characters from the all the given buffer.<br/>
		 * If it cannot write all characters because end is reached, EOFException is thrown.
		 * 
		 * @param buffers the buffers to write
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if all characters cannot be written because end is reached
		 * @throws IOException in case an error occurred while writing
		 */
		default void writeCharsFully(List<CharBuffer> buffers) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			Objects.requireNonNull(buffers);
			for (var b : buffers) writeCharsFully(b);
		}
		
		/**
		 * Write exactly <code>len</code> characters from the given buffer.<br/>
		 * If it cannot write all characters because end is reached, EOFException is thrown.
		 * 
		 * @param buf the buffer to write
		 * @param off offset in the buffer
		 * @param len number of characters to write
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if all characters cannot be written because end is reached
		 * @throws NegativeValueException if off or len is negative
		 * @throws LimitExceededException if off + len &gt; buf.length
		 * @throws IOException in case an error occurred while writing
		 */
		default void writeCharsFully(char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, buf, off, len);
			while (len > 0) {
				int nb = writeChars(buf, off, len);
				if (nb <= 0) throw new EOFException();
				off += nb;
				len -= nb;
			}
		}
		
		/**
		 * Write all characters from the given buffer.<br/>
		 * If it cannot write all characters because end is reached, EOFException is thrown.
		 * 
		 * @param buf the buffer to write
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if all characters cannot be written because end is reached
		 * @throws IOException in case an error occurred while writing
		 */
		default void writeCharsFully(char[] buf) throws IOException {
			IOChecks.checkArrayOperation(this, buf);
			writeCharsFully(buf, 0, buf.length);
		}

		
		/**
		 * Writable and Seekable characters IO.
		 */
		interface Seekable extends CharsIO.Writable, IO.Seekable {

			/**
			 * Write a single character at the given position.
			 * @param pos position
			 * @param value character to write
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if pos is beyond the end
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while writing
			 */
			void writeCharAt(long pos, char value) throws IOException;
			
			/**
			 * Write <i>some</i> characters at the given position. At least one character is written, but the buffer is not necessarily
			 * consumed, if no more character can be written because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #writeCharsFullyAt(long,CharBuffer)} this operation will write as much characters
			 * as possible in a single operation, but will not write all if writing more characters
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buffer the buffer to write
			 * @return number of characters written, or -1 if the end is reached
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while writing
			 */
			int writeCharsAt(long pos, CharBuffer buffer) throws IOException;
			
			/**
			 * Write up to <code>len</code> characters at the given position. At least one character is written, but the requested number of characters
			 * are not necessarily written, if no more character can be written because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #writeCharsFullyAt(long,char[],int,int)} this operation will write as much characters
			 * as possible in a single operation, but will not write all if writing more characters
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buf the buffer to write
			 * @param off offset in the buffer
			 * @param len maximum number of characters to write
			 * @return number of characters written, or -1 if the end is reached
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos, off or len is negative
			 * @throws LimitExceededException if off + len &gt; buf.length
			 * @throws IOException in case an error occurred while writing
			 */
			default int writeCharsAt(long pos, char[] buf, int off, int len) throws IOException {
				IOChecks.checkArrayOperation(this, pos, buf, off, len);
				return writeCharsAt(pos, CharBuffer.wrap(buf, off, len));
			}
			
			/**
			 * Write <i>some</i> characters at the given position. At least one character is written, but the buffer is not necessarily
			 * consumed, if no more character can be written because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #writeCharsFully(char[])} this operation will write as much characters
			 * as possible in a single operation, but will not write all if writing more characters
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buf the buffer to write
			 * @return number of characters written, or -1 if the end is reached
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while writing
			 */
			default int writeCharsAt(long pos, char[] buf) throws IOException {
				IOChecks.checkArrayOperation(this, pos, buf);
				return writeCharsAt(pos, buf, 0, buf.length);
			}
			
			/**
			 * Write all characters from the given buffer at the given position.<br/>
			 * If it cannot write all characters because end is reached, EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buffer the buffer to write
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if all characters cannot be written because end is reached
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while writing
			 */
			default void writeCharsFullyAt(long pos, CharBuffer buffer) throws IOException {
				IOChecks.checkBufferOperation(this, pos, buffer);
				int done = 0;
				while (buffer.hasRemaining()) {
					int nb = writeCharsAt(pos + done, buffer);
					if (nb <= 0) throw new EOFException();
					done += nb;
				}
			}
			
			/**
			 * Write exactly <code>len</code> characters from the given buffer at the given position.<br/>
			 * If it cannot write all characters because end is reached, EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buf the buffer to write
			 * @param off offset in the buffer
			 * @param len number of characters to write
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if all characters cannot be written because end is reached
			 * @throws NegativeValueException if pos, off or len is negative
			 * @throws LimitExceededException if off + len &gt; buf.length
			 * @throws IOException in case an error occurred while writing
			 */
			default void writeCharsFullyAt(long pos, char[] buf, int off, int len) throws IOException {
				IOChecks.checkArrayOperation(this, pos, buf, off, len);
				writeCharsFullyAt(pos, CharBuffer.wrap(buf, off, len));
			}
			
			/**
			 * Write all characters from the given buffer at the given position.<br/>
			 * If it cannot write all characters because end is reached, EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buf the buffer to write
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if all characters cannot be written because end is reached
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while writing
			 */
			default void writeCharsFullyAt(long pos, char[] buf) throws IOException {
				IOChecks.checkArrayOperation(this, pos, buf);
				writeCharsFullyAt(pos, buf, 0, buf.length);
			}
			
			/**
			 * Write all characters at the given position from the all the given buffer.<br/>
			 * If it cannot write all characters because end is reached, EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buffers the buffers to write
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos is negative
			 * @throws EOFException if all characters cannot be written because end is reached
			 * @throws IOException in case an error occurred while writing
			 */
			default void writeCharsFullyAt(long pos, List<CharBuffer> buffers) throws IOException {
				if (isClosed()) throw new ClosedChannelException();
				NegativeValueException.check(pos, IOChecks.FIELD_POS);
				Objects.requireNonNull(buffers);
				long p = pos;
				for (var b : buffers) {
					int nb = b.remaining();
					writeCharsFullyAt(p, b);
					p += nb;
				}
			}
			
			/** @return a Writable view of this IO. */
			default CharsIO.Writable asWritableCharsIO() {
				return CharsIOView.Writable.of(this);
			}
			
			/** Writable Seekable and Appendable CharsIO. */
			interface Appendable extends CharsIO.Writable.Seekable, IO.Writable.Appendable {
				
			}
			
			/** Writable Seekable and Resizable CharsIO. */
			interface Resizable extends CharsIO.Writable.Seekable, IO.Writable.Resizable {
				
				/** @return a non-resizable view of this CharsIO. */
				default CharsIO.Writable.Seekable asNonResizableWritableSeekableCharsIO() {
					return CharsIOView.Writable.Seekable.of(this);
				}
				
			}
			
			/** Writable Seekable Appendable and Resizable CharsIO. */
			interface AppendableResizable extends CharsIO.Writable.Seekable.Appendable, CharsIO.Writable.Seekable.Resizable {
				
			}
		}
		
	}
	
	/** Readable and Writable Seekable CharsIO. */
	interface ReadWrite extends CharsIO.Readable.Seekable, CharsIO.Writable.Seekable {
		
		/** @return a Readable and Seekable view of this IO. */
		default CharsIO.Readable.Seekable asReadableSeekableCharsIO() {
			return new CharsIOView.Readable.Seekable(this);
		}
		
		/** @return a Writable and Seekable view of this IO. */
		default CharsIO.Writable.Seekable asWritableSeekableCharsIO() {
			return CharsIOView.Writable.Seekable.of(this);
		}
		
		/** Readable and Writable Seekable Resizable CharsIO. */
		interface Resizable extends ReadWrite, CharsIO.Writable.Seekable.Resizable {
			
			/** @return a non-resizable view of this CharsIO. */
			default CharsIO.ReadWrite asNonResizableReadWriteCharsIO() {
				return CharsIOView.ReadWrite.of(this);
			}
			
			/** @return a writable, seekable and resizable CharsIO. */
			default CharsIO.Writable.Seekable.Resizable asWritableSeekableResizableCharsIO() {
				return CharsIOView.Writable.Seekable.Resizable.of(this);
			}
			
		}
		
		/** Readable and Writable Seekable Appendable CharsIO. */
		interface Appendable extends ReadWrite, CharsIO.Writable.Seekable.Appendable {
			
		}
		
		/** Readable and Writable Seekable Appendable and Resizable CharsIO. */
		interface AppendableResizable extends ReadWrite.Appendable, ReadWrite.Resizable {
			
		}
		
	}

	/**
	 * Create a readable and seekable CharsIO from a CharSequence.
	 * @param chars char sequence
	 * @return CharsIO
	 */
	static CharsIO.Readable.Seekable asReadableSeekable(CharSequence chars) {
		if (chars instanceof CharArray ca) return ca.asCharsIO().asReadableSeekableCharsIO();
		return new ReadableSeekableCharsIOFromCharSequence(chars);
	}

	/**
	 * Create a CharsIO from a BytesIO, using s specific Charset to decode bytes into characters.
	 * @param bytes BytesIO
	 * @param charset charset to use
	 * @param closeIoOnClose if true the BytesIO will be closed when the CharsIO is closed
	 * @return the CharsIO
	 */
	static CharsIO.Readable fromBytesIO(BytesIO.Readable bytes, Charset charset, boolean closeIoOnClose) {
		return new ReadableCharsIOFromBytesIO(bytes, charset, closeIoOnClose);
	}
	
}
