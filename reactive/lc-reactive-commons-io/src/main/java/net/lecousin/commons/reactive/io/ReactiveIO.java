package net.lecousin.commons.reactive.io;

import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

/**
 * An I/O, the reactive way.
 */
public interface ReactiveIO {

	/** Close this I/O.
	 * @return empty on success, or IOException if an I/O error occurs.
	 */
	Mono<Void> close();
	
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
	void onClose(Mono<Void> listener);
	
	/** @return the scheduler in which operations on this IO should be performed. */
	Scheduler getScheduler();
	
	/**
	 * Marker interface for a Readable IO.
	 */
	interface Readable extends ReactiveIO { }
	
	/**
	 * Marker interface for a Writable IO.
	 */
	interface Writable extends ReactiveIO {
		
		/**
	     * Flushes this writable IO and forces any buffered output to be written out.
	     * @return empty on success, or fails with an IOException if an I/O error occurs.
	     */
		Mono<Void> flush();
		
		/** Marker interface for a Writable on which we can append data at the end. */
		interface Appendable extends Writable { }
		
		/** Writable that can be resized. */
		interface Resizable extends Writable, KnownSize {
			
			/** Set the size of this IO
			 * 
			 * @param newSize new size
			 * @return empty on success, or<ul>
			 * 	<li>ClosedChannelException if this IO is already closed</li>
			 * 	<li>NegativeValueException in case newLength is negative</li>
			 * 	<li>IOException in case of other error</li>
			 * </ul>
			 */
			Mono<Void> setSize(long newSize);
		}
	}
	
	/**
	 * An IO with a known size.
	 */
	interface KnownSize extends ReactiveIO {
		/**
		 * @return the size of this IO on success, or<ul>
		 * 	<li>ClosedChannelException if this IO is already closed.</li>
		 *  <li>IOException in case the size cannot be obtained.</li>
		 * </ul>
		 */
		Mono<Long> size();
	}
	
	/**
	 * A Seekable IO.
	 */
	interface Seekable extends KnownSize {
		
		/**
		 * @return the current position in the IO on success, or<ul>
		 * 	<li>ClosedChannelException if this IO is already closed.</li>
		 * 	<li>IOException in case the position cannot be obtained</li>
		 * </ul>
		 */
		Mono<Long> position();
		
		/**
		 * Move the position in this IO.
		 * @param from from where the offset is
		 * @param offset offset
		 * @return the new position on success, or<ul>
		 * 	<li>ClosedChannelException if this IO is already closed.</li>
		 * 	<li>NullPointerException if from is null</li>
		 * 	<li>IllegalArgumentException if the resulting position would be negative</li>
		 * 	<li>EOFException in case the move cannot be done because it would be beyond the end of this IO</li>
		 * 	<li>IOException in case of error</li>
		 * </ul>
		 */
		Mono<Long> seek(SeekFrom from, long offset);
	}

}
