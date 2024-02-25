package net.lecousin.commons.math;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * List of RangeLong representing a fragmented data.
 */
public class FragmentedRangeLong extends LinkedList<RangeLong> {
	
	private static final long serialVersionUID = 1L;

	/** Constructor. */
	public FragmentedRangeLong() {
		// default
	}

	/** Constructor.
	 * @param r initial range
	 */
	public FragmentedRangeLong(RangeLong r) {
		add(r);
	}
	
	/** Return the intersection between the 2 fragmented data.
	 * @param list1 fragmented list 1
	 * @param list2 fragmented list 2
	 * @return the intersection
	 */
	@SuppressWarnings({
		"java:S135", // break and continue
		"squid:S3776" // complexity
	})
	public static FragmentedRangeLong intersect(FragmentedRangeLong list1, FragmentedRangeLong list2) {
		FragmentedRangeLong result = new FragmentedRangeLong();
		if (list1.isEmpty() || list2.isEmpty()) return result;
		for (RangeLong r1 : list1) {
			for (RangeLong r2 : list2) {
				if (r2.getMax() < r1.getMin()) continue;
				if (r2.getMin() > r1.getMax()) break;
				long min = Math.max(r1.getMin(), r2.getMin());
				long max = Math.min(r1.getMax(), r2.getMax());
				result.addRange(min, max);
			}
		}
		return result;
	}
	
	/** @return a copy of this instance. */
	public FragmentedRangeLong copy() {
		FragmentedRangeLong c = new FragmentedRangeLong();
		for (RangeLong r : this) c.add(r.copy());
		return c;
	}
	
	/** Add a range.
	 * @param r range to add
	 */
	public void addRange(RangeLong r) {
		addRange(r.getMin(), r.getMax());
	}
	
