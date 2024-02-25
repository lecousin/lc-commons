package net.lecousin.commons.io.data;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;

/**
 * Utilities to check methods' inputs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataChecks {
	
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
		if (buf == null) throw new NullPointerException(FIELD_BUF + " is null");
		NegativeValueException.check(off, FIELD_OFF);
		NegativeValueException.check(len, FIELD_LEN);
		LimitExceededException.check((long) off + len, buf.length, FIELD_OFF + " + " + FIELD_LEN, FIELD_BUF + ".length");
	}
	
	/**
	 * Check byte array parameters.
	 * @param buf buffer
	 * @param off offset
	 * @param len length
	 * @return an optional exception
	 */
	public static Optional<Exception> byteArrayChecker(byte[] buf, int off, int len) {
		if (buf == null) return Optional.of(new NullPointerException(FIELD_BUF + " is null"));
		if (off < 0) return Optional.of(new NegativeValueException(off, FIELD_OFF));
		if (len < 0) return Optional.of(new NegativeValueException(len, FIELD_LEN));
		if (off + len > buf.length) return Optional.of(new LimitExceededException(off + len, buf.length, FIELD_OFF + " + " + FIELD_LEN, FIELD_BUF + ".length"));
		return Optional.empty();
	}
	
}
