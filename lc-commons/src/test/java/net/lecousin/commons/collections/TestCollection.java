package net.lecousin.commons.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Implements generic tests for a collection.
 * 
 * @param <T> element type
 * @param <C> collection class
 */
// CHECKSTYLE DISABLE: MagicNumber
@SuppressWarnings({"java:S3776", "java:S2119", "java:S6541", "java:S135"})
public interface TestCollection<T, C extends Collection<T>> {

	/** Instantiate an empty collection.
	 * @return the collection
	 */
	C createCollection();
	
	/** Generate an element.
	 * @param random can be used to generate a random element
	 * @return a new random element
	 */
	T generateElement(Random random);
	
	/** @return the class of the elements */
	Class<T> getCollectionElementType();
	
	/** Generate some random elements.
	 * @param random can be used to generate random elements
	 * @param nb number of elements to generate
	 * @return generated elements
	 */
	default T[] generateElements(Random random, int nb) {
		@SuppressWarnings("unchecked")
		T[] elements = (T[]) Array.newInstance(getCollectionElementType(), nb);
		for (int i = 0; i < elements.length; ++i)
			elements[i] = generateNewElement(random, elements);
		return elements;
	}
	
	/** Generate a new random element, that is not equal to any existing element.
	 * @param random can be used to generate random elements
	 * @param elements existing elements
	 * @return the new element
	 */
	default T generateNewElement(Random random, Object... elements) {
		T newValue;
		do {
			newValue = generateElement(random);
		} while (Arrays.asList(elements).contains(newValue));
		return newValue;
	}

	/** Test an empty collection. */
	@Test
	default void testCollectionEmpty() {
		C c = createCollection();
		checkEmptyCollection(c);
	}

	/** Test to add and remove elements. */
	@SuppressWarnings({ "unchecked" })
	@Test
	default void testCollectionAddRemoveElements() {
		C c = createCollection();
		Random random = new Random();
		T e1 = generateElement(random);
		T e2 = generateNewElement(random, e1);
		
		// add 1 element
		c.add(e1);
		checkCollection(c, e1);

		// remove non present element
		Assertions.assertFalse(c.remove(e2));
		checkCollection(c, e1);
		
		// remove element
		Assertions.assertTrue(c.remove(e1));
		checkEmptyCollection(c);

		// add 3 elements
		T e3 = generateNewElement(random, e1, e2);
		Assertions.assertTrue(c.add(e1));
		checkCollection(c, e1);
		Assertions.assertTrue(c.add(e2));
		checkCollection(c, e1, e2);
		Assertions.assertTrue(c.add(e3));
		checkCollection(c, e1, e2, e3);

		// remove non present element
		T e4 = generateNewElement(random, e1, e2, e3);
		Assertions.assertFalse(c.remove(e4));
		checkCollection(c, e1, e2, e3);
		// remove 1 element
		Assertions.assertTrue(c.remove(e2));
		checkCollection(c, e1, e3);

		T e5 = generateNewElement(random, e1, e2, e3, e4);
		T e6 = generateNewElement(random, e1, e2, e3, e4, e5);
		
		ArrayList<T> arr = new ArrayList<>(10);
		arr.add(e4);
		arr.add(e5);
		arr.add(e6);
		
		// test addAll
		c.addAll(arr);
		checkCollection(c, e1, e3, e4, e5, e6);
		arr.clear();
		c.addAll(arr);
		checkCollection(c, e1, e3, e4, e5, e6);
		
		// test removeAll
		arr.clear();
		arr.add(e3);
		arr.add(e6);
		c.removeAll(arr);
		checkCollection(c, e1, e4, e5);
		arr.add(e2);
		arr.add(e3);
		arr.add(e4);
		arr.add(e6);
		c.removeAll(arr);
		checkCollection(c, e1, e5);
		
		// remove
		Assertions.assertTrue(c.remove(e5));
		Assertions.assertFalse(c.remove(e2));
		Assertions.assertTrue(c.remove(e1));
		Assertions.assertFalse(c.remove(e1));
		checkEmptyCollection(c);
		
		// test clear
		c.clear();
		checkEmptyCollection(c);
		c.add(e1);
		c.add(e2);
		c.add(e3);
		checkCollection(c, e1, e2, e3);
		c.clear();
		checkEmptyCollection(c);
		c.clear();
		checkEmptyCollection(c);
		
		// test to add many elements
		T[] elements = (T[]) Array.newInstance(getCollectionElementType(), 10000);
		for (int i = 0; i < elements.length; ++i) {
			elements[i] = generateNewElement(random, elements);
			c.add(elements[i]);
		}
		Assertions.assertEquals(elements.length, c.size());
		for (int i = 0; i < elements.length; ++i)
			Assertions.assertTrue(c.contains(elements[i]));
		
		// test to remove half of elements
		for (int i = 0; i < elements.length; i += 2)
			Assertions.assertTrue(c.remove(elements[i]));
		Assertions.assertEquals(elements.length / 2, c.size());
		for (int i = 0; i < elements.length; ++i)
			if ((i % 2) == 0)
				Assertions.assertFalse(c.contains(elements[i]));
			else
				Assertions.assertTrue(c.contains(elements[i]));
		
		// test to remove elements from 2000 to 7000 (2500 elements)
		for (int i = 2000; i < 7000; ++i)
			if ((i % 2) == 0)
				Assertions.assertFalse(c.remove(elements[i]));
			else
				Assertions.assertTrue(c.remove(elements[i]));
		Assertions.assertEquals(elements.length / 2 - 2500, c.size());
		
		// test to remove elements from 9000 to 10000 (500 elements)
		for (int i = 9000; i < 10000; ++i)
			if ((i % 2) == 0)
				Assertions.assertFalse(c.remove(elements[i]));
			else
				Assertions.assertTrue(c.remove(elements[i]));
		Assertions.assertEquals(elements.length / 2 - 2500 - 500, c.size());
		
		arr.clear();
		for (T e : c) arr.add(e);
		Assertions.assertEquals(elements.length / 2 - 2500 - 500, arr.size());
		Assertions.assertTrue(c.containsAll(arr));
		arr.add(elements[0]);
		Assertions.assertFalse(c.containsAll(arr));
		arr.remove(elements[0]);
		for (int i = 500; i < 1000; ++i)
			arr.remove(elements[i]);
		Assertions.assertTrue(c.containsAll(arr));
	}
	
