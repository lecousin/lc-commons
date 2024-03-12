package net.lecousin.commons.io.text;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.io.chars.CharsIO;
import net.lecousin.commons.io.chars.memory.CharArray;
import net.lecousin.commons.io.chars.utils.CompositeCharsIO;
import net.lecousin.commons.test.ParameterizedTestUtils;
import net.lecousin.commons.test.ParameterizedTestUtils.CompositeArgumentsProvider;
import net.lecousin.commons.test.TestCase;

public abstract class AbstractTextParserTest<T> {
	
	protected abstract List<TestCase.NoInput<TextParser<T>>> getParserTestCases();
	protected abstract List<Pair<String, Consumer<T>>> getInputTestCases();

	public static class TextParserArgumentsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			AbstractTextParserTest<?> instance = ParameterizedTestUtils.createTestInstance(context);
			return instance.getParserTestCases().stream()
				.flatMap(parser -> instance.getInputTestCases().stream()
					.map(input -> Arguments.of(
						parser.getName() + ", input = " + input.getLeft(),
						(Supplier<TextParser<?>>) () -> parser.getArgument(),
						input.getLeft(),
						input.getRight()
					))
				);
		}
	}
	
	public static class ArgumentsWithCharset extends CompositeArgumentsProvider {
		public ArgumentsWithCharset() {
			super(new TextParserArgumentsProvider());
			add(
				List.of(StandardCharsets.UTF_8, StandardCharsets.UTF_16LE, StandardCharsets.UTF_16BE).stream()
				.map(cs -> Arguments.of(", charset " + cs.displayName(), cs))
			);
		}
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(ArgumentsWithCharset.class)
	@SuppressWarnings("java:S2699")
	void testWithInputStream(String displayName, Supplier<TextParser<T>> parser, String input, Consumer<T> checker, Charset charset) throws IOException {
		checker.accept(parser.get().parse(new ByteArrayInputStream(input.getBytes(charset)), charset, false));
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(TextParserArgumentsProvider.class)
	@SuppressWarnings("java:S2699")
	void testWithCompositeIOWithSmallParts(String displayName, Supplier<TextParser<T>> parser, String input, Consumer<T> checker) throws IOException {
		char[] chars = input.toCharArray();
		List<CharsIO.Readable> ios = new LinkedList<>();
		for (int i = 0; i < chars.length; ++i)
			ios.add(new CharArray(chars, i, 1).asCharsIO());
		CharsIO.Readable io = CompositeCharsIO.fromReadable(ios, true, true);
		checker.accept(parser.get().parse(io));
	}
	
}
