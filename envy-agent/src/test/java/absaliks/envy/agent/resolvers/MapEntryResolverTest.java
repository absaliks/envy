package absaliks.envy.agent.resolvers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import absaliks.envy.agent.services.resolvers.MapEntryResolver;

class MapEntryResolverTest {

  private final MapEntryResolver resolver =
      new MapEntryResolver(
          Map.of(
              "usr", "john.doe",
              "orders.base-url", "http://orders:8080"));

  @ParameterizedTest
  @CsvSource(
      textBlock =
          """
          usr,                   john.doe
          orders.base-url,       http://orders:8080
          
          # ----[ unresolvable ]---------------------
          unknown,
          server.port,
          """)
  void resolve(String expression, String expectedValue) {
    var resolved = resolver.resolve(expression);

    assertThat(resolved).isEqualTo(expectedValue);
  }
}
