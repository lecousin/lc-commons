package net.lecousin.commons.cache;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import net.lecousin.commons.executors.LcExecutors;

/**
 * A cache using a map, where value are removed if there are not used since a given delay.
 * @param <K> type of key
 * @param <V> type of value
 */
public class MapExpireCache<K, V> {
	
	private final class Item {
		private V value;
		private long lastUse = System.currentTimeMillis();
		
		private Item(V item) {
			this.value = item;
		}
	}
	
	private Map<K, Item> map = new HashMap<>();
	
	/**
	 * Constructor.
	 * @param expirationDelay delay after which a value can be removed from the map if it was not used
	 * @param checkInterval interval to check for expired values
	 */
	public MapExpireCache(Duration expirationDelay, Duration checkInterval) {
		clean(expirationDelay.toMillis(), checkInterval);
	}
	
	/** Get a value.
	 * 
	 * @param key key
	 * @return the value or empty if not in the cache
	 */
	public Optional<V> get(K key) {
		synchronized (map) {
			Item item = map.get(key);
			if (item == null)
				return Optional.empty();
			item.lastUse = System.currentTimeMillis();
			return Optional.of(item.value);
		}
	}
	
	/** Put a value.
	 * 
	 * @param key key
	 * @param value value
	 */
	public void put(K key, V value) {
		synchronized (map) {
			Item item = map.get(key);
			if (item != null) {
				item.value = value;
				item.lastUse = System.currentTimeMillis();
			} else {
				map.put(key, new Item(value));
			}
		}
	}
	
	/** Remove a value.
	 * 
	 * @param key key
	 */
	public void remove(K key) {
		synchronized (map) {
			map.remove(key);
		}
	}
	
	/** @return all values contained in this cache. */
	public Collection<V> getAll() {
		List<V> list = new LinkedList<>();
		synchronized (map) {
			map.values().forEach(item -> list.add(item.value));
		}
		return list;
	}
	
	private void clean(long expirationDelay, Duration scheduleInterval) {
		long now = System.currentTimeMillis();
		synchronized (map) {
			for (Iterator<Entry<K, Item>> it = map.entrySet().iterator(); it.hasNext();) {
				if (now - it.next().getValue().lastUse > expirationDelay)
					it.remove();
			}
		}
		LcExecutors.getCpu().scheduleWithFixedDelay(() -> clean(expirationDelay, scheduleInterval), scheduleInterval);
	}

}
