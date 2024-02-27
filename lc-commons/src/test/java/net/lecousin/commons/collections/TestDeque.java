package net.lecousin.commons.collections;

import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Implements generic tests for a Deque.
 * 
 * @param <T> element type
 * @param <C> collection class
 */
// CHECKSTYLE DISABLE: MagicNumber
public interface TestDeque<T, C extends Deque<T>> extends TestQueueFIFO<T, C> {

	/** Test a Deque. */
	@Test
	default void testDeque() {
		C c = createCollection();
		checkEmptyCollection(c);
		
		T[] elements = generateElements(new Random(), 10);
		
		// addFirst, removeFirst
		c.addFirst(elements[0]);
		c.addFirst(elements[1]);
		c.addFirst(elements[2]);
		checkCollection(c, elements[2], elements[1], elements[0]);
		Assertions.assertEquals(elements[2], c.removeFirst());
		checkCollection(c, elements[1], elements[0]);
		Assertions.assertEquals(elements[1], c.removeFirst());
		checkCollection(c, elements[0]);
		Assertions.assertEquals(elements[0], c.removeFirst());
		checkEmptyCollection(c);
		
		// addFirst, removeLast
		c.addFirst(elements[0]);
		c.addFirst(elements[1]);
		c.addFirst(elements[2]);
		c.addFirst(elements[3]);
		c.addFirst(elements[4]);
		c.addFirst(elements[5]);
		checkCollection(c, elements[5], elements[4], elements[3], elements[2], elements[1], elements[0]);
		Assertions.assertEquals(elements[0], c.removeLast());
		checkCollection(c, elements[5], elements[4], elements[3], elements[2], elements[1]);
		Assertions.assertEquals(elements[1], c.removeLast());
		checkCollection(c, elements[5], elements[4], elements[3], elements[2]);
		Assertions.assertEquals(elements[5], c.removeFirst());
		checkCollection(c, elements[4], elements[3], elements[2]);
		Assertions.assertEquals(elements[2], c.removeLast());
		checkCollection(c, elements[4], elements[3]);
		Assertions.assertEquals(elements[4], c.removeFirst());
		checkCollection(c, elements[3]);
		Assertions.assertEquals(elements[3], c.removeLast());
		checkEmptyCollection(c);
		
		// addLast
		c.addLast(elements[0]);
		c.addLast(elements[1]);
		c.addLast(elements[2]);
		checkCollection(c, elements[0], elements[1], elements[2]);
		Assertions.assertEquals(elements[0], c.removeFirst());
		checkCollection(c, elements[1], elements[2]);
		Assertions.assertEquals(elements[2], c.removeLast());
		checkCollection(c, elements[1]);
		c.clear();
		checkEmptyCollection(c);
		
		// offerFirst, offerLast
		c.offerFirst(elements[0]);
		c.offerFirst(elements[1]);
		c.offerFirst(elements[2]);
		checkCollection(c, elements[2], elements[1], elements[0]);
		c.offerLast(elements[3]);
		c.offerLast(elements[4]);
		c.offerLast(elements[5]);
		checkCollection(c, elements[2], elements[1], elements[0], elements[3], elements[4], elements[5]);
		
		// push, pop
		c.push(elements[6]);
		c.push(elements[7]);
		checkCollection(c, elements[7], elements[6], elements[2], elements[1], elements[0], elements[3], elements[4], elements[5]);
		Assertions.assertEquals(elements[7], c.pop());
		Assertions.assertEquals(elements[6], c.pop());
		checkCollection(c, elements[2], elements[1], elements[0], elements[3], elements[4], elements[5]);
		
		// pollFirst, pollLast
		Assertions.assertEquals(elements[2], c.pollFirst());
		Assertions.assertEquals(elements[1], c.pollFirst());
		Assertions.assertEquals(elements[5], c.pollLast());
		Assertions.assertEquals(elements[4], c.pollLast());
		checkCollection(c, elements[0], elements[3]);
		
		// add some more
		c.clear();
		c.addFirst(elements[5]);
		c.offerFirst(elements[4]);
		c.addLast(elements[6]);
		c.offerLast(elements[7]);
		c.offerFirst(elements[3]);
		c.addFirst(elements[2]);
		c.addLast(elements[8]);
		c.offerLast(elements[9]);
		c.addFirst(elements[1]);
		c.offerFirst(elements[0]);
		checkCollection(c, elements[0], elements[1], elements[2], elements[3], elements[4], elements[5], elements[6], elements[7], elements[8], elements[9]);
		
		// various remove
		Assertions.assertEquals(elements[0], c.removeFirst());
		Assertions.assertEquals(elements[9], c.removeLast());
		Assertions.assertEquals(elements[1], c.pollFirst());
		Assertions.assertEquals(elements[8], c.pollLast());
		checkCollection(c, elements[2], elements[3], elements[4], elements[5], elements[6], elements[7]);
		Assertions.assertTrue(c.removeFirstOccurrence(elements[3]));
		Assertions.assertFalse(c.removeFirstOccurrence(elements[1]));
		checkCollection(c, elements[2], elements[4], elements[5], elements[6], elements[7]);
		Assertions.assertTrue(c.removeLastOccurrence(elements[5]));
		Assertions.assertFalse(c.removeLastOccurrence(elements[3]));
		checkCollection(c, elements[2], elements[4], elements[6], elements[7]);
		Assertions.assertTrue(c.removeLastOccurrence(elements[7]));
		Assertions.assertFalse(c.removeLastOccurrence(elements[5]));
		checkCollection(c, elements[2], elements[4], elements[6]);
		Assertions.assertTrue(c.removeLastOccurrence(elements[2]));
		Assertions.assertFalse(c.removeLastOccurrence(elements[7]));
		checkCollection(c, elements[4], elements[6]);
		Assertions.assertTrue(c.removeFirstOccurrence(elements[6]));
		Assertions.assertFalse(c.removeFirstOccurrence(elements[2]));
		checkCollection(c, elements[4]);
		Assertions.assertTrue(c.removeFirstOccurrence(elements[4]));
		Assertions.assertFalse(c.removeFirstOccurrence(elements[2]));
		checkEmptyCollection(c);
		
		// add some duplicates
		c.addFirst(elements[0]);
		c.addFirst(elements[1]);
		c.addLast(elements[2]);
		c.addLast(elements[3]);
		checkCollection(c, elements[1], elements[0], elements[2], elements[3]);
		c.addFirst(elements[1]);
		c.addFirst(elements[3]);
		c.addFirst(elements[0]);
		c.addFirst(elements[2]);
		c.addLast(elements[1]);
		c.addLast(elements[3]);
		c.addLast(elements[0]);
		c.addLast(elements[2]);
		checkCollection(c, elements[2], elements[0], elements[3], elements[1], elements[1], elements[0], elements[2], elements[3], elements[1], elements[3], elements[0], elements[2]);
		Assertions.assertTrue(c.removeFirstOccurrence(elements[1]));
		checkCollection(c, elements[2], elements[0], elements[3], elements[1], elements[0], elements[2], elements[3], elements[1], elements[3], elements[0], elements[2]);
		Assertions.assertTrue(c.removeLastOccurrence(elements[3]));
		checkCollection(c, elements[2], elements[0], elements[3], elements[1], elements[0], elements[2], elements[3], elements[1], elements[0], elements[2]);
		
		c.clear();
		checkEmptyCollection(c);
	}
	
