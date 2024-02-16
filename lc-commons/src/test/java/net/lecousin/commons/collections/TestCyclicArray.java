package net.lecousin.commons.collections;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.test.collections.TestDeque;

class TestCyclicArray implements TestDeque<Integer, CyclicArray<Integer>> {

	@Override
	public CyclicArray<Integer> createCollection() {
		return new CyclicArray<>();
	}

	@Override
	public Integer generateElement(Random random) {
		return random.nextInt();
	}

	@Override
	public Class<Integer> getCollectionElementType() {
		return Integer.class;
	}

	@Test
	void testFullAndEmpty() {
		CyclicArray<Integer> q = new CyclicArray<>(1);
		Assertions.assertTrue(q.isEmpty());
		Assertions.assertFalse(q.isFull());
		Assertions.assertEquals(5, q.getNbAvailableSlots());
		q.add(Integer.valueOf(1));
		Assertions.assertFalse(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		Assertions.assertEquals(4, q.getNbAvailableSlots());
		q.add(Integer.valueOf(2));
		Assertions.assertFalse(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		Assertions.assertEquals(3, q.getNbAvailableSlots());
		q.add(Integer.valueOf(3));
		Assertions.assertFalse(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		Assertions.assertEquals(2, q.getNbAvailableSlots());
		q.add(Integer.valueOf(4));
		Assertions.assertFalse(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		Assertions.assertEquals(1, q.getNbAvailableSlots());
		q.add(Integer.valueOf(5));
		Assertions.assertTrue(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		Assertions.assertEquals(0, q.getNbAvailableSlots());
		q.removeFirst();
		Assertions.assertFalse(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		q.add(Integer.valueOf(6));
		Assertions.assertTrue(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		q.removeFirst();
		Assertions.assertFalse(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		q.removeFirst();
		Assertions.assertFalse(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		q.add(Integer.valueOf(7));
		Assertions.assertFalse(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		q.add(Integer.valueOf(8));
		Assertions.assertTrue(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		q.removeFirst();
		Assertions.assertFalse(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		q.removeFirst();
		Assertions.assertFalse(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		q.removeFirst();
		Assertions.assertFalse(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		q.removeFirst();
		Assertions.assertFalse(q.isFull());
		Assertions.assertFalse(q.isEmpty());
		q.removeFirst();
		Assertions.assertFalse(q.isFull());
		Assertions.assertTrue(q.isEmpty());
	}
	
	@Test
	void testSpecificCases() {
		CyclicArray<Integer> q = new CyclicArray<>(5);

		// make it full
		q.add(Integer.valueOf(1));
		q.add(Integer.valueOf(2));
		q.add(Integer.valueOf(3));
		q.add(Integer.valueOf(4));
		q.add(Integer.valueOf(5));
		Assertions.assertTrue(q.isFull());
		
		// case when full: getLast, ppekLast, removeLast, pollLast
		Assertions.assertEquals(5, q.getLast().intValue());
		Assertions.assertEquals(5, q.peekLast().intValue());
		Assertions.assertEquals(5, q.removeLast().intValue());
		checkCollection(q, 1, 2, 3, 4);
		q.add(Integer.valueOf(-1));
		checkCollection(q, 1, 2, 3, 4, -1);
		Assertions.assertEquals(-1, q.pollLast().intValue());
		checkCollection(q, 1, 2, 3, 4);
		// removeLast and pollLast with end == 0
		q.add(Integer.valueOf(6));
		checkCollection(q, 1, 2, 3, 4, 6);
		Assertions.assertEquals(1, q.removeFirst().intValue());
		checkCollection(q, 2, 3, 4, 6);
		Assertions.assertEquals(6, q.removeLast().intValue());
		checkCollection(q, 2, 3, 4);
		q.add(Integer.valueOf(-1));
		checkCollection(q, 2, 3, 4, -1);
		Assertions.assertEquals(-1, q.pollLast().intValue());
		checkCollection(q, 2, 3, 4);
		
		// test clear when full
		q.add(Integer.valueOf(7));
		q.add(Integer.valueOf(8));
		checkCollection(q, 2, 3, 4, 7, 8);
		Assertions.assertTrue(q.isFull());
		q.clear();
		checkEmptyCollection(q);
		
		// test addAll when full
		q = new CyclicArray<>(5);
		q.add(Integer.valueOf(1));
		q.add(Integer.valueOf(2));
		q.add(Integer.valueOf(3));
		q.add(Integer.valueOf(4));
		q.add(Integer.valueOf(5));
		Assertions.assertTrue(q.isFull());
		q.addAll(Arrays.asList(10, 11));
		checkCollection(q, 1, 2, 3, 4, 5, 10, 11);


		// test clear when end < start
		q = new CyclicArray<>(5);
		q.add(Integer.valueOf(11));
		q.add(Integer.valueOf(12));
		q.add(Integer.valueOf(13));
		q.add(Integer.valueOf(14));
		q.remove(Integer.valueOf(11));
		q.remove(Integer.valueOf(12));
		checkCollection(q, 13, 14);
		q.add(Integer.valueOf(15));
		q.add(Integer.valueOf(16));
		checkCollection(q, 13, 14, 15, 16);
		q.clear();
		checkEmptyCollection(q);
		
		// increase when end < start
		q = new CyclicArray<>(5);
		q.add(Integer.valueOf(11));
		q.add(Integer.valueOf(12));
		q.add(Integer.valueOf(13));
		q.add(Integer.valueOf(14));
		q.removeFirst();
		q.removeFirst();
		q.add(Integer.valueOf(15));
		q.add(Integer.valueOf(16));
		checkCollection(q, 13, 14, 15, 16);
		q.add(Integer.valueOf(17));
		q.add(Integer.valueOf(18));
		checkCollection(q, 13, 14, 15, 16, 17, 18);
		q.clear();
		checkEmptyCollection(q);
		
		// increase when end > start
		q = new CyclicArray<>(5);
		q.add(Integer.valueOf(11));
		q.add(Integer.valueOf(12));
		q.add(Integer.valueOf(13));
		q.addAll(Arrays.asList(14, 15, 16));
		checkCollection(q, 11, 12, 13, 14, 15, 16);
		
		Assertions.assertThrows(NegativeValueException.class, () -> new CyclicArray<Integer>(-10));
	}
	
	@Test
	void testIncreaseAndDecrease() {
		CyclicArray<Integer> q = new CyclicArray<>(5);

		// make it full
		q.add(Integer.valueOf(1));
		q.add(Integer.valueOf(2));
		q.add(Integer.valueOf(3));
		q.add(Integer.valueOf(4));
		q.add(Integer.valueOf(5));
		Assertions.assertTrue(q.isFull());

		q.removeFirst();
		q.removeFirst();
		q.add(Integer.valueOf(6));
		q.add(Integer.valueOf(7));
		Assertions.assertTrue(q.isFull());
		
		// increase
		for (int i = 8; i < 150; ++i)
			q.add(Integer.valueOf(i));
		
		// decrease
		for (int i = 0; i < 140; ++i)
			q.removeFirst();
	}

	@Test
	void testRemoveAny() {
		CyclicArray<Integer> q = new CyclicArray<>(5);
		q.add(Integer.valueOf(21));
		q.add(Integer.valueOf(22));
		q.add(Integer.valueOf(23));
		q.add(Integer.valueOf(24));
		q.add(Integer.valueOf(25));
		checkCollection(q, 21, 22, 23, 24, 25);
		Assertions.assertTrue(q.removeAny(Integer.valueOf(22)));
		Assertions.assertFalse(q.removeAny(Integer.valueOf(20)));
		checkCollection(q, 21, 23, 24, 25);
		Assertions.assertTrue(q.removeAny(Integer.valueOf(24)));
		Assertions.assertFalse(q.removeAny(Integer.valueOf(24)));
		checkCollection(q, 21, 23, 25);
		Assertions.assertTrue(q.removeAny(Integer.valueOf(21)));
		Assertions.assertFalse(q.removeAny(Integer.valueOf(21)));
		checkCollection(q, 23, 25);
		Assertions.assertTrue(q.removeAny(Integer.valueOf(25)));
		Assertions.assertFalse(q.removeAny(Integer.valueOf(25)));
		checkCollection(q, 23);
		Assertions.assertTrue(q.removeAny(Integer.valueOf(23)));
		Assertions.assertFalse(q.removeAny(Integer.valueOf(23)));
		checkEmptyCollection(q);
		q.add(1);
		q.add(2);
		q.add(3);
		q.removeFirst();
		q.removeFirst();
		q.add(4);
		q.add(5);
		q.add(6);
		checkCollection(q, 3, 4, 5, 6);
		q.removeAny(5);
		q.removeAny(3);
		checkCollection(q, 4, 6);
		q.removeAny(3);
		checkCollection(q, 4, 6);
		q.clear();
		q.add(1);
		q.add(2);
		q.add(3);
		q.removeFirst();
		q.removeFirst();
		q.add(4);
		q.add(5);
		q.add(6);
		checkCollection(q, 3, 4, 5, 6);
		q.removeAny(7);
		checkCollection(q, 3, 4, 5, 6);
		q.add(7);
		checkCollection(q, 3, 4, 5, 6, 7);
		q.removeAny(8);
		checkCollection(q, 3, 4, 5, 6, 7);
	}

	@Test
	void testAddAll() {
		CyclicArray<Integer> q = new CyclicArray<>(5);
		q.addAll(Arrays.asList(1, 2, 3));
		checkCollection(q, 1, 2, 3);
		q.removeFirst();
		q.removeFirst();
		checkCollection(q, 3);
		q.addAll(Arrays.asList(4, 5, 6));
		checkCollection(q, 3, 4, 5, 6);
		q.removeFirst();
		q.removeFirst();
		checkCollection(q, 5, 6);
		q.addAll(Arrays.asList(7, 8, 9));
		checkCollection(q, 5, 6, 7, 8, 9);
		q.removeFirst();
		q.removeFirst();
		checkCollection(q, 7, 8, 9);
		q.addAll(Arrays.asList(10, 11, 12));
		checkCollection(q, 7, 8, 9, 10, 11, 12);
		q.clear();
	}

	@Test
	void testRemoveFirstOccurence() {
		CyclicArray<Integer> q = new CyclicArray<>(5);
		q.add(1);
		q.add(2);
		q.add(1);
		q.removeFirstOccurrence(1);
		checkCollection(q, 2, 1);
		q.removeFirstOccurrence(3);
		checkCollection(q, 2, 1);
		q.removeFirstOccurrence(3);
		checkCollection(q, 2, 1);
		q.add(3);
		q.add(4);
		q.add(5);
		q.removeFirstOccurrence(3);
		checkCollection(q, 2, 1, 4, 5);
		q.add(1);
		checkCollection(q, 2, 1, 4, 5, 1);
		q.removeFirstOccurrence(3);
		checkCollection(q, 2, 1, 4, 5, 1);
		q.removeFirstOccurrence(1);
		checkCollection(q, 2, 4, 5, 1);
		q.add(5);
		checkCollection(q, 2, 4, 5, 1, 5);
		q.removeFirst();
		checkCollection(q, 4, 5, 1, 5);
		q.add(6);
		checkCollection(q, 4, 5, 1, 5, 6);
		q.removeFirstOccurrence(6);
		checkCollection(q, 4, 5, 1, 5);
		q.removeFirstOccurrence(6);
		checkCollection(q, 4, 5, 1, 5);
		q = new CyclicArray<>(5);
		q.add(1);
		q.add(2);
		q.add(3);
		q.add(4);
		q.removeFirst();
		q.removeFirst();
		q.add(3);
		q.add(4);
		checkCollection(q, 3, 4, 3, 4);
		q.removeFirstOccurrence(4);
		checkCollection(q, 3, 3, 4);
		q.removeFirstOccurrence(4);
		checkCollection(q, 3, 3);
		q = new CyclicArray<>(5);
		q.add(1);
		q.add(2);
		q.add(3);
		q.add(4);
		q.removeFirst();
		q.removeFirst();
		q.add(5);
		q.add(6);
		checkCollection(q, 3, 4, 5, 6);
		q.removeFirstOccurrence(6);
		checkCollection(q, 3, 4, 5);
	}

	@Test
	void testRemoveLastOccurence() {
		CyclicArray<Integer> q = new CyclicArray<>(5);
		q.add(1);
		q.add(2);
		q.add(1);
		q.removeLastOccurrence(1);
		checkCollection(q, 1, 2);
		q.removeLastOccurrence(3);
		checkCollection(q, 1, 2);
		q = new CyclicArray<>(5);
		q.add(1);
		q.add(2);
		q.add(1);
		q.add(3);
		q.add(2);
		q.removeLastOccurrence(1);
		checkCollection(q, 1, 2, 3, 2);
		q.removeLastOccurrence(4);
		checkCollection(q, 1, 2, 3, 2);
		q.removeFirst();
		q.add(3);
		checkCollection(q, 2, 3, 2, 3);
		q.removeLastOccurrence(4);
		checkCollection(q, 2, 3, 2, 3);
		q.removeLastOccurrence(2);
		checkCollection(q, 2, 3, 3);
		q.add(4);
		q.add(4);
		checkCollection(q, 2, 3, 3, 4, 4);
		q.removeFirst();
		q.add(2);
		checkCollection(q, 3, 3, 4, 4, 2);
		q.removeLastOccurrence(4);
		checkCollection(q, 3, 3, 4, 2);
		q = new CyclicArray<>(5);
		q.add(1);
		q.add(2);
		q.add(1);
		q.add(3);
		q.add(2);
		q.removeFirst();
		q.removeFirst();
		q.add(3);
		q.add(2);
		checkCollection(q, 1, 3, 2, 3, 2);
		q.removeLastOccurrence(4);
		checkCollection(q, 1, 3, 2, 3, 2);
		q.removeLastOccurrence(3);
		checkCollection(q, 1, 3, 2, 2);
		q = new CyclicArray<>(5);
		q.add(1);
		q.add(2);
		q.add(1);
		q.add(3);
		q.add(2);
		q.removeFirst();
		q.removeFirst();
		q.add(1);
		checkCollection(q, 1, 3, 2, 1);
		q.removeLastOccurrence(4);
		checkCollection(q, 1, 3, 2, 1);
		q.removeLastOccurrence(1);
		checkCollection(q, 1, 3, 2);
	}
	
	@Test
	void testRemoveInstance() {
		CyclicArray<Object> q = new CyclicArray<>(5);
		Object o1 = new Object();
		Object o2 = new Object();
		Object o3 = new Object();
		Object o4 = new Object();
		Object o5 = new Object();
		Assertions.assertFalse(q.removeInstance(o1));
		q.add(o1);
		q.add(o2);
		q.add(o3);
		q.removeFirst();
		q.add(o4);
		q.add(o5);
		Assertions.assertArrayEquals(new Object[] { o2, o3, o4, o5 }, q.toArray());
		Assertions.assertEquals(o2, q.removeFirst());
		q.add(o1);
		Assertions.assertArrayEquals(new Object[] { o3, o4, o5, o1 }, q.toArray());
		Assertions.assertFalse(q.removeInstance(o2));
		Assertions.assertArrayEquals(new Object[] { o3, o4, o5, o1 }, q.toArray());
		Assertions.assertTrue(q.removeInstance(o1));
		Assertions.assertArrayEquals(new Object[] { o3, o4, o5 }, q.toArray());
		Assertions.assertFalse(q.removeInstance(o1));
		Assertions.assertArrayEquals(new Object[] { o3, o4, o5 }, q.toArray());
		Assertions.assertTrue(q.removeInstance(o4));
		Assertions.assertArrayEquals(new Object[] { o3, o5 }, q.toArray());
		Assertions.assertFalse(q.removeInstance(o4));
		Assertions.assertArrayEquals(new Object[] { o3, o5 }, q.toArray());
		Assertions.assertTrue(q.removeInstance(o5));
		Assertions.assertArrayEquals(new Object[] { o3 }, q.toArray());
		Assertions.assertFalse(q.removeInstance(o5));
		Assertions.assertArrayEquals(new Object[] { o3 }, q.toArray());
		Assertions.assertTrue(q.removeInstance(o3));
		Assertions.assertArrayEquals(new Object[] {}, q.toArray());
		Assertions.assertFalse(q.removeInstance(o3));
		Assertions.assertArrayEquals(new Object[] {}, q.toArray());
		
		q = new CyclicArray<>(5);
		q.add(o1);
		q.add(o2);
		q.add(o3);
		q.removeFirst();
		q.add(o4);
		q.add(o5);
		Assertions.assertArrayEquals(new Object[] { o2, o3, o4, o5 }, q.toArray());
		q.removeFirst();
		q.add(o1);
		q.add(o2);
		Assertions.assertArrayEquals(new Object[] {  o3, o4, o5, o1, o2 }, q.toArray());
		Assertions.assertFalse(q.removeInstance(1));
		Assertions.assertTrue(q.removeInstance(o4));
		Assertions.assertArrayEquals(new Object[] {  o3, o5, o1, o2 }, q.toArray());
		
		// remove last when array is full
		q = new CyclicArray<>(5);
		q.add(o1);
		q.add(o2);
		q.add(o3);
		q.add(o4);
		q.add(o5);
		q.removeFirstOccurrence(o5);
		Assertions.assertArrayEquals(new Object[] {  o1, o2, o3, o4 }, q.toArray());
	}
	
	@Test
	void testRemoveAllNoOrder() {
		CyclicArray<Object> q = new CyclicArray<>(5);
		Object o1 = new Object();
		Object o2 = new Object();
		Object o3 = new Object();
		Object o4 = new Object();
		Object o5 = new Object();
		List<Object> list;

		q.add(o1);
		q.add(o2);
		list = q.removeAllNoOrder();
		Assertions.assertEquals(2, list.size());
		Assertions.assertTrue(list.contains(o1));
		Assertions.assertTrue(list.contains(o2));
		Assertions.assertTrue(q.isEmpty());

		q.add(o1);
		q.add(o2);
		q.add(o3);
		q.add(o4);
		q.add(o5);
		list = q.removeAllNoOrder();
		Assertions.assertEquals(5, list.size());
		Assertions.assertTrue(list.contains(o1));
		Assertions.assertTrue(list.contains(o2));
		Assertions.assertTrue(list.contains(o3));
		Assertions.assertTrue(list.contains(o4));
		Assertions.assertTrue(list.contains(o5));
		Assertions.assertTrue(q.isEmpty());
		list = q.removeAllNoOrder();
		Assertions.assertEquals(0, list.size());
		Assertions.assertTrue(q.isEmpty());

		q.add(o1);
		q.add(o2);
		q.add(o3);
		q.add(o4);
		q.add(o5);
		q.removeFirst();
		q.removeFirst();
		q.add(o1);
		q.toArray(new Object[10]);
		list = q.removeAllNoOrder();
		Assertions.assertEquals(4, list.size());
		Assertions.assertTrue(list.contains(o1));
		Assertions.assertTrue(list.contains(o3));
		Assertions.assertTrue(list.contains(o4));
		Assertions.assertTrue(list.contains(o5));
		Assertions.assertTrue(q.isEmpty());


		q.add(o1);
		q.add(o2);
		q.add(o3);
		q.add(o4);
		q.add(o5);
		q.toArray(new Object[10]);
		q.removeFirst();
		q.toArray(new Object[10]);
		list = q.removeAllNoOrder();
		Assertions.assertEquals(4, list.size());
		Assertions.assertTrue(list.contains(o2));
		Assertions.assertTrue(list.contains(o3));
		Assertions.assertTrue(list.contains(o4));
		Assertions.assertTrue(list.contains(o5));
		Assertions.assertTrue(q.isEmpty());
	}
	
	@Test
	void testResize() {
		CyclicArray<Integer> q = new CyclicArray<>(8);
		Assertions.assertEquals(8, q.getCapacity());
		// add 100
		// capacity = 8 + (4 -> 5) = 13 + 6 = 19 + 9 = 28 + 14 = 42 + 21 = 63 + 31 = 94 + 47 = 141
		for (int i = 0; i < 100; ++i)
			q.add(i);
		Assertions.assertEquals(100, q.size());
		Assertions.assertEquals(141, q.getCapacity());
		for (int i = 0; i < 100; ++i)
			Assertions.assertEquals(i, q.get(i));
		Assertions.assertEquals(100, q.size());
		Assertions.assertEquals(141, q.getCapacity());
		// remove 90 -> 10 remaining
		for (int i = 0; i < 90; ++i)
			Assertions.assertEquals(i, q.poll());
		Assertions.assertEquals(10, q.size());
		Assertions.assertEquals(141, q.getCapacity());
		for (int i = 0; i < 10; ++i)
			Assertions.assertEquals(i + 90, q.get(i));
		// add 10 -> size = 20
		for (int i = 0; i < 10; ++i)
			q.add(100 + i);
		Assertions.assertEquals(20, q.size());
		Assertions.assertEquals(141, q.getCapacity());
		for (int i = 0; i < 20; ++i)
			Assertions.assertEquals(i + 90, q.get(i));
		
		q.decreaseIfNeeded();
		// capacity = 141 - 70 = 71 
		Assertions.assertEquals(71, q.getCapacity());
		Assertions.assertEquals(20, q.size());
		for (int i = 0; i < 20; ++i)
			Assertions.assertEquals(i + 90, q.get(i));
		// 71 - 35 = 36
		q.decreaseIfNeeded();
		Assertions.assertEquals(36, q.getCapacity());
		Assertions.assertEquals(20, q.size());
		for (int i = 0; i < 20; ++i)
			Assertions.assertEquals(i + 90, q.get(i));
		// 36 - 18 = 18 => 36
		q.decreaseIfNeeded();
		Assertions.assertEquals(36, q.getCapacity());
		Assertions.assertEquals(20, q.size());
		for (int i = 0; i < 20; ++i)
			Assertions.assertEquals(i + 90, q.get(i));
		
		// add 15
		for (int i = 0; i < 15; ++i)
			q.add(110 + i);
		Assertions.assertEquals(36, q.getCapacity());
		Assertions.assertEquals(35, q.size());
		for (int i = 0; i < 35; ++i)
			Assertions.assertEquals(i + 90, q.get(i));
		
		// remove 20
		for (int i = 0; i < 20; ++i)
			Assertions.assertEquals(i + 90, q.poll());
		Assertions.assertEquals(36, q.getCapacity());
		Assertions.assertEquals(15, q.size());
		for (int i = 0; i < 15; ++i)
			Assertions.assertEquals(i + 110, q.get(i));
		
		// add 10
		for (int i = 0; i < 10; ++i)
			q.add(125 + i);
		Assertions.assertEquals(36, q.getCapacity());
		Assertions.assertEquals(25, q.size());
		for (int i = 0; i < 25; ++i)
			Assertions.assertEquals(i + 110, q.get(i));
		
		// resize to 40
		q.resize(40);
		Assertions.assertEquals(40, q.getCapacity());
		Assertions.assertEquals(25, q.size());
		for (int i = 0; i < 25; ++i)
			Assertions.assertEquals(i + 110, q.get(i));
		
		// resize to 1 => 25
		q.resize(1);
		Assertions.assertEquals(25, q.getCapacity());
		Assertions.assertEquals(25, q.size());
		for (int i = 0; i < 25; ++i)
			Assertions.assertEquals(i + 110, q.get(i));
		
		// resize to 1 => 25
		q.resize(1);
		Assertions.assertEquals(25, q.getCapacity());
		Assertions.assertEquals(25, q.size());
		for (int i = 0; i < 25; ++i)
			Assertions.assertEquals(i + 110, q.get(i));
		
		// remove 10
		for (int i = 0; i < 10; ++i)
			Assertions.assertEquals(i + 110, q.poll());
		Assertions.assertEquals(25, q.getCapacity());
		Assertions.assertEquals(15, q.size());
		
		// add 5
		for (int i = 0; i < 5; ++i)
			q.add(135 + i);
		Assertions.assertEquals(25, q.getCapacity());
		Assertions.assertEquals(20, q.size());
		for (int i = 0; i < 20; ++i)
			Assertions.assertEquals(i + 120, q.get(i));
		
		// resize to 1 => 20
		q.resize(1);
		Assertions.assertEquals(20, q.getCapacity());
		Assertions.assertEquals(20, q.size());
		for (int i = 0; i < 20; ++i)
			Assertions.assertEquals(i + 120, q.get(i));
		
		// decrease when empty
		for (int i = 0; i < 100; ++i)
			q.add(i);
		q.clear();
		for (int i = 0; i < 100; ++i)
			q.decreaseIfNeeded();
		Assertions.assertEquals(8, q.getCapacity());
		
		// decrease with end == newSize
		for (int i = 0; i < 13; ++i)
			q.add(i);
		Assertions.assertEquals(13, q.getCapacity());
		q.removeFirst();
		q.removeFirst();
		q.add(100);
		for (int i = 0; i < 9; ++i)
			q.removeFirst();
		q.resize(8);
		
		// decrease with end == 0
		q.clear();
		for (int i = 0; i < 13; ++i)
			q.add(i);
		Assertions.assertEquals(13, q.getCapacity());
		for (int i = 0; i < 5; ++i)
			q.removeFirst();
		q.resize(8);
	}
	
	@Override
	public void checkEmptyCollection(CyclicArray<Integer> c) {
		TestDeque.super.checkEmptyCollection(c);
		Assertions.assertThrows(NoSuchElementException.class, () -> c.get(0));
	}


	@Override
	public void checkCollection(CyclicArray<Integer> c, Object... values) {
		TestDeque.super.checkCollection(c, values);
		for (int i = 0; i < values.length; ++i) {
			Assertions.assertEquals(values[i], c.get(i));
		}
		Assertions.assertThrows(NoSuchElementException.class, () -> c.get(values.length));
		Assertions.assertThrows(IndexOutOfBoundsException.class, () -> c.get(-1));
	}
}
