package net.lecousin.commons.io;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;

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
	interface Readable extends IO { }
	
	/**
	 * Marker interface for a Writable IO.
	 */
	interface Writable extends IO { }
	
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
