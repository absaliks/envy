package absaliks.envy.agent.resolvers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import absaliks.envy.agent.services.SystemPropertiesProcessor;
import absaliks.envy.agent.services.resolvers.ExpressionResolver;
import absaliks.envy.agent.services.ExpressionResolverService;
import absaliks.envy.agent.services.resolvers.MapEntryResolver;

class SystemPropertiesProcessorTest {

  private static final Map<String, String> ENVY_PROPERTIES =
      Map.of(
          "username", "Joe",
          "password", "pwd123456",
          "orders.base-url", "http://orders:8080");

  private final List<ExpressionResolver> resolvers =
      List.of(new MapEntryResolver(ENVY_PROPERTIES));

  private final ExpressionResolverService expressionResolverService =
      new ExpressionResolverService(resolvers);

  @Test
  void unwrappedExpression() {
    var systemProperties =
        Map.of(
            "sys.username", "$username",
            "sys.base-url", "$orders.base-url");

    var changed = SystemPropertiesProcessor.process(systemProperties, expressionResolverService);

    assertThat(changed)
        .isEqualTo(
            Map.of(
                "sys.username", "Joe",
                "sys.base-url", "http://orders:8080"));
  }

  @Test
  void wrappedExpression() {
    var systemProperties =
        Map.of(
            "sys.username", "${username}",
            "sys.greeting", "Hello, ${username}!",
            "sys.cmd", "curl ${ orders.base-url }/api");

    var changed = SystemPropertiesProcessor.process(systemProperties, expressionResolverService);

    assertThat(changed)
        .isEqualTo(
            Map.of(
                "sys.username", "Joe",
                "sys.greeting", "Hello, Joe!",
                "sys.cmd", "curl http://orders:8080/api"));
  }

  @Test
  void multipleExpressionsInOneProperty() {
    var systemProperties =
        Map.of(
            "sys.auth1", "Basic ${username}:${password}!",
            "sys.auth2", "${username}${password}");

    var changed = SystemPropertiesProcessor.process(systemProperties, expressionResolverService);

    assertThat(changed)
        .isEqualTo(
            Map.of(
                "sys.auth1", "Basic Joe:pwd123456!",
                "sys.auth2", "Joepwd123456"));
  }

  @Test
  void unresolved() {
    var systemProperties =
        Map.of(
            "", "empty-key",
            "empty-value", "",
            "sigil", "$",
            "unbraced-sigil.in-the-middle1", "Hello $username",
            "unbraced-sigil.in-the-middle2", "Hello $username ",
            "unfinished-brace1", "Hello ${username",
            "unfinished-brace2", "Hello $username}",
            "unknown-property1", "$unknown",
            "unknown-property2", "${unknown1}:${unknown2}",
            "no-placeholders", "java1.jar:java2.jar");

    var changed = SystemPropertiesProcessor.process(systemProperties, expressionResolverService);

    assertThat(changed).isEqualTo(Map.of());
  }
}
