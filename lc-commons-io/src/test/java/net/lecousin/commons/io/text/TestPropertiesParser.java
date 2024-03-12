package net.lecousin.commons.io.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import net.lecousin.commons.io.text.PropertiesParser.Property;
import net.lecousin.commons.test.TestCase.NoInput;

public class TestPropertiesParser extends AbstractTextParserTest<List<Property<String>>> {

	@Override
	protected List<NoInput<TextParser<List<Property<String>>>>> getParserTestCases() {
		return List.of(new NoInput<>("Simple", () -> (TextParser<List<Property<String>>>) PropertiesParser.simple()));
	}
	
	@Override
	protected List<Pair<String, Consumer<List<Property<String>>>>> getInputTestCases() {
		return List.of(
			Pair.of("", props -> assertThat(props).isEmpty()),
			Pair.of("abc", props ->
				assertThat(props).hasSize(1)
				.anyMatch(p -> "abc".equals(p.getName()) && "".equals(p.getValue()))
			),
			Pair.of("abc=def", props ->
				assertThat(props).hasSize(1)
				.anyMatch(p -> "abc".equals(p.getName()) && "def".equals(p.getValue()))
			),
			Pair.of("abc=def\n# comment\na\\=b=c\\\\\\d\n", props ->
				assertThat(props).hasSize(2)
				.anyMatch(p -> "abc".equals(p.getName()) && "def".equals(p.getValue()))
				.anyMatch(p -> "a=b".equals(p.getName()) && "c\\d".equals(p.getValue()))
			),
			Pair.of("# comment\r\n\r\nabc=def\r\n\n# comment\n\n=\r\n\n\r\n", props ->
				assertThat(props).hasSize(2)
				.anyMatch(p -> "abc".equals(p.getName()) && "def".equals(p.getValue()))
				.anyMatch(p -> "".equals(p.getName()) && "".equals(p.getValue()))
			),
			Pair.of("abc=def\nzz\r\n", props ->
				assertThat(props).hasSize(2)
				.anyMatch(p -> "abc".equals(p.getName()) && "def".equals(p.getValue()))
				.anyMatch(p -> "zz".equals(p.getName()) && "".equals(p.getValue()))
			)
		);
	}
	
}
