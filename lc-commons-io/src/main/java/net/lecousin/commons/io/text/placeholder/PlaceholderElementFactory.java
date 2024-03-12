package net.lecousin.commons.io.text.placeholder;

import java.util.List;

/**
 * Generate a PlaceholderElement from a list of elements that were found inside a placeholder.
 * @param <T> type of input to resolve the placeholders
 */
public interface PlaceholderElementFactory<T> {

	/**
	 * Generate a PlaceholderElement from a list of elements that were found inside a placeholder.
	 * @param insideElements elements found inside a placeholder
	 * @return the generated element
	 */
	PlaceholderElement<? super T> create(List<PlaceholderElement<? super T>> insideElements);
	
}
