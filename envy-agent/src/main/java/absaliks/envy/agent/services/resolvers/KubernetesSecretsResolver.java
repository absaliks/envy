package absaliks.envy.agent.services.resolvers;

import lombok.RequiredArgsConstructor;

/** Looks up value in kubernetes secrets */
@RequiredArgsConstructor
public class KubernetesSecretsResolver implements ExpressionResolver {

  private final String kubernetesContextName;

  @Override
  public String resolve(String expression) {
//    return properties.get(expression);
    return null;
  }
}
