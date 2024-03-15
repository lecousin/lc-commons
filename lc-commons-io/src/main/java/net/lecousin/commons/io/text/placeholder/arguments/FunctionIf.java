package net.lecousin.commons.io.text.placeholder.arguments;

import java.math.BigDecimal;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lecousin.commons.io.text.placeholder.PlaceholderElement;
import net.lecousin.commons.io.text.placeholder.PlaceholderStringElement;

/**
 * If function in a placeholder: {{if:condition;true_value;false_value}}.
 */
@Slf4j
// CHECKSTYLE DISABLE: MagicNumber
public class FunctionIf extends FunctionPlaceholderHandler {

	@Override
	public String getName() {
		return "if";
	}
	
	@Override
	public PlaceholderElement<? super List<Object>> create(List<List<PlaceholderElement<? super List<Object>>>> arguments) {
		if (arguments.size() < 2) {
			log.warn("Invalid number of arguments for placeholder function if: {}", arguments);
			return new PlaceholderStringElement("");
		}
		if (arguments.size() < 3) {
			arguments.add(List.of(new PlaceholderStringElement("")));
		}
		List<PlaceholderElement<? super List<Object>>> condition = arguments.get(0);
		List<List<PlaceholderElement<? super List<Object>>>> list;
		
		list = PlaceholderElement.splitByString("<=", condition, 1);
		if (list.size() == 2) return new IfElement(new LessOrEqualsCondition(list.get(0), list.get(1)), arguments.get(1), arguments.get(2));
		list = PlaceholderElement.splitByString(">=", condition, 1);
		if (list.size() == 2) return new IfElement(new GreaterOrEqualsCondition(list.get(0), list.get(1)), arguments.get(1), arguments.get(2));
		list = PlaceholderElement.splitByString("!=", condition, 1);
		if (list.size() == 2) return new IfElement(new NotEqualsCondition(list.get(0), list.get(1)), arguments.get(1), arguments.get(2));
		list = PlaceholderElement.splitByString("=", condition, 1);
		if (list.size() == 2) return new IfElement(new EqualsCondition(list.get(0), list.get(1)), arguments.get(1), arguments.get(2));
		list = PlaceholderElement.splitByString("<", condition, 1);
		if (list.size() == 2) return new IfElement(new LessThanCondition(list.get(0), list.get(1)), arguments.get(1), arguments.get(2));
		list = PlaceholderElement.splitByString(">", condition, 1);
		if (list.size() == 2) return new IfElement(new GreaterThanCondition(list.get(0), list.get(1)), arguments.get(1), arguments.get(2));
		return new IfElement(new RawCondition(condition), arguments.get(1), arguments.get(2));
	}
	
	/** Placeholder element corresponding to a If function. */
	@RequiredArgsConstructor
	public static class IfElement implements ArgumentsPlaceholder {
		
		private final Condition condition;
		private final List<PlaceholderElement<? super List<Object>>> trueValue;
		private final List<PlaceholderElement<? super List<Object>>> falseValue;
		
		@Override
		public String resolve(List<Object> arguments) {
			if (condition.evaluate(arguments))
				return PlaceholderElement.resolveList(trueValue, arguments);
			return PlaceholderElement.resolveList(falseValue, arguments);
		}
	
		
		@Override
		public String toString() {
			return "{{if:" + condition + ";" + trueValue + ";" + falseValue + "}}";
		}

	}
	
	/** Condition. */
	public interface Condition {
		/** Evaluate the condition using the given arguments.
		 * 
		 * @param arguments arguments
		 * @return result of the condition
		 */
		boolean evaluate(List<Object> arguments);
	}
	
	/** Condition with only text: "true", "yes" or "1" is considered as true, else if is evaluated as false. */
	@RequiredArgsConstructor
	public static class RawCondition implements Condition {
		private final List<PlaceholderElement<? super List<Object>>> value;
		
		@Override
		public boolean evaluate(List<Object> arguments) {
			String s = PlaceholderElement.resolveList(value, arguments);
			if (s.isBlank()) return false;
			return "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "1".equals(s);
		}
		
		@Override
		public String toString() {
			return value.toString();
		}
	}
	
	/** Equals condition: the 2 operands must be equals when converted into string using arguments. */
	@RequiredArgsConstructor
	public static class EqualsCondition implements Condition {
		private final List<PlaceholderElement<? super List<Object>>> operand1;
		private final List<PlaceholderElement<? super List<Object>>> operand2;
		
