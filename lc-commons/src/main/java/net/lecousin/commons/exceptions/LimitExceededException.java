package net.lecousin.commons.exceptions;

/**
 * A value is exceeding the limit.
 */
public class LimitExceededException extends IndexOutOfBoundsException {

	private static final long serialVersionUID = 1L;

	/** Constructor.
	 * 
	 * @param value value exceeding the limit
	 * @param limit the limit
	 * @param valueName name of the value
	 * @param limitName name of the limit
	 */
	public LimitExceededException(Object value, Object limit, String valueName, String limitName) {
		super(valueName + " > " + limitName + " (" + value + " > " + limit + ")");
	}
	
	/**
	 * Check a value is not greater than the limit
	 * @param value value to check
	 * @param limit maximum value
	 * @param valueName name of the value for error message
	 * @param limitName name of the limit for error message
	 * @throws LimitExceededException if value is greater than limit
	 */
	public static void check(long value, long limit, String valueName, String limitName) {
		if (value > limit) throw new LimitExceededException(value, limit, valueName, limitName);
	}
	
	/**
	 * First check the value is non negative, then that is is not greater than limit.
	 * @param value value
	 * @param limit maximum accepted value
	 * @param valueName name of the value for error message
	 * @param limitName name of the limit for error message
	 * @throws NegativeValueException in case value is negative
	 * @throws LimitExceededException in case value is greater than limit
	 */
	public static void checkWithNonNegative(long value, long limit, String valueName, String limitName) {
		NegativeValueException.check(value, valueName);
		check(value, limit, valueName, limitName);
	}
	
}
