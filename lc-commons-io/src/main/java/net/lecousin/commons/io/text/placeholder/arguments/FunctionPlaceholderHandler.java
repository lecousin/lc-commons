package net.lecousin.commons.io.text.placeholder.arguments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import net.lecousin.commons.io.text.placeholder.PlaceholderElement;
import net.lecousin.commons.io.text.placeholder.PlaceholderStringElement;

/**
 * Abstract class for a function in a placeholder: {{function_name:argument1;argument2;argument3;...}}.
 * Functions can be registered using the ServiceLoader mechanism, in a resource file 
 * META-INF/services/net.lecousin.commons.io.text.placeholder.arguments.FunctionPlaceholderHandler
 */
public abstract class FunctionPlaceholderHandler {
	
	private static Map<String, FunctionPlaceholderHandler> functions = null;

	/** Create a placeholder element, using the given function and content.
	 * 
	 * @param name function name
	 * @param content elements after the colon ':'
	 * @return the generated element
	 */
	public static PlaceholderElement<? super List<Object>> create(String name, List<PlaceholderElement<? super List<Object>>> content) {
		synchronized (FunctionPlaceholderHandler.class) {
			if (functions == null) {
				functions = new HashMap<>();
				ServiceLoader.load(FunctionPlaceholderHandler.class).stream()
					.map(ServiceLoader.Provider::get)
					.forEach(handler -> functions.put(handler.getName().toLowerCase(), handler));
			}
		}
		FunctionPlaceholderHandler function = functions.get(name);
		if (function == null) return new PlaceholderStringElement("");
		List<List<PlaceholderElement<? super List<Object>>>> arguments = PlaceholderElement.splitByCharacter(';', content, Integer.MAX_VALUE);
		return function.create(arguments);
	}
	
	/** @return the name of the function. */
	public abstract String getName();
	
	/** Create an element using the given function arguments.
	 * 
	 * @param arguments arguments
	 * @return generated element
	 */
	public abstract PlaceholderElement<? super List<Object>> create(List<List<PlaceholderElement<? super List<Object>>>> arguments);
	
}
