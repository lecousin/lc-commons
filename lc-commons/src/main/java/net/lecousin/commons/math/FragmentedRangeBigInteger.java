package net.lecousin.commons.math;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * List of RangeBigInteger representing a fragmented data.
 */
public class FragmentedRangeBigInteger extends LinkedList<RangeBigInteger> {
	
	private static final long serialVersionUID = -2633315842445860994L;

	/** Constructor. */
	public FragmentedRangeBigInteger() {
		// default
	}

	/** Constructor.
	 * @param r initial range
	 */
	public FragmentedRangeBigInteger(RangeBigInteger r) {
		add(r);
	}
	
	/** Return the intersection between the 2 fragmented data.
	 * @param list1 fragmented list 1
	 * @param list2 fragmented list 2
	 * @return the intersection
	 */
	@SuppressWarnings("java:S135") // break and continue
	public static FragmentedRangeBigInteger intersect(FragmentedRangeBigInteger list1, FragmentedRangeBigInteger list2) {
		FragmentedRangeBigInteger result = new FragmentedRangeBigInteger();
		if (list1.isEmpty() || list2.isEmpty()) return result;
		for (RangeBigInteger r1 : list1) {
			for (RangeBigInteger r2 : list2) {
				if (r2.getMax().compareTo(r1.getMin()) < 0) continue;
				if (r2.getMin().compareTo(r1.getMax()) > 0) break;
				BigInteger min = r1.getMin().max(r2.getMin());
				BigInteger max = r1.getMax().min(r2.getMax());
				result.addRange(min, max);
			}
		}
		return result;
	}
	
	/** @return a copy of this instance. */
	public FragmentedRangeBigInteger copy() {
		FragmentedRangeBigInteger c = new FragmentedRangeBigInteger();
		for (RangeBigInteger r : this) c.add(new RangeBigInteger(r));
		return c;
	}
	
