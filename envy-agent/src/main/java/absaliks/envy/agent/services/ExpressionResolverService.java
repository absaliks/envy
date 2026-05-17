package absaliks.envy.agent.services;

import absaliks.envy.agent.services.resolvers.KubernetesSecretsResolver;
import absaliks.envy.agent.services.resolvers.MapEntryResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExpressionResolverService {

  private final MapEntryResolver envyPropertiesResolver;
  private final KubernetesSecretsResolver secretsResolver;

  public void resolve(List<Property> properties) {
    envyPropertiesResolver.resolve(properties);
    secretsResolver.resolve(properties);
  }
}
