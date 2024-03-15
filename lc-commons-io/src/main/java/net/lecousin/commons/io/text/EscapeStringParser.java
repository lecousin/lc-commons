package net.lecousin.commons.io.text;

import java.nio.CharBuffer;

/**
 * Simple parser, handling escape character \ and returning the escaped string.
 */
public class EscapeStringParser implements TextParser<String> {

	private StringBuilder s = new StringBuilder();
	private boolean escape = false;
	
	@Override
	public void parse(CharBuffer chars) {
		while (chars.hasRemaining()) {
			char c = chars.get();
			if (escape) {
				s.append(c);
				escape = false;
			} else if (c == '\\') {
				escape = true;
			} else {
				s.append(c);
			}
		}
	}
	
	@Override
	public String endOfInput() {
		if (escape) s.append('\\');
		return s.toString();
	}
	
}
