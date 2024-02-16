package net.lecousin.commons.math;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Range of long values, with a minimum and maximum.
 */
@Getter
@Setter
@AllArgsConstructor
public class RangeLong {
	
	private long min;
	private long max;

	/** Copy constructor.
	 * @param copy range to copy
	 */
	public RangeLong(RangeLong copy) {
		this.min = copy.min;
		this.max = copy.max;
	}
	
	/** @return a copy of this range. */
	public RangeLong copy() {
		return new RangeLong(this);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RangeLong o) {
			return o.min == min && o.max == max;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(min, max);
	}

	
	/** @return true if this range contains the given value.
	 * @param value value
	 */
	public boolean contains(long value) {
		return value >= min && value <= max;
	}
	
	/** @return the length (max - min + 1). */
	public long getLength() {
		return max - min + 1;
	}
	
	/** @return the intersection between this range and the given range.
	 * @param r range
	 */
	public RangeLong intersect(RangeLong r) {
		if (min > r.max) return null;
		if (max < r.min) return null;
		return new RangeLong(Math.max(min, r.min), Math.min(max, r.max));
	}
	
	/** Remove the intersection between this range and the given range, and return the range before and the range after the intersection.
	 * @param o other range
	 * @return the intersection
	 */
	public Pair<Optional<RangeLong>, Optional<RangeLong>> removeIntersect(RangeLong o) {
		if (o.max < min || o.min > max) // o is outside: no intersection
			return Pair.of(Optional.of(copy()), Optional.empty());
		if (o.min <= min) {
			// nothing before
			if (o.max >= max)
				return Pair.of(Optional.empty(), Optional.empty()); // o is fully overlapping this
			return Pair.of(Optional.empty(), Optional.of(new RangeLong(o.max + 1, max)));
		}
		if (o.max >= max) {
			// nothing after
			return Pair.of(Optional.of(new RangeLong(min, o.min - 1)), Optional.empty());
		}
		// in the middle
		return Pair.of(Optional.of(new RangeLong(min, o.min - 1)), Optional.of(new RangeLong(o.max + 1, max)));
	}
	
	@Override
	public String toString() {
		return "[" + min + "-" + max + "]";
	}
	
}
