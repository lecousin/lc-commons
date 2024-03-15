package net.lecousin.commons.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;

class TestIOChecks {

	@Test
	void byteArray() {
		assertThrows(NullPointerException.class, () -> IOChecks.checkArray(null, -1, -1));
		assertThat(IOChecks.arrayChecker(null, -1, -1)).isPresent().containsInstanceOf(NullPointerException.class);

		assertThrows(NegativeValueException.class, () -> IOChecks.checkArray(new byte[0], -1, 0));
		assertThat(IOChecks.arrayChecker(new byte[0], -1, 0)).isPresent().containsInstanceOf(NegativeValueException.class);

		assertThrows(NegativeValueException.class, () -> IOChecks.checkArray(new byte[0], 0, -1));
		assertThat(IOChecks.arrayChecker(new byte[0], 0, -1)).isPresent().containsInstanceOf(NegativeValueException.class);

		assertThrows(LimitExceededException.class, () -> IOChecks.checkArray(new byte[10], 11, 1));
		assertThat(IOChecks.arrayChecker(new byte[10], 11, 1)).isPresent().containsInstanceOf(LimitExceededException.class);

		assertThrows(LimitExceededException.class, () -> IOChecks.checkArray(new byte[10], 4, 7));
		assertThat(IOChecks.arrayChecker(new byte[10], 4, 7)).isPresent().containsInstanceOf(LimitExceededException.class);

		IOChecks.checkArray(new byte[10], 4, 6);
		assertThat(IOChecks.arrayChecker(new byte[10], 4, 6)).isEmpty();
	}
	
}
