package net.lecousin.commons.io.text.i18n;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * I18nString composed of sub-I18nString.
 */
public class CompositeI18nString implements I18nString {

	private static final long serialVersionUID = 1L;
	
	/** sub-strings */
	private final I18nString[] strings;
	
	/**
	 * Constructor.
	 * @param strings sub-strings
	 */
	public CompositeI18nString(I18nString... strings) {
		this.strings = strings;
	}
	
	/**
	 * Create a CompositeI18nString based on the given elements.<br/>
	 * Each element which is not an I18nString is converted into a StaticI18nString.
	 * 
	 * @param elements elements
	 * @return CompositeI18nString
	 */
	public static CompositeI18nString of(Serializable... elements) {
		I18nString[] i18nElements = new I18nString[elements.length];
		for (int i = 0; i < elements.length; ++i)
			if (elements[i] instanceof I18nString i18n)
				i18nElements[i] = i18n;
			else
				i18nElements[i] = new StaticI18nString(Objects.toString(elements[i]));
		return new CompositeI18nString(i18nElements);
	}
	
	@Override
	public String localize(Locale locale) {
		StringBuilder s = new StringBuilder();
		localize(locale, s);
		return s.toString();
	}
	
	@Override
	public void localize(Locale locale, StringBuilder output) {
		for (var str : strings)
			str.localize(locale, output);
	}
	
	@Override
	public CompletableFuture<String> localizeAsync(Locale locale) {
		return localizeAsync(locale, new StringBuilder()).thenApply(StringBuilder::toString);
	}
	
	@Override
	public CompletableFuture<StringBuilder> localizeAsync(Locale locale, StringBuilder output) {
		CompletableFuture<StringBuilder> initialFuture = new CompletableFuture<>();
		CompletableFuture<StringBuilder> future = initialFuture;
		for (var str : strings)
			future = future.thenComposeAsync(out -> str.localizeAsync(locale, out));
		initialFuture.complete(output);
		return future;
	}
	
}