	/** Add the given range.
	 * @param start start value
	 * @param end end value
	 */
	@SuppressWarnings({
		"java:S135", // break and continue
		"java:S3776" // complexity
	})
	public void addRange(long start, long end) {
		if (isEmpty()) {
			add(new RangeLong(start, end));
			return;
		}
		for (int i = 0; i < size(); ++i) {
			RangeLong r = get(i);
			if (end < r.getMin()) { 
				if (end == r.getMin() - 1)
					r.setMin(start);
				else
					add(i, new RangeLong(start, end)); 
				return; 
			}
			if (start == r.getMax() + 1) {
				r.setMax(end);
				for (int j = i + 1; j < size();) {
					RangeLong r2 = get(j);
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
				RangeLong r2 = get(j);
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
		add(new RangeLong(start, end));
	}

	/** Add the given ranges.
	 * @param ranges ranges to add
	 */
	public void addRanges(Collection<RangeLong> ranges) {
		for (RangeLong r : ranges)
			addRange(r);
	}

	/** Add a single value.
	 * @param value value to add
	 */
	@SuppressWarnings("java:S3776") // complexity
	public void addValue(long value) {
		if (isEmpty()) {
			add(new RangeLong(value, value));
			return;
		}
		for (int i = 0; i < size(); ++i) {
			RangeLong r = get(i);
			if (value < r.getMin()) { 
				if (value == r.getMin() - 1)
					r.setMin(value);
				else
					add(i, new RangeLong(value, value)); 
				return; 
			}
			if (value == r.getMax() + 1) {
				r.setMax(value);
				if (i < size() - 1) {
					RangeLong r2 = get(i + 1);
					if (r2.getMin() == value + 1) {
						r.setMax(r2.getMax());
						remove(i + 1);
					}
				}
				return;
			}
			if (value <= r.getMax()) return;
		}
		add(new RangeLong(value, value));
	}
	
	/** @return true if this fragmented data contains the given value.
	 * @param val value
	 */
	public boolean containsValue(long val) {
		for (RangeLong r : this) {
			if (val >= r.getMin() && val <= r.getMax()) return true;
			if (val < r.getMin()) return false;
		}
		return false;
	}

	/** @return true if this fragmented data contains the given range of value.
	 * @param min start value
	 * @param max end value
	 */
	public boolean containsRange(long min, long max) {
		for (RangeLong r : this) {
			if (min >= r.getMin() && max <= r.getMax()) return true;
			if (min < r.getMin()) return false;
		}
		return false;
	}
	
	/** @return true if this fragmented range contains at least one value of the given range.
	 * @param range range
	 */
	@SuppressWarnings("java:S135") // break and continue
	public boolean containsOneValueIn(RangeLong range) {
		for (RangeLong r : this) {
			if (r.getMax() < range.getMin()) continue;
			if (r.getMin() > range.getMax()) break;
			return true;
		}
		return false;
	}
	
	/** @return true if this fragmented range contains at least one value of the given ranges.
	 * @param ranges ranges
	 */
	public boolean containsOneValueIn(Collection<RangeLong> ranges) {
		for (RangeLong r : ranges)
			if (containsOneValueIn(r))
				return true;
		return false;
	}
	
	/** @return the minimum value. */
	public long getMin() {
		if (isEmpty()) return Long.MAX_VALUE;
		return getFirst().getMin();
	}

	/** @return the maximum value. */
	public long getMax() {
		if (isEmpty()) return Long.MIN_VALUE;
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
	public RangeLong removeBestRangeForSize(long size) {
		RangeLong best = null;
		long bestSize = Long.MAX_VALUE;
		for (Iterator<RangeLong> it = iterator(); it.hasNext();) {
			RangeLong r = it.next();
			if (r.getMax() - r.getMin() + 1 == size) {
				it.remove();
				return r;
			}
			if (r.getMax() - r.getMin() + 1 < size) continue;
			long s = r.getMax() - r.getMin() + 1;
			if (s < bestSize) {
				best = r;
				bestSize = s;
			}
		}
		if (best == null) return null;
		RangeLong res = new RangeLong(best.getMin(), best.getMin() + size - 1);
		best.setMin(best.getMin() + size);
		return res;
	}
	
	/** Remove the largest range.
	 * @return the removed range, or null if this fragmented range is empty
	 */
	public RangeLong removeBiggestRange() {
		if (isEmpty()) return null;
		if (size() == 1) return remove(0);
		int biggestIndex = 0;
		RangeLong r = get(0);
		long biggestSize = r.getMax() - r.getMin() + 1;
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
	public Long removeFirstValue() {
		if (isEmpty()) return null;
		RangeLong r = getFirst();
		long value = r.getMin();
		if (r.getMin() == r.getMax()) removeFirst();
		else r.setMin(r.getMin() + 1);
		return Long.valueOf(value);
	}
	
	/** Remove the given range.
	 * @param start start value
	 * @param end end value
	 */
	@SuppressWarnings({
		"squid:ForLoopCounterChangedCheck", // when removing an element, we need to change it
		"java:S3776" // complexity
	})
	public void removeRange(long start, long end) {
		for (int i = 0; i < size(); ++i) {
			RangeLong r = get(i);
			if (r.getMin() > end) return;
			if (r.getMax() < start) continue;
			if (r.getMin() < start) {
				if (r.getMax() == end) {
					r.setMax(start - 1);
					return;
				} else if (r.getMax() < end) {
					long j = r.getMax();
					r.setMax(start - 1);
					start = j + 1;
				} else {
					RangeLong nr = new RangeLong(end + 1, r.getMax());
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
	public void removeValue(long value) {
		removeRange(value, value);
	}
	
	/** @return the total size, summing the ranges length. */
	public long getTotalSize() {
		long total = 0;
		for (RangeLong r : this)
			total += r.getMax() - r.getMin() + 1;
		return total;
	}
	
	/** Add the given ranges.
	 * @param col ranges
	 */
	public void addCopy(Collection<RangeLong> col) {
		for (RangeLong r : col)
			addRange(r.getMin(), r.getMax());
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("{");
		boolean first = true;
		for (RangeLong r : this) {
			if (first) first = false;
			else s.append(",");
			s.append("[").append(r.getMin()).append("-").append(r.getMax()).append("]");
		}
		s.append("}");
		return s.toString();
	}
	
}
