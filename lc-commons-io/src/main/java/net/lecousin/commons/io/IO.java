package net.lecousin.commons.io;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import net.lecousin.commons.exceptions.NegativeValueException;

/** Interface for an IO.
 * An IO is a Closeable, on which we can ask if it is already closed, and we can listen to the close event.
 */
public interface IO extends Closeable {

	/**
	 * @return true if this IO is closed.
	 */
	boolean isClosed();
	
	/**
	 * Listen to the close event.<br/>
	 * If the IO is already closed, the listener is immediately called.
	 * 
	 * @param listener the listener
	 */
	void onClose(Runnable listener);
	
	/**
	 * Marker interface for a Readable IO.
	 */
	interface Readable extends IO {
		
		/**
		 * Skip up to <code>toSkip</code> elements.
		 * 
		 * @param toSkip maximum number of elements to skip
		 * @return the number of elements skipped, or -1 if no byte can be skipped because end is reached
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws IOException in case an error occurred while skipping bytes
		 */
		long skipUpTo(long toSkip) throws IOException;
		
		/**
		 * Skip exactly <code>toSkip</code> elements.
		 * 
		 * @param toSkip number of elements to skip
		 * @throws ClosedChannelException if this IO is already closed
		 * @throws EOFException if the requested number of elements cannot be skipped because it would reach the end
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

	}
	
	/**
	 * Marker interface for a Writable IO.
	 */
	interface Writable extends IO {
		
		/**
	     * Flushes this writable IO and forces any buffered output to be written out.
	     * @throws IOException if an I/O error occurs.
	     */
		void flush() throws IOException;
		
		/** Marker interface for a Writable on which we can append data at the end. */
		interface Appendable extends Writable { }
		
		/** Writable that can be resized. */
		interface Resizable extends Writable, KnownSize {
			
			/** Set the size of this IO
			 * 
			 * @param newSize new size
			 * @throws ClosedChannelException if this IO is already closed
			 * @throws NegativeValueException in case newLength is negative
			 * @throws IOException in case of other error
			 */
			void setSize(long newSize) throws IOException;
		}
	}
	
	/**
	 * An IO with a known size.
	 */
	interface KnownSize extends IO {
		/**
		 * @return the size of this IO
		 * @throws ClosedChannelException if this IO is already closed.
		 * @throws IOException in case the size cannot be obtained.
		 */
		long size() throws IOException;
	}
	
	/**
	 * A Seekable IO.
	 */
	interface Seekable extends KnownSize {
		
		/** Type of seek. */
		enum SeekFrom {
			/** From the start of the IO. */
			START,
			/** From the end of the IO. */
			END,
			/** From the current position. */
			CURRENT
		}
		
		/**
		 * @return the current position in the IO
		 * @throws ClosedChannelException if this IO is already closed.
		 * @throws IOException in case the position cannot be obtained
		 */
		long position() throws IOException;
		
		/**
		 * Move the position in this IO.
		 * @param from from where the offset is
		 * @param offset offset
		 * @return the new position
		 * @throws ClosedChannelException if this IO is already closed.
		 * @throws NullPointerException if from is null
		 * @throws IllegalArgumentException if the resulting position would be negative
		 * @throws EOFException in case the move cannot be done because it would be beyond the end of this IO
		 * @throws IOException in case of error
		 */
		long seek(SeekFrom from, long offset) throws IOException;
	}
	
}
