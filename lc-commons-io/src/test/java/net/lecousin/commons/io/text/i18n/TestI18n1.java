package net.lecousin.commons.io.text.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

class TestI18n1 {

	@Test
	void test1() throws Exception {
		CompletableFuture<String> s1 = new TranslatedString("test1", "key1").localizeAsync(Locale.US);
		CompletableFuture<String> s2 = new TranslatedString("test1", "key2").localizeAsync(Locale.US);
		assertThat(s1.get()).isEqualTo("value1");
		assertThat(s2.get()).isEqualTo("value2");
		StringBuilder s = new StringBuilder();
		new TranslatedString("test1", "key1").localize(Locale.US, s);
		assertThat(s.toString()).isEqualTo("value1");
		
		assertThat(new TranslatedString("test1", "key1").localize(Locale.US)).isEqualTo("value1");
		assertThat(new TranslatedString("test1", "key1").localize(Locale.FRENCH)).isEqualTo("value1");
		assertThat(new TranslatedString("test1", "key2").localize(Locale.US)).isEqualTo("value2");
		assertThat(new TranslatedString("test1", "key3", 1L).localize(Locale.US)).isEqualTo("the value is <1>");
		assertThat(new TranslatedString("test1", "key3", new NumberI18nString(123456)).localize(Locale.US)).isEqualTo("the value is <123,456>");
		assertThat(new TranslatedString("test1", "key3", "abc").localize(Locale.US)).isEqualTo("the value is <abc>");
		assertThat(new TranslatedString("test1", "key3").localize(Locale.US)).isEqualTo("the value is <>");
		assertThat(new TranslatedString("test1", "key4", 1L).localize(Locale.US)).isEqualTo("hello {{1}} 1.");
		assertThat(new TranslatedString("test1", "key4", "abc").localize(Locale.US)).isEqualTo("hello {{1}} abc.");
		assertThat(new TranslatedString("test1", "key=5").localize(Locale.US)).isEqualTo("value5");
		assertThat(new TranslatedString("test1", "iamempty").localize(Locale.US)).isEqualTo("");
		assertThat(new TranslatedString("test1", "key6", 12, "hello").localize(Locale.US)).isEqualTo("12hello");
		assertThat(new TranslatedString("test1", "key7").localize(Locale.US)).isEqualTo("}}");
		assertThat(new TranslatedString("test1", "key8").localize(Locale.US)).isEqualTo("{{");
		assertThat(new TranslatedString("test1", "key9").localize(Locale.US)).isEqualTo("}");
		assertThat(new TranslatedString("test1", "key10").localize(Locale.US)).isEqualTo("{");
		assertThat(new TranslatedString("test1", "key11").localize(Locale.US)).isEqualTo("{{toto");

		assertThat(new TranslatedString("test1", "test_less", 10, 20).localize(Locale.US)).isEqualTo("using 10 and 20 the result is less");
		assertThat(new TranslatedString("test1", "test_less", 20, 10).localize(Locale.US)).isEqualTo("using 20 and 10 the result is not less");
		assertThat(new TranslatedString("test1", "test_less", 10, 10).localize(Locale.US)).isEqualTo("using 10 and 10 the result is not less");

		assertThat(new TranslatedString("test1", "test_less_or_equals", 10, 20).localize(Locale.US)).isEqualTo("using 10 and 20 the result is less or equals");
		assertThat(new TranslatedString("test1", "test_less_or_equals", 20, 10).localize(Locale.US)).isEqualTo("using 20 and 10 the result is not less or equals");
		assertThat(new TranslatedString("test1", "test_less_or_equals", 10, 10).localize(Locale.US)).isEqualTo("using 10 and 10 the result is less or equals");
		
		assertThat(new TranslatedString("test1", "test_greater", 10, 20).localize(Locale.US)).isEqualTo("using 10 and 20 the result is not greater");
		assertThat(new TranslatedString("test1", "test_greater", 20, 10).localize(Locale.US)).isEqualTo("using 20 and 10 the result is greater");
		assertThat(new TranslatedString("test1", "test_greater", 10, 10).localize(Locale.US)).isEqualTo("using 10 and 10 the result is not greater");
		
		assertThat(new TranslatedString("test1", "test_greater_or_equals", 10, 20).localize(Locale.US)).isEqualTo("using 10 and 20 the result is not greater or equals");
		assertThat(new TranslatedString("test1", "test_greater_or_equals", 20, 10).localize(Locale.US)).isEqualTo("using 20 and 10 the result is greater or equals");
		assertThat(new TranslatedString("test1", "test_greater_or_equals", 10, 10).localize(Locale.US)).isEqualTo("using 10 and 10 the result is greater or equals");
		
		assertThat(new TranslatedString("test1", "test_equals", 10, 20).localize(Locale.US)).isEqualTo("using 10 and 20 the result is not equals");
		assertThat(new TranslatedString("test1", "test_equals", 20, 10).localize(Locale.US)).isEqualTo("using 20 and 10 the result is not equals");
		assertThat(new TranslatedString("test1", "test_equals", 10, 10).localize(Locale.US)).isEqualTo("using 10 and 10 the result is equals");
		
		assertThat(new TranslatedString("test1", "test_not_equals", 10, 20).localize(Locale.US)).isEqualTo("using 10 and 20 the result is not equals");
		assertThat(new TranslatedString("test1", "test_not_equals", 20, 10).localize(Locale.US)).isEqualTo("using 20 and 10 the result is not equals");
		assertThat(new TranslatedString("test1", "test_not_equals", 10, 10).localize(Locale.US)).isEqualTo("using 10 and 10 the result is equals");

		assertThat(new TranslatedString("test1", "unknown", 10, 10).localize(Locale.US)).isEqualTo("[test1#unknown]");
	}
	
	@Test
	void testNumber() throws Exception {
		NumberI18nString n = new NumberI18nString(156789L);
		assertThat(n.localize(Locale.US)).isEqualTo("156,789");
		assertThat(n.localize(Locale.FRENCH)).isEqualTo("156\u202F789");
		assertThat(n.localizeAsync(Locale.US).get()).isEqualTo("156,789");
		assertThat(n.localizeAsync(Locale.FRENCH).get()).isEqualTo("156\u202F789");
	}
	
	@Test
	void testDoesNotExist() {
		assertThat(new TranslatedString("unknown", "unknownkey").localize(Locale.US)).isEqualTo("[unknown#unknownkey]");
	}
	
	@Test
	void testComposite() throws Exception {
		var composite = CompositeI18nString.of(
			"hello ",
			new TranslatedString("test1", "key1"),
			" world ",
			CompositeI18nString.of(new NumberI18nString(12345L), new StaticI18nString(" !!"))
		);
		assertThat(composite.localize(Locale.US)).isEqualTo("hello value1 world 12,345 !!");
		
		StringBuilder out = new StringBuilder();
		composite.localize(Locale.US, out);
		assertThat(out).hasToString("hello value1 world 12,345 !!");
		
		assertThat(composite.localizeAsync(Locale.US).get()).isEqualTo("hello value1 world 12,345 !!");
		out = new StringBuilder();
		assertThat(composite.localizeAsync(Locale.US, out).get()).hasToString("hello value1 world 12,345 !!");
		
	}
	
}
