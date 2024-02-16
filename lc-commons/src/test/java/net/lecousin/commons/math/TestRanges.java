package net.lecousin.commons.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

class TestRanges {

	@Test
	@SuppressWarnings("java:S5785")
	void testRangeInteger() throws Exception {
		RangeInteger r1 = new RangeInteger(10, 20);
		RangeInteger r2 = new RangeInteger(15, 25);
		
		assertEquals(10, r1.getMin());
		assertEquals(20, r1.getMax());

		// copy
		RangeInteger cr1 = new RangeInteger(r1);
		assertEquals(10, cr1.getMin());
		assertEquals(20, cr1.getMax());
		cr1 = r1.copy();
		assertEquals(10, cr1.getMin());
		assertEquals(20, cr1.getMax());
		
		// equals
		assertTrue(r1.equals(r1));
		assertTrue(r1.equals(cr1));
		assertFalse(r1.equals(r2));
		assertFalse(r1.equals(new RangeInteger(10, 21)));
		assertFalse(r1.equals(new RangeInteger(11, 20)));
		assertFalse(r1.equals(new Object()));
		assertFalse(r1.equals(null));
		
		// hashCode
		assertEquals(r1.hashCode(), cr1.hashCode());
		
		// contains
		for (int i = 0; i < 50; ++i)
			if (i <10 || i > 20)
				assertFalse(r1.contains(i), Integer.toString(i));
			else
				assertTrue(r1.contains(i), Integer.toString(i));
		
		// intersect
		RangeInteger inter = r1.intersect(r2);
		assertEquals(15, inter.getMin());
		assertEquals(20, inter.getMax());
		inter = r2.intersect(r1);
		assertEquals(15, inter.getMin());
		assertEquals(20, inter.getMax());
		inter = r1.intersect(cr1);
		assertEquals(10, inter.getMin());
		assertEquals(20, inter.getMax());
		inter = r1.intersect(new RangeInteger(100, 200));
		assertNull(inter);
		inter = r1.intersect(new RangeInteger(3, 5));
		assertNull(inter);
		
		// removeIntersect
		assertEquals(
			Pair.of(Optional.empty(), Optional.of(new RangeInteger(151, 200))),
			new RangeInteger(100, 200).removeIntersect(new RangeInteger(50, 150)));
		assertEquals(
			Pair.of(Optional.of(new RangeInteger(100, 149)), Optional.empty()),
			new RangeInteger(100, 200).removeIntersect(new RangeInteger(150, 220)));
		assertEquals(
			Pair.of(Optional.of(new RangeInteger(100, 200)), Optional.empty()),
			new RangeInteger(100, 200).removeIntersect(new RangeInteger(50, 75)));
		assertEquals(
			Pair.of(Optional.of(new RangeInteger(100, 200)), Optional.empty()),
			new RangeInteger(100, 200).removeIntersect(new RangeInteger(300, 400)));
		assertEquals(
			Pair.of(Optional.of(new RangeInteger(100, 124)), Optional.of(new RangeInteger(151, 200))),
			new RangeInteger(100, 200).removeIntersect(new RangeInteger(125, 150)));
		assertEquals(
			Pair.of(Optional.empty(), Optional.of(new RangeInteger(151, 200))),
			new RangeInteger(100, 200).removeIntersect(new RangeInteger(100, 150)));
		assertEquals(
			Pair.of(Optional.of(new RangeInteger(100, 149)), Optional.empty()),
			new RangeInteger(100, 200).removeIntersect(new RangeInteger(150, 200)));
		assertEquals(
			Pair.of(Optional.empty(), Optional.empty()),
			new RangeInteger(100, 200).removeIntersect(new RangeInteger(100, 200)));
		
		// length
		assertEquals(11, r1.getLength());
		assertEquals(11, r2.getLength());
		
		// toString
		assertEquals("[10-20]", r1.toString());
	}

