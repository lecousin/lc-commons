package net.lecousin.commons.io.text.i18n;

import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lecousin.commons.cache.MapExpireCache;
import net.lecousin.commons.executors.LcExecutors;
import net.lecousin.commons.io.text.PropertiesParser;
import net.lecousin.commons.io.text.PropertiesParser.Property;
import net.lecousin.commons.io.text.placeholder.arguments.ArgumentsPlaceholder.Compiled;

/**
 * Translate a namespace, a key and arguments into a string using resource bundles.
 * <p>A resource bundle is a resource file under the directory /i18n/, with a name:<ul>
 * <li>namespace</li>
 * <li>namespace_language</li>
 * <li>namespace_language_country</li>
 * <li>namespace_language_countr_variant</li>
 * </ul>
 */
// CHECKSTYLE DISABLE: MagicNumber
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class I18nResourceBundle {

	private static final Set<String> EMPTY_FILES = new HashSet<>();
	private static final Map<String, CompletableFuture<Optional<Map<String, Compiled>>>> LOADING = new HashMap<>();
	private static final MapExpireCache<String, Map<String, Compiled>> MAP = new MapExpireCache<>(Duration.ofMinutes(5), Duration.ofMinutes(2));
	
	/** Translate.
	 * 
	 * @param locale locale to translate to
	 * @param namespace namespace
	 * @param key key in the namespace file
	 * @param arguments arguments used in placeholders
	 * @return the translated string
	 */
	public static String get(Locale locale, String namespace, String key, Serializable[] arguments) {
		try {
			return getAsync(locale, namespace, key, arguments).get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return "[Interrupted]";
		} catch (Exception e) {
			return "[Error]";
		}
	}
	
	/** Translate with a future: if everything is already loaded, the future is immediately done,
	 * but if some resources need to be loaded, the loading will be performed in a separate thread.
	 * 
	 * @param locale locale to translate to
	 * @param namespace namespace
	 * @param key key in the namespace file
	 * @param arguments arguments used in placeholders
	 * @return the translated string
	 */
	public static CompletableFuture<String> getAsync(Locale locale, String namespace, String key, Serializable[] arguments) {
		List<Object> resolved = resolveArguments(locale, arguments);
		CompletableFuture<String> result = new CompletableFuture<>();
		getPlaceholders(getFilenames(locale, namespace), key)
		.thenAccept(placeholders -> {
			if (placeholders.isEmpty()) {
				log.warn("Key {} does not exist in namespace {} for locale {}", key, namespace, locale);
				result.complete("[" + namespace + "#" + key + "]");
			} else {
				result.complete(placeholders.get().resolve(resolved));
			}
		});
		return result;
	}
	
	private static List<String> getFilenames(Locale locale, String namespace) {
		String baseFilename = "i18n/" + namespace;
		List<String> filenames = new ArrayList<>(4);
		if (!locale.getLanguage().isEmpty()) {
			String languageFilename = baseFilename + "_" + locale.getLanguage().toLowerCase();
			if (!locale.getCountry().isEmpty()) {
				String countryFilename = languageFilename + "_" + locale.getCountry().toLowerCase();
				if (!locale.getVariant().isEmpty()) {
					String variantFilename = countryFilename + "_" + locale.getVariant().toLowerCase();
					filenames.add(variantFilename);
				}
				filenames.add(countryFilename);
			}
			filenames.add(languageFilename);
		}
		filenames.add(baseFilename);
		return filenames;
	}
	
	private static List<Object> resolveArguments(Locale locale, Serializable[] arguments) {
		List<Object> resolved = new ArrayList<>(arguments.length);
		for (int i = 0; i < arguments.length; ++i) {
			Object arg = arguments[i];
			if (arg instanceof I18nString s)
				arg = s.localize(locale);
			resolved.add(arg);
		}
		return resolved;
	}
	
	private static CompletableFuture<Optional<Compiled>> getPlaceholders(List<String> filenames, String key) {
		CompletableFuture<Optional<Compiled>> result = new CompletableFuture<>();
		getPlaceholders(filenames, key, 0, result);
		return result;
	}
	
	private static void getPlaceholders(List<String> filenames, String key, int index, CompletableFuture<Optional<Compiled>> result) {
		getPlaceholders(filenames.get(index), key)
			.thenAccept(placeholders -> {
				if (placeholders.isPresent())
					result.complete(placeholders);
				else if (index < filenames.size() - 1)
					getPlaceholders(filenames, key, index + 1, result);
				else
					result.complete(Optional.empty());
			});
	}
	
	private static CompletableFuture<Optional<Compiled>> getPlaceholders(String filename, String key) {
		CompletableFuture<Optional<Map<String, Compiled>>> future;
		boolean toLoad = false;
		synchronized (EMPTY_FILES) {
			if (EMPTY_FILES.contains(filename)) return CompletableFuture.completedFuture(Optional.empty());
			var opt = MAP.get(filename);
			if (opt.isPresent()) return CompletableFuture.completedFuture(Optional.ofNullable(opt.get().get(key)));
			future = LOADING.get(filename);
			if (future == null) {
				future = new CompletableFuture<>();
				LOADING.put(filename, future);
				toLoad = true;
			}
		}
		CompletableFuture<Optional<Compiled>> result = new CompletableFuture<>();
		if (toLoad) {
			final CompletableFuture<Optional<Map<String, Compiled>>> f = future;
			parse(filename).thenAccept(parsingResult -> {
				synchronized (EMPTY_FILES) {
					if (parsingResult.isEmpty()) EMPTY_FILES.add(filename);
					else MAP.put(filename, parsingResult.get());
					LOADING.remove(filename);
				}
				f.complete(parsingResult);
				result.complete(parsingResult.flatMap(r -> Optional.ofNullable(r.get(key))));
			});
			return result;
		}
		future.thenAccept(parsingResult -> result.complete(parsingResult.flatMap(r -> Optional.ofNullable(r.get(key)))));
		return result;
	}
	
	@SuppressWarnings({"java:S2142", "java:S2112"})
	private static CompletableFuture<Optional<Map<String, Compiled>>> parse(String filename) {
		CompletableFuture<Optional<Map<String, Compiled>>> result = new CompletableFuture<>();
		LcExecutors.getNonCpu().execute(() -> {
			Set<URL> urls = new HashSet<>();
			try {
				Enumeration<URL> resources = ClassLoader.getSystemResources(filename);
				while (resources.hasMoreElements()) {
					urls.add(resources.nextElement());
				}
			} catch (Exception e) {
				log.warn("Error looking for resource {} using the system class loader", filename, e);
			}
			try {
				Enumeration<URL> resources = I18nResourceBundle.class.getClassLoader().getResources(filename);
				while (resources.hasMoreElements()) {
					urls.add(resources.nextElement());
				}
			} catch (Exception e) {
				log.warn("Error looking for resource {} using the I18nResourceBundle class loader", filename, e);
			}
			if (urls.isEmpty()) {
				result.complete(Optional.empty());
				return;
			}
			@SuppressWarnings("unchecked")
			CompletableFuture<List<Property<Compiled>>>[] futures = new CompletableFuture[urls.size()];
			ArrayList<URL> list = new ArrayList<>(urls);
			for (int i = 0; i < list.size(); ++i) {
				CompletableFuture<List<Property<Compiled>>> future = new CompletableFuture<>();
				final int index = i;
				LcExecutors.getNonCpu().execute(() -> {
					try (var input = list.get(index).openStream()) {
						future.complete(
							new PropertiesParser<>(Compiled::parser)
							.parse(input, StandardCharsets.UTF_8, false)
						);
					} catch (Exception e) {
						log.warn("Error parsing resource file {}", filename, e);
						future.complete(List.of());
					}
				});
				futures[i] = future;
			}
			CompletableFuture.allOf(futures)
				.thenRun(() -> {
					Map<String, Compiled> properties = new HashMap<>();
					for (var future : futures) {
						try {
							future.get().forEach(p -> properties.put(p.getName(), p.getValue()));
						} catch (Exception e) {
							// ignore
						}
					}
					result.complete(properties.isEmpty() ? Optional.empty() : Optional.of(properties));
				});
		});
		return result;
	}
	
}
