package net.lecousin.commons.math;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * List of RangeInteger representing a fragmented data.
 */
public class FragmentedRangeInteger extends LinkedList<RangeInteger> {
	
	private static final long serialVersionUID = -2633315842445860994L;

	/** Constructor. */
	public FragmentedRangeInteger() {
		// default
	}

	/** Constructor.
	 * @param r initial range
	 */
	public FragmentedRangeInteger(RangeInteger r) {
		add(r);
	}
	
	/** Return the intersection between the 2 fragmented data.
	 * @param list1 fragmented list 1
	 * @param list2 fragmented list 2
	 * @return the intersection
	 */
	@SuppressWarnings("java:S135") // break and continue
	public static FragmentedRangeInteger intersect(FragmentedRangeInteger list1, FragmentedRangeInteger list2) {
		FragmentedRangeInteger result = new FragmentedRangeInteger();
		if (list1.isEmpty() || list2.isEmpty()) return result;
		for (RangeInteger r1 : list1) {
			for (RangeInteger r2 : list2) {
				if (r2.getMax() < r1.getMin()) continue;
				if (r2.getMin() > r1.getMax()) break;
				int min = Math.max(r1.getMin(), r2.getMin());
				int max = Math.min(r1.getMax(), r2.getMax());
				result.addRange(min, max);
			}
		}
		return result;
	}
	
	/** @return a copy of this instance. */
	public FragmentedRangeInteger copy() {
		FragmentedRangeInteger c = new FragmentedRangeInteger();
		for (RangeInteger r : this) c.add(r.copy());
		return c;
	}
	
	/** Add a range.
	 * @param r range to add
	 */
	public void addRange(RangeInteger r) {
		addRange(r.getMin(), r.getMax());
	}
	
	/** Add the given range.
	 * @param start start value
	 * @param end end value
	 */
	@SuppressWarnings({
		"java:S135", // break and continue
		"squid:S3776" // complexity
	})
	public void addRange(int start, int end) {
		if (isEmpty()) {
			add(new RangeInteger(start, end));
			return;
		}
		for (int i = 0; i < size(); ++i) {
			RangeInteger r = get(i);
			if (end < r.getMin()) { 
				if (end == r.getMin() - 1)
					r.setMin(start);
				else
					add(i, new RangeInteger(start, end)); 
				return; 
			}
			if (start == r.getMax() + 1) {
				r.setMax(end);
				for (int j = i + 1; j < size();) {
					RangeInteger r2 = get(j);
					if (end < r2.getMin() - 1) break;
					if (end == r2.getMin() - 1) {
						r.setMax(r2.getMax());
						remove(j);
						break;
					}
					if (end >= r2.getMax()) {
						remove(j);
						continue;
					}
					r.setMax(r2.getMax());
					remove(j);
					break;
				}
				return;
			}
			if (start > r.getMax()) continue;
			if (start < r.getMin()) r.setMin(start);
			if (end <= r.getMax()) return;
			r.setMax(end);
			for (int j = i + 1; j < size();) {
				RangeInteger r2 = get(j);
				if (end >= r2.getMax()) {
					remove(j);
					continue;
				}
				if (end < r2.getMin() - 1) break;
				r.setMax(r2.getMax());
				remove(j);
				break;
			}
			return;
		}
		add(new RangeInteger(start, end));
	}

	/** Add the given ranges.
	 * @param ranges ranges to add
	 */
	public void addRanges(Collection<RangeInteger> ranges) {
		for (RangeInteger r : ranges)
			addRange(r);
	}
	

	/** Add a single value.
	 * @param value value to add
	 */
	@SuppressWarnings("java:S3776") // complexity
	public void addValue(int value) {
		if (isEmpty()) {
			add(new RangeInteger(value, value));
			return;
		}
		for (int i = 0; i < size(); ++i) {
			RangeInteger r = get(i);
			if (value < r.getMin()) { 
				if (value == r.getMin() - 1)
					r.setMin(value);
				else
					add(i, new RangeInteger(value, value)); 
				return; 
			}
			if (value == r.getMax() + 1) {
				r.setMax(value);
				if (i < size() - 1) {
					RangeInteger r2 = get(i + 1);
					if (r2.getMin() == value + 1) {
						r.setMax(r2.getMax());
						remove(i + 1);
					}
				}
				return;
			}
			if (value <= r.getMax()) return;
		}
		add(new RangeInteger(value, value));
	}
	
	/** @return true if this fragmented data contains the given value.
	 * @param val value
	 */
	public boolean containsValue(int val) {
		for (RangeInteger r : this) {
			if (val >= r.getMin() && val <= r.getMax()) return true;
			if (val < r.getMin()) return false;
		}
		return false;
	}

	/** @return true if this fragmented data contains the given range of value.
	 * @param start start value
	 * @param end end value
	 */
	public boolean containsRange(int start, int end) {
		if (start > end) return true;
		for (RangeInteger r : this) {
			if (r.getMin() > start) return false;
			if (r.getMax() < start) continue;
			return r.getMax() >= end;
		}
		return false;
	}
	