	@Override
	@SuppressWarnings("java:S2119")
	default void checkEmptyCollection(C c) {
		TestQueueFIFO.super.checkEmptyCollection(c);
		Assertions.assertThrows(NoSuchElementException.class, c::getFirst);
		Assertions.assertThrows(NoSuchElementException.class, c::getLast);
		Assertions.assertThrows(NoSuchElementException.class, c::removeFirst);
		Assertions.assertThrows(NoSuchElementException.class, c::removeLast);
		Assertions.assertThrows(NoSuchElementException.class, c::pop);
		Assertions.assertThrows(NoSuchElementException.class, () -> c.descendingIterator().next());
		Assertions.assertNull(c.poll());
		Assertions.assertNull(c.pollFirst());
		Assertions.assertNull(c.pollLast());
		Assertions.assertNull(c.peek());
		Assertions.assertNull(c.peekFirst());
		Assertions.assertNull(c.peekLast());
		Random random = new Random();
		Assertions.assertFalse(c.removeFirstOccurrence(generateElement(random)));
		Assertions.assertFalse(c.removeLastOccurrence(generateElement(random)));
		Assertions.assertFalse(c.descendingIterator().hasNext());
	}
	
	@Override
	default void checkCollection(C c, Object... values) {
		TestQueueFIFO.super.checkCollection(c, values);
		Assertions.assertTrue(c.getFirst() == values[0]);
		Assertions.assertTrue(c.peek() == values[0]);
		Assertions.assertTrue(c.peekFirst() == values[0]);
		Assertions.assertTrue(c.getLast() == values[values.length - 1]);
		Assertions.assertTrue(c.peekLast() == values[values.length - 1]);
		Iterator<T> it = c.descendingIterator();
		for (int i = values.length - 1; i >= 0; --i) {
			Assertions.assertTrue(it.hasNext());
			Assertions.assertEquals(it.next(), values[i]);
		}
		Assertions.assertFalse(it.hasNext());
		Assertions.assertThrows(NoSuchElementException.class, it::next);
	}
	
}
