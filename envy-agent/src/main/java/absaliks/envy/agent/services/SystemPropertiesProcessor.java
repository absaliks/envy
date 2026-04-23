package absaliks.envy.agent.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemPropertiesProcessor {

  // Pattern for getting wrapped expressions, e.g. "URL is ${event-db.hostname}"
  private static final Pattern WRAPPED_EXPRESSION_PATTERN = Pattern.compile("\\$\\{([\\w\\s.-]+)}");
  // Pattern for getting unwrapped expressions, e.g. "$event-db.hostname"
  private static final Pattern UNWRAPPED_EXPRESSION_PATTERN = Pattern.compile("^\\$[\\w\\s.-]+$");

  public static Map<String, String> process(
      Map<String, String> systemProperties, ExpressionResolverService expResolver) {
    var changedProperties = new HashMap<String, String>();
    systemProperties.forEach(
        (key, value) ->
            resolveExpressions(expResolver, key, value)
                .ifPresent(newValue -> changedProperties.put(key, newValue)));
    return changedProperties;
  }

  private static Optional<String> resolveExpressions(
      ExpressionResolverService expResolver, String key, String value) {
    if (value == null || value.length() < 2 || value.indexOf('$') == -1) {
      return Optional.empty();
    }

    // expressions not wrapped in {} braces must take whole string, e.g. "$user.name"
    if (UNWRAPPED_EXPRESSION_PATTERN.matcher(value).matches()) {
      var expression = value.substring(1);
      return expResolver.resolve(key, expression);
    }

    // wrapped expressions can be at any position
    var wrappedMatcher = WRAPPED_EXPRESSION_PATTERN.matcher(value);
    if (!wrappedMatcher.find()) {
      return Optional.empty();
    }
    var sb = new StringBuilder();
    do {
      var expression = wrappedMatcher.group(1);
      var replacement = expResolver.resolve(key, expression)
          .orElse(wrappedMatcher.group(0));
      wrappedMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    } while (wrappedMatcher.find());
    wrappedMatcher.appendTail(sb);
    return Optional.of(sb.toString()).filter(s -> !s.equals(value));
  }
}
