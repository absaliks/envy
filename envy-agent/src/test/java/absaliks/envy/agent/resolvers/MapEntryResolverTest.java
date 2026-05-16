package absaliks.envy.agent.resolvers;

import static org.assertj.core.api.Assertions.assertThat;

import absaliks.envy.agent.services.Resolvable;
import absaliks.envy.agent.services.resolvers.MapEntryResolver;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
    var resolvableExpression = new Resolvable.Expression("$" + expression, expression);

    resolver.resolve(List.of(resolvableExpression));

    var value =
        resolvableExpression.value().isInitialized() ? resolvableExpression.value().get() : null;
    assertThat(value).isEqualTo(expectedValue);
  }
}
