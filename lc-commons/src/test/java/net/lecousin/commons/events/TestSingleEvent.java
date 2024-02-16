package net.lecousin.commons.events;

import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestSingleEvent {

	@Test
	void testWithoutListeners() {
		SingleEvent<Integer> e = new SingleEvent<>();
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		e.emit(51);
		Assertions.assertTrue(e.isEmitted());
		Assertions.assertEquals(51, e.getEmittedEvent());
		
		Assertions.assertThrows(IllegalStateException.class, () -> e.emit(52));
		Assertions.assertTrue(e.isEmitted());
		Assertions.assertEquals(51, e.getEmittedEvent());
		
		Assertions.assertFalse(e.hasListeners());
		MutableBoolean runnableCalled = new MutableBoolean(false);
		Runnable runnable = () -> runnableCalled.setTrue();
		e.listen(runnable);
		Assertions.assertTrue(runnableCalled.booleanValue());
		Assertions.assertFalse(e.hasListeners());
		
		MutableObject<Integer> consumerCalled = new MutableObject<Integer>(null);
		Consumer<Integer> consumer = item -> consumerCalled.setValue(item);
		e.listen(consumer);
		Assertions.assertEquals(51, consumerCalled.getValue());
		Assertions.assertFalse(e.hasListeners());
	}

	@Test
	void testWithoutListenersWithBeforeListeners() {
		SingleEvent<Integer> e = new SingleEvent<>();
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		MutableBoolean called = new MutableBoolean(false);
		e.emit(50, () -> {
			called.setTrue();
			return false;
		});
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		Assertions.assertTrue(called.getValue());
		
		called.setFalse();
		e.emit(51, () -> {
			called.setTrue();
			return true;
		});
		Assertions.assertTrue(e.isEmitted());
		Assertions.assertEquals(51, e.getEmittedEvent());
		Assertions.assertTrue(called.getValue());
		
		called.setFalse();
		Assertions.assertThrows(IllegalStateException.class, () -> e.emit(52, () -> { called.setTrue(); return true; }));
		Assertions.assertTrue(e.isEmitted());
		Assertions.assertEquals(51, e.getEmittedEvent());
		Assertions.assertFalse(called.getValue());
	}
	
	@Test
	void testWithListeners() {
		SingleEvent<Integer> e = new SingleEvent<>();
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		
		MutableBoolean runnableCalled = new MutableBoolean(false);
		Runnable runnable = () -> runnableCalled.setTrue();
		MutableObject<Integer> consumerCalled = new MutableObject<Integer>(null);
		Consumer<Integer> consumer = item -> consumerCalled.setValue(item);

		e.listen(runnable);
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertTrue(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());
		
		e.listen(consumer);
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertTrue(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());

		e.emit(51, null);
		Assertions.assertTrue(e.isEmitted());
		Assertions.assertEquals(51, e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		Assertions.assertTrue(runnableCalled.getValue());
		Assertions.assertEquals(51, consumerCalled.getValue());
		
		runnableCalled.setFalse();
		consumerCalled.setValue(null);
		Assertions.assertThrows(IllegalStateException.class, () -> e.emit(52, null));
		Assertions.assertTrue(e.isEmitted());
		Assertions.assertEquals(51, e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());

		e.listen(runnable);
		Assertions.assertTrue(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());
		Assertions.assertFalse(e.hasListeners());
		runnableCalled.setFalse();

		e.listen(consumer);
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertEquals(51, consumerCalled.getValue());
		Assertions.assertFalse(e.hasListeners());
	}
	
	@Test
	void testUnlisten() {
		SingleEvent<Integer> e = new SingleEvent<>();
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		
		MutableBoolean runnableCalled = new MutableBoolean(false);
		Runnable runnable = () -> runnableCalled.setTrue();
		MutableObject<Integer> consumerCalled = new MutableObject<Integer>(null);
		Consumer<Integer> consumer = item -> consumerCalled.setValue(item);

		e.listen(runnable);
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertTrue(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());
		
		e.listen(consumer);
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertTrue(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());
		
		e.unlisten(consumer);
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertTrue(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());
		
		e.unlisten(runnable);
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());
		
		e.unlisten(consumer);
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());
		
		e.unlisten(runnable);
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());

		e.emit(51, null);
		Assertions.assertTrue(e.isEmitted());
		Assertions.assertEquals(51, e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());

		e.unlisten(consumer);
		e.unlisten(runnable);
	}
	
	
	@Test
	void testListenerError() {
		SingleEvent<Integer> e = new SingleEvent<>();
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		
		Runnable runnable = () -> { throw new RuntimeException("test"); };
		MutableObject<Integer> consumerCalled = new MutableObject<Integer>(null);
		Consumer<Integer> consumer = item -> consumerCalled.setValue(item);

		e.listen(runnable);
		e.listen(consumer);

		e.emit(51, null);
		Assertions.assertTrue(e.isEmitted());
		Assertions.assertEquals(51, e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		Assertions.assertEquals(51, consumerCalled.getValue());
	}
	
	@Test
	void testWithListenersAndBeforeListeners() {
		SingleEvent<Integer> e = new SingleEvent<>();
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		
		MutableBoolean runnableCalled = new MutableBoolean(false);
		Runnable runnable = () -> runnableCalled.setTrue();
		MutableObject<Integer> consumerCalled = new MutableObject<Integer>(null);
		Consumer<Integer> consumer = item -> consumerCalled.setValue(item);

		e.listen(runnable);
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertTrue(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());
		
		e.listen(consumer);
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertTrue(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());
		
		MutableBoolean called = new MutableBoolean(false);

		e.emit(50, () -> {
			called.setTrue();
			return false;
		});
		Assertions.assertFalse(e.isEmitted());
		Assertions.assertNull(e.getEmittedEvent());
		Assertions.assertTrue(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());
		Assertions.assertTrue(called.getValue());
		
		called.setFalse();
		e.emit(51, () -> {
			called.setTrue();
			return true;
		});
		Assertions.assertTrue(e.isEmitted());
		Assertions.assertEquals(51, e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		Assertions.assertTrue(runnableCalled.getValue());
		Assertions.assertEquals(51, consumerCalled.getValue());
		Assertions.assertTrue(called.getValue());
	}
	
	@Test
	void testAlreadyAvailableEvent() {
		SingleEvent<Integer> e = new SingleEvent<>(51);
		Assertions.assertTrue(e.isEmitted());
		Assertions.assertEquals(51, e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		
		MutableBoolean runnableCalled = new MutableBoolean(false);
		Runnable runnable = () -> runnableCalled.setTrue();
		MutableObject<Integer> consumerCalled = new MutableObject<Integer>(null);
		Consumer<Integer> consumer = item -> consumerCalled.setValue(item);

		Assertions.assertThrows(IllegalStateException.class, () -> e.emit(52, null));
		Assertions.assertTrue(e.isEmitted());
		Assertions.assertEquals(51, e.getEmittedEvent());
		Assertions.assertFalse(e.hasListeners());
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());

		e.listen(runnable);
		Assertions.assertTrue(runnableCalled.getValue());
		Assertions.assertNull(consumerCalled.getValue());
		Assertions.assertFalse(e.hasListeners());
		runnableCalled.setFalse();

		e.listen(consumer);
		Assertions.assertFalse(runnableCalled.getValue());
		Assertions.assertEquals(51, consumerCalled.getValue());
		Assertions.assertFalse(e.hasListeners());
	}
	
	@Test
	@SuppressWarnings("java:S2925") // Thread.sleep
	void testWaitEvent() throws Exception {
		for (int trial = 0; trial < 100; ++trial) {
			@SuppressWarnings("unchecked")
			SingleEvent<Integer>[] events = new SingleEvent[100];
			Thread[] threads = new Thread[50];
			for (int i = 0; i < 100; ++i)
				events[i] = new SingleEvent<>();
			
			for (int i = 0; i < 100; ++i) {
				final int index = i;
				Thread t = new Thread(() -> {
					if ((index % 5) == 0)
						try {
							Thread.sleep(index / 10 + 1);
						} catch (InterruptedException e) {
							return;
						}
					events[index].emit(index, null);
				});
				if (i < 50)
					t.start();
				else
					threads[i - 50] = t;
			}
			
			MutableObject<Throwable> error = new MutableObject<>(null);
			for (int i = 0; i < 100; ++i) {
				final int index = i;
				new Thread(() -> {
					try {
						events[index].waitEvent(index < 20 ? 0 : 10000);
						Assertions.assertEquals(index, events[index].getEmittedEvent());
					} catch (Throwable t) {
						error.setValue(t);
					}
				}).start();
				if (i >= 50)
					threads[i - 50].start();
			}
			for (int i = 0; i < 100; ++i) {
				events[i].waitEvent(10000);
			}
			Assertions.assertNull(error.getValue());
		}
	}
	
	@Test
	void testWaitEventTimeout() {
		SingleEvent<Integer> e = new SingleEvent<>();
		Assertions.assertThrows(TimeoutException.class, () -> e.waitEvent(1));
	}
}
