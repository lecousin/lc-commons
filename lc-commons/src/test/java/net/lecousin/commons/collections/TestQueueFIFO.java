package net.lecousin.commons.collections;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Implements generic tests for a Queue working as FIFO.
 * 
 * @param <T> element type
 * @param <C> collection class
 */
// CHECKSTYLE DISABLE: MagicNumber
public interface TestQueueFIFO<T, C extends Queue<T>> extends TestCollection<T, C> {

	/** Test push and pop elements. */
	@Test
	default void testQueueFIFOPushAndPop() {
		C col = createCollection();
		checkEmptyCollection(col);
		
		T[] elements = generateElements(new Random(), 10);
		
		// offer, poll
		col.offer(elements[0]);
		checkCollection(col, elements[0]);
		col.offer(elements[1]);
		checkCollection(col, elements[0], elements[1]);
		col.offer(elements[2]);
		checkCollection(col, elements[0], elements[1], elements[2]);
		Assertions.assertEquals(elements[0], col.poll());
		checkCollection(col, elements[1], elements[2]);
		Assertions.assertEquals(elements[1], col.poll());
		checkCollection(col, elements[2]);
		Assertions.assertEquals(elements[2], col.poll());
		checkEmptyCollection(col);

		// offer, peek, poll
		col.offer(elements[0]);
		col.offer(elements[1]);
		col.offer(elements[2]);
		col.offer(elements[3]);
		col.offer(elements[4]);
		checkCollection(col, elements[0], elements[1], elements[2], elements[3], elements[4]);
		Assertions.assertEquals(elements[0], col.peek());
		Assertions.assertEquals(elements[0], col.poll());
		checkCollection(col, elements[1], elements[2], elements[3], elements[4]);
		Assertions.assertEquals(elements[1], col.peek());
		Assertions.assertEquals(elements[1], col.poll());
		checkCollection(col, elements[2], elements[3], elements[4]);
		Assertions.assertEquals(elements[2], col.peek());
		Assertions.assertEquals(elements[2], col.poll());
		checkCollection(col, elements[3], elements[4]);
		Assertions.assertEquals(elements[3], col.peek());
		Assertions.assertEquals(elements[3], col.poll());
		checkCollection(col, elements[4]);
		Assertions.assertEquals(elements[4], col.peek());
		Assertions.assertEquals(elements[4], col.poll());
		checkEmptyCollection(col);
		
		// offer, element, poll
		col.offer(elements[0]);
		col.offer(elements[1]);
		col.offer(elements[2]);
		col.offer(elements[3]);
		col.offer(elements[4]);
		checkCollection(col, elements[0], elements[1], elements[2], elements[3], elements[4]);
		Assertions.assertEquals(elements[0], col.element());
		Assertions.assertEquals(elements[0], col.poll());
		checkCollection(col, elements[1], elements[2], elements[3], elements[4]);
		Assertions.assertEquals(elements[1], col.element());
		Assertions.assertEquals(elements[1], col.poll());
		checkCollection(col, elements[2], elements[3], elements[4]);
		Assertions.assertEquals(elements[2], col.element());
		Assertions.assertEquals(elements[2], col.poll());
		checkCollection(col, elements[3], elements[4]);
		Assertions.assertEquals(elements[3], col.element());
		Assertions.assertEquals(elements[3], col.poll());
		checkCollection(col, elements[4]);
		Assertions.assertEquals(elements[4], col.element());
		Assertions.assertEquals(elements[4], col.poll());
		checkEmptyCollection(col);

		// offer, remove, poll
		col.offer(elements[0]);
		col.offer(elements[1]);
		col.offer(elements[2]);
		checkCollection(col, elements[0], elements[1], elements[2]);
		Assertions.assertEquals(elements[0], col.poll());
		checkCollection(col, elements[1], elements[2]);
		col.offer(elements[3]);
		checkCollection(col, elements[1], elements[2], elements[3]);
		col.offer(elements[4]);
		col.offer(elements[5]);
		checkCollection(col, elements[1], elements[2], elements[3], elements[4], elements[5]);
		Assertions.assertEquals(elements[1], col.remove());
		checkCollection(col, elements[2], elements[3], elements[4], elements[5]);
		col.offer(elements[6]);
		col.offer(elements[7]);
		col.offer(elements[8]);
		checkCollection(col, elements[2], elements[3], elements[4], elements[5], elements[6], elements[7], elements[8]);
		Assertions.assertEquals(elements[2], col.poll());
		checkCollection(col, elements[3], elements[4], elements[5], elements[6], elements[7], elements[8]);
		Assertions.assertEquals(elements[3], col.peek());
		Assertions.assertEquals(elements[3], col.remove());
		checkCollection(col, elements[4], elements[5], elements[6], elements[7], elements[8]);
		Assertions.assertEquals(elements[4], col.poll());
		checkCollection(col, elements[5], elements[6], elements[7], elements[8]);
		Assertions.assertEquals(elements[5], col.remove());
		checkCollection(col, elements[6], elements[7], elements[8]);
		col.offer(elements[9]);
		checkCollection(col, elements[6], elements[7], elements[8], elements[9]);
		Assertions.assertEquals(elements[6], col.poll());
		checkCollection(col, elements[7], elements[8], elements[9]);
		Assertions.assertEquals(elements[7], col.poll());
		checkCollection(col, elements[8], elements[9]);
		Assertions.assertEquals(elements[8], col.peek());
		Assertions.assertEquals(elements[8], col.remove());
		checkCollection(col, elements[9]);
		Assertions.assertEquals(elements[9], col.remove());
		checkEmptyCollection(col);
	}
	
	@Override
	default void checkEmptyCollection(C col) {
		TestCollection.super.checkEmptyCollection(col);
		Assertions.assertThrows(NoSuchElementException.class, col::remove);
		Assertions.assertThrows(NoSuchElementException.class, col::element);
		Assertions.assertNull(col.poll());
		Assertions.assertNull(col.peek());
	}
	
	@Override
	default void checkCollection(C col, Object... expectedValues) {
		TestCollection.super.checkCollection(col, expectedValues);

		Assertions.assertEquals(expectedValues.length, col.size());
		Assertions.assertFalse(col.isEmpty());
		Assertions.assertEquals(col.peek(), expectedValues[0]);
		for (int i = 0; i < expectedValues.length; ++i)
			Assertions.assertTrue(col.contains(expectedValues[i]), "Contains " + expectedValues[i] + " return false, elements are " + Arrays.toString(expectedValues));
		Assertions.assertFalse(col.contains(new Object()));
		Assertions.assertTrue(col.contains(expectedValues[0]));
		Assertions.assertTrue(col.contains(expectedValues[expectedValues.length - 1]));
		Iterator<T> it = col.iterator();
		for (int i = 0; i < expectedValues.length; ++i) {
			Assertions.assertTrue(it.hasNext());
			Assertions.assertEquals(it.next(), expectedValues[i]);
		}
		Assertions.assertFalse(it.hasNext());
		Assertions.assertThrows(NoSuchElementException.class, it::next);
		Object[] a = col.toArray();
		Assertions.assertTrue(a.length == expectedValues.length);
		for (int i = 0; i < expectedValues.length; ++i) {
			Assertions.assertTrue(a[i] == expectedValues[i], "Element at index " + i + " should be " + expectedValues[i] + " found is " + a[i]);
		}
	}
}
