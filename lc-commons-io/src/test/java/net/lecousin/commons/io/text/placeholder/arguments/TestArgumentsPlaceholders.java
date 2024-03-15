package net.lecousin.commons.io.text.placeholder.arguments;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import net.lecousin.commons.io.text.AbstractTextParserTest;
import net.lecousin.commons.io.text.TextParser;
import net.lecousin.commons.io.text.placeholder.PlaceholderElement;
import net.lecousin.commons.io.text.placeholder.PlaceholderTextParser;
import net.lecousin.commons.test.TestCase.NoInput;

public class TestArgumentsPlaceholders extends AbstractTextParserTest<List<PlaceholderElement<? super List<Object>>>> {

	@Override
	protected List<NoInput<TextParser<List<PlaceholderElement<? super List<Object>>>>>> getParserTestCases() {
		return List.of(
			new NoInput<>("PlaceholderTextParser", () -> new PlaceholderTextParser<>("{{", "}}", ArgumentsPlaceholderFactory.INSTANCE))
		);
	}
	
	@Override
	protected List<Pair<String, Consumer<List<PlaceholderElement<? super List<Object>>>>>> getInputTestCases() {
		return List.of(
			testCase("abc", List.of(1, 2, 3), "abc"),
			testCase("ab{{1}}cd{{2}}ef", List.of(10, 20), "ab10cd20ef"),
			testCase("ab{{0}}cd{{1}}ef{{2}}gh", List.of(9), "abcd9efgh"),
			testCase("ab {\\{1}} cd \\{{1}} ef {{1\\}}hello}} gh {{1}\\} ij}} kl", List.of(9), "ab {{1}} cd {{1}} ef  gh  kl"),
			testCase("{{1}}", List.of("hello"), "hello"),
			testCase("}}1{{", List.of(9), "}}1{{"),
			testCase("ab{{1}}\r", List.of(9), "ab9"),
			testCase("{", List.of(9), "{"),
			testCase("}", List.of(9), "}"),
			testCase("hello \\{{1}} {{1}}.", List.of(9), "hello {{1}} 9."),
			
			testCase("{{if:{{1}}={{2}};yes;no}}", List.of(10, 20), "no"),
			testCase("{{if:{{1}}={{2}};yes;no}}", List.of(10, 10), "yes"),
			testCase("{{if:{{1}}={{2}};yes;no}}", List.of("ab", "cd"), "no"),
			testCase("{{if:{{1}}={{2}};yes;no}}", List.of("ab", "ab"), "yes"),
			
			testCase("{{if:{{1}}!={{2}};yes;no}}", List.of(10, 20), "yes"),
			testCase("{{if:{{1}}!={{2}};yes;no}}", List.of(10, 10), "no"),
			testCase("{{if:{{1}}!={{2}};yes;no}}", List.of("ab", "cd"), "yes"),
			testCase("{{if:{{1}}!={{2}};yes;no}}", List.of("ab", "ab"), "no"),
			
			testCase("{{if:{{1}}<{{2}};yes;no}}", List.of(10, 20), "yes"),
			testCase("{{if:{{1}}<{{2}};yes;no}}", List.of(10, 10), "no"),
			testCase("{{if:{{1}}<{{2}};yes;no}}", List.of(20, 10), "no"),
			testCase("{{if:{{1}}<{{2}};yes;no}}", List.of("ab", "cd"), "yes"),
			testCase("{{if:{{1}}<{{2}};yes;no}}", List.of("ab", "ab"), "no"),
			testCase("{{if:{{1}}<{{2}};yes;no}}", List.of("cd", "ab"), "no"),
			
			testCase("{{if:{{1}}<={{2}};yes;no}}", List.of(10, 20), "yes"),
			testCase("{{if:{{1}}<={{2}};yes;no}}", List.of(10, 10), "yes"),
			testCase("{{if:{{1}}<={{2}};yes;no}}", List.of(20, 10), "no"),
			testCase("{{if:{{1}}<={{2}};yes;no}}", List.of("ab", "cd"), "yes"),
			testCase("{{if:{{1}}<={{2}};yes;no}}", List.of("ab", "ab"), "yes"),
			testCase("{{if:{{1}}<={{2}};yes;no}}", List.of("cd", "ab"), "no"),
			
			testCase("{{if:{{1}}>{{2}};yes;no}}", List.of(10, 20), "no"),
			testCase("{{if:{{1}}>{{2}};yes;no}}", List.of(10, 10), "no"),
			testCase("{{if:{{1}}>{{2}};yes;no}}", List.of(20, 10), "yes"),
			testCase("{{if:{{1}}>{{2}};yes;no}}", List.of("ab", "cd"), "no"),
			testCase("{{if:{{1}}>{{2}};yes;no}}", List.of("ab", "ab"), "no"),
			testCase("{{if:{{1}}>{{2}};yes;no}}", List.of("cd", "ab"), "yes"),
			
			testCase("{{if:{{1}}>={{2}};yes;no}}", List.of(10, 20), "no"),
			testCase("{{if:{{1}}>={{2}};yes;no}}", List.of(10, 10), "yes"),
			testCase("{{if:{{1}}>={{2}};yes;no}}", List.of(20, 10), "yes"),
			testCase("{{if:{{1}}>={{2}};yes;no}}", List.of("ab", "cd"), "no"),
			testCase("{{if:{{1}}>={{2}};yes;no}}", List.of("ab", "ab"), "yes"),
			testCase("{{if:{{1}}>={{2}};yes;no}}", List.of("cd", "ab"), "yes"),
			
			testCase("{{if:{{1}};yes;no}}", List.of("true"), "yes"),
			testCase("{{if:{{1}};yes;no}}", List.of("false"), "no"),
			testCase("{{if:{{1}};yes;no}}", List.of("yes"), "yes"),
			testCase("{{if:{{1}};yes;no}}", List.of("no"), "no"),
			testCase("{{if:{{1}};yes;no}}", List.of("1"), "yes"),
			testCase("{{if:{{1}};yes;no}}", List.of("0"), "no"),
			testCase("{{if:{{1}};yes;no}}", List.of(""), "no"),
			testCase("{{if:{{1}};yes}}", List.of("true"), "yes"),
			testCase("{{if:{{1}}}}", List.of("true"), ""),
			
			testCase("{{if:{{1}};{{2}};{{3}}}}", List.of("true", 7, 9), "7"),
			testCase("{{if:{{1}};{{2}};{{3}}}}", List.of("false", 7, 9), "9"),
			
			testCase("{{if:{{1}};yes={{2}}!;no={{3}}?}}", List.of("true", 7, 9), "yes=7!"),
			testCase("{{if:{{1}};yes={{2}}!;no={{3}}?}}", List.of("false", 7, 9), "no=9?"),
			
			testCase("{{if:12<37;ok;wrong}}", List.of(10, 1), "ok"),
			testCase("{{if:12<37;;}}", List.of(10, 1), ""),
			testCase("{{if:12<37;hey {{wrong:invalid}};no}}", List.of(10, 1), "hey "),
			testCase("{{if:12<37;hey {{wrong:invalid}};}}", List.of(10, 1), "hey "),
			testCase("{{if:12<37;hey {{wrong:invalid}} {{wrong:invalid}} {{wrong:invalid}};}}", List.of(10, 1), "hey   "),
			
			testCase("hello {{unknown:wrong}} world", List.of(9), "hello  world"),
			
			testCase("{{}}", List.of(9), ""),
			testCase("{{{{1}}}}", List.of(9), ""),
			testCase("{{1{{2}}}}", List.of(9), "")
		);
	}

	private Pair<String, Consumer<List<PlaceholderElement<? super List<Object>>>>> testCase(String input, List<Object> arguments, String expected) {
		return Pair.of(
			input,
			placeholders -> {
				assertThat(PlaceholderElement.resolveList(placeholders, arguments)).isEqualTo(expected);
				placeholders.toString();
			}
		);
	}
}
