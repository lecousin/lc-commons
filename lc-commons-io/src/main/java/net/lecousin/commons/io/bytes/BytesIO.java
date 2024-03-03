package net.lecousin.commons.io.bytes;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.IOChecks;

/**
 * IO working on bytes.
 */
public interface BytesIO extends IO {

	/**
	 * Readable bytes IO.
	 */
	interface Readable extends BytesIO, IO.Readable {

		/**
		 * Read a single byte.
		 * @return byte read
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if no more byte can be read
		 * @throws IOException in case an error occurred while reading
		 */
		byte readByte() throws IOException;
		
		/**
		 * Read <i>some</i> bytes. At least one byte is read, but the buffer is not necessarily
		 * filled, if no more byte can be read because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #readBytesFully(ByteBuffer)} this operation will read as much bytes
		 * as possible in a single operation, but will not fill the buffer if reading more bytes
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buffer the buffer to fill
		 * @return number of bytes read, or -1 if the end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws IOException in case an error occurred while reading
		 */
		int readBytes(ByteBuffer buffer) throws IOException;
		
		/**
		 * Read <i>some</i> bytes. At least one byte is read, but the requested bytes are not necessarily
		 * read, if no more byte can be read because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #readBytesFully(byte[], int, int)} this operation will read as much bytes
		 * as possible in a single operation, but will not fill the buffer if reading more bytes
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buf the buffer to fill
		 * @param off offset in the buffer
		 * @param len maximum number of bytes to read
		 * @return number of bytes read, or -1 if the end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws NegativeValueException if off or len is negative
		 * @throws LimitExceededException if off + len &gt; buf.length
		 * @throws IOException in case an error occurred while reading
		 */
		default int readBytes(byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf, off, len);
			return readBytes(ByteBuffer.wrap(buf, off, len));
		}
		
