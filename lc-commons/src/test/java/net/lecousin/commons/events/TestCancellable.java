package net.lecousin.commons.events;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestCancellable {
	
	@Test
	void testChain() {
		MutableBoolean[] cancelled = new MutableBoolean[10];
		Cancellable[] cancellable = new Cancellable[10];
		for (int i = 0; i < 10; ++i) {
			cancelled[i] = new MutableBoolean(false);
			if (i == 7)
				continue;
			final int index = i;
			cancellable[i] = () -> {
				cancelled[index].setTrue();
				return index < 7;
			};
		}
		
		Cancellable chain = Cancellable.of(cancellable);
		for (int i = 0; i < 10; ++i)
			Assertions.assertFalse(cancelled[i].booleanValue());
		Assertions.assertFalse(chain.cancel());
		for (int i = 0; i < 10; ++i)
			if (i != 7)
				Assertions.assertTrue(cancelled[i].booleanValue());
		Assertions.assertFalse(cancelled[7].booleanValue());
	}

}
