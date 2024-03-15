package net.lecousin.commons.io;

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;

/**
 * Utilities to check methods' inputs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IOChecks {
	
	/** Parameter buf. */
	public static final String FIELD_BUF = "buf";
	/** Parameter off. */
	public static final String FIELD_OFF = "off";
	/** Parameter len. */
	public static final String FIELD_LEN = "len";
	/** Parameter pos. */
	public static final String FIELD_POS = "pos";
	/** Parameter buffer. */
	public static final String FIELD_BUFFER = "buffer";
	
	private static final String LENGTH = ".length";

	/**
	 * Check array parameters.
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @param <T> type of array
	 * @throws NullPointerException if buf is null
	 * @throws NegativeValueException if off or len is negative
	 * @throws LimitExceededException if off + len is greater than buf.length
	 */
	@SuppressWarnings("java:S1695") // NPE thrown
	public static <T> void checkArray(T buf, int off, int len) {
		if (buf == null) throw new NullPointerException(FIELD_BUF);
		NegativeValueException.check(off, FIELD_OFF);
		NegativeValueException.check(len, FIELD_LEN);
		LimitExceededException.check((long) off + len, Array.getLength(buf), FIELD_OFF + " + " + FIELD_LEN, FIELD_BUF + LENGTH);
	}
	
	/**
	 * Checks the I/O is not closed, then that the given array parameters are correct.
	 * @param io the IO
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @param <T> type of array
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buf is null
	 * @throws NegativeValueException if off or len is negative
	 * @throws LimitExceededException if off + len is greater than buf.length
	 */
	public static <T> void checkArrayOperation(IO io, T buf, int off, int len) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		checkArray(buf, off, len);
	}
	
	/**
	 * Checks the I/O is not closed, then that the given array is not null.
	 * @param io the IO
	 * @param buf buffer
	 * @param <T> type of array
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buf is null
	 */
	public static <T> void checkArrayOperation(IO io, T buf) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		Objects.requireNonNull(buf, FIELD_BUF);
	}
	
	/**
	 * Checks the I/O is not closed, the position is not negative, then that the given array parameters are correct.
	 * @param io the IO
	 * @param pos position
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @param <T> type of array
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buf is null
	 * @throws NegativeValueException if pos, off or len is negative
	 * @throws LimitExceededException if off + len is greater than buf.length
	 */
	public static <T> void checkArrayOperation(IO io, long pos, T buf, int off, int len) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		NegativeValueException.check(pos, FIELD_POS);
		checkArray(buf, off, len);
	}
	
	/**
	 * Checks the I/O is not closed, the position is not negative, then that the given array is not null.
	 * @param io the IO
	 * @param pos position
	 * @param buf buffer
	 * @param <T> type of array
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buf is null
	 * @throws NegativeValueException if pos is negative
	 */
	public static <T> void checkArrayOperation(IO io, long pos, T buf) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		NegativeValueException.check(pos, FIELD_POS);
		Objects.requireNonNull(buf, FIELD_BUF);
	}
	
	/**
	 * Checks the I/O is not closed, the position is not negative, then that the given Buffer is not null.
	 * @param io the IO
	 * @param pos position
	 * @param buffer buffer
	 * @param <T> type of buffer
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buffer is null
	 * @throws NegativeValueException if pos is negative
	 */
	public static <T extends Buffer> void checkBufferOperation(IO io, long pos, T buffer) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		NegativeValueException.check(pos, FIELD_POS);
		Objects.requireNonNull(buffer, FIELD_BUFFER);
	}
	
	/**
	 * Check array parameters.
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @param <T> type of array
	 * @return an optional exception
	 */
	public static <T> Optional<Exception> arrayChecker(T buf, int off, int len) {
		if (buf == null) return Optional.of(new NullPointerException(FIELD_BUF));
		if (off < 0) return Optional.of(new NegativeValueException(off, FIELD_OFF));
		if (len < 0) return Optional.of(new NegativeValueException(len, FIELD_LEN));
		if (off + len > Array.getLength(buf)) return Optional.of(new LimitExceededException(off + len, Array.getLength(buf), FIELD_OFF + " + " + FIELD_LEN, FIELD_BUF + LENGTH));
		return Optional.empty();
	}
	
}
