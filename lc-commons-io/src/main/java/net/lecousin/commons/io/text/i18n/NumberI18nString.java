package net.lecousin.commons.io.text.i18n;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;

/**
 * A number to localize.
 */
@RequiredArgsConstructor
public class NumberI18nString implements I18nString {

	private final Number number;
	
	@Override
	public String localize(Locale locale) {
		return NumberFormat.getNumberInstance(locale).format(number);
	}
	
	@Override
	public CompletableFuture<String> localizeAsync(Locale locale) {
		return CompletableFuture.completedFuture(localize(locale));
	}
	
}
