package absaliks.envy.agent.services;

import static absaliks.envy.agent.Agent.config;

import absaliks.envy.agent.utils.Log;
import absaliks.envy.agent.utils.Utils;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SystemPropertiesProcessor {

  public static Map<String, String> process(
      Map<String, String> systemProperties, ExpressionResolverService expResolver) {
    var resolvableProperties =
        systemProperties.entrySet().stream()
            .map(e -> createResolvablePropertyIfHasExpressions(e.getKey(), e.getValue()))
            .filter(Objects::nonNull)
            .toList();
    expResolver.resolve(resolvableProperties);
    return resolvableProperties.stream()
        .filter(Property::isValueChanged)
        .peek(
            property -> {
              var value =
                  property.isSensitive && !config().debug()
                      ? Utils.mask(property.value)
                      : property.value;
              Log.info("Resolved " + property.key + "=" + value);
            })
        .collect(Collectors.toMap(p -> p.key, p -> p.value));
  }

  private static Property createResolvablePropertyIfHasExpressions(String key, String value) {
    var expressions = ExpressionFinder.findExpressions(value);
    return expressions.isEmpty() ? null : new Property(key, value, expressions);
  }
}