		/**
		 * Read <i>some</i> bytes. At least one byte is read, but the buffer is not necessarily
		 * filled, if no more byte can be read because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #readBytesFully(byte[])} this operation will read as much bytes
		 * as possible in a single operation, but will not fill the buffer if reading more bytes
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buf the buffer to fill
		 * @return number of bytes read, or -1 if the end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws IOException in case an error occurred while reading
		 */
		default int readBytes(byte[] buf) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf);
			return readBytes(buf, 0, buf.length);
		}
		
		/**
		 * Read some bytes and return the buffer.<br/>
		 * Compared to the method {@link #readBytes(ByteBuffer)}, this method lets the implementation allocate
		 * a buffer before to read (and potentially make a better decision if it knows in advance the amount of bytes that
		 * can be read at once). However the counterpart is that each call will allocate a new buffer, making reuse of buffers impossible.
		 * 
		 * @return a buffer if some bytes can be read, or empty in case the end is reached.
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws IOException in case an error occurred while reading
		 */
		Optional<ByteBuffer> readBuffer() throws IOException;
		
		/**
		 * Read bytes to fill the given buffer.<br/>
		 * Compared to {@link #readBytes(ByteBuffer)} this method ensures that the buffer is filled, or
		 * EOFException is thrown.
		 * 
		 * @param buffer the buffer to fill
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if the buffer cannot be filled because it would reached the end
		 * @throws IOException in case an error occurred while reading
		 */
		default void readBytesFully(ByteBuffer buffer) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			while (buffer.hasRemaining())
				if (readBytes(buffer) <= 0) throw new EOFException();
		}
		
		/**
		 * Read <code>len</code> bytes to the given buffer.<br/>
		 * Compared to {@link #readBytes(byte[], int, int)} this method ensures that the requested
		 * number of bytes are read, or EOFException is thrown.
		 * 
		 * @param buf the buffer to fill
		 * @param off offset in the buffer
		 * @param len number of bytes to read
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if the buffer cannot be filled because it would reached the end
		 * @throws NegativeValueException if off or len is negative
		 * @throws LimitExceededException if off + len &gt; buf.length
		 * @throws IOException in case an error occurred while reading
		 */
		default void readBytesFully(byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf, off, len);
			while (len > 0) {
				int nb = readBytes(buf, off, len);
				if (nb <= 0) throw new EOFException();
				off += nb;
				len -= nb;
			}
		}
		
		/**
		 * Read bytes to fill the given buffer.<br/>
		 * Compared to {@link #readBytes(byte[])} this method ensures that the buffer is filled, or
		 * EOFException is thrown.
		 * 
		 * @param buf the buffer to fill
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if the buffer cannot be filled because it would reached the end
		 * @throws IOException in case an error occurred while reading
		 */
		default void readBytesFully(byte[] buf) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf);
			readBytesFully(buf, 0, buf.length);
		}
		
		/**
		 * Skip up to <code>toSkip</code> bytes.
		 * 
		 * @param toSkip maximum number of bytes to skip
		 * @return the number of bytes skipped, or -1 if no byte can be skipped because end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws IOException in case an error occurred while skipping bytes
		 */
		long skipUpTo(long toSkip) throws IOException;
		
		/**
		 * Skip exactly <code>toSkip</code> bytes.
		 * 
		 * @param toSkip number of bytes to skip
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if the requested number of bytes cannot be skipped because it would reach the end
		 * @throws IOException in case an error occurred while skipping bytes
		 */
		default void skipFully(long toSkip) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			NegativeValueException.check(toSkip, "toSkip");
			long done = 0;
			while (done < toSkip) {
				long nb = skipUpTo(toSkip - done);
				if (nb <= 0) throw new EOFException();
				done += nb;
			}
		}
		
		/**
		 * Readable and Seekable bytes IO.
		 */
		interface Seekable extends BytesIO.Readable, IO.Seekable {

			/**
			 * Read a single byte at the given position.
			 * @param pos position
			 * @return byte read
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if the position is at the end
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while reading
			 */
			byte readByteAt(long pos) throws IOException;
			
			/**
			 * Read <i>some</i> bytes at the given position. At least one byte is read, but the buffer is not necessarily
			 * filled, if no more byte can be read because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #readBytesFullyAt(long,ByteBuffer)} this operation will read as much bytes
			 * as possible in a single operation, but will not fill the buffer if reading more bytes
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buffer the buffer to fill
			 * @return number of bytes read, or -1 if the end is reached
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while reading
			 */
			int readBytesAt(long pos, ByteBuffer buffer) throws IOException;
			
			/**
			 * Read <i>some</i> bytes at the given position. At least one byte is read, but the requested bytes are not necessarily
			 * read, if no more byte can be read because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #readBytesFullyAt(long, byte[], int, int)} this operation will read as much bytes
			 * as possible in a single operation, but will not fill the buffer if reading more bytes
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
			 * @param len maximum number of bytes to read
			 * @return number of bytes read, or -1 if the end is reached
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos, off or len is negative
			 * @throws LimitExceededException if off + len &gt; buf.length
			 * @throws IOException in case an error occurred while reading
			 */
			default int readBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
				IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
				return readBytesAt(pos, ByteBuffer.wrap(buf, off, len));
			}
			
			/**
			 * Read <i>some</i> bytes at the given position. At least one byte is read, but the buffer is not necessarily
			 * filled, if no more byte can be read because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #readBytesFullyAt(long,byte[])} this operation will read as much bytes
			 * as possible in a single operation, but will not fill the buffer if reading more bytes
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buf the buffer to fill
			 * @return number of bytes read, or -1 if the end is reached
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while reading
			 */
			default int readBytesAt(long pos, byte[] buf) throws IOException {
				IOChecks.checkByteArrayOperation(this, pos, buf);
				return readBytesAt(pos, buf, 0, buf.length);
			}
			
			/**
			 * Read bytes at the given position to fill the given buffer.<br/>
			 * Compared to {@link #readBytesAt(long,ByteBuffer)} this method ensures that the buffer is filled, or
			 * EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buffer the buffer to fill
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if the buffer cannot be filled because it would reached the end
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while reading
			 */
			default void readBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
				if (isClosed()) throw new ClosedChannelException();
				NegativeValueException.check(pos, IOChecks.FIELD_POS);
				int done = 0;
				while (buffer.hasRemaining()) {
					int nb = readBytesAt(pos + done, buffer);
					if (nb <= 0) throw new EOFException();
					done += nb;
				}
			}
			
			/**
			 * Read <code>len</code> bytes at the given position into the given buffer.<br/>
			 * Compared to {@link #readBytesAt(long, byte[], int, int)} this method ensures that the requested
			 * number of bytes are read, or EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buf the buffer to fill
			 * @param off offset in the buffer
			 * @param len number of bytes to read
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if the buffer cannot be filled because it would reached the end
			 * @throws NegativeValueException if pos, off or len is negative
			 * @throws LimitExceededException if off + len &gt; buf.length
			 * @throws IOException in case an error occurred while reading
			 */
			default void readBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
				IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
				while (len > 0) {
					int nb = readBytesAt(pos, buf, off, len);
					if (nb <= 0) throw new EOFException();
					off += nb;
					pos += nb;
					len -= nb;
				}
			}
			
			/**
			 * Read bytes at the given position to fill the given buffer.<br/>
			 * Compared to {@link #readBytesAt(long,byte[])} this method ensures that the buffer is filled, or
			 * EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buf the buffer to fill
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if the buffer cannot be filled because it would reached the end
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while reading
			 */
			default void readBytesFullyAt(long pos, byte[] buf) throws IOException {
				IOChecks.checkByteArrayOperation(this, pos, buf);
				readBytesFullyAt(pos, buf, 0, buf.length);
			}
			
			/** @return a Readable view of this IO. */
			default BytesIO.Readable asReadableBytesIO() {
				return new BytesIOView.Readable(this);
			}
			
		}

		
	}
	
	
	
	/**
	 * Writable bytes IO.
	 */
	interface Writable extends BytesIO, IO.Writable {

		/**
		 * Write a single byte.
		 * @param value byte to write
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if no more byte can be written
		 * @throws IOException in case an error occurred while writing
		 */
		void writeByte(byte value) throws IOException;
		
		/**
		 * Write <i>some</i> bytes. At least one byte is written, but the buffer is not necessarily
		 * consumed, if no more byte can be written because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #writeBytesFully(ByteBuffer)} this operation will write as much bytes
		 * as possible in a single operation, but will not write all if writing more bytes
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buffer the buffer to write
		 * @return number of bytes written, or -1 if the end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws IOException in case an error occurred while writing
		 */
		int writeBytes(ByteBuffer buffer) throws IOException;
		
		/**
		 * Write up to <code>len</code> bytes. At least one byte is written, but the requested number of bytes
		 * are not necessarily written, if no more byte can be written because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #writeBytesFully(byte[],int,int)} this operation will write as much bytes
		 * as possible in a single operation, but will not write all if writing more bytes
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buf the buffer to write
		 * @param off offset in the buffer
		 * @param len maximum number of bytes to write
		 * @return number of bytes written, or -1 if the end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws NegativeValueException if off or len is negative
		 * @throws LimitExceededException if off + len &gt; buf.length
		 * @throws IOException in case an error occurred while writing
		 */
		default int writeBytes(byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf, off, len);
			return writeBytes(ByteBuffer.wrap(buf, off, len));
		}
		
		/**
		 * Write <i>some</i> bytes. At least one byte is written, but the buffer is not necessarily
		 * consumed, if no more byte can be written because the end is reached -1 is returned.
		 * <p>
		 * Compare to {@link #writeBytesFully(byte[])} this operation will write as much bytes
		 * as possible in a single operation, but will not write all if writing more bytes
		 * would be costly.<br/>
		 * </p>
		 * <p>
		 * Another important difference is that it will never throw EOFException, but return -1
		 * if end is reached.
		 * </p>
		 * 
		 * @param buf the buffer to write
		 * @return number of bytes written, or -1 if the end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws IOException in case an error occurred while writing
		 */
		default int writeBytes(byte[] buf) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf);
			return writeBytes(buf, 0, buf.length);
		}
		
		/**
		 * Write all bytes from the given buffer.<br/>
		 * If it cannot write all bytes because end is reached, EOFException is thrown.
		 * 
		 * @param buffer the buffer to write
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if all bytes cannot be written because end is reached
		 * @throws IOException in case an error occurred while writing
		 */
		default void writeBytesFully(ByteBuffer buffer) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			while (buffer.hasRemaining())
				if (writeBytes(buffer) <= 0) throw new EOFException();
		}
		
		/**
		 * Write all bytes from the all the given buffer.<br/>
		 * If it cannot write all bytes because end is reached, EOFException is thrown.
		 * 
		 * @param buffers the buffers to write
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if all bytes cannot be written because end is reached
		 * @throws IOException in case an error occurred while writing
		 */
		default void writeBytesFully(List<ByteBuffer> buffers) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			Objects.requireNonNull(buffers);
			for (var b : buffers) writeBytesFully(b);
		}
		
		/**
		 * Write exactly <code>len</code> bytes from the given buffer.<br/>
		 * If it cannot write all bytes because end is reached, EOFException is thrown.
		 * 
		 * @param buf the buffer to write
		 * @param off offset in the buffer
		 * @param len number of bytes to write
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if all bytes cannot be written because end is reached
		 * @throws NegativeValueException if off or len is negative
		 * @throws LimitExceededException if off + len &gt; buf.length
		 * @throws IOException in case an error occurred while writing
		 */
		default void writeBytesFully(byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf, off, len);
			while (len > 0) {
				int nb = writeBytes(buf, off, len);
				if (nb <= 0) throw new EOFException();
				off += nb;
				len -= nb;
			}
		}
		
		/**
		 * Write all bytes from the given buffer.<br/>
		 * If it cannot write all bytes because end is reached, EOFException is thrown.
		 * 
		 * @param buf the buffer to write
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if all bytes cannot be written because end is reached
		 * @throws IOException in case an error occurred while writing
		 */
		default void writeBytesFully(byte[] buf) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf);
			writeBytesFully(buf, 0, buf.length);
		}

		
		/**
		 * Writable and Seekable bytes IO.
		 */
		interface Seekable extends BytesIO.Writable, IO.Seekable {

			/**
			 * Write a single byte at the given position.
			 * @param pos position
			 * @param value byte to write
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if pos is beyond the end
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while writing
			 */
			void writeByteAt(long pos, byte value) throws IOException;
			
			/**
			 * Write <i>some</i> bytes at the given position. At least one byte is written, but the buffer is not necessarily
			 * consumed, if no more byte can be written because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #writeBytesFullyAt(long,ByteBuffer)} this operation will write as much bytes
			 * as possible in a single operation, but will not write all if writing more bytes
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buffer the buffer to write
			 * @return number of bytes written, or -1 if the end is reached
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while writing
			 */
			int writeBytesAt(long pos, ByteBuffer buffer) throws IOException;
			
			/**
			 * Write up to <code>len</code> bytes at the given position. At least one byte is written, but the requested number of bytes
			 * are not necessarily written, if no more byte can be written because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #writeBytesFullyAt(long,byte[],int,int)} this operation will write as much bytes
			 * as possible in a single operation, but will not write all if writing more bytes
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
			 * @param len maximum number of bytes to write
			 * @return number of bytes written, or -1 if the end is reached
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos, off or len is negative
			 * @throws LimitExceededException if off + len &gt; buf.length
			 * @throws IOException in case an error occurred while writing
			 */
			default int writeBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
				IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
				return writeBytesAt(pos, ByteBuffer.wrap(buf, off, len));
			}
			
			/**
			 * Write <i>some</i> bytes at the given position. At least one byte is written, but the buffer is not necessarily
			 * consumed, if no more byte can be written because the end is reached -1 is returned.
			 * <p>
			 * Compare to {@link #writeBytesFully(byte[])} this operation will write as much bytes
			 * as possible in a single operation, but will not write all if writing more bytes
			 * would be costly.<br/>
			 * </p>
			 * <p>
			 * Another important difference is that it will never throw EOFException, but return -1
			 * if end is reached.
			 * </p>
			 * 
			 * @param pos position
			 * @param buf the buffer to write
			 * @return number of bytes written, or -1 if the end is reached
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while writing
			 */
			default int writeBytesAt(long pos, byte[] buf) throws IOException {
				IOChecks.checkByteArrayOperation(this, pos, buf);
				return writeBytesAt(pos, buf, 0, buf.length);
			}
			
			/**
			 * Write all bytes from the given buffer at the given position.<br/>
			 * If it cannot write all bytes because end is reached, EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buffer the buffer to write
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if all bytes cannot be written because end is reached
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while writing
			 */
			default void writeBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
				IOChecks.checkByteBufferOperation(this, pos, buffer);
				int done = 0;
				while (buffer.hasRemaining()) {
					int nb = writeBytesAt(pos + done, buffer);
					if (nb <= 0) throw new EOFException();
					done += nb;
				}
			}
			
			/**
			 * Write exactly <code>len</code> bytes from the given buffer at the given position.<br/>
			 * If it cannot write all bytes because end is reached, EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buf the buffer to write
			 * @param off offset in the buffer
			 * @param len number of bytes to write
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if all bytes cannot be written because end is reached
			 * @throws NegativeValueException if pos, off or len is negative
			 * @throws LimitExceededException if off + len &gt; buf.length
			 * @throws IOException in case an error occurred while writing
			 */
			default void writeBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
				IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
				writeBytesFullyAt(pos, ByteBuffer.wrap(buf, off, len));
			}
			
			/**
			 * Write all bytes from the given buffer at the given position.<br/>
			 * If it cannot write all bytes because end is reached, EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buf the buffer to write
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws EOFException if all bytes cannot be written because end is reached
			 * @throws NegativeValueException if pos is negative
			 * @throws IOException in case an error occurred while writing
			 */
			default void writeBytesFullyAt(long pos, byte[] buf) throws IOException {
				IOChecks.checkByteArrayOperation(this, pos, buf);
				writeBytesFullyAt(pos, buf, 0, buf.length);
			}
			
			/**
			 * Write all bytes ath the given position from the all the given buffer.<br/>
			 * If it cannot write all bytes because end is reached, EOFException is thrown.
			 * 
			 * @param pos position
			 * @param buffers the buffers to write
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException if pos is negative
			 * @throws EOFException if all bytes cannot be written because end is reached
			 * @throws IOException in case an error occurred while writing
			 */
			default void writeBytesFullyAt(long pos, List<ByteBuffer> buffers) throws IOException {
				if (isClosed()) throw new ClosedChannelException();
				NegativeValueException.check(pos, IOChecks.FIELD_POS);
				Objects.requireNonNull(buffers);
				long p = pos;
				for (var b : buffers) {
					int nb = b.remaining();
					writeBytesFullyAt(p, b);
					p += nb;
				}
			}
			
			/** @return a Writable view of this IO. */
			default BytesIO.Writable asWritableBytesIO() {
				return BytesIOView.Writable.of(this);
			}
			
			/** Writable Seekable and Appendable BytesIO. */
			interface Appendable extends BytesIO.Writable.Seekable, IO.Writable.Appendable {
				
			}
			
			/** Writable Seekable and Resizable BytesIO. */
			interface Resizable extends BytesIO.Writable.Seekable, IO.Writable.Resizable {
				
				/** @return a non-resizable view of this BytesIO. */
				default BytesIO.Writable.Seekable asNonResizableWritableSeekableBytesIO() {
					return BytesIOView.Writable.Seekable.of(this);
				}
				
			}
			
			/** Writable Seekable Appendable and Resizable BytesIO. */
			interface AppendableResizable extends BytesIO.Writable.Seekable.Appendable, BytesIO.Writable.Seekable.Resizable {
				
			}
		}
		
	}
	
	/** Readable and Writable Seekable BytesIO. */
	interface ReadWrite extends BytesIO.Readable.Seekable, BytesIO.Writable.Seekable {
		
		/** @return a Readable and Seekable view of this IO. */
		default BytesIO.Readable.Seekable asReadableSeekableBytesIO() {
			return new BytesIOView.Readable.Seekable(this);
		}
		
		/** @return a Writable and Seekable view of this IO. */
		default BytesIO.Writable.Seekable asWritableSeekableBytesIO() {
			return BytesIOView.Writable.Seekable.of(this);
		}
		
		/** Readable and Writable Seekable Resizable BytesIO. */
		interface Resizable extends ReadWrite, BytesIO.Writable.Seekable.Resizable {
			
			/** @return a non-resizable view of this BytesIO. */
			default BytesIO.ReadWrite asNonResizableReadWriteBytesIO() {
				return BytesIOView.ReadWrite.of(this);
			}
			
		}
		
		/** Readable and Writable Seekable Appendable BytesIO. */
		interface Appendable extends ReadWrite, BytesIO.Writable.Seekable.Appendable {
			
		}
		
		/** Readable and Writable Seekable Appendable and Resizable BytesIO. */
		interface AppendableResizable extends ReadWrite.Appendable, ReadWrite.Resizable {
			
		}
		
	}

}
