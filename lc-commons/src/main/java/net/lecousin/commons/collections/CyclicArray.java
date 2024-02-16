package net.lecousin.commons.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.lecousin.commons.exceptions.NegativeValueException;

/**
 * Implementation of Deque using an array in an efficient way.
 * <p>
 * It keeps the index of the first and last element.
 * When removing the first element, the other elements are not moved, and the
 * first element is now at index 1 in the internal array.
 * When reaching the end of the array, if the first element is not at the index 0,
 * it can use the index 0 as the last index, such as the element at index 0 in 
 * the array becomes the element after the element at index array.length - 1.
 * </p>
 * <p>
 * It allows removing/adding an element at the head to be as efficient as removing/adding at the tail
 * (no need to copy elements as in a classical array).
 * </p>
 * <p>
 * When full, its size is increased by half of the current size.
 * </p>
 * <p>
 * It is not thread-safe.
 * </p>
 * @param <T> type of elements
 */
@SuppressWarnings("squid:S3776") // complexity: to keep performance we do not split into methods
public class CyclicArray<T> implements Deque<T> {
	
	private static final String ERROR_MESSAGE_EMPTY_COLLECTION = "Collection is empty";
	
	/** Minimum size = 5. */
	public static final int MINIMUM_SIZE = 5;
	/** Minimum increase size. */
	public static final int MINIMUM_INCREASE = 5;

	/** Initialize with a size of 5. */
	public CyclicArray() {
		this(MINIMUM_SIZE, MINIMUM_SIZE);
	}

	/** Initialize with the given initial size.
	 * @param initSize initial size
	 */
	public CyclicArray(int initSize) {
		this(initSize, initSize);
	}
	
	/** Initialize with the given initial size.
	 * @param initSize initial size
	 * @param minSize minimum size
	 */
	public CyclicArray(int initSize, int minSize) {
		NegativeValueException.check(initSize, "initSize");
		if (minSize < MINIMUM_SIZE) minSize = MINIMUM_SIZE;
		if (initSize < minSize) initSize = minSize;
		this.minSize = minSize;
		array = new Object[initSize];
	}
	
	private Object[] array;
	/** First element index (except if empty: start == end). */
	private int start = 0;
	/** Index of next element to insert, or -1 if the array is full. */
	private int end = 0;
	private int minSize;
	
	/** @return true if the array is fully used, in other words if adding a new item will cause the array to grow. */
	public boolean isFull() {
		return end == -1;
	}
	
	/** @return the current capacity of the array. */
	public int getCapacity() {
		return array.length;
	}
	
	/** @return the remaining number of items that can be added before the array needs to be increased. */
	public int getNbAvailableSlots() {
		return array.length - size();
	}
	
	@Override
	public void addLast(T element) {
		if (end == -1) increase();
		array[end++] = element;
		if (end == array.length)
			end = 0;
		if (end == start)
			end = -1;
	}
	
	@Override
	public void addFirst(T e) {
		if (end == -1) increase();
		if ((start = dec(start, array.length)) == end)
			end = -1;
		array[start] = e;
	}
	
	private static int dec(int v, int last) {
		if (--v >= 0)
	        return v;
		return last - 1;
	}
	
	@Override
	@SuppressWarnings("squid:S3358") // making one ternary by line make it as clear as if else if else...
	public boolean addAll(Collection<? extends T> elements) {
		int nb = elements.size();
		if (nb == 0) return false;
		int a = end == -1 ? 0 :
				end < start ? start - end :
				start + (array.length - end);
		if (a < nb) increase(array.length + (nb - a) + MINIMUM_INCREASE);
		for (T e : elements) {
			array[end++] = e;
			if (end == array.length) end = 0;
			if (end == start)
				end = -1;
		}
		return true;
	}
	
