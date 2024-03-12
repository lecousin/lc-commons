package net.lecousin.commons.io.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import net.lecousin.commons.test.TestCase.NoInput;

public class TestEscapeStringParser extends AbstractTextParserTest<String> {

	@Override
	protected List<NoInput<TextParser<String>>> getParserTestCases() {
		return List.of(new NoInput<>("EscapeStringParser", () -> new EscapeStringParser()));
	}
	
	@Override
	protected List<Pair<String, Consumer<String>>> getInputTestCases() {
		return List.of(
			Pair.of("abcd", s -> assertThat(s).isEqualTo("abcd")),
			Pair.of("a\\bcd", s -> assertThat(s).isEqualTo("abcd")),
			Pair.of("a\\\\bcd", s -> assertThat(s).isEqualTo("a\\bcd")),
			Pair.of("abcd\\", s -> assertThat(s).isEqualTo("abcd\\")),
			Pair.of("\\zabcd", s -> assertThat(s).isEqualTo("zabcd"))
		);
	}
	
}
