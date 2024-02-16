package net.lecousin.commons.math;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class TestFragmentedRangeBigInteger {

	@Test
	void test() throws Exception {
		FragmentedRangeBigInteger f = new FragmentedRangeBigInteger();
		assertEquals(0, f.size());
		assertEquals(BigInteger.ZERO, f.getMin());
		assertEquals(BigInteger.ZERO, f.getMax());
		assertNull(f.removeFirstValue());
		assertEquals(0, FragmentedRangeBigInteger.intersect(new FragmentedRangeBigInteger(), new FragmentedRangeBigInteger()).size());
		assertNull(f.removeBiggestRange());
		f.removeRange(BigInteger.valueOf(10), BigInteger.valueOf(20));
		// 12
		f.addValue(BigInteger.valueOf(12));
		assertEquals(1, f.size());
		assertEquals(0, FragmentedRangeBigInteger.intersect(f, new FragmentedRangeBigInteger()).size());
		assertEquals(0, FragmentedRangeBigInteger.intersect(new FragmentedRangeBigInteger(), f).size());
		assertEquals(BigInteger.valueOf(12), f.removeFirstValue());
		assertEquals(0, f.size());
		f.addValue(BigInteger.valueOf(12));
		assertEquals(1, f.size());
		// 10-15
		f.addRange(new RangeBigInteger(BigInteger.valueOf(10), BigInteger.valueOf(15)));
		assertEquals(1, f.size());
		f = new FragmentedRangeBigInteger();
		// 10-15
		f.addRange(new RangeBigInteger(BigInteger.valueOf(10), BigInteger.valueOf(15)));
		assertEquals(1, f.size());
		// remove
		assertEquals(new RangeBigInteger(BigInteger.valueOf(10), BigInteger.valueOf(15)), f.removeBiggestRange());
		assertEquals(0, f.size());
		// 10-15
		f.addRange(new RangeBigInteger(BigInteger.valueOf(10), BigInteger.valueOf(15)));
		assertEquals(1, f.size());
		// 10-20
		f.addRange(new RangeBigInteger(BigInteger.valueOf(16), BigInteger.valueOf(20)));
		assertEquals(1, f.size());
		// 10-20, 22-30
		f.addRange(new RangeBigInteger(BigInteger.valueOf(22), BigInteger.valueOf(30)));
		assertEquals(2, f.size());
		// 10-30
		f.addValue(BigInteger.valueOf(21));
		assertEquals(1, f.size());
		// 9-30
		f.addValue(BigInteger.valueOf(9));
		assertEquals(1, f.size());
		// 9-31
		f.addValue(BigInteger.valueOf(31));
		assertEquals(1, f.size());

		// 9-31, 100-150, 200-250
		f.addRanges(Arrays.asList(new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(150)), new RangeBigInteger(BigInteger.valueOf(200), BigInteger.valueOf(250))));
		assertEquals(3, f.size());
		// 9-31, 100-150, 175-180, 200-250
		f.addRange(BigInteger.valueOf(175), BigInteger.valueOf(180));
		assertEquals(4, f.size());
		// 9-31, 100-150, 175-180, 190-250
		f.addRange(BigInteger.valueOf(190), BigInteger.valueOf(199));
		assertEquals(4, f.size());
		
		assertEquals(BigInteger.valueOf(9), f.getMin());
		assertEquals(BigInteger.valueOf(250), f.getMax());

		assertTrue(f.containsValue(9));
		assertTrue(f.containsValue(10));
		assertTrue(f.containsValue(100));
		assertTrue(f.containsValue(110));
		assertTrue(f.containsValue(195));
		assertFalse(f.containsValue(0));
		assertFalse(f.containsValue(8));
		assertFalse(f.containsValue(50));
		assertFalse(f.containsValue(160));
		assertFalse(f.containsValue(300));
		
		assertTrue(f.containsRange(BigInteger.valueOf(100), BigInteger.valueOf(150)));
		assertTrue(f.containsRange(BigInteger.valueOf(101), BigInteger.valueOf(150)));
		assertTrue(f.containsRange(BigInteger.valueOf(110), BigInteger.valueOf(120)));
		assertFalse(f.containsRange(BigInteger.valueOf(99), BigInteger.valueOf(120)));
		assertFalse(f.containsRange(BigInteger.valueOf(130), BigInteger.valueOf(160)));
		assertFalse(f.containsRange(BigInteger.valueOf(300), BigInteger.valueOf(400)));
		assertTrue(f.containsRange(BigInteger.valueOf(500), BigInteger.valueOf(120)));

		assertTrue(f.containsOneValueIn(Arrays.asList(new RangeBigInteger(BigInteger.valueOf(101), BigInteger.valueOf(150)), new RangeBigInteger(BigInteger.valueOf(50), BigInteger.valueOf(70)))));
		assertTrue(f.containsOneValueIn(Arrays.asList(new RangeBigInteger(BigInteger.valueOf(50), BigInteger.valueOf(70)), new RangeBigInteger(BigInteger.valueOf(101), BigInteger.valueOf(150)))));
		assertFalse(f.containsOneValueIn(Arrays.asList(new RangeBigInteger(BigInteger.valueOf(50), BigInteger.valueOf(70)), new RangeBigInteger(BigInteger.valueOf(300), BigInteger.valueOf(400)))));
		
		// 9-31, 100-155, 175-180, 190-250
		f.addRange(BigInteger.valueOf(151), BigInteger.valueOf(155));
		assertEquals(4, f.size());
		// 9-31, 100-155, 157, 175-180, 190-250
		f.addValue(BigInteger.valueOf(157));
		assertEquals(5, f.size());
		// 10-31, 100-155, 157, 175-180, 190-250
		assertEquals(9, f.removeFirstValue().intValue());
		assertEquals(5, f.size());
		assertFalse(f.containsValue(9));
		assertTrue(f.containsValue(10));
		// 11-31, 100-155, 157, 175-180, 190-250
		f.removeValue(BigInteger.valueOf(10));
		assertEquals(5, f.size());
		assertFalse(f.containsValue(10));
		assertTrue(f.containsValue(11));
		// 14-31, 100-155, 157, 175-180, 190-250
		f.removeRange(BigInteger.valueOf(11), BigInteger.valueOf(13));
		assertEquals(5, f.size());
		assertFalse(f.containsValue(13));
		assertTrue(f.containsValue(14));
		// 14-31, 100-155, 157, 175-250
		f.addRange(BigInteger.valueOf(181), BigInteger.valueOf(189));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(155)),
			new RangeBigInteger(BigInteger.valueOf(157), BigInteger.valueOf(157)),
			new RangeBigInteger(BigInteger.valueOf(175), BigInteger.valueOf(250)));
		// 14-31, 100-155, 157-300
		f.addRange(BigInteger.valueOf(158), BigInteger.valueOf(300));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(155)),
			new RangeBigInteger(BigInteger.valueOf(157), BigInteger.valueOf(300)));
		// 14-31, 100-155, 157-199, 251-300
		f.removeRange(BigInteger.valueOf(200), BigInteger.valueOf(250));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(155)),
			new RangeBigInteger(BigInteger.valueOf(157), BigInteger.valueOf(199)),
			new RangeBigInteger(BigInteger.valueOf(251), BigInteger.valueOf(300)));
		// 14-31, 100-155, 157-300
		f.addRange(BigInteger.valueOf(200), BigInteger.valueOf(270));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(155)),
			new RangeBigInteger(BigInteger.valueOf(157), BigInteger.valueOf(300)));
		// 14-31, 100-155, 157-199, 251-300
		f.removeRange(BigInteger.valueOf(200), BigInteger.valueOf(250));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(155)),
			new RangeBigInteger(BigInteger.valueOf(157), BigInteger.valueOf(199)),
			new RangeBigInteger(BigInteger.valueOf(251), BigInteger.valueOf(300)));
		// 14-31, 100-155, 157-400
		f.addRange(BigInteger.valueOf(180), BigInteger.valueOf(400));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(155)),
			new RangeBigInteger(BigInteger.valueOf(157), BigInteger.valueOf(400)));
		// 14-31, 100-155, 157-400, 500-500
		f.addValue(BigInteger.valueOf(500));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(155)),
			new RangeBigInteger(BigInteger.valueOf(157), BigInteger.valueOf(400)),
			new RangeBigInteger(BigInteger.valueOf(500), BigInteger.valueOf(500)));
		
		f.addRange(BigInteger.valueOf(159), BigInteger.valueOf(162));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
				new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(155)),
				new RangeBigInteger(BigInteger.valueOf(157), BigInteger.valueOf(400)),
				new RangeBigInteger(BigInteger.valueOf(500), BigInteger.valueOf(500)));
		
		check(FragmentedRangeBigInteger.intersect(f, new FragmentedRangeBigInteger(new RangeBigInteger(BigInteger.valueOf(90), BigInteger.valueOf(450)))),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(155)),
			new RangeBigInteger(BigInteger.valueOf(157), BigInteger.valueOf(400)));
		FragmentedRangeBigInteger f2 = new FragmentedRangeBigInteger();
		f2.add(new RangeBigInteger(BigInteger.valueOf(18), BigInteger.valueOf(20)));
		f2.add(new RangeBigInteger(BigInteger.valueOf(200), BigInteger.valueOf(300)));
		f2.add(new RangeBigInteger(BigInteger.valueOf(350), BigInteger.valueOf(450)));
		check(FragmentedRangeBigInteger.intersect(f, f2),
			new RangeBigInteger(BigInteger.valueOf(18), BigInteger.valueOf(20)),
			new RangeBigInteger(BigInteger.valueOf(200), BigInteger.valueOf(300)),
			new RangeBigInteger(BigInteger.valueOf(350), BigInteger.valueOf(400)));
		check(f.copy(), new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(155)),
			new RangeBigInteger(BigInteger.valueOf(157), BigInteger.valueOf(400)),
			new RangeBigInteger(BigInteger.valueOf(500), BigInteger.valueOf(500)));
		
		assertNull(f.removeBestRangeForSize(BigInteger.valueOf(1000)));
		assertEquals(new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(155)), f.removeBestRangeForSize(BigInteger.valueOf(56)));
		assertEquals(new RangeBigInteger(BigInteger.valueOf(157), BigInteger.valueOf(356)), f.removeBestRangeForSize(BigInteger.valueOf(200)));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(357), BigInteger.valueOf(400)),
			new RangeBigInteger(BigInteger.valueOf(500), BigInteger.valueOf(500)));
		assertEquals(new RangeBigInteger(BigInteger.valueOf(357), BigInteger.valueOf(400)), f.removeBiggestRange());
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(500), BigInteger.valueOf(500)));
		assertEquals(BigInteger.valueOf(19), f.getTotalSize());
		f.addCopy(Arrays.asList(
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(120)),
			new RangeBigInteger(BigInteger.valueOf(130), BigInteger.valueOf(140))
		));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(120)),
			new RangeBigInteger(BigInteger.valueOf(130), BigInteger.valueOf(140)),
			new RangeBigInteger(BigInteger.valueOf(500), BigInteger.valueOf(500)));
		f.toString();
		// so far = 14-31, 100-120, 130-140, 500-500
		// 14-31, 100-117, 130-140, 500-500
		f.removeRange(BigInteger.valueOf(118), BigInteger.valueOf(125));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(100), BigInteger.valueOf(117)),
			new RangeBigInteger(BigInteger.valueOf(130), BigInteger.valueOf(140)),
			new RangeBigInteger(BigInteger.valueOf(500), BigInteger.valueOf(500)));
		// 14-31, 104-117, 130-140, 500-500
		f.removeRange(BigInteger.valueOf(80), BigInteger.valueOf(103));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(104), BigInteger.valueOf(117)),
			new RangeBigInteger(BigInteger.valueOf(130), BigInteger.valueOf(140)),
			new RangeBigInteger(BigInteger.valueOf(500), BigInteger.valueOf(500)));
		// 14-31, 130-140, 500-500
		f.removeRange(BigInteger.valueOf(80), BigInteger.valueOf(117));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(130), BigInteger.valueOf(140)),
			new RangeBigInteger(BigInteger.valueOf(500), BigInteger.valueOf(500)));
		// 14-31, 500-500
		f.removeRange(BigInteger.valueOf(125), BigInteger.valueOf(145));
		check(f, new RangeBigInteger(BigInteger.valueOf(14), BigInteger.valueOf(31)),
			new RangeBigInteger(BigInteger.valueOf(500), BigInteger.valueOf(500)));
		
		f = new FragmentedRangeBigInteger();
		f.addRange(BigInteger.valueOf(10), BigInteger.valueOf(20));
		f.addRange(BigInteger.valueOf(30), BigInteger.valueOf(40));
		f.addRange(BigInteger.valueOf(50), BigInteger.valueOf(60));
		f.addRange(BigInteger.valueOf(70), BigInteger.valueOf(80));
		f.addRange(BigInteger.valueOf(90), BigInteger.valueOf(100));
		f.addRange(BigInteger.valueOf(25), BigInteger.valueOf(75));
		check(f, new RangeBigInteger(BigInteger.valueOf(10), BigInteger.valueOf(20)), new RangeBigInteger(BigInteger.valueOf(25), BigInteger.valueOf(80)), new RangeBigInteger(BigInteger.valueOf(90), BigInteger.valueOf(100)));
		f.addRange(BigInteger.valueOf(24), BigInteger.valueOf(85));
		check(f, new RangeBigInteger(BigInteger.valueOf(10), BigInteger.valueOf(20)), new RangeBigInteger(BigInteger.valueOf(24), BigInteger.valueOf(85)), new RangeBigInteger(BigInteger.valueOf(90), BigInteger.valueOf(100)));
		f.addValue(BigInteger.valueOf(21));
		check(f, new RangeBigInteger(BigInteger.valueOf(10), BigInteger.valueOf(21)), new RangeBigInteger(BigInteger.valueOf(24), BigInteger.valueOf(85)), new RangeBigInteger(BigInteger.valueOf(90), BigInteger.valueOf(100)));
		f.addValue(BigInteger.valueOf(15));
		check(f, new RangeBigInteger(BigInteger.valueOf(10), BigInteger.valueOf(21)), new RangeBigInteger(BigInteger.valueOf(24), BigInteger.valueOf(85)), new RangeBigInteger(BigInteger.valueOf(90), BigInteger.valueOf(100)));
		f.removeRange(BigInteger.valueOf(19), BigInteger.valueOf(21));
		check(f, new RangeBigInteger(BigInteger.valueOf(10), BigInteger.valueOf(18)), new RangeBigInteger(BigInteger.valueOf(24), BigInteger.valueOf(85)), new RangeBigInteger(BigInteger.valueOf(90), BigInteger.valueOf(100)));
		f.removeRange(BigInteger.valueOf(10), BigInteger.valueOf(18));
		check(f, new RangeBigInteger(BigInteger.valueOf(24), BigInteger.valueOf(85)), new RangeBigInteger(BigInteger.valueOf(90), BigInteger.valueOf(100)));
		f.removeRange(BigInteger.valueOf(24), BigInteger.valueOf(87));
		check(f, new RangeBigInteger(BigInteger.valueOf(90), BigInteger.valueOf(100)));
		
		f = new FragmentedRangeBigInteger();
		f.addRange(BigInteger.valueOf(10), BigInteger.valueOf(19)); // 10
		f.addRange(BigInteger.valueOf(30), BigInteger.valueOf(37)); // 8
		f.addRange(BigInteger.valueOf(50), BigInteger.valueOf(55)); // 6
		f.addRange(BigInteger.valueOf(60), BigInteger.valueOf(63)); // 4
		f.addRange(BigInteger.valueOf(70), BigInteger.valueOf(70)); // 1
		f.addRange(BigInteger.valueOf(80), BigInteger.valueOf(81)); // 2
		f.addRange(BigInteger.valueOf(90), BigInteger.valueOf(96)); // 7
		assertEquals(new RangeBigInteger(BigInteger.valueOf(60), BigInteger.valueOf(62)), f.removeBestRangeForSize(BigInteger.valueOf(3)));
		assertEquals(new RangeBigInteger(BigInteger.valueOf(80), BigInteger.valueOf(81)), f.removeBestRangeForSize(BigInteger.valueOf(2)));
		assertEquals(new RangeBigInteger(BigInteger.valueOf(10), BigInteger.valueOf(18)), f.removeBestRangeForSize(BigInteger.valueOf(9)));
		assertThat(f).containsExactly(
			new RangeBigInteger(BigInteger.valueOf(19), BigInteger.valueOf(19)),
			new RangeBigInteger(BigInteger.valueOf(30), BigInteger.valueOf(37)),
			new RangeBigInteger(BigInteger.valueOf(50), BigInteger.valueOf(55)),
			new RangeBigInteger(BigInteger.valueOf(63), BigInteger.valueOf(63)),
			new RangeBigInteger(BigInteger.valueOf(70), BigInteger.valueOf(70)),
			new RangeBigInteger(BigInteger.valueOf(90), BigInteger.valueOf(96)));
	}
	
	private static void check(List<RangeBigInteger> list, RangeBigInteger... expected) {
		assertEquals(expected.length, list.size());
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(expected[i].getMin(), list.get(i).getMin(), "Range " + i + " start");
			assertEquals(expected[i].getMax(), list.get(i).getMax(), "Range " + i + " end");
		}
	}
	
}
