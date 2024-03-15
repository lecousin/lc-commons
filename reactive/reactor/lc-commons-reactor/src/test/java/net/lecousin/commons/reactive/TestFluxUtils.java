package net.lecousin.commons.reactive;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import reactor.core.publisher.Flux;

class TestFluxUtils {

	public static class ArgsBuffer implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			List<Integer> nbItems = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 50, 100);
			List<Integer> buffer = Arrays.asList(1, 2, 5, 15);
			return nbItems.stream()
				.flatMap(arg1 -> buffer.stream().map(arg2 -> Arguments.of(arg1, arg2)));
		}
	}
	
	@ParameterizedTest(name = "{0} items grouped by {1}")
	@ArgumentsSource(ArgsBuffer.class)
	void testBufferUntil(int nbItems, int buffer) {
		List<Integer> source = new LinkedList<>();
		for (int i = 0; i < nbItems; ++i)
			source.add(i);
		List<List<Integer>> result = FluxUtils.bufferUntil(
			Flux.fromIterable(source),
			0,
			(value, item) -> value + 1,
			value -> value >= buffer
		).collectList().block();
		Iterator<List<Integer>> it = result.iterator();
		for (int i = 0; i < nbItems; i += buffer) {
			assertThat(it.hasNext()).isTrue();
			List<Integer> list = it.next();
			List<Integer> expected = new LinkedList<>();
			for (int j = 0; j < buffer && i + j < nbItems; ++j)
				expected.add(i + j);
			assertThat(list).containsExactlyElementsOf(expected);
		}
		assertThat(it.hasNext()).isFalse();
	}
	
}
