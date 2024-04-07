package net.lecousin.commons.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Utilities for collections. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LcCollectionUtils {

	/**
	 * Remove first element matching the given predicate, and return the element.
	 * @param <T> type of element
	 * @param collection collection
	 * @param predicate predicate
	 * @return the element removed or empty if not found
	 */
	public static <T> Optional<T> removeFirstMatching(Collection<T> collection, Predicate<T> predicate) {
		for (Iterator<T> it = collection.iterator(); it.hasNext();) {
			T element = it.next();
			if (predicate.test(element)) {
				it.remove();
				return Optional.of(element);
			}
		}
		return Optional.empty();
	}
	
}
