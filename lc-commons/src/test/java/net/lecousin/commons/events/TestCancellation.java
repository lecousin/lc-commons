package net.lecousin.commons.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestCancellation {

	@Test
	void test() {
		Cancellation c1 = new Cancellation();
		Cancellation c2 = new Cancellation();
		Cancellation c3 = new Cancellation();
		c1.setCancellation(c2);
		c1.cancel();
		assertThat(c2.isCancelled()).isTrue();
		c1.setCancellation(c3);
		assertThat(c3.isCancelled()).isTrue();
	}
	
}