	@Override
	public T removeFirst() {
		if (end == start) throw new NoSuchElementException(ERROR_MESSAGE_EMPTY_COLLECTION);
		if (end == -1) end = start;
		@SuppressWarnings("unchecked")
		final T e = (T) array[start];
		array[start] = null;
		if (++start == array.length) start = 0;
		return e;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T removeLast() {
		if (end == start) throw new NoSuchElementException(ERROR_MESSAGE_EMPTY_COLLECTION);
		if (end == -1) end = start;
		T e;
		if (end == 0) {
			end = array.length - 1;
			e = (T) array[end];
		} else {
			e = (T) array[--end];
		}
		array[end] = null;
		return e;
	}
	
	@Override
	public boolean isEmpty() {
		return start == end;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T getFirst() {
		if (end == start) throw new NoSuchElementException(ERROR_MESSAGE_EMPTY_COLLECTION);
		return (T) array[start];
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T getLast() {
		// empty
		if (end == start) throw new NoSuchElementException(ERROR_MESSAGE_EMPTY_COLLECTION);
		// full
		if (end == -1) {
			if (start == 0)
				return (T) array[array.length - 1];
			return (T) array[start - 1];
		}
		if (end == 0)
			return (T) array[array.length - 1];
		return (T) array[end - 1];
	}
	
	/** @return the item at the given index.
	 * @param index index
	 * @throws NoSuchElementException in case there is no item at the given index
	 * @throws NegativeValueException if index is negative
	 */
	@SuppressWarnings("unchecked")
	public T get(int index) {
		if (index < 0) throw new NegativeValueException(index, "index");
		// empty
		if (end == start) throw new NoSuchElementException(ERROR_MESSAGE_EMPTY_COLLECTION);
		int i = start + index;
		if (end > start) {
			if (i >= end)
				throw new NoSuchElementException("Index " + index + ", Size = " + size());
			return (T) array[i];
		}
		if (i < array.length)
			return (T) array[i];
		i = index - (array.length - start);
		if ((end == -1 && i >= start) || (end != -1 && i >= end))
			throw new NoSuchElementException("Index " + index + ", Size = " + size());
		return (T) array[i];
	}
	
	@Override
	public int size() {
		if (end == -1) return array.length;
		if (start == end) return 0;
		if (end > start) return end - start;
		return array.length - start + end;
	}
	
	@Override
	public void clear() {
		if (end == start) return; // empty
		if (end == -1) {
			// full
			for (int i = array.length - 1; i >= 0; --i)
				array[i] = null;
		} else if (end < start) {
			while (start < array.length) array[start++] = null;
			for (start = 0; start < end; ++start) array[start] = null;
		} else {
			while (start < end) array[start++] = null;
		}
		start = end = 0;
	}
	
	@Override
	public boolean add(T e) {
		addLast(e);
		return true;
	}
	
	@Override
	public boolean offer(T e) {
		addLast(e);
		return false;
	}
	
	@Override
	public boolean offerFirst(T e) {
		addFirst(e);
		return true;
	}
	
	@Override
	@SuppressWarnings("squid:S4144") // it is identical to add because it is the same behavior
	public boolean offerLast(T e) {
		addLast(e);
		return true;
	}
	
	@Override
	public T peek() {
		return peekFirst();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T peekFirst() {
		if (end == start) return null; // empty
		return (T) array[start];
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T peekLast() {
		// empty
		if (end == start) return null;
		// full
		if (end == -1) {
			if (start == 0)
				return (T) array[array.length - 1];
			return (T) array[start - 1];
		}
		if (end == 0)
			return (T) array[array.length - 1];
		return (T) array[end - 1];
	}
	
	@Override
	public T poll() {
		return pollFirst();
	}
	
	@Override
	public T pollFirst() {
		if (end == start) return null;
		if (end == -1) end = start;
		@SuppressWarnings("unchecked")
		final T e = (T) array[start];
		array[start++] = null;
		if (start == array.length) start = 0;
		return e;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T pollLast() {
		if (end == start) return null;
		if (end == -1) end = start;
		T e;
		if (end == 0) {
			end = array.length - 1;
			e = (T) array[end];
		} else {
			e = (T) array[--end];
		}
		array[end] = null;
		return e;
	}
	
	@Override
	public void push(T e) {
		addFirst(e);
	}
	
	@Override
	public T pop() {
		return removeFirst();
	}
	
	@Override
	public T element() {
		return getFirst();
	}
	
	@Override
	public T remove() {
		return removeFirst();
	}
	
	private void increase() {
		int newSize = array.length;
		newSize = newSize + (newSize >> 1);
		if (newSize < array.length + MINIMUM_INCREASE) newSize = array.length + MINIMUM_INCREASE;
		increase(newSize);
	}
	
	private void increase(int newSize) {
		Object[] a = new Object[newSize];
		if (end == -1) {
			System.arraycopy(array, start, a, 0, array.length - start);
			if (start > 0)
				System.arraycopy(array, 0, a, array.length - start, start);
			end = array.length;
		} else if (end < start) {
			System.arraycopy(array, start, a, 0, array.length - start);
			System.arraycopy(array, 0, a, array.length - start, end + 1);
			end = array.length - start + end;
		} else {
			System.arraycopy(array, start, a, 0, end - start + 1);
			end -= start;
		}
		start = 0;
		array = a;
	}
	
	/** Resize the capacity of this array.
	 * @param newSize new size
	 */
	public void resize(int newSize) {
		if (newSize < array.length) {
			// decrease asked => check minimum
			if (newSize < minSize)
				newSize = minSize; // cannot go beyond minSize
			int s = size();
			if (newSize < s)
				newSize = s; // cannot go beyond the current number of elements
		} else {
			increase(newSize);
			return;
		}
		
		if (newSize == array.length)
			return; // no change
		decrease(newSize);
	}
	
	/** Decrease the capacity of this array if needed (if less than half of the array is used). */
	public void decreaseIfNeeded() {
		int s = size();
		int newSize = array.length - (array.length >> 1);
		if (s >= newSize)
			return;
		resize(newSize);
	}
	
	private void decrease(int newSize) {
		Object[] a = new Object[newSize];
		if (end > start) {
			System.arraycopy(array, start, a, 0, end - start);
			end = end - start;
			if (end == newSize)
				end = -1;
			start = 0;
		} else if (end < start) {
			System.arraycopy(array, start, a, 0, array.length - start);
			if (end > 0)
				System.arraycopy(array, 0, a, array.length - start, end);
			end = array.length - start + end;
			if (end == newSize)
				end = -1;
			start = 0;
		} else {
			start = end = 0;
		}
		array = a;
	}
	
	// other operation which may be costly
	
	@Override
	public boolean contains(Object o) {
		if (start == end) return false; // empty
		if (end == -1) {
			// full
			for (int i = array.length - 1; i >= 0; --i)
				if (array[i].equals(o))
					return true;
			return false;
		}
		if (end < start) {
			for (int i = array.length - 1; i >= start; --i)
				if (array[i].equals(o))
					return true;
			for (int i = 0; i < end; ++i)
				if (array[i].equals(o))
					return true;
			return false;
		}
		for (int i = start; i < end; ++i)
			if (array[i].equals(o))
				return true;
		return false;
	}

	/** The implementation of this operation has not been optimized. */
	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) if (!contains(o)) return false;
		return true;
	}

	/** remove this element, not necessarily the first or last occurrence.
	 * @param element the element to remove
	 * @return true if the element has been found and removed
	 */
	public boolean removeAny(Object element) {
		if (end == start) return false;
		if (end == -1) {
			for (int i = array.length - 1; i >= 0; --i)
				if (array[i].equals(element)) {
					removeAt(i);
					return true;
				}
			return false;
		}
		if (end < start) {
			for (int i = array.length - 1; i >= start; --i)
				if (array[i].equals(element)) {
					removeAt(i);
					return true;
				}
			for (int i = 0; i < end; ++i)
				if (array[i].equals(element)) {
					removeAt(i);
					return true;
				}
			return false;
		}
		for (int i = start; i < end; ++i)
			if (array[i].equals(element)) {
				removeAt(i);
				return true;
			}
		return false;
	}
	
	@Override
	public boolean removeFirstOccurrence(Object element) {
		if (end == start) return false;
		if (end == -1) {
			for (int i = start; i < array.length; ++i)
				if (array[i].equals(element)) {
					removeAt(i);
					return true;
				}
			for (int i = 0; i < start; ++i)
				if (array[i].equals(element)) {
					removeAt(i);
					return true;
				}
			return false;
		}
		if (end < start) {
			for (int i = start; i < array.length; ++i)
				if (array[i].equals(element)) {
					removeAt(i);
					return true;
				}
			for (int i = 0; i < end; ++i)
				if (array[i].equals(element)) {
					removeAt(i);
					return true;
				}
			return false;
		}
		for (int i = start; i < end; ++i)
			if (array[i].equals(element)) {
				removeAt(i);
				return true;
			}
		return false;
	}
	
	@Override
	public boolean removeLastOccurrence(Object element) {
		if (end == start) return false;
		if (end == -1) {
			for (int i = start - 1; i >= 0; --i)
				if (array[i].equals(element)) {
					removeAt(i);
					return true;
				}
			for (int i = array.length - 1; i >= start; --i)
				if (array[i].equals(element)) {
					removeAt(i);
					return true;
				}
			return false;
		}
		if (end < start) {
			for (int i = end - 1; i >= 0; --i)
				if (array[i].equals(element)) {
					removeAt(i);
					return true;
				}
			for (int i = array.length - 1; i >= start; --i)
				if (array[i].equals(element)) {
					removeAt(i);
					return true;
				}
			return false;
		}
		for (int i = end - 1; i >= start; --i)
			if (array[i].equals(element)) {
				removeAt(i);
				return true;
			}
		return false;
	}

	/** Remove the given instance.
	 * @param element instance to remove
	 * @return true if the instance has been found and removed
	 */
	public boolean removeInstance(T element) {
		if (end == start) return false;
		if (end == -1) {
			for (int i = array.length - 1; i >= 0; --i)
				if (array[i] == element) {
					removeAt(i);
					return true;
				}
			return false;
		}
		if (end < start) {
			for (int i = array.length - 1; i >= start; --i)
				if (array[i] == element) {
					removeAt(i);
					return true;
				}
			for (int i = 0; i < end; ++i)
				if (array[i] == element) {
					removeAt(i);
					return true;
				}
			return false;
		}
		for (int i = start; i < end; ++i)
			if (array[i] == element) {
				removeAt(i);
				return true;
			}
		return false;
	}
	
	// skip checkstyle: OverloadMethodsDeclarationOrder
	@Override
	public boolean remove(Object o) {
		return removeFirstOccurrence(o);
	}
	
	/** The implementation of this operation has not been optimized. */
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object o : c) changed |= remove(o);
		return changed;
	}
	
	private void removeAt(int index) {
		if (index == start) {
			removeFirst();
			return;
		}
		if (index >= start) {
			if (end == -1) {
				// array is full
				if (index < array.length - 1)
					System.arraycopy(array, index + 1, array, index, array.length - index - 1);
				if (start == 0) {
					end = array.length - 1;
					array[end] = null;
					return;
				}
				array[array.length - 1] = array[0];
				if (start == 1) {
					array[0] = null;
					end = 0;
					return;
				}
				System.arraycopy(array, 1, array, 0, start - 1);
				end = start - 1;
				array[end] = null;
				return;
			}
			if (index == end - 1) {
				// last element, just null it and decrement end
				array[index] = null;
				end--;
				return;
			}
			if (end > start) {
				// we just need to shift elements and decrease end
				System.arraycopy(array, index + 1, array, index, end - index - 1);
				array[--end] = null;
				return;
			}
			// end < start
			if (index < array.length - 1)
				System.arraycopy(array, index + 1, array, index, array.length - index - 1);
			if (end == 0) {
				end = array.length - 1;
				array[end] = null;
				return;
			}
			array[array.length - 1] = array[0];
			if (end == 1) {
				array[0] = null;
				end = 0;
				return;
			}
			System.arraycopy(array, 1, array, 0, end - 1);
			array[--end] = null;
			return;
		}
		// index < start
		if (end == -1) {
			// array is full
			if (index == start - 1) {
				// last element
				array[index] = null;
				end = index;
				return;
			}
			System.arraycopy(array, index + 1, array, index, start - index - 1);
			array[start - 1] = null;
			end = start - 1;
			return;
		}
		if (index == end - 1) {
			// last element
			array[index] = null;
			end--;
			return;
		}
		System.arraycopy(array, index + 1, array, index, end - index);
		array[--end] = null;
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		if (end == start)
			return false;
		List<T> toRemove = new ArrayList<>(size());
		for (T element : this)
			if (!c.contains(element))
				toRemove.add(element);
		return removeAll(toRemove);
	}
	
	@Override
	public Iterator<T> iterator() {
		return new It();
	}
	
	private class It implements Iterator<T> {
		It() {
			pos = 0;
		}
		
		private int pos;
		
		@Override
		public boolean hasNext() {
			return pos < size();
		}
		
		@Override
		public T next() {
			if (pos >= size()) throw new NoSuchElementException();
			int i = start + pos;
			if (i >= array.length) i -= array.length;
			@SuppressWarnings("unchecked")
			T e = (T) array[i];
			pos++;
			return e;
		}
	}

	@Override
	public Iterator<T> descendingIterator() {
		return new DIt();
	}
	
	private class DIt implements Iterator<T> {
		DIt() {
			pos = size() - 1;
		}
		
		private int pos;
		
		@Override
		public boolean hasNext() {
			return pos >= 0;
		}
		
		@Override
		public T next() {
			if (pos < 0) throw new NoSuchElementException();
			int i = start + pos;
			if (i >= array.length) i -= array.length;
			@SuppressWarnings("unchecked")
			T e = (T) array[i];
			pos--;
			return e;
		}
	}
	
	/** Remove all elements and return them, but the returned list is not ordered.
	 * The reason it is not ordered is for performance, when the order is not important.
	 * @return removed elements
	 */
	@SuppressWarnings("unchecked")
	public List<T> removeAllNoOrder() {
		if (end == -1) {
			List<Object> a = Arrays.asList(array);
			array = new Object[array.length];
			start = end = 0;
			return (List<T>) a;
		}
		if (start == end) return Collections.emptyList();
		if (end > start) {
			Object[] a = new Object[end - start];
			System.arraycopy(array, start, a, 0, end - start);
			for (int i = start; i < end; ++i) array[i] = null;
			start = end = 0;
			return (List<T>) Arrays.asList(a);
		}
		Object[] a = new Object[array.length - start + end];
		System.arraycopy(array, start, a, 0, array.length - start);
		if (end > 0)
			System.arraycopy(array, 0, a, array.length - start, end);
		for (int i = array.length - 1; i >= 0; --i) array[i] = null;
		start = end = 0;
		return (List<T>) Arrays.asList(a);
	}
	
	@Override
	public Object[] toArray() {
		if (end == start) return new Object[0];
		if (end == -1) {
			Object[] a = new Object[array.length];
			System.arraycopy(array, start, a, 0, array.length - start);
			if (start > 0)
				System.arraycopy(array, 0, a, array.length - start, start);
			return a;
		}
		Object[] a = new Object[size()];
		if (end < start) {
			System.arraycopy(array, start, a, 0, array.length - start);
			if (end > 0) System.arraycopy(array, 0, a, array.length - start, end);
		} else {
			System.arraycopy(array, start, a, 0, end - start);
		}
		return a;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <U extends Object> U[] toArray(U[] a) {
		int nb = size();
		if (a.length < nb)
			a = (U[]) Array.newInstance(a.getClass().getComponentType(), nb);
		if (end == -1) {
			System.arraycopy(array, start, a, 0, array.length - start);
			if (start > 0)
				System.arraycopy(array, 0, a, array.length - start, start);
		} else if (end < start) {
			System.arraycopy(array, start, a, 0, array.length - start);
			if (end > 0) System.arraycopy(array, 0, a, array.length - start, end);
		} else {
			System.arraycopy(array, start, a, 0, end - start);
		}
		if (nb < a.length)
			a[nb] = null;
		return a;
	}
	
}
