package net.lecousin.commons.io.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;

class TestDataChecks {

	@Test
	void byteArray() {
		assertThrows(NullPointerException.class, () -> DataChecks.checkByteArray(null, -1, -1));
		assertThat(DataChecks.byteArrayChecker(null, -1, -1)).isPresent().containsInstanceOf(NullPointerException.class);

		assertThrows(NegativeValueException.class, () -> DataChecks.checkByteArray(new byte[0], -1, 0));
		assertThat(DataChecks.byteArrayChecker(new byte[0], -1, 0)).isPresent().containsInstanceOf(NegativeValueException.class);

		assertThrows(NegativeValueException.class, () -> DataChecks.checkByteArray(new byte[0], 0, -1));
		assertThat(DataChecks.byteArrayChecker(new byte[0], 0, -1)).isPresent().containsInstanceOf(NegativeValueException.class);

		assertThrows(LimitExceededException.class, () -> DataChecks.checkByteArray(new byte[10], 11, 1));
		assertThat(DataChecks.byteArrayChecker(new byte[10], 11, 1)).isPresent().containsInstanceOf(LimitExceededException.class);

		assertThrows(LimitExceededException.class, () -> DataChecks.checkByteArray(new byte[10], 4, 7));
		assertThat(DataChecks.byteArrayChecker(new byte[10], 4, 7)).isPresent().containsInstanceOf(LimitExceededException.class);

		DataChecks.checkByteArray(new byte[10], 4, 6);
		assertThat(DataChecks.byteArrayChecker(new byte[10], 4, 6)).isEmpty();
	}
	
}
