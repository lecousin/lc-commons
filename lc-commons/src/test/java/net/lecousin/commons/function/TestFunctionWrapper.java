package net.lecousin.commons.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"java:S5785"})
class TestFunctionWrapper {

	@SuppressWarnings("unlikely-arg-type")
	@Test
	void testRunnableAsConsumer() {
		MutableBoolean called = new MutableBoolean(false);
		Runnable r = () -> called.setTrue();
		Consumer<Integer> c = FunctionWrapper.asConsumer(r);
		assertFalse(called.booleanValue());
		c.accept(0);
		assertTrue(called.booleanValue());
		
		assertTrue(c.equals(r));
		assertFalse(c.equals(null));
		assertFalse(c.equals(1));
		
		assertEquals(r.toString(), c.toString());
		assertEquals(r.hashCode(), c.hashCode());
		
		assertFalse(c.equals(FunctionWrapper.asConsumer(() -> {})));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	void testSupplierAsFunction() {
		MutableBoolean called = new MutableBoolean(false);
		Supplier<Integer> s = () -> {
			called.setTrue();
			return 1;
		};
		Function<String, Integer> f = FunctionWrapper.asFunction(s);
		assertFalse(called.booleanValue());
		assertEquals(1, f.apply(""));
		assertTrue(called.booleanValue());
		
		assertTrue(f.equals(s));
		assertFalse(f.equals(null));
		assertFalse(f.equals(1));
		
		assertEquals(s.toString(), f.toString());
		assertEquals(s.hashCode(), f.hashCode());
		
		assertFalse(f.equals(FunctionWrapper.asFunction(() -> 1)));
	}
	
}
