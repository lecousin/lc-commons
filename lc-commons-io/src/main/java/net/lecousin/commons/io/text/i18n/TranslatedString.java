package net.lecousin.commons.io.text.i18n;

import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * A translatable string, using I18nResourceBundle.
 */
public class TranslatedString implements I18nString {

	private static final long serialVersionUID = 1L;
	
	/** namespace */
	private final String namespace;
	/** key */
	private final String key;
	/** arguments */
	private final Serializable[] arguments;
	
	/**
	 * Constructor. See {@link I18nResourceBundle#get(Locale, String, String, Serializable[])}.
	 * @param namespace namespace
	 * @param key key
	 * @param arguments arguments
	 */
	public TranslatedString(String namespace, String key, Serializable... arguments) {
		this.namespace = namespace;
		this.key = key;
		this.arguments = arguments;
	}
	
	@Override
	public String localize(Locale locale) {
		return I18nResourceBundle.get(locale, namespace, key, arguments);
	}
	
	@Override
	public CompletableFuture<String> localizeAsync(Locale locale) {
		return I18nResourceBundle.getAsync(locale, namespace, key, arguments);
	}
	
}