	/** Test retainAll method. */
	@Test
	default void testRetainAll() {
		C c = createCollection();
		Random random = new Random();
		T e1 = generateElement(random);
		T e2 = generateNewElement(random, e1);
		T e3 = generateNewElement(random, e1, e2);
		T e4 = generateNewElement(random, e1, e2, e3);
		T e5 = generateNewElement(random, e1, e2, e3, e4);
		
		c.addAll(Arrays.asList(e1, e2, e3, e4, e5));
		checkCollection(c, e1, e2, e3, e4, e5);
		
		c.retainAll(Arrays.asList(e1, e3, e4, e5));
		checkCollection(c, e1, e3, e4, e5);

		c.retainAll(Arrays.asList(e2, e3));
		checkCollection(c, e3);

		c.retainAll(Arrays.asList(e1, e2));
		checkEmptyCollection(c);

		c.retainAll(Arrays.asList(e1, e2));
		checkEmptyCollection(c);
	}
	

	/** Check a collection is empty.
	 * @param c the collection to check
	 */
	default void checkEmptyCollection(C c) {
		Assertions.assertTrue(c.isEmpty());
		Assertions.assertEquals(0, c.size());
		
		Assertions.assertFalse(c.iterator().hasNext());
		Assertions.assertThrows(NoSuchElementException.class, () -> c.iterator().next());

		Random random = new Random();
		
		Assertions.assertFalse(c.remove(generateElement(random)));
		Assertions.assertFalse(c.removeAll(Arrays.asList(generateElement(random), generateElement(random), generateElement(random))));
		
		Assertions.assertFalse(c.contains(generateElement(random)));
		Assertions.assertFalse(c.containsAll(Arrays.asList(generateElement(random), generateElement(random), generateElement(random))));
		Assertions.assertFalse(c.containsAll(Arrays.asList(generateElement(random))));
		
		Assertions.assertArrayEquals(new Object[0], c.toArray());
		Assertions.assertArrayEquals(new Object[0], c.toArray(new Object[0]));

		@SuppressWarnings("unchecked")
		T[] a1 = (T[]) java.lang.reflect.Array.newInstance(getCollectionElementType(), 2);
		a1[0] = generateElement(random);
		a1[1] = generateElement(random);

		@SuppressWarnings("unchecked")
		T[] a2 = (T[]) java.lang.reflect.Array.newInstance(getCollectionElementType(), 2);
		a2[0] = null;
		a2[1] = a1[1];
		
		T[] a3 = c.toArray(a1);
		Assertions.assertSame(a1, a3);
		Assertions.assertArrayEquals(a2, a1);
	}

