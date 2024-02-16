package net.lecousin.commons.math;

import java.math.BigInteger;
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
public class RangeBigInteger {
	
	private BigInteger min;
	private BigInteger max;

	/** Copy constructor.
	 * @param copy range to copy
	 */
	public RangeBigInteger(RangeBigInteger copy) {
		this.min = copy.min;
		this.max = copy.max;
	}
	
	/** @return a copy of this range. */
	public RangeBigInteger copy() {
		return new RangeBigInteger(min, max);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RangeBigInteger o) {
			return o.min.equals(min) && o.max.equals(max);
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
	public boolean contains(BigInteger value) {
		return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
	}
	
	/** @return the length (max - min + 1). */
	public BigInteger getLength() {
		return max.subtract(min).add(BigInteger.ONE);
	}

	/** @return the intersection between this range and the given range.
	 * @param r range
	 */
	public RangeBigInteger intersect(RangeBigInteger r) {
		if (min.compareTo(r.max) > 0) return null;
		if (max.compareTo(r.min) < 0) return null;
		return new RangeBigInteger(min.compareTo(r.min) <= 0 ? r.min : min, max.compareTo(r.max) <= 0 ? max : r.max);
	}
	
	/** Remove the intersection between this range and the given range, and return the range before and the range after the intersection.
	 * @param o other range
	 * @return the intersection
	 */
	public Pair<Optional<RangeBigInteger>, Optional<RangeBigInteger>> removeIntersect(RangeBigInteger o) {
		if (o.max.compareTo(min) < 0 || o.min.compareTo(max) > 0) // o is outside: no intersection
			return Pair.of(Optional.of(copy()), Optional.empty());
		if (o.min.compareTo(min) <= 0) {
			// nothing before
			if (o.max.compareTo(max) >= 0)
				return Pair.of(Optional.empty(), Optional.empty()); // o is fully overlapping this
			return Pair.of(Optional.empty(), Optional.of(new RangeBigInteger(o.max.add(BigInteger.ONE), max)));
		}
		if (o.max.compareTo(max) >= 0) {
			// nothing after
			return Pair.of(Optional.of(new RangeBigInteger(min, o.min.subtract(BigInteger.ONE))), Optional.empty());
		}
		// in the middle
		return Pair.of(Optional.of(new RangeBigInteger(min, o.min.subtract(BigInteger.ONE))), Optional.of(new RangeBigInteger(o.max.add(BigInteger.ONE), max)));
	}
	
	@Override
	public String toString() {
		return "[" + min.toString() + "-" + max.toString() + "]";
	}
	
}
