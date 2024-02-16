package net.lecousin.commons.exceptions;

import java.util.Optional;

/**
 * A negative value has been received but is not allowed.
 */
public class NegativeValueException extends IndexOutOfBoundsException {

	private static final long serialVersionUID = 1L;

	/** Constructor.
	 * 
	 * @param value negative value
	 * @param parameterName name of the value
	 */
	public NegativeValueException(Object value, String parameterName) {
		super(parameterName + " < 0 (" + value + " < 0)");
	}
	
	/**
	 * Check value is not negative.
	 * @param value value to check
	 * @param parameterName name of the parameter containing the value, for error message
	 * @throws NegativeValueException in case value is negative
	 */
	public static void check(long value, String parameterName) {
		if (value < 0) throw new NegativeValueException(value, parameterName);
	}
	
	/**
	 * Same ad {@link #check(long, String)} but returns an Optional instead of throwing the exception.
	 * @param value the value to check
	 * @param parameterName name of the value
	 * @return NegativeValueException or empty
	 */
	public static Optional<Exception> checker(long value, String parameterName) {
		if (value < 0) return Optional.of(new NegativeValueException(value, parameterName));
		return Optional.empty();
	}
	
}
