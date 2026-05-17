package absaliks.envy.agent.services.resolvers;

import absaliks.envy.agent.services.Property;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/** Simple resolver that lookups expression in a Map */
@RequiredArgsConstructor
public class MapEntryResolver {

  private final Map<String, String> data;

  public void resolve(List<Property> property) {
    property.forEach(p -> p.resolve(data, false));
  }
}
