package net.lecousin.commons.io.text.placeholder.arguments;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lecousin.commons.io.text.placeholder.PlaceholderElement;
import net.lecousin.commons.io.text.placeholder.PlaceholderElementFactory;
import net.lecousin.commons.io.text.placeholder.PlaceholderStringElement;

/**
 * Generate a PlaceholderElement from a list of elements that were found inside a placeholder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ArgumentsPlaceholderFactory implements PlaceholderElementFactory<List<Object>> {

	/** Singleton. */
	public static final ArgumentsPlaceholderFactory INSTANCE = new ArgumentsPlaceholderFactory();
	
	@Override
	public PlaceholderElement<? super List<Object>> create(List<PlaceholderElement<? super List<Object>>> insideElements) {
		if (insideElements.isEmpty()) return new PlaceholderStringElement("");
		PlaceholderElement<? super List<Object>> element = insideElements.get(0);
		if (element instanceof PlaceholderStringElement s) {
			String str = s.getString();
			int colon = str.indexOf(':');
			// if starts with {{string: => function
			if (colon > 0) {
				String name = str.substring(0, colon).toLowerCase();
				ArrayList<PlaceholderElement<? super List<Object>>> elements = new ArrayList<>(insideElements);
				elements.set(0, new PlaceholderStringElement(str.substring(colon + 1)));
				return FunctionPlaceholderHandler.create(name, elements);
			}
			// else it must be a number
			if (insideElements.size() == 1)
				try {
					return new ArgumentIndexPlaceholder(Integer.valueOf(s.getString()));
				} catch (NumberFormatException e) {
					// ignore
				}
		}
		log.warn("Invalid arguments placeholders: {}", insideElements);
		return new PlaceholderStringElement("");
	}
	
}
