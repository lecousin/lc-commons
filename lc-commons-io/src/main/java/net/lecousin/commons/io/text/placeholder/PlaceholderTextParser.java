package net.lecousin.commons.io.text.placeholder;

import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.commons.io.text.TextParser;

/**
 * Parse a text containing placeholders.
 * @param <T> type of input to resolve the placeholders
 */
public class PlaceholderTextParser<T> implements TextParser<List<PlaceholderElement<? super T>>> {

	private final char[] start;
	private final char[] end;
	private final PlaceholderElementFactory<T> factory;
	
	private int startFound = 0;
	private int endFound = 0;
	private boolean escape = false;
	private StringBuilder str = new StringBuilder();
	private PlaceholderTextParser<T> insideParser = null;
	
	private List<PlaceholderElement<? super T>> result = new LinkedList<>();
	
	/**
	 * Constructor.
	 * @param start start of a placeholder
	 * @param end end of a placeholder
	 * @param factory element factory to generate a PlaceholderElement from the elements inside a placeholder
	 */
	public PlaceholderTextParser(char[] start, char[] end, PlaceholderElementFactory<T> factory) {
		this.start = start;
		this.end = end;
		this.factory = factory;
	}
	
	/**
	 * Constructor.
	 * @param start start of a placeholder
	 * @param end end of a placeholder
	 * @param factory element factory to generate a PlaceholderElement from the elements inside a placeholder
	 */
	public PlaceholderTextParser(String start, String end, PlaceholderElementFactory<T> factory) {
		this(start.toCharArray(), end.toCharArray(), factory);
	}
	
	@Override
	public void parse(CharBuffer chars) {
		while (chars.hasRemaining()) {
			if (!parsePartial(chars)) break;
			str.append(end);
		}
	}
	
	@SuppressWarnings("java:S3776")
	private boolean parsePartial(CharBuffer chars) {
		while (chars.hasRemaining()) {
			if (insideParser != null) {
				if (insideParser.parsePartial(chars)) {
					result.add(factory.create(insideParser.endOfInput()));
					insideParser = null;
				}
				continue;
			}
			char c = chars.get();
			if (escape) {
				str.append(c);
				escape = false;
				continue;
			}
			if (c == start[startFound]) {
				if (++startFound == start.length) {
					// full start found
					if (!str.isEmpty()) {
						result.add(new PlaceholderStringElement(str.toString()));
						str = new StringBuilder();
					}
					insideParser = new PlaceholderTextParser<>(start, end, factory);
					startFound = 0;
				}
				continue;
			}
			if (startFound > 0) {
				str.append(start, 0, startFound);
				startFound = 0;
			}
			if (c == end[endFound]) {
				if (++endFound == end.length) {
					// full end found
					if (!str.isEmpty()) {
						result.add(new PlaceholderStringElement(str.toString()));
						str = new StringBuilder();
					}
					endFound = 0;
					return true;
				}
				continue;
			}
			if (endFound > 0) {
				str.append(end, 0, endFound);
				endFound = 0;
			}
			if (c == '\r') continue;
			if (c == '\\') {
				escape = true;
				continue;
			}
			str.append(c);
		}
		return false;
	}
	
	@Override
	public List<PlaceholderElement<? super T>> endOfInput() {
		if (startFound > 0) {
			str.append(start, 0, startFound);
		}
		if (endFound > 0) {
			str.append(end, 0, endFound);
		}
		if (!str.isEmpty())
			result.add(new PlaceholderStringElement(str.toString()));
		if (insideParser != null) {
			result.add(new PlaceholderStringElement(new String(start)));
			result.addAll(insideParser.endOfInput());
		}
		return result;
	}
}
