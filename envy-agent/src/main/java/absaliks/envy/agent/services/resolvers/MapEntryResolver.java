package absaliks.envy.agent.services.resolvers;

import absaliks.envy.agent.services.Resolvable.Expression;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/** Simple resolver that lookups expression in a Map */
@RequiredArgsConstructor
public class MapEntryResolver implements ExpressionsResolver {

  private final Map<String, String> properties;

  @Override
  public void resolve(List<Expression> expressions) {
    expressions.forEach(
        e -> {
          if (!e.value().isInitialized() && properties.containsKey(e.expression())) {
            var value = properties.get(e.expression());
            e.value().set(value);
          }
        });
  }
}
