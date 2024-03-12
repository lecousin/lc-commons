package net.lecousin.commons.io.text.placeholder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Placeholder element which is just a string found between 2 placeholders.
 */
@AllArgsConstructor
public class PlaceholderStringElement implements PlaceholderElement<Object> {

	@Getter
	@Setter
	private String string;
	
	@Override
	public String resolve(Object resolveWith) {
		return string;
	}
	
	@Override
	public String toString() {
		return string;
	}
	
}
