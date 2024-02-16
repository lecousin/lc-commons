package net.lecousin.commons.events;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestEvent {

	@Test
	void test() {
		MutableInt called1 = new MutableInt(0);
		Runnable runnable1 = () -> { called1.increment(); };
		MutableInt called2 = new MutableInt(0);
		Runnable runnable2 = () -> { called2.increment(); };
		MutableInt val1 = new MutableInt(0);
		Consumer<Integer> listener1 = value -> val1.setValue(value.intValue());
		MutableInt val2 = new MutableInt(0);
		Consumer<Integer> listener2 = value -> val2.setValue(value.intValue());
		
		Event<Integer> e = new Event<>();
		
		Assertions.assertEquals(0, called1.getValue());
		Assertions.assertEquals(0, called2.getValue());
		Assertions.assertEquals(0, val1.getValue());
		Assertions.assertEquals(0, val2.getValue());
		Assertions.assertFalse(e.hasListeners());
		e.emit(Integer.valueOf(10));
		Assertions.assertEquals(0, called1.getValue());
		Assertions.assertEquals(0, called2.getValue());
		Assertions.assertEquals(0, val1.getValue());
		Assertions.assertEquals(0, val2.getValue());
		Assertions.assertFalse(e.hasListeners());
		
		e.listen(runnable1);
		Assertions.assertTrue(e.hasListeners());
		e.listen(runnable2);
		Assertions.assertTrue(e.hasListeners());
		e.listen(listener1);
		Assertions.assertTrue(e.hasListeners());
		e.listen(listener2);
		Assertions.assertTrue(e.hasListeners());
		Assertions.assertEquals(0, called1.getValue());
		Assertions.assertEquals(0, called2.getValue());
		Assertions.assertEquals(0, val1.getValue());
		Assertions.assertEquals(0, val2.getValue());
		e.emit(Integer.valueOf(20));
		Assertions.assertEquals(1, called1.getValue());
		Assertions.assertEquals(1, called2.getValue());
		Assertions.assertEquals(20, val1.getValue());
		Assertions.assertEquals(20, val2.getValue());
		Assertions.assertTrue(e.hasListeners());

		e.unlisten(runnable1);
		Assertions.assertTrue(e.hasListeners());
		e.unlisten(listener1);
		Assertions.assertTrue(e.hasListeners());
		Assertions.assertEquals(1, called1.getValue());
		Assertions.assertEquals(1, called2.getValue());
		Assertions.assertEquals(20, val1.getValue());
		Assertions.assertEquals(20, val2.getValue());
		e.emit(Integer.valueOf(30));
		Assertions.assertEquals(1, called1.getValue());
		Assertions.assertEquals(2, called2.getValue());
		Assertions.assertEquals(20, val1.getValue());
		Assertions.assertEquals(30, val2.getValue());
		Assertions.assertTrue(e.hasListeners());
		
		e.unlisten(runnable2);
		Assertions.assertTrue(e.hasListeners());
		e.unlisten(listener2);
		Assertions.assertFalse(e.hasListeners());
		Assertions.assertEquals(1, called1.getValue());
		Assertions.assertEquals(2, called2.getValue());
		Assertions.assertEquals(20, val1.getValue());
		Assertions.assertEquals(30, val2.getValue());
		e.emit(Integer.valueOf(40));
		Assertions.assertEquals(1, called1.getValue());
		Assertions.assertEquals(2, called2.getValue());
		Assertions.assertEquals(20, val1.getValue());
		Assertions.assertEquals(30, val2.getValue());
		Assertions.assertFalse(e.hasListeners());
		
		e.unlisten(listener1);
		Assertions.assertFalse(e.hasListeners());
		e.unlisten(runnable1);
		Assertions.assertFalse(e.hasListeners());
		Assertions.assertEquals(1, called1.getValue());
		Assertions.assertEquals(2, called2.getValue());
		Assertions.assertEquals(20, val1.getValue());
		Assertions.assertEquals(30, val2.getValue());
		e.emit(Integer.valueOf(50));
		Assertions.assertEquals(1, called1.getValue());
		Assertions.assertEquals(2, called2.getValue());
		Assertions.assertEquals(20, val1.getValue());
		Assertions.assertEquals(30, val2.getValue());
		Assertions.assertFalse(e.hasListeners());
		
		Event<Integer> e2 = new Event<Integer>();
		e.listen(runnable1);
		e.listen(listener1);
		e.unlisten(listener2);
		e2.listen(e::emit);
		Assertions.assertEquals(1, called1.getValue());
		Assertions.assertEquals(2, called2.getValue());
		Assertions.assertEquals(20, val1.getValue());
		Assertions.assertEquals(30, val2.getValue());
		e2.emit(Integer.valueOf(60));
		Assertions.assertEquals(2, called1.getValue());
		Assertions.assertEquals(2, called2.getValue());
		Assertions.assertEquals(60, val1.getValue());
		Assertions.assertEquals(30, val2.getValue());
	}
	
	@Test
	void testListenerError() {
		Event<Integer> e = new Event<>();
		Runnable listener1 = () -> { throw new RuntimeException("a test"); };
		Consumer<Integer> listener2 = ev -> { throw new RuntimeException("a test"); };
		e.listen(listener1);
		e.listen(listener2);
		assertDoesNotThrow(() -> e.emit(Integer.valueOf(10)));
	}
	
	@Test
	void testSubscribe() {
		MutableInt called1 = new MutableInt(0);
		Runnable runnable1 = () -> { called1.increment(); };
		MutableInt val1 = new MutableInt(0);
		Consumer<Integer> listener1 = value -> val1.setValue(value.intValue());
		
		Event<Integer> e = new Event<>();
		
		Assertions.assertEquals(0, called1.getValue());
		Assertions.assertEquals(0, val1.getValue());
		Assertions.assertFalse(e.hasListeners());
		e.emit(Integer.valueOf(10));
		Assertions.assertEquals(0, called1.getValue());
		Assertions.assertEquals(0, val1.getValue());
		Assertions.assertFalse(e.hasListeners());
		
		Cancellable r1 = e.subscribe(runnable1);
		Assertions.assertTrue(e.hasListeners());
		Cancellable l1 = e.subscribe(listener1);
		Assertions.assertTrue(e.hasListeners());
		Assertions.assertTrue(e.hasListeners());
		Assertions.assertEquals(0, called1.getValue());
		Assertions.assertEquals(0, val1.getValue());
		e.emit(Integer.valueOf(20));
		Assertions.assertEquals(1, called1.getValue());
		Assertions.assertEquals(20, val1.getValue());
		Assertions.assertTrue(e.hasListeners());

		r1.cancel();
		Assertions.assertTrue(e.hasListeners());
		Assertions.assertEquals(1, called1.getValue());
		Assertions.assertEquals(20, val1.getValue());
		e.emit(Integer.valueOf(30));
		Assertions.assertEquals(1, called1.getValue());
		Assertions.assertEquals(30, val1.getValue());
		Assertions.assertTrue(e.hasListeners());

		l1.cancel();
		Assertions.assertFalse(e.hasListeners());
		Assertions.assertEquals(1, called1.getValue());
		Assertions.assertEquals(30, val1.getValue());
		e.emit(Integer.valueOf(40));
		Assertions.assertEquals(1, called1.getValue());
		Assertions.assertEquals(30, val1.getValue());
		Assertions.assertFalse(e.hasListeners());
	}
	
}
