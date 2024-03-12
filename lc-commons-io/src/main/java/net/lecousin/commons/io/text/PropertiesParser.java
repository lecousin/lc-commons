package net.lecousin.commons.io.text;

import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.lecousin.commons.io.text.PropertiesParser.Property;

/** Properties parser.
 * @param <T> type of properties value
 */
public class PropertiesParser<T> implements TextParser<List<Property<T>>> {

	private static final int NAME_BUFFER_SIZE = 128;
	
	private final Supplier<TextParser<T>> valueParserSupplier;
	
	private StringBuilder name = new StringBuilder(NAME_BUFFER_SIZE);
	private TextParser<T> valueParser = null;
	private boolean commentLine = false;
	private boolean escape = false;
	
	private final List<Property<T>> properties = new LinkedList<>();
	
	/**
	 * Property.
	 * @param <T> type of value
	 */
	@Data
	@AllArgsConstructor
	public static class Property<T> {
		private String name;
		private T value;
	}
	
	/**
	 * Constructor.
	 * @param valueParserSupplier provide a new parser to parse a value
	 */
	public PropertiesParser(Supplier<TextParser<T>> valueParserSupplier) {
		this.valueParserSupplier = valueParserSupplier;
	}
	
	/** @return a simple PropertiesParser, with escaped string as value. */
	public static PropertiesParser<String> simple() {
		return new PropertiesParser<>(EscapeStringParser::new);
	}
	
	@Override
	public void parse(CharBuffer chars) {
		do {
			if (commentLine)
				skipLine(chars);
			if (valueParser == null)
				parseName(chars);
			else
				parseValue(chars);
		} while (chars.hasRemaining());
	}
	
	private void parseName(CharBuffer chars) {
		while (chars.hasRemaining()) {
			char c = chars.get();
			if (name.length() == 0) {
				if (Character.isWhitespace(c))
					continue;
				if (c == '#') {
					commentLine = true;
					parse(chars);
					return;
				}
			}
			if (escape) {
				name.append(c);
				escape = false;
				continue;
			}
			if (c == '\\') {
				escape = true;
				continue;
			}
			if (c == '=') {
				valueParser = valueParserSupplier.get();
				return;
			}
			if (c == '\r') continue;
			if (c == '\n') {
				endOfProperty();
				continue;
			}
			name.append(c);
		}
	}
	
	private void endOfProperty() {
		escape = false;
		if (valueParser == null) {
			valueParser = valueParserSupplier.get();
		}
		T value = valueParser.endOfInput();
		properties.add(new Property<>(name.toString(), value));
		name = new StringBuilder(NAME_BUFFER_SIZE);
		valueParser = null;
	}
	
	private void parseValue(CharBuffer chars) {
		int r = chars.remaining();
		char c = 0;
		while (r > 0 && (c = chars.charAt(0)) == '\r') {
			chars.get();
			r--;
		}
		if (r == 0) return;
		if (c == '\n') {
			chars.get();
			endOfProperty();
			return;
		}
		int i;
		for (i = 0; i < r; ++i) {
			c = chars.charAt(i);
			if (c == '\n' || c == '\r') break;
		}
		int l = chars.limit();
		chars.limit(chars.position() + i);
		valueParser.parse(chars);
		if (i == r)
			return; // no end of line
		chars.limit(l);
		if (c == '\n')
			endOfProperty();
	}
	
	private void skipLine(CharBuffer chars) {
		while (chars.hasRemaining()) {
			if (chars.get() == '\n') {
				commentLine = false;
				return;
			}
		}
	}
	
	@Override
	public List<Property<T>> endOfInput() {
		if (!name.isEmpty()) endOfProperty();
		return properties;
	}
	
}
