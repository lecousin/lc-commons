package net.lecousin.commons.math;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Range of integer values, with a minimum and maximum.
 */
@Getter
@Setter
@AllArgsConstructor
public class RangeInteger {
	
	private int min;
	private int max;

	/** Copy constructor.
	 * @param copy range to copy
	 */
	public RangeInteger(RangeInteger copy) {
		this.min = copy.min;
		this.max = copy.max;
	}
	
	/** @return a copy of this range. */
	public RangeInteger copy() {
		return new RangeInteger(min, max);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RangeInteger o) {
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
	public boolean contains(int value) {
		return value >= min && value <= max;
	}

	/** @return the length (max - min + 1). */
	public int getLength() {
		return max - min + 1;
	}
	
	/** @return the intersection between this range and the given range.
	 * @param r range
	 */
	public RangeInteger intersect(RangeInteger r) {
		if (min > r.max) return null;
		if (max < r.min) return null;
		return new RangeInteger(Math.max(min, r.min), Math.min(max, r.max));
	}
	
	/** Remove the intersection between this range and the given range, and return the range before and the range after the intersection.
	 * @param o other range
	 * @return the intersection
	 */
	public Pair<Optional<RangeInteger>, Optional<RangeInteger>> removeIntersect(RangeInteger o) {
		if (o.max < min || o.min > max) // o is outside: no intersection
			return Pair.of(Optional.of(copy()), Optional.empty());
		if (o.min <= min) {
			// nothing before
			if (o.max >= max)
				return Pair.of(Optional.empty(), Optional.empty()); // o is fully overlapping this
			return Pair.of(Optional.empty(), Optional.of(new RangeInteger(o.max + 1, max)));
		}
		if (o.max >= max) {
			// nothing after
			return Pair.of(Optional.of(new RangeInteger(min, o.min - 1)), Optional.empty());
		}
		// in the middle
		return Pair.of(Optional.of(new RangeInteger(min, o.min - 1)), Optional.of(new RangeInteger(o.max + 1, max)));
	}
	
	@Override
	public String toString() {
		return "[" + min + "-" + max + "]";
	}
	
}
