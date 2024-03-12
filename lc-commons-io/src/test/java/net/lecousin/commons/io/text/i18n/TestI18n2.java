package net.lecousin.commons.io.text.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;

class TestI18n2 {

	@Test
	void test() {
		// key1 is only defined in the default
		assertThat(new TranslatedString("test2", "key1").localize(Locale.ENGLISH)).isEqualTo("default1");
		assertThat(new TranslatedString("test2", "key1").localize(Locale.US)).isEqualTo("default1");
		assertThat(new TranslatedString("test2", "key1").localize(Locale.UK)).isEqualTo("default1");
		assertThat(new TranslatedString("test2", "key1").localize(Locale.FRENCH)).isEqualTo("default1");
		assertThat(new TranslatedString("test2", "key1").localize(Locale.FRANCE)).isEqualTo("default1");
		assertThat(new TranslatedString("test2", "key1").localize(Locale.CANADA_FRENCH)).isEqualTo("default1");
		assertThat(new TranslatedString("test2", "key1").localize(Locale.JAPAN)).isEqualTo("default1");
		assertThat(new TranslatedString("test2", "key1").localize(Locale.ROOT)).isEqualTo("default1");
		
		// key2 is overridden at language level
		assertThat(new TranslatedString("test2", "key2").localize(Locale.ENGLISH)).isEqualTo("english2");
		assertThat(new TranslatedString("test2", "key2").localize(Locale.US)).isEqualTo("english2");
		assertThat(new TranslatedString("test2", "key2").localize(Locale.UK)).isEqualTo("english2");
		assertThat(new TranslatedString("test2", "key2").localize(Locale.FRENCH)).isEqualTo("french2");
		assertThat(new TranslatedString("test2", "key2").localize(Locale.FRANCE)).isEqualTo("french2");
		assertThat(new TranslatedString("test2", "key2").localize(Locale.CANADA_FRENCH)).isEqualTo("french2");
		assertThat(new TranslatedString("test2", "key2").localize(Locale.JAPAN)).isEqualTo("default2");
		assertThat(new TranslatedString("test2", "key2").localize(Locale.ROOT)).isEqualTo("default2");
		
		// key3 is overridden at country level, except for fr_ca and en_gb
		assertThat(new TranslatedString("test2", "key3").localize(Locale.ENGLISH)).isEqualTo("english3");
		assertThat(new TranslatedString("test2", "key3").localize(Locale.US)).isEqualTo("us3");
		assertThat(new TranslatedString("test2", "key3").localize(Locale.UK)).isEqualTo("english3");
		assertThat(new TranslatedString("test2", "key3").localize(Locale.FRENCH)).isEqualTo("french3");
		assertThat(new TranslatedString("test2", "key3").localize(Locale.FRANCE)).isEqualTo("france3");
		assertThat(new TranslatedString("test2", "key3").localize(Locale.CANADA_FRENCH)).isEqualTo("french3");
		assertThat(new TranslatedString("test2", "key3").localize(Locale.JAPAN)).isEqualTo("default3");
		assertThat(new TranslatedString("test2", "key3").localize(Locale.ROOT)).isEqualTo("default3");
		
		// key4 is overridden by all countries
		assertThat(new TranslatedString("test2", "key4").localize(Locale.ENGLISH)).isEqualTo("english4");
		assertThat(new TranslatedString("test2", "key4").localize(Locale.US)).isEqualTo("us4");
		assertThat(new TranslatedString("test2", "key4").localize(Locale.UK)).isEqualTo("gb4");
		assertThat(new TranslatedString("test2", "key4").localize(Locale.FRENCH)).isEqualTo("french4");
		assertThat(new TranslatedString("test2", "key4").localize(Locale.FRANCE)).isEqualTo("france4");
		assertThat(new TranslatedString("test2", "key4").localize(Locale.CANADA_FRENCH)).isEqualTo("frca4");
		assertThat(new TranslatedString("test2", "key4").localize(Locale.JAPAN)).isEqualTo("default4");
		assertThat(new TranslatedString("test2", "key4").localize(Locale.ROOT)).isEqualTo("default4");
	}
	
}
