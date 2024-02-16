package net.lecousin.commons.events;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestEmptyEvent {

	@Test
	void test() {
		MutableInt called = new MutableInt(0);
		Runnable listener = () -> { called.increment(); };
		EmptyEvent e = new EmptyEvent();
		Assertions.assertEquals(0, called.getValue());
		e.emit();
		Assertions.assertEquals(0, called.getValue());
		Assertions.assertFalse(e.hasListeners());
		
		e.listen(listener);
		Assertions.assertEquals(0, called.getValue());
		Assertions.assertTrue(e.hasListeners());
		e.emit();
		Assertions.assertEquals(1, called.getValue());
		Assertions.assertTrue(e.hasListeners());
		
		e.listen(listener);
		e.unlisten(() -> {});
		Assertions.assertEquals(1, called.getValue());
		e.emit();
		Assertions.assertEquals(3, called.getValue());
		Assertions.assertTrue(e.hasListeners());
		
		e.unlisten(listener);
		Assertions.assertEquals(3, called.getValue());
		e.emit();
		Assertions.assertEquals(4, called.getValue());
		Assertions.assertTrue(e.hasListeners());
		
		e.unlisten(listener);
		Assertions.assertEquals(4, called.getValue());
		e.emit();
		Assertions.assertEquals(4, called.getValue());
		Assertions.assertFalse(e.hasListeners());

		e.unlisten(listener);
		e.emit();
		Assertions.assertEquals(4, called.getValue());
		e.listen(listener);
		Assertions.assertEquals(4, called.getValue());
		e.emit();
		Assertions.assertEquals(5, called.getValue());
		e.unlisten(listener);
		Assertions.assertEquals(5, called.getValue());
		e.emit();
		Assertions.assertEquals(5, called.getValue());
		
		Cancellable c = e.subscribe(listener);
		Assertions.assertEquals(5, called.getValue());
		Assertions.assertTrue(e.hasListeners());
		e.emit();
		Assertions.assertEquals(6, called.getValue());
		Assertions.assertTrue(e.hasListeners());
		
		Assertions.assertTrue(c.cancel());
		Assertions.assertEquals(6, called.getValue());
		Assertions.assertFalse(e.hasListeners());
		e.emit();
		Assertions.assertEquals(6, called.getValue());
		Assertions.assertFalse(e.hasListeners());
	}
	
	@Test
	void testListenerError() {
		EmptyEvent e = new EmptyEvent();
		Runnable listener1 = () -> { throw new RuntimeException("a test"); };
		e.listen(listener1);
		assertDoesNotThrow(() -> e.emit());
	}

}
