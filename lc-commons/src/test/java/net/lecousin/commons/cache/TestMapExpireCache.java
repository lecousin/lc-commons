package net.lecousin.commons.cache;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestMapExpireCache {

	@Test
	@SuppressWarnings("java:S2925")
	void test() throws Exception {
		MapExpireCache<String, String> cache = new MapExpireCache<>(Duration.ofMillis(1000), Duration.ofMillis(500));
		
		try {
			Assertions.assertTrue(cache.get("1").isEmpty());
			
			cache.put("1", "one");
			Assertions.assertEquals("one", cache.get("1").get());
			Assertions.assertEquals("one", cache.get("1").get());
			Thread.sleep(2500);
			Assertions.assertTrue(cache.get("1").isEmpty());
			
			cache.put("2", "two");
			Assertions.assertEquals("two", cache.get("2").get());
			Assertions.assertEquals(1, cache.getAll().size());
			cache.put("2", "second");
			Assertions.assertEquals("second", cache.get("2").get());
			Assertions.assertEquals(1, cache.getAll().size());
			cache.remove("2");
			Assertions.assertTrue(cache.get("2").isEmpty());
			Assertions.assertEquals(0, cache.getAll().size());
		} finally {
			cache.cancel();
		}
	}
	
}
