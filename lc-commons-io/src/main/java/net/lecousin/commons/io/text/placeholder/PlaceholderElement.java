package net.lecousin.commons.io.text.placeholder;

import java.util.LinkedList;
import java.util.List;

/**
 * Marker interface for a placeholder element.
 * @param <T> type of input to resolve this placeholder element
 */
public interface PlaceholderElement<T> {
	
	/** Resolve this placeholder element to generate a string.
	 * 
	 * @param resolveWith input
	 * @return generated string
	 */
	String resolve(T resolveWith);
	
	/**
	 * Resolve this placeholder element to generate a string, and append it to the output.
	 * @param resolveWith input
	 * @param output output
	 */
	default void resolve(T resolveWith, StringBuilder output) {
		output.append(resolve(resolveWith));
	}

	/**
	 * Resolve every placeholder element with an input, and generate a string.
	 * @param elements elements
	 * @param resolveWith input
	 * @param <T> type of input
	 * @return generated string
	 */
	static <T> String resolveList(List<PlaceholderElement<? super T>> elements, T resolveWith) {
		StringBuilder s = new StringBuilder();
		for (PlaceholderElement<? super T> element : elements)
			element.resolve(resolveWith, s);
		return s.toString();
	}

	/** Split elements using the given character as separator.
	 * 
	 * @param c separator
	 * @param elements input elements
	 * @param maxSeparators maximum number of separators to consider
	 * @param <T> type of input to resolve placeholders
	 * @return splitted elements
	 */
	static <T> List<List<PlaceholderElement<? super T>>> splitByCharacter(char c, List<PlaceholderElement<? super T>> elements, int maxSeparators) {
		List<List<PlaceholderElement<? super T>>> list = new LinkedList<>();
		List<PlaceholderElement<? super T>> current = new LinkedList<>();
		for (PlaceholderElement<? super T> element : elements) {
			do {
				if (list.size() < maxSeparators && element instanceof PlaceholderStringElement se) {
					int index = se.getString().indexOf(c);
					if (index >= 0) {
						if (index > 0)
							current.add(new PlaceholderStringElement(se.getString().substring(0, index)));
						list.add(current);
						current = new LinkedList<>();
						if (index == se.getString().length() - 1)
							break;
						element = new PlaceholderStringElement(se.getString().substring(index + 1));
						continue;
					}
				}
				current.add(element);
				break;
			} while (true);
		}
		if (!current.isEmpty())
			list.add(current);
		return list;
	}
	
	/** Split elements using the given string as separator.
	 * 
	 * @param separator separator
	 * @param elements input elements
	 * @param maxSeparators maximum number of separators to consider
	 * @param <T> type of input to resolve placeholders
	 * @return splitted elements
	 */
	static <T> List<List<PlaceholderElement<? super T>>> splitByString(String separator, List<PlaceholderElement<? super T>> elements, int maxSeparators) {
		mergeStrings(elements);
		List<List<PlaceholderElement<? super T>>> list = new LinkedList<>();
		List<PlaceholderElement<? super T>> current = new LinkedList<>();
		for (PlaceholderElement<? super T> element : elements) {
			do {
				if (list.size() < maxSeparators && element instanceof PlaceholderStringElement se) {
					int index = se.getString().indexOf(separator);
					if (index >= 0) {
						if (index > 0)
							current.add(new PlaceholderStringElement(se.getString().substring(0, index)));
						list.add(current);
						current = new LinkedList<>();
						if (index == se.getString().length() - separator.length())
							break;
						element = new PlaceholderStringElement(se.getString().substring(index + separator.length()));
						continue;
					}
				}
				current.add(element);
				break;
			} while (true);
		}
		if (!current.isEmpty())
			list.add(current);
		return list;
	}
	
	/**
	 * Merge adjacent string elements into a single string element
	 * @param elements elements
	 * @param <T> type of input to resolve placeholders
	 */
	static <T> void mergeStrings(List<PlaceholderElement<? super T>> elements) {
		if (elements.size() < 2) return;
		var it = elements.iterator();
		var previous = it.next();
		while (it.hasNext()) {
			var next = it.next();
			if (next instanceof PlaceholderStringElement s2 && previous instanceof PlaceholderStringElement s1) {
				s1.setString(s1.getString() + s2.getString());
				it.remove();
			} else {
				previous = next;
			}
		}
	}
	
}