	/**
	 * Check a collection contains the expected elements.
	 * @param c the collection
	 * @param expectedValues expected elements
	 */
	@SuppressWarnings({ "unchecked" })
	default void checkCollection(C c, Object... expectedValues) {
		if (expectedValues.length == 0) {
			checkEmptyCollection(c);
			return;
		}

		Random random = new Random();
		T unexpectedValue = generateNewElement(random, expectedValues);

		// size
		Assertions.assertFalse(c.isEmpty());
		Assertions.assertEquals(expectedValues.length, c.size());

		// contains
		for (int i = 0; i < expectedValues.length; ++i)
			Assertions.assertTrue(c.contains(expectedValues[i]));
		Assertions.assertFalse(c.contains(unexpectedValue));
		
		// iterator
		Iterator<T> it = c.iterator();
		boolean[] found = new boolean[expectedValues.length];
		for (int i = 0; i < expectedValues.length; ++i) found[i] = false;
		for (int i = 0; i < expectedValues.length; ++i) {
			Assertions.assertTrue(it.hasNext());
			T val = it.next();
			boolean valFound = false;
			for (int j = 0; j < found.length; ++j) {
				if (found[j]) continue;
				if (Objects.equals(expectedValues[j], val)) {
					found[j] = true;
					valFound = true;
					break;
				}
			}
			Assertions.assertTrue(valFound, "Value " + val + " returned by the iterator is not expected in the collection");
		}
		for (int i = 0; i < found.length; ++i)
			Assertions.assertTrue(found[i], "Value " + expectedValues[i] + " was never returned by the iterator");
		Assertions.assertFalse(it.hasNext());
		Assertions.assertThrows(NoSuchElementException.class, it::next);
		
		// to array
		Object[] a = c.toArray();
		Assertions.assertEquals(a.length, expectedValues.length);
		for (int i = 0; i < expectedValues.length; ++i) found[i] = false;
		for (int i = 0; i < expectedValues.length; ++i) {
			Assertions.assertTrue(a[i] == null || getCollectionElementType().isAssignableFrom(a[i].getClass()));
			boolean valFound = false;
			for (int j = 0; j < found.length; ++j) {
				if (found[j]) continue;
				if (Objects.equals(a[i], expectedValues[j])) {
					found[j] = true;
					valFound = true;
					break;
				}
			}
			Assertions.assertTrue(valFound, "Value " + a[i] + " returned by toArray is not expected in the collection");
		}
		for (int i = 0; i < found.length; ++i)
			Assertions.assertTrue(found[i], "Value " + expectedValues[i] + " was not in the result of toArray");
		
		// toArray too small
		a = c.toArray((T[]) Array.newInstance(getCollectionElementType(), expectedValues.length - 1));
		Assertions.assertEquals(a.length, expectedValues.length);
		for (int i = 0; i < expectedValues.length; ++i) found[i] = false;
		for (int i = 0; i < expectedValues.length; ++i) {
			Assertions.assertTrue(a[i] == null || getCollectionElementType().isAssignableFrom(a[i].getClass()));
			boolean valFound = false;
			for (int j = 0; j < found.length; ++j) {
				if (found[j]) continue;
				if (Objects.equals(a[i], expectedValues[j])) {
					found[j] = true;
					valFound = true;
					break;
				}
			}
			Assertions.assertTrue(valFound, "Value " + a[i] + " returned by toArray is not expected in the collection");
		}
		for (int i = 0; i < found.length; ++i)
			Assertions.assertTrue(found[i], "Value " + expectedValues[i] + " was not in the result of toArray");
		
		// toArray bigger
		a = c.toArray((T[]) Array.newInstance(getCollectionElementType(), expectedValues.length + 10));
		Assertions.assertEquals(a.length, expectedValues.length + 10);
		for (int i = 0; i < expectedValues.length; ++i) found[i] = false;
		for (int i = 0; i < expectedValues.length; ++i) {
			Assertions.assertTrue(a[i] == null || getCollectionElementType().isAssignableFrom(a[i].getClass()));
			boolean valFound = false;
			for (int j = 0; j < found.length; ++j) {
				if (found[j]) continue;
				if (Objects.equals(a[i], expectedValues[j])) {
					found[j] = true;
					valFound = true;
					break;
				}
			}
			Assertions.assertTrue(valFound, "Value " + a[i] + " returned by toArray is not expected in the collection");
		}
		for (int i = 0; i < found.length; ++i)
			Assertions.assertTrue(found[i], "Value " + expectedValues[i] + " was not in the result of toArray");
	}

}
