package net.lecousin.commons.io.text.placeholder.arguments;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;

/**
 * Placeholder with the index of the argument.
 */
@RequiredArgsConstructor
public class ArgumentIndexPlaceholder implements ArgumentsPlaceholder {

	private final int index;
	
	@Override
	public String resolve(List<Object> arguments) {
		if (index <= 0 || index > arguments.size()) return "";
		return Objects.toString(arguments.get(index - 1));
	}
	
	@Override
	public String toString() {
		return "{{" + index + "}}";
	}
	
}
