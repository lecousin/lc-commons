package net.lecousin.commons.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TestExceptions {

	@Test
	void testNonNullChecker() {
		assertThat(ExceptionsUtils.nonNullChecker(1, "test")).isEmpty();
		assertThat(ExceptionsUtils.nonNullChecker(null, "test")).isPresent().containsInstanceOf(NullPointerException.class);
	}
	
	@Test
	void testNegativeValueChecker() {
		assertThat(NegativeValueException.checker(0, "test")).isEmpty();
		assertThat(NegativeValueException.checker(1, "test")).isEmpty();
		assertThat(NegativeValueException.checker(-1, "test")).isPresent().containsInstanceOf(NegativeValueException.class);
	}
	
	@Test
	void testLimitWithNegative() {
		LimitExceededException.checkWithNonNegative(0, 0, "test", "test2");
		assertThrows(NegativeValueException.class, () -> LimitExceededException.checkWithNonNegative(-1, 0, "test", "test2"));
		assertThrows(LimitExceededException.class, () -> LimitExceededException.checkWithNonNegative(1, 0, "test", "test2"));
	}
	
}