		@Override
		public boolean evaluate(List<Object> arguments) {
			return PlaceholderElement.resolveList(operand1, arguments).equals(PlaceholderElement.resolveList(operand2, arguments));
		}
		
		@Override
		public String toString() {
			return operand1 + " = " + operand2;
		}
	}
	
	/** Not equals condition: the 2 operands must not be equals when converted into string using arguments. */
	@RequiredArgsConstructor
	public static class NotEqualsCondition implements Condition {
		private final List<PlaceholderElement<? super List<Object>>> operand1;
		private final List<PlaceholderElement<? super List<Object>>> operand2;
		
		@Override
		public boolean evaluate(List<Object> arguments) {
			return !PlaceholderElement.resolveList(operand1, arguments).equals(PlaceholderElement.resolveList(operand2, arguments));
		}
		
		@Override
		public String toString() {
			return operand1 + " != " + operand2;
		}
	}
	
	/** Convert the 2 operands into numbers and compare them, if thay are not numbers, compare the strings. */
	@RequiredArgsConstructor
	public static class LessOrEqualsCondition implements Condition {
		private final List<PlaceholderElement<? super List<Object>>> operand1;
		private final List<PlaceholderElement<? super List<Object>>> operand2;
		
		@Override
		public boolean evaluate(List<Object> arguments) {
			String s1 = PlaceholderElement.resolveList(operand1, arguments);
			String s2 = PlaceholderElement.resolveList(operand2, arguments);
			try {
				BigDecimal n1 = new BigDecimal(s1);
				BigDecimal n2 = new BigDecimal(s2);
				return n1.compareTo(n2) <= 0;
			} catch (NumberFormatException e) {
				return s1.compareTo(s2) <= 0;
			}
		}
		
		@Override
		public String toString() {
			return operand1 + " <= " + operand2;
		}
	}
	
	/** Convert the 2 operands into numbers and compare them, if thay are not numbers, compare the strings. */
	@RequiredArgsConstructor
	public static class LessThanCondition implements Condition {
		private final List<PlaceholderElement<? super List<Object>>> operand1;
		private final List<PlaceholderElement<? super List<Object>>> operand2;
		
		@Override
		public boolean evaluate(List<Object> arguments) {
			String s1 = PlaceholderElement.resolveList(operand1, arguments);
			String s2 = PlaceholderElement.resolveList(operand2, arguments);
			try {
				BigDecimal n1 = new BigDecimal(s1);
				BigDecimal n2 = new BigDecimal(s2);
				return n1.compareTo(n2) < 0;
			} catch (NumberFormatException e) {
				return s1.compareTo(s2) < 0;
			}
		}
		
		@Override
		public String toString() {
			return operand1 + " < " + operand2;
		}
	}
	
	/** Convert the 2 operands into numbers and compare them, if thay are not numbers, compare the strings. */
	@RequiredArgsConstructor
	public static class GreaterOrEqualsCondition implements Condition {
		private final List<PlaceholderElement<? super List<Object>>> operand1;
		private final List<PlaceholderElement<? super List<Object>>> operand2;
		
		@Override
		public boolean evaluate(List<Object> arguments) {
			String s1 = PlaceholderElement.resolveList(operand1, arguments);
			String s2 = PlaceholderElement.resolveList(operand2, arguments);
			try {
				BigDecimal n1 = new BigDecimal(s1);
				BigDecimal n2 = new BigDecimal(s2);
				return n1.compareTo(n2) >= 0;
			} catch (NumberFormatException e) {
				return s1.compareTo(s2) >= 0;
			}
		}
		
		@Override
		public String toString() {
			return operand1 + " >= " + operand2;
		}
	}
	
	/** Convert the 2 operands into numbers and compare them, if thay are not numbers, compare the strings. */
	@RequiredArgsConstructor
	public static class GreaterThanCondition implements Condition {
		private final List<PlaceholderElement<? super List<Object>>> operand1;
		private final List<PlaceholderElement<? super List<Object>>> operand2;
		
		@Override
		public boolean evaluate(List<Object> arguments) {
			String s1 = PlaceholderElement.resolveList(operand1, arguments);
			String s2 = PlaceholderElement.resolveList(operand2, arguments);
			try {
				BigDecimal n1 = new BigDecimal(s1);
				BigDecimal n2 = new BigDecimal(s2);
				return n1.compareTo(n2) > 0;
			} catch (NumberFormatException e) {
				return s1.compareTo(s2) > 0;
			}
		}
		
		@Override
		public String toString() {
			return operand1 + " > " + operand2;
		}
	}
	
}