	@Test
	@SuppressWarnings("java:S5785")
	void testRangeLong() throws Exception {
		RangeLong r1 = new RangeLong(10, 20);
		RangeLong r2 = new RangeLong(15, 25);
		
		assertEquals(10, r1.getMin());
		assertEquals(20, r1.getMax());

		// copy
		RangeLong cr1 = new RangeLong(r1);
		assertEquals(10, cr1.getMin());
		assertEquals(20, cr1.getMax());
		cr1 = r1.copy();
		assertEquals(10, cr1.getMin());
		assertEquals(20, cr1.getMax());
		
		// equals
		assertTrue(r1.equals(r1));
		assertTrue(r1.equals(cr1));
		assertFalse(r1.equals(new RangeLong(10, 21)));
		assertFalse(r1.equals(new RangeLong(11, 20)));
		assertFalse(r1.equals(r2));
		assertFalse(r1.equals(new Object()));
		assertFalse(r1.equals(null));
		
		// hashCode
		assertEquals(r1.hashCode(), cr1.hashCode());
		
		// contains
		for (int i = 0; i < 50; ++i)
			if (i <10 || i > 20)
				assertFalse(r1.contains(i), Integer.toString(i));
			else
				assertTrue(r1.contains(i), Integer.toString(i));
		
		// intersect
		RangeLong inter = r1.intersect(r2);
		assertEquals(15, inter.getMin());
		assertEquals(20, inter.getMax());
		inter = r2.intersect(r1);
		assertEquals(15, inter.getMin());
		assertEquals(20, inter.getMax());
		inter = r1.intersect(cr1);
		assertEquals(10, inter.getMin());
		assertEquals(20, inter.getMax());
		inter = r1.intersect(new RangeLong(100, 200));
		assertNull(inter);
		inter = r1.intersect(new RangeLong(3, 5));
		assertNull(inter);
		
		// removeIntersect
		assertEquals(
			Pair.of(Optional.empty(), Optional.of(new RangeLong(151, 200))),
			new RangeLong(100, 200).removeIntersect(new RangeLong(50, 150)));
		assertEquals(
			Pair.of(Optional.of(new RangeLong(100, 149)), Optional.empty()),
			new RangeLong(100, 200).removeIntersect(new RangeLong(150, 220)));
		assertEquals(
			Pair.of(Optional.of(new RangeLong(100, 200)), Optional.empty()),
			new RangeLong(100, 200).removeIntersect(new RangeLong(50, 75)));
		assertEquals(
			Pair.of(Optional.of(new RangeLong(100, 200)), Optional.empty()),
			new RangeLong(100, 200).removeIntersect(new RangeLong(300, 400)));
		assertEquals(
			Pair.of(Optional.of(new RangeLong(100, 124)), Optional.of(new RangeLong(151, 200))),
			new RangeLong(100, 200).removeIntersect(new RangeLong(125, 150)));
		assertEquals(
			Pair.of(Optional.empty(), Optional.of(new RangeLong(151, 200))),
			new RangeLong(100, 200).removeIntersect(new RangeLong(100, 150)));
		assertEquals(
			Pair.of(Optional.of(new RangeLong(100, 149)), Optional.empty()),
			new RangeLong(100, 200).removeIntersect(new RangeLong(150, 200)));
		assertEquals(
			Pair.of(Optional.empty(), Optional.empty()),
			new RangeLong(100, 200).removeIntersect(new RangeLong(100, 200)));
		
		// length
		assertEquals(11, r1.getLength());
		assertEquals(11, r2.getLength());
		
		// toString
		assertEquals("[10-20]", r1.toString());
	}

