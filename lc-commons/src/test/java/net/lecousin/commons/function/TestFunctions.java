package net.lecousin.commons.function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class TestFunctions {

	@Test
	void testFailableTriConsumer() {
		FailableTriConsumer<Integer, String, Long, IOException> consumer = (a, b, c) -> {
			if (a < 0) throw new IOException();
			if (b == null) throw new IOException();
			if (c > 0) throw new IOException();
		};
		assertDoesNotThrow(() -> consumer.accept(1, "", -1L));
		assertThrows(IOException.class, () -> consumer.accept(-1, "", -1L));
		assertThrows(IOException.class, () -> consumer.accept(1, null, -1L));
		assertThrows(IOException.class, () -> consumer.accept(1, "", 1L));
		assertDoesNotThrow(() -> FailableTriConsumer.nop().accept(null, null, null));

		assertDoesNotThrow(() -> consumer.andThen(FailableTriConsumer.nop()).accept(1, "", -1L));
		assertThrows(IOException.class, () -> consumer.andThen(FailableTriConsumer.nop()).accept(-1, "", -1L));
		assertThrows(IOException.class, () -> consumer.andThen(FailableTriConsumer.nop()).accept(1, null, -1L));
		assertThrows(IOException.class, () -> consumer.andThen(FailableTriConsumer.nop()).accept(1, "", 1L));
	}
	
}