	/** Add a range.
	 * @param r range to add
	 */
	public void addRange(RangeBigInteger r) {
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
	public void addRange(BigInteger start, BigInteger end) {
		if (isEmpty()) {
			add(new RangeBigInteger(start, end));
			return;
		}
		for (int i = 0; i < size(); ++i) {
			RangeBigInteger r = get(i);
			if (end.compareTo(r.getMin()) < 0) { 
				if (end.equals(r.getMin().subtract(BigInteger.ONE)))
					r.setMin(start);
				else
					add(i, new RangeBigInteger(start, end)); 
				return; 
			}
			if (start.equals(r.getMax().add(BigInteger.ONE))) {
				r.setMax(end);
				for (int j = i + 1; j < size();) {
					RangeBigInteger r2 = get(j);
					int c = end.compareTo(r2.getMin().subtract(BigInteger.ONE));
					if (c < 0) break;
					if (c == 0) {
						r.setMax(r2.getMax());
						remove(j);
						break;
					}
					if (end.compareTo(r2.getMax()) >= 0) {
						remove(j);
						continue;
					}
					r.setMax(r2.getMax());
					remove(j);
					break;
				}
				return;
			}
			if (start.compareTo(r.getMax()) > 0) continue;
			if (start.compareTo(r.getMin()) < 0) r.setMin(start);
			if (end.compareTo(r.getMax()) <= 0) return;
			r.setMax(end);
			for (int j = i + 1; j < size();) {
				RangeBigInteger r2 = get(j);
				if (end.compareTo(r2.getMax()) >= 0) {
					remove(j);
					continue;
				}
				if (end.compareTo(r2.getMin().subtract(BigInteger.ONE)) < 0) break;
				r.setMax(r2.getMax());
				remove(j);
				break;
			}
			return;
		}
		add(new RangeBigInteger(start, end));
	}
	
	/** Add the given ranges.
	 * @param ranges ranges to add
	 */
	public void addRanges(Collection<RangeBigInteger> ranges) {
		for (RangeBigInteger r : ranges)
			addRange(r);
	}
	
	/** Add a single value.
	 * @param value value to add
	 */
	@SuppressWarnings("java:S3776") // complexity
	public void addValue(BigInteger value) {
		if (isEmpty()) {
			add(new RangeBigInteger(value, value));
			return;
		}
		for (int i = 0; i < size(); ++i) {
			RangeBigInteger r = get(i);
			if (value.compareTo(r.getMin()) < 0) { 
				if (value.compareTo(r.getMin().subtract(BigInteger.ONE)) == 0)
					r.setMin(value);
				else
					add(i, new RangeBigInteger(value, value)); 
				return; 
			}
			if (value.compareTo(r.getMax().add(BigInteger.ONE)) == 0) {
				r.setMax(value);
				if (i < size() - 1) {
					RangeBigInteger r2 = get(i + 1);
					if (r2.getMin().compareTo(value.add(BigInteger.ONE)) == 0) {
						r.setMax(r2.getMax());
						remove(i + 1);
					}
				}
				return;
			}
			if (value.compareTo(r.getMax()) <= 0) return;
		}
		add(new RangeBigInteger(value, value));
	}
	
	/** @return true if this fragmented data contains the given value.
	 * @param val value
	 */
	public boolean containsValue(long val) {
		BigInteger b = BigInteger.valueOf(val);
		return containsValue(b);
	}
	
	/** @return true if this fragmented data contains the given value.
	 * @param val value
	 */
	public boolean containsValue(BigInteger val) {
		for (RangeBigInteger r : this) {
			int c = r.getMin().compareTo(val);
			if (c <= 0 && val.compareTo(r.getMax()) <= 0) return true;
			if (c > 0) return false;
		}
		return false;
	}

	/** @return true if this fragmented data contains the given range of value.
	 * @param min start value
	 * @param max end value
	 */
	public boolean containsRange(BigInteger min, BigInteger max) {
		for (RangeBigInteger r : this) {
			int i = min.compareTo(r.getMin());
			if (i >= 0 && max.compareTo(r.getMax()) <= 0) return true;
			if (i < 0) return false;
		}
		return false;
	}
	
	/** @return true if this fragmented range contains at least one value of the given range.
	 * @param range range
	 */
	@SuppressWarnings("java:S135") // break and continue
	public boolean containsOneValueIn(RangeBigInteger range) {
		for (RangeBigInteger r : this) {
			if (r.getMax().compareTo(range.getMin()) < 0) continue;
			if (r.getMin().compareTo(range.getMax()) > 0) break;
			return true;
		}
		return false;
	}
	
	/** @return true if this fragmented range contains at least one value of the given ranges.
	 * @param ranges ranges
	 */
	public boolean containsOneValueIn(Collection<RangeBigInteger> ranges) {
		for (RangeBigInteger r : ranges)
			if (containsOneValueIn(r))
				return true;
		return false;
	}

	/** @return the minimum value. */
	public BigInteger getMin() {
		if (isEmpty()) return BigInteger.ZERO;
		return getFirst().getMin();
	}

	/** @return the maximum value. */
	public BigInteger getMax() {
		if (isEmpty()) return BigInteger.ZERO;
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
	public RangeBigInteger removeBestRangeForSize(BigInteger size) {
		RangeBigInteger best = null;
		BigInteger bestSize = null;
		for (Iterator<RangeBigInteger> it = iterator(); it.hasNext();) {
			RangeBigInteger r = it.next();
			BigInteger l = r.getLength();
			int c = size.compareTo(l);
			if (c == 0) {
				it.remove();
				return r;
			}
			if (c > 0) continue;
			if (bestSize == null || bestSize.compareTo(l) > 0) {
				best = r;
				bestSize = l;
			}
		}
		if (best == null) return null;
		RangeBigInteger res = new RangeBigInteger(best.getMin(), best.getMin().add(size).subtract(BigInteger.ONE));
		best.setMin(best.getMin().add(size));
		return res;
	}
	
	/** Remove the largest range.
	 * @return the removed range, or null if this fragmented range is empty
	 */
	public RangeBigInteger removeBiggestRange() {
		if (isEmpty()) return null;
		if (size() == 1) return remove(0);
		int biggestIndex = 0;
		RangeBigInteger r = get(0);
		BigInteger biggestSize = r.getLength();
		for (int i = 1; i < size(); ++i) {
			r = get(i);
			BigInteger l = r.getLength();
			if (l.compareTo(biggestSize) > 0) {
				biggestSize = l;
				biggestIndex = i;
			}
		}
		return remove(biggestIndex);
	}
	
	/** Remove and return the first value, or null if empty.
	 * @return the value removed or null if empty
	 */
	public BigInteger removeFirstValue() {
		if (isEmpty()) return null;
		RangeBigInteger r = getFirst();
		BigInteger value = r.getMin();
		if (r.getMin().equals(r.getMax())) removeFirst();
		else r.setMin(r.getMin().add(BigInteger.ONE));
		return value;
	}

	/** Remove the given range.
	 * @param start start value
	 * @param end end value
	 */
	@SuppressWarnings({
		"squid:ForLoopCounterChangedCheck", // when removing an element, we need to change it
		"java:S3776" // complexity
	})
	public void removeRange(BigInteger start, BigInteger end) {
		for (int i = 0; i < size(); ++i) {
			RangeBigInteger r = get(i);
			if (r.getMin().compareTo(end) > 0) return;
			if (r.getMax().compareTo(start) < 0) continue;
			if (r.getMin().compareTo(start) < 0) {
				if (r.getMax().equals(end)) {
					r.setMax(start.subtract(BigInteger.ONE));
					return;
				} else if (r.getMax().compareTo(end) < 0) {
					BigInteger j = r.getMax();
					r.setMax(start.subtract(BigInteger.ONE));
					start = j.add(BigInteger.ONE);
				} else {
					RangeBigInteger nr = new RangeBigInteger(end.add(BigInteger.ONE), r.getMax());
					r.setMax(start.subtract(BigInteger.ONE));
					add(i + 1, nr);
					return;
				}
			} else {
				if (r.getMax().equals(end)) {
					remove(i);
					return;
				} else if (r.getMax().compareTo(end) < 0) {
					remove(i);
					start = r.getMax().add(BigInteger.ONE);
					i--;
				} else {
					r.setMin(end.add(BigInteger.ONE));
					return;
				}
			}
		}
	}
	
	/** Remove a single value.
	 * @param value value to remove
	 */
	public void removeValue(BigInteger value) {
		removeRange(value, value);
	}
	
	/** @return the total size, summing the ranges length. */
	public BigInteger getTotalSize() {
		BigInteger total = BigInteger.ZERO;
		for (RangeBigInteger r : this)
			total = total.add(r.getLength());
		return total;
	}

	/** Add the given ranges.
	 * @param col ranges
	 */
	public void addCopy(Collection<RangeBigInteger> col) {
		for (RangeBigInteger r : col)
			addRange(r.getMin(), r.getMax());
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("{");
		boolean first = true;
		for (RangeBigInteger r : this) {
			if (first) first = false;
			else s.append(",");
			s.append("[").append(r.getMin()).append("-").append(r.getMax()).append("]");
		}
		s.append("}");
		return s.toString();
	}
	
}