	/** @return true if this fragmented range contains at least one value of the given range.
	 * @param range range
	 */
	@SuppressWarnings("java:S135") // break and continue
	public boolean containsOneValueIn(RangeInteger range) {
		for (RangeInteger r : this) {
			if (r.getMax() < range.getMin()) continue;
			if (r.getMin() > range.getMax()) break;
			return true;
		}
		return false;
	}
	
	/** @return true if this fragmented range contains at least one value of the given ranges.
	 * @param ranges ranges
	 */
	public boolean containsOneValueIn(Collection<RangeInteger> ranges) {
		for (RangeInteger r : ranges)
			if (containsOneValueIn(r))
				return true;
		return false;
	}
	
	/** @return the minimum value. */
	public int getMin() {
		if (isEmpty()) return Integer.MAX_VALUE;
		return getFirst().getMin();
	}

	/** @return the maximum value. */
	public int getMax() {
		if (isEmpty()) return Integer.MIN_VALUE;
		return getLast().getMax();
	}
	
	/**
	 * If a range with the exact size exists, it is returned.
	 * Else, the smaller range greater than the given size is used,
	 * the given size is removed from it, and the removed range is returned.
	 * If no range can contain the size, null is returned.
	 * @param size requested size
	 * @return range of the requested size of null if no range can contain this size
	 */
	public RangeInteger removeBestRangeForSize(int size) {
		RangeInteger best = null;
		int bestSize = Integer.MAX_VALUE;
		for (Iterator<RangeInteger> it = iterator(); it.hasNext();) {
			RangeInteger r = it.next();
			if (r.getMax() - r.getMin() + 1 == size) {
				it.remove();
				return r;
			}
			if (r.getMax() - r.getMin() + 1 < size) continue;
			int s = r.getMax() - r.getMin() + 1;
			if (s < bestSize) {
				best = r;
				bestSize = s;
			}
		}
		if (best == null) return null;
		RangeInteger res = new RangeInteger(best.getMin(), best.getMin() + size - 1);
		best.setMin(best.getMin() + size);
		return res;
	}
	
	/** Remove the largest range.
	 * @return the removed range, or null if this fragmented range is empty
	 */
	public RangeInteger removeBiggestRange() {
		if (isEmpty()) return null;
		if (size() == 1) return remove(0);
		int biggestIndex = 0;
		RangeInteger r = get(0);
		int biggestSize = r.getMax() - r.getMin() + 1;
		for (int i = 1; i < size(); ++i) {
			r = get(i);
			if (r.getMax() - r.getMin() + 1 > biggestSize) {
				biggestSize = r.getMax() - r.getMin() + 1;
				biggestIndex = i;
			}
		}
		return remove(biggestIndex);
	}
	
	/** Remove and return the first value, or null if empty.
	 * @return the value removed or null if empty
	 */
	public Integer removeFirstValue() {
		if (isEmpty()) return null;
		RangeInteger r = getFirst();
		int value = r.getMin();
		if (r.getMin() == r.getMax()) removeFirst();
		else r.setMin(r.getMin() + 1);
		return Integer.valueOf(value);
	}

	/** Remove the given range.
	 * @param start start value
	 * @param end end value
	 */
	@SuppressWarnings({
		"squid:ForLoopCounterChangedCheck", // when removing an element, we need to change it
		"java:S3776" // complexity
	})
	public void remove(int start, int end) {
		for (int i = 0; i < size(); ++i) {
			RangeInteger r = get(i);
			if (r.getMin() > end) return;
			if (r.getMax() < start) continue;
			if (r.getMin() < start) {
				if (r.getMax() == end) {
					r.setMax(start - 1);
					return;
				} else if (r.getMax() < end) {
					int j = r.getMax();
					r.setMax(start - 1);
					start = j + 1;
				} else {
					RangeInteger nr = new RangeInteger(end + 1, r.getMax());
					r.setMax(start - 1);
					add(i + 1, nr);
					return;
				}
			} else {
				if (r.getMax() == end) {
					remove(i);
					return;
				} else if (r.getMax() < end) {
					remove(i);
					start = r.getMax() + 1;
					i--;
				} else {
					r.setMin(end + 1);
					return;
				}
			}
		}
	}
	
	/** Remove a single value.
	 * @param value value to remove
	 */
	public void removeValue(int value) {
		remove(value, value);
	}
	
	/** @return the total size, summing the ranges length. */
	public int getTotalSize() {
		int total = 0;
		for (RangeInteger r : this)
			total += r.getMax() - r.getMin() + 1;
		return total;
	}
	
	/** Add the given ranges.
	 * @param col ranges
	 */
	public void addCopy(Collection<RangeInteger> col) {
		for (RangeInteger r : col)
			addRange(r.getMin(), r.getMax());
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("{");
		boolean first = true;
		for (RangeInteger r : this) {
			if (first) first = false;
			else s.append(",");
			s.append("[").append(r.getMin()).append("-").append(r.getMax()).append("]");
		}
		s.append("}");
		return s.toString();
	}
	
}