	@Test
	@SuppressWarnings("java:S5785")
	void testRangeBigInteger() throws Exception {
		RangeBigInteger r1 = new RangeBigInteger(BigInteger.valueOf(10), BigInteger.valueOf(20));
		RangeBigInteger r2 = new RangeBigInteger(BigInteger.valueOf(15), BigInteger.valueOf(25));
		
		assertEquals(10, r1.getMin().longValue());
		assertEquals(20, r1.getMax().longValue());

		// copy
		RangeBigInteger cr1 = new RangeBigInteger(r1);
		assertEquals(10, cr1.getMin().longValue());
		assertEquals(20, cr1.getMax().longValue());
		cr1 = r1.copy();
		assertEquals(10, cr1.getMin().longValue());
		assertEquals(20, cr1.getMax().longValue());
		
		// equals
		assertTrue(r1.equals(r1));
		assertTrue(r1.equals(cr1));
		assertFalse(r1.equals(new RangeBigInteger(BigInteger.valueOf(10), BigInteger.valueOf(21))));
		assertFalse(r1.equals(new RangeBigInteger(BigInteger.valueOf(11), BigInteger.valueOf(20))));
		assertFalse(r1.equals(r2));
		assertFalse(r1.equals(new Object()));
		assertFalse(r1.equals(null));
		
		// hashCode
		assertEquals(r1.hashCode(), cr1.hashCode());
		
		// contains
		for (int i = 0; i < 50; ++i)
			if (i <10 || i > 20)
				assertFalse(r1.contains(BigInteger.valueOf(i)), Integer.toString(i));
			else
				assertTrue(r1.contains(BigInteger.valueOf(i)), Integer.toString(i));
		
		// intersect
		RangeBigInteger inter = r1.intersect(r2);
		assertEquals(15, inter.getMin().longValue());
		assertEquals(20, inter.getMax().longValue());
		inter = r2.intersect(r1);
		assertEquals(15, inter.getMin().longValue());
		assertEquals(20, inter.getMax().longValue());
		inter = r1.intersect(cr1);
		assertEquals(10, inter.getMin().longValue());
		assertEquals(20, inter.getMax().longValue());
		inter = r1.intersect(new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(200)));
		assertNull(inter);
		inter = r1.intersect(new RangeBigInteger(BigInteger.valueOf(3), BigInteger.valueOf(5)));
		assertNull(inter);
		
		// removeIntersect
		assertEquals(
			Pair.of(Optional.empty(), Optional.of(new RangeBigInteger(BigInteger.valueOf(151), BigInteger.valueOf(200)))),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(200)).removeIntersect(new RangeBigInteger(BigInteger.valueOf(50), BigInteger.valueOf(150))));
		assertEquals(
			Pair.of(Optional.of(new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(149))), Optional.empty()),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(200)).removeIntersect(new RangeBigInteger(BigInteger.valueOf(150), BigInteger.valueOf(220))));
		assertEquals(
			Pair.of(Optional.of(new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(200))), Optional.empty()),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(200)).removeIntersect(new RangeBigInteger(BigInteger.valueOf(50), BigInteger.valueOf(75))));
		assertEquals(
			Pair.of(Optional.of(new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(200))), Optional.empty()),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(200)).removeIntersect(new RangeBigInteger(BigInteger.valueOf(300), BigInteger.valueOf(400))));
		assertEquals(
			Pair.of(Optional.of(new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(124))), Optional.of(new RangeBigInteger(BigInteger.valueOf(151), BigInteger.valueOf(200)))),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(200)).removeIntersect(new RangeBigInteger(BigInteger.valueOf(125), BigInteger.valueOf(150))));
		assertEquals(
			Pair.of(Optional.empty(), Optional.of(new RangeBigInteger(BigInteger.valueOf(151), BigInteger.valueOf(200)))),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(200)).removeIntersect(new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(150))));
		assertEquals(
			Pair.of(Optional.of(new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(149))), Optional.empty()),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(200)).removeIntersect(new RangeBigInteger(BigInteger.valueOf(150), BigInteger.valueOf(200))));
		assertEquals(
			Pair.of(Optional.empty(), Optional.empty()),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(200)).removeIntersect(new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(200))));
		
		// length
		assertEquals(11, r1.getLength().longValue());
		assertEquals(11, r2.getLength().longValue());
		
		// toString
		assertEquals("[10-20]", r1.toString());
	}
	
}
