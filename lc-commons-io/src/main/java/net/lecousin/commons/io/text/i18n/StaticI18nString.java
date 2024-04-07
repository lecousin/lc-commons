package net.lecousin.commons.io.text.i18n;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Non-translated string.
 */
public class StaticI18nString implements I18nString {

	private static final long serialVersionUID = 1L;
	
	private final String str;
	
	/**
	 * Constructor.
	 * @param s static string
	 */
	public StaticI18nString(String s) {
		this.str = s;
	}
	
	@Override
	public String localize(Locale locale) {
		return str;
	}
	
	@Override
	public CompletableFuture<String> localizeAsync(Locale locale) {
		return CompletableFuture.completedFuture(str);
	}
	
}
