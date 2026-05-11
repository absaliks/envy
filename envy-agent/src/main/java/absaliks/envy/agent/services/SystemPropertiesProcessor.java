package absaliks.envy.agent.services;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SystemPropertiesProcessor {

  // Pattern for getting wrapped expressions, e.g. "URL is ${event-db.hostname}"
  private static final Pattern WRAPPED_EXPRESSION_PATTERN =
      Pattern.compile("\\$\\{([\\w\\s:.-]+)}");
  // Pattern for getting unwrapped expressions, e.g. "$event-db.hostname"
  private static final Pattern UNWRAPPED_EXPRESSION_PATTERN = Pattern.compile("^\\$[\\w\\s:.-]+$");

  public static Map<String, String> process(
      Map<String, String> systemProperties, ExpressionResolverService expResolver) {
    var resolvables =
        systemProperties.entrySet().stream()
            .map(e -> getResolvable(e.getKey(), e.getValue()))
            .filter(Objects::nonNull)
            .toList();
    expResolver.resolve(resolvables);
    return resolvables.stream()
        .map(SystemPropertiesProcessor::tryResolveValue)
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private static Entry<String, String> tryResolveValue(Resolvable resolvable) {
    var value = resolvable.rawValue();
    for (var expression : resolvable.expressions()) {
      if (!expression.value().isInitialized()) continue;
      var expValue = expression.value().get();
      if (expValue != null) {
        value =
            value == expression.placeholder() // if expression takes whole string
                ? expValue // no need to replace substring, just swap the value
                : value.replace(expression.placeholder(), expValue);
      }
    }
    return value != resolvable.rawValue() ? new SimpleEntry<>(resolvable.key(), value) : null;
  }

  private static Resolvable getResolvable(String key, String value) {
    if (value == null || value.length() < 2 || value.indexOf('$') == -1) {
      return null;
    }
    // expressions not wrapped in {} braces must take whole string, e.g. "$user.name"
    if (UNWRAPPED_EXPRESSION_PATTERN.matcher(value).matches()) {
      var expression = value.substring(1).strip();
      return new Resolvable(key, value, List.of(new Resolvable.Expression(value, expression)));
    }

    // wrapped expressions can be at any position
    var wrappedMatcher = WRAPPED_EXPRESSION_PATTERN.matcher(value);
    if (!wrappedMatcher.find()) {
      return null;
    }
    var expressions = new ArrayList<Resolvable.Expression>();
    do {
      var placeholder = wrappedMatcher.group(0);
      var expression = wrappedMatcher.group(1).strip();
      expressions.add(new Resolvable.Expression(placeholder, expression));
    } while (wrappedMatcher.find());
    return new Resolvable(key, value, expressions);
  }
}
