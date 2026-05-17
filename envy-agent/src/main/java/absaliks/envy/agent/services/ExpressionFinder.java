package absaliks.envy.agent.services;

import absaliks.envy.agent.services.Property.Expression;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ExpressionFinder {

  // Pattern for getting wrapped expressions, e.g. "URL is ${event-db.hostname}"
  private static final Pattern WRAPPED_EXPRESSION_PATTERN =
      Pattern.compile("\\$\\{([\\w\\s:.-]+)}");
  // Pattern for getting unwrapped expressions, e.g. "$event-db.hostname"
  private static final Pattern UNWRAPPED_EXPRESSION_PATTERN = Pattern.compile("^\\$[\\w\\s:.-]+$");

  public static List<Expression> findExpressions(String value) {
    if (value == null || value.length() < 2 || value.indexOf('$') == -1) {
      return List.of();
    }
    // expressions not wrapped in {} braces must take whole string, e.g. "$user.name"
    if (UNWRAPPED_EXPRESSION_PATTERN.matcher(value).matches()) {
      var expression = value.substring(1).strip();
      return List.of(new Expression(value, expression));
    }

    // wrapped expressions can be at any position
    var wrappedMatcher = WRAPPED_EXPRESSION_PATTERN.matcher(value);
    if (!wrappedMatcher.find()) {
      return List.of();
    }
    var expressions = new ArrayList<Expression>(3);
    do {
      var placeholder = wrappedMatcher.group(0);
      var expression = wrappedMatcher.group(1).strip();
      expressions.add(new Expression(placeholder, expression));
    } while (wrappedMatcher.find());
    return expressions;
  }
}
