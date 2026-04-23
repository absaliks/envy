package absaliks.envy.agent.services.resolvers;

import java.util.Map;
import lombok.RequiredArgsConstructor;

/** Simple resolver that lookups expression in a Map */
@RequiredArgsConstructor
public class MapEntryResolver implements ExpressionResolver {

  private final Map<String, String> properties;

  @Override
  public String resolve(String expression) {
    return properties.get(expression);
  }
}
