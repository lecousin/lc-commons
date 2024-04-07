package net.lecousin.commons.collections;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

class TestLcCollectionUtils {

	@Test
	void removeFirstMatching() {
		Collection<Integer> col;
		Predicate<Integer> predicate = i -> i.intValue() == 10;

		col = List.of();
		assertThat(LcCollectionUtils.removeFirstMatching(col, predicate)).isEmpty();
		assertThat(col).isEmpty();
		
		col = new LinkedList<>(List.of(1, 5, 10, 20, 30));
		assertThat(LcCollectionUtils.removeFirstMatching(col, predicate)).contains(10);
		assertThat(col).containsExactly(1, 5, 20, 30);
		
		col = new LinkedList<>(List.of(10));
		assertThat(LcCollectionUtils.removeFirstMatching(col, predicate)).contains(10);
		assertThat(col).isEmpty();
		
		col = new LinkedList<>(List.of(1, 5, 10));
		assertThat(LcCollectionUtils.removeFirstMatching(col, predicate)).contains(10);
		assertThat(col).containsExactly(1, 5);
		
		col = new LinkedList<>(List.of(1, 10, 2, 10, 3, 10));
		assertThat(LcCollectionUtils.removeFirstMatching(col, predicate)).contains(10);
		assertThat(col).containsExactly(1, 2, 10, 3, 10);
		assertThat(LcCollectionUtils.removeFirstMatching(col, predicate)).contains(10);
		assertThat(col).containsExactly(1, 2, 3, 10);
		assertThat(LcCollectionUtils.removeFirstMatching(col, predicate)).contains(10);
		assertThat(col).containsExactly(1, 2, 3);
	}
	
}
