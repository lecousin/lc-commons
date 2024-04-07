package net.lecousin.commons.io.text.i18n;

import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * A localizable string.
 */
public interface I18nString extends Serializable {

	/** Localize into the given locale.
	 * 
	 * @param locale the target locale
	 * @return the translated string
	 */
	String localize(Locale locale);
	
	/**
	 * Localize into the given future, in an asynchronous way if needed.
	 * @param locale the target locale
	 * @return the translated string
	 */
	CompletableFuture<String> localizeAsync(Locale locale);
	
	/**
	 * Localize into the given locale and append it to the output.
	 * @param locale the target locale
	 * @param output output
	 */
	default void localize(Locale locale, StringBuilder output) {
		output.append(localize(locale));
	}
	
	/**
	 * Localize into the given locale and append it to the output.
	 * @param locale the target locale
	 * @param output output
	 * @return future completed with the output filled
	 */
	default CompletableFuture<StringBuilder> localizeAsync(Locale locale, StringBuilder output) {
		return localizeAsync(locale).thenApplyAsync(s -> {
			output.append(s);
			return output;
		});
	}
	
}
