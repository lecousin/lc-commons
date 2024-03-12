package net.lecousin.commons.io.text.placeholder.arguments;

import java.util.List;

import net.lecousin.commons.io.text.TextParser;
import net.lecousin.commons.io.text.placeholder.PlaceholderElement;
import net.lecousin.commons.io.text.placeholder.PlaceholderTextParser;

/**
 * Placeholder that can be resolved using a list of arguments.
 */
public interface ArgumentsPlaceholder extends PlaceholderElement<List<Object>> {

	/** Compiled arguments placeholders. */
	class Compiled {
		private List<PlaceholderElement<? super List<Object>>> placeholders;
		
		/** Constructor.
		 * 
		 * @param placeholders placeholders
		 */
		public Compiled(List<PlaceholderElement<? super List<Object>>> placeholders) {
			this.placeholders = placeholders;
			PlaceholderElement.mergeStrings(this.placeholders);
		}
		
		/** Resolve with given arguments and generate a string.
		 * 
		 * @param arguments arguments
		 * @return generated string
		 */
		public String resolve(List<Object> arguments) {
			return PlaceholderElement.resolveList(placeholders, arguments);
		}
		
		/**
		 * Create a parser using {@code {{} and {@code }}} as start and end of placeholders.
		 * @return the parser
		 */
		public static TextParser<Compiled> parser() {
			return parser("{{", "}}");
		}
		
		/**
		 * Create a parser using the given start and end of placeholders.
		 * @param placeholderStart start of a placeholder
		 * @param placeholderEnd end of a placeholder
		 * @return the parser
		 */
		public static TextParser<Compiled> parser(String placeholderStart, String placeholderEnd) {
			return new PlaceholderTextParser<List<Object>>(placeholderStart, placeholderEnd, ArgumentsPlaceholderFactory.INSTANCE)
				.map(Compiled::new);
		}
	}
	
}
