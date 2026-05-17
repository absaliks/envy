package absaliks.envy.agent.services.resolvers;

import absaliks.envy.agent.services.KubernetesService;
import absaliks.envy.agent.services.Property;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/** Looks up value in kubernetes secrets */
@RequiredArgsConstructor
public class KubernetesSecretsResolver {

  private static final String PREFIX = "k8sSecret:";

  private final KubernetesService kubernetesService;
  private final Map<String, String> envProperties;

  public void resolve(List<Property> properties) {
    var secrets =
        properties.stream()
            .flatMap(p -> p.expressions.stream())
            .map(K8sSecret::evaluate)
            .filter(Objects::nonNull)
            .toList();
    if (secrets.isEmpty()) {
      return;
    }

    var k8sContext = envProperties.get("__env.k8s-context");
    if (k8sContext != null) {
      kubernetesService.setContext(k8sContext);
    }
    var k8sNamespace = envProperties.get("__env.k8s-namespace");
    var secretNames = secrets.stream().map(K8sSecret::name).collect(Collectors.toSet());
    var entries = kubernetesService.getSecretEntries(k8sNamespace, secretNames);
    var resolvedExpressionsMap =
        secrets.stream()
            .map(
                ks -> {
                  var value = entries.get(ks.key);
                  return new SimpleEntry<>(ks.expression, value);
                })
            .filter(e -> e.getValue() != null)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    properties.forEach(p -> p.resolve(resolvedExpressionsMap, true));
  }

  /**
   * @param expression <code>"k8sSecret:{secretName}.{secretField}"</code>
   * @param name <code>"{secretName}"</code>
   * @param key <code>"{secretName}.{secretField}"</code>
   */
  private record K8sSecret(String expression, String name, String key) {

    static K8sSecret evaluate(Property.Expression expression) {
      var expressionStr = expression.expression();
      int dotIndex;
      if (expressionStr.startsWith(PREFIX)
          && (dotIndex = expressionStr.indexOf('.', PREFIX.length() + 1)) > -1
          && dotIndex < expressionStr.length() - 1 // field name is at least 1 char long
      ) {
        var key = expressionStr.substring(PREFIX.length());
        var secret = expressionStr.substring(PREFIX.length(), dotIndex);
        return new K8sSecret(expressionStr, secret, key);
      }
      return null;
    }
  }
}
