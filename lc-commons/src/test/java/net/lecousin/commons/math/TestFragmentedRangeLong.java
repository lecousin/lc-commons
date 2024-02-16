package net.lecousin.commons.math;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class TestFragmentedRangeLong {

	@Test
	void test() throws Exception {
		FragmentedRangeLong f = new FragmentedRangeLong();
		assertEquals(0, f.size());
		assertEquals(Long.MAX_VALUE, f.getMin());
		assertEquals(Long.MIN_VALUE, f.getMax());
		assertNull(f.removeFirstValue());
		assertEquals(0, FragmentedRangeLong.intersect(new FragmentedRangeLong(), new FragmentedRangeLong()).size());
		assertNull(f.removeBiggestRange());
		f.removeRange(10, 20);
		// 12
		f.addValue(12);
		assertEquals(1, f.size());
		assertEquals(0, FragmentedRangeLong.intersect(f, new FragmentedRangeLong()).size());
		assertEquals(0, FragmentedRangeLong.intersect(new FragmentedRangeLong(), f).size());
		assertEquals(12, f.removeFirstValue().longValue());
		assertEquals(0, f.size());
		f.addValue(12);
		assertEquals(1, f.size());
		// 10-15
		f.addRange(new RangeLong(10, 15));
		assertEquals(1, f.size());
		f = new FragmentedRangeLong();
		// 10-15
		f.addRange(new RangeLong(10, 15));
		assertEquals(1, f.size());
		// remove
		assertEquals(new RangeLong(10, 15), f.removeBiggestRange());
		assertEquals(0, f.size());
		// 10-15
		f.addRange(new RangeLong(10, 15));
		assertEquals(1, f.size());
		// 10-20
		f.addRange(new RangeLong(16, 20));
		assertEquals(1, f.size());
		// 10-20, 22-30
		f.addRange(new RangeLong(22, 30));
		assertEquals(2, f.size());
		// 10-30
		f.addValue(21);
		assertEquals(1, f.size());
		// 9-30
		f.addValue(9);
		assertEquals(1, f.size());
		// 9-31
		f.addValue(31);
		assertEquals(1, f.size());
		
		assertFalse(f.containsOneValueIn(new RangeLong(0, 8)));
		assertFalse(f.containsOneValueIn(new RangeLong(35, 50)));
		assertTrue(f.containsOneValueIn(new RangeLong(10, 50)));
		assertTrue(f.containsOneValueIn(new RangeLong(5, 9)));
		assertTrue(f.containsOneValueIn(new RangeLong(31, 50)));

		// 9-31, 100-150, 200-250
		f.addRanges(Arrays.asList(new RangeLong(100, 150), new RangeLong(200, 250)));
		assertEquals(3, f.size());
		// 9-31, 100-150, 175-180, 200-250
		f.addRange(175, 180);
		assertEquals(4, f.size());
		// 9-31, 100-150, 175-180, 190-250
		f.addRange(190, 199);
		assertEquals(4, f.size());
		
		assertEquals(9, f.getMin());
		assertEquals(250, f.getMax());

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
		
		assertTrue(f.containsRange(100, 150));
		assertTrue(f.containsRange(101, 150));
		assertTrue(f.containsRange(110, 120));
		assertFalse(f.containsRange(99, 120));
		assertFalse(f.containsRange(130, 160));
		assertFalse(f.containsRange(300, 400));
		assertTrue(f.containsRange(500, 120));

		assertTrue(f.containsOneValueIn(Arrays.asList(new RangeLong(101, 150), new RangeLong(50, 70))));
		assertTrue(f.containsOneValueIn(Arrays.asList(new RangeLong(50, 70), new RangeLong(101, 150))));
		assertFalse(f.containsOneValueIn(Arrays.asList(new RangeLong(50, 70), new RangeLong(300, 400))));
		
		// 9-31, 100-155, 175-180, 190-250
		f.addRange(151, 155);
		assertEquals(4, f.size());
		// 9-31, 100-155, 157, 175-180, 190-250
		f.addValue(157);
		assertEquals(5, f.size());
		// 10-31, 100-155, 157, 175-180, 190-250
		assertEquals(9, f.removeFirstValue().longValue());
		assertEquals(5, f.size());
		assertFalse(f.containsValue(9));
		assertTrue(f.containsValue(10));
		// 11-31, 100-155, 157, 175-180, 190-250
		f.removeValue(10);
		assertEquals(5, f.size());
		assertFalse(f.containsValue(10));
		assertTrue(f.containsValue(11));
		// 14-31, 100-155, 157, 175-180, 190-250
		f.removeRange(11, 13);
		assertEquals(5, f.size());
		assertFalse(f.containsValue(13));
		assertTrue(f.containsValue(14));
		// 14-31, 100-155, 157, 175-250
		f.addRange(181, 189);
		check(f, new RangeLong(14, 31), new RangeLong(100, 155), new RangeLong(157, 157), new RangeLong(175, 250));
		// 14-31, 100-155, 157-300
		f.addRange(158, 300);
		check(f, new RangeLong(14, 31), new RangeLong(100, 155), new RangeLong(157, 300));
		// 14-31, 100-155, 157-199, 251-300
		f.removeRange(200, 250);
		check(f, new RangeLong(14, 31), new RangeLong(100, 155), new RangeLong(157, 199), new RangeLong(251, 300));
		// 14-31, 100-155, 157-300
		f.addRange(200, 270);
		check(f, new RangeLong(14, 31), new RangeLong(100, 155), new RangeLong(157, 300));
		// 14-31, 100-155, 157-199, 251-300
		f.removeRange(200, 250);
		check(f, new RangeLong(14, 31), new RangeLong(100, 155), new RangeLong(157, 199), new RangeLong(251, 300));
		// 14-31, 100-155, 157-400
		f.addRange(180, 400);
		check(f, new RangeLong(14, 31), new RangeLong(100, 155), new RangeLong(157, 400));
		// 14-31, 100-155, 157-400, 500-500
		f.addValue(500);
		check(f, new RangeLong(14, 31), new RangeLong(100, 155), new RangeLong(157, 400), new RangeLong(500, 500));
		
		f.addRange(159, 162);
		check(f, new RangeLong(14, 31), new RangeLong(100, 155), new RangeLong(157, 400), new RangeLong(500, 500));

		check(FragmentedRangeLong.intersect(f, new FragmentedRangeLong(new RangeLong(90, 450))),
			new RangeLong(100, 155), new RangeLong(157, 400));
		FragmentedRangeLong f2 = new FragmentedRangeLong();
		f2.add(new RangeLong(18, 20));
		f2.add(new RangeLong(200, 300));
		f2.add(new RangeLong(350, 450));
		check(FragmentedRangeLong.intersect(f, f2), new RangeLong(18, 20), new RangeLong(200, 300), new RangeLong(350, 400));
		check(f.copy(), new RangeLong(14, 31), new RangeLong(100, 155), new RangeLong(157, 400), new RangeLong(500, 500));
		
		assertNull(f.removeBestRangeForSize(1000));
		assertEquals(new RangeLong(100, 155), f.removeBestRangeForSize(56));
		assertEquals(new RangeLong(157, 356), f.removeBestRangeForSize(200));
		check(f, new RangeLong(14, 31), new RangeLong(357, 400), new RangeLong(500, 500));
		assertEquals(new RangeLong(357, 400), f.removeBiggestRange());
		check(f, new RangeLong(14, 31), new RangeLong(500, 500));
		assertEquals(19, f.getTotalSize());
		f.addCopy(Arrays.asList(new RangeLong(100, 120), new RangeLong(130, 140)));
		check(f, new RangeLong(14, 31), new RangeLong(100, 120), new RangeLong(130, 140), new RangeLong(500, 500));
		f.toString();
		// so far = 14-31, 100-120, 130-140, 500-500
		// 14-31, 100-117, 130-140, 500-500
		f.removeRange(118, 125);
		check(f, new RangeLong(14, 31), new RangeLong(100, 117), new RangeLong(130, 140), new RangeLong(500, 500));
		// 14-31, 104-117, 130-140, 500-500
		f.removeRange(80, 103);
		check(f, new RangeLong(14, 31), new RangeLong(104, 117), new RangeLong(130, 140), new RangeLong(500, 500));
		// 14-31, 130-140, 500-500
		f.removeRange(80, 117);
		check(f, new RangeLong(14, 31), new RangeLong(130, 140), new RangeLong(500, 500));
		// 14-31, 500-500
		f.removeRange(125, 145);
		check(f, new RangeLong(14, 31), new RangeLong(500, 500));
		
		f = new FragmentedRangeLong();
		f.addRange(10, 20);
		f.addRange(30, 40);
		f.addRange(50, 60);
		f.addRange(70, 80);
		f.addRange(90, 100);
		f.addRange(25, 75);
		check(f, new RangeLong(10, 20), new RangeLong(25, 80), new RangeLong(90, 100));
		f.addRange(24, 85);
		check(f, new RangeLong(10, 20), new RangeLong(24, 85), new RangeLong(90, 100));
		f.addValue(21);
		check(f, new RangeLong(10, 21), new RangeLong(24, 85), new RangeLong(90, 100));
		f.addValue(15);
		check(f, new RangeLong(10, 21), new RangeLong(24, 85), new RangeLong(90, 100));
		f.removeRange(19, 21);
		check(f, new RangeLong(10, 18), new RangeLong(24, 85), new RangeLong(90, 100));
		f.removeRange(10, 18);
		check(f, new RangeLong(24, 85), new RangeLong(90, 100));
		f.removeRange(24, 87);
		check(f, new RangeLong(90, 100));
		
		f = new FragmentedRangeLong();
		f.addRange(10, 19); // 10
		f.addRange(30, 37); // 8
		f.addRange(50, 55); // 6
		f.addRange(60, 63); // 4
		f.addRange(70, 70); // 1
		f.addRange(80, 81); // 2
		f.addRange(90, 96); // 7
		assertEquals(new RangeLong(60, 62), f.removeBestRangeForSize(3));
		assertEquals(new RangeLong(80, 81), f.removeBestRangeForSize(2));
		assertEquals(new RangeLong(10, 18), f.removeBestRangeForSize(9));
		assertThat(f).containsExactly(new RangeLong(19, 19), new RangeLong(30, 37), new RangeLong(50, 55), new RangeLong(63, 63), new RangeLong(70, 70), new RangeLong(90, 96));
	}
	
	private static void check(List<RangeLong> list, RangeLong... expected) {
		assertEquals(expected.length, list.size());
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(expected[i].getMin(), list.get(i).getMin(), "Range " + i + " start");
			assertEquals(expected[i].getMax(), list.get(i).getMax(), "Range " + i + " end");
		}
	}
	
}
