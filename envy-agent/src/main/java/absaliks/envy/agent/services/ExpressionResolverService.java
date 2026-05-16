package absaliks.envy.agent.services;

import absaliks.envy.agent.services.resolvers.ExpressionsResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExpressionResolverService {

  private final List<ExpressionsResolver> resolvers;

  public void resolve(List<Resolvable> resolvables) {
    var expressions = resolvables.stream().flatMap(r -> r.expressions().stream()).toList();
    for (var resolver : resolvers) {
      resolver.resolve(expressions);
    }
  }
}
