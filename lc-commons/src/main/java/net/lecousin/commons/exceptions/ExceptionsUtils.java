package net.lecousin.commons.exceptions;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Utilities for exception handling. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionsUtils {

	/** Check if a parameter is null.
	 * 
	 * @param value the value that must not be null
	 * @param valueName name of the value for error message
	 * @return a NullPointerException or empty.
	 */
	public static Optional<Exception> nonNullChecker(Object value, String valueName) {
		if (value == null) return Optional.of(new NullPointerException(valueName));
		return Optional.empty();
	}
	
}
