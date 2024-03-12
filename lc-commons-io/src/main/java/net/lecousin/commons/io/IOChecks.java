package net.lecousin.commons.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
	 * Check byte array parameters.
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @throws NullPointerException if buf is null
	 * @throws NegativeValueException if off or len is negative
	 * @throws LimitExceededException if off + len is greater than buf.length
	 */
	@SuppressWarnings("java:S1695") // NPE thrown
	public static void checkByteArray(byte[] buf, int off, int len) {
		if (buf == null) throw new NullPointerException(FIELD_BUF);
		NegativeValueException.check(off, FIELD_OFF);
		NegativeValueException.check(len, FIELD_LEN);
		LimitExceededException.check((long) off + len, buf.length, FIELD_OFF + " + " + FIELD_LEN, FIELD_BUF + LENGTH);
	}
	
	/**
	 * Checks the I/O is not closed, then that the given byte array parameters are correct.
	 * @param io the IO
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buf is null
	 * @throws NegativeValueException if off or len is negative
	 * @throws LimitExceededException if off + len is greater than buf.length
	 */
	public static void checkByteArrayOperation(IO io, byte[] buf, int off, int len) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		checkByteArray(buf, off, len);
	}
	
	/**
	 * Checks the I/O is not closed, then that the given byte array is not null.
	 * @param io the IO
	 * @param buf buffer
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buf is null
	 */
	public static void checkByteArrayOperation(IO io, byte[] buf) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		Objects.requireNonNull(buf, FIELD_BUF);
	}
	
	/**
	 * Checks the I/O is not closed, the position is not negative, then that the given byte array parameters are correct.
	 * @param io the IO
	 * @param pos position
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buf is null
	 * @throws NegativeValueException if pos, off or len is negative
	 * @throws LimitExceededException if off + len is greater than buf.length
	 */
	public static void checkByteArrayOperation(IO io, long pos, byte[] buf, int off, int len) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		NegativeValueException.check(pos, FIELD_POS);
		checkByteArray(buf, off, len);
	}
	
	/**
	 * Checks the I/O is not closed, the position is not negative, then that the given byte array is not null.
	 * @param io the IO
	 * @param pos position
	 * @param buf buffer
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buf is null
	 * @throws NegativeValueException if pos is negative
	 */
	public static void checkByteArrayOperation(IO io, long pos, byte[] buf) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		NegativeValueException.check(pos, FIELD_POS);
		Objects.requireNonNull(buf, FIELD_BUF);
	}
	
	/**
	 * Checks the I/O is not closed, the position is not negative, then that the given ByteBuffer is not null.
	 * @param io the IO
	 * @param pos position
	 * @param buffer buffer
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buffer is null
	 * @throws NegativeValueException if pos is negative
	 */
	public static void checkByteBufferOperation(IO io, long pos, ByteBuffer buffer) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		NegativeValueException.check(pos, FIELD_POS);
		Objects.requireNonNull(buffer, FIELD_BUFFER);
	}
	
	/**
	 * Check byte array parameters.
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @return an optional exception
	 */
	public static Optional<Exception> byteArrayChecker(byte[] buf, int off, int len) {
		if (buf == null) return Optional.of(new NullPointerException(FIELD_BUF));
		if (off < 0) return Optional.of(new NegativeValueException(off, FIELD_OFF));
		if (len < 0) return Optional.of(new NegativeValueException(len, FIELD_LEN));
		if (off + len > buf.length) return Optional.of(new LimitExceededException(off + len, buf.length, FIELD_OFF + " + " + FIELD_LEN, FIELD_BUF + LENGTH));
		return Optional.empty();
	}
	
	
	/**
	 * Check character array parameters.
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @throws NullPointerException if buf is null
	 * @throws NegativeValueException if off or len is negative
	 * @throws LimitExceededException if off + len is greater than buf.length
	 */
	@SuppressWarnings("java:S1695") // NPE thrown
	public static void checkCharArray(char[] buf, int off, int len) {
		if (buf == null) throw new NullPointerException(FIELD_BUF);
		NegativeValueException.check(off, FIELD_OFF);
		NegativeValueException.check(len, FIELD_LEN);
		LimitExceededException.check((long) off + len, buf.length, FIELD_OFF + " + " + FIELD_LEN, FIELD_BUF + LENGTH);
	}
	
	/**
	 * Checks the I/O is not closed, then that the given character array parameters are correct.
	 * @param io the IO
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buf is null
	 * @throws NegativeValueException if off or len is negative
	 * @throws LimitExceededException if off + len is greater than buf.length
	 */
	public static void checkCharArrayOperation(IO io, char[] buf, int off, int len) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		checkCharArray(buf, off, len);
	}
	
	/**
	 * Checks the I/O is not closed, then that the given character array is not null.
	 * @param io the IO
	 * @param buf buffer
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buf is null
	 */
	public static void checkCharArrayOperation(IO io, char[] buf) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		Objects.requireNonNull(buf, FIELD_BUF);
	}
	
	/**
	 * Checks the I/O is not closed, the position is not negative, then that the given character array parameters are correct.
	 * @param io the IO
	 * @param pos position
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buf is null
	 * @throws NegativeValueException if pos, off or len is negative
	 * @throws LimitExceededException if off + len is greater than buf.length
	 */
	public static void checkCharArrayOperation(IO io, long pos, char[] buf, int off, int len) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		NegativeValueException.check(pos, FIELD_POS);
		checkCharArray(buf, off, len);
	}
	
	/**
	 * Checks the I/O is not closed, the position is not negative, then that the given character array is not null.
	 * @param io the IO
	 * @param pos position
	 * @param buf buffer
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buf is null
	 * @throws NegativeValueException if pos is negative
	 */
	public static void checkCharArrayOperation(IO io, long pos, char[] buf) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		NegativeValueException.check(pos, FIELD_POS);
		Objects.requireNonNull(buf, FIELD_BUF);
	}
	
	/**
	 * Checks the I/O is not closed, the position is not negative, then that the given CharBuffer is not null.
	 * @param io the IO
	 * @param pos position
	 * @param buffer buffer
	 * @throws ClosedChannelException if the IO is closed
	 * @throws NullPointerException if buffer is null
	 * @throws NegativeValueException if pos is negative
	 */
	public static void checkCharBufferOperation(IO io, long pos, CharBuffer buffer) throws ClosedChannelException {
		if (io.isClosed()) throw new ClosedChannelException();
		NegativeValueException.check(pos, FIELD_POS);
		Objects.requireNonNull(buffer, FIELD_BUFFER);
	}
	
	/**
	 * Check character array parameters.
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @return an optional exception
	 */
	public static Optional<Exception> charArrayChecker(char[] buf, int off, int len) {
		if (buf == null) return Optional.of(new NullPointerException(FIELD_BUF));
		if (off < 0) return Optional.of(new NegativeValueException(off, FIELD_OFF));
		if (len < 0) return Optional.of(new NegativeValueException(len, FIELD_LEN));
		if (off + len > buf.length) return Optional.of(new LimitExceededException(off + len, buf.length, FIELD_OFF + " + " + FIELD_LEN, FIELD_BUF + LENGTH));
		return Optional.empty();
	}

}
