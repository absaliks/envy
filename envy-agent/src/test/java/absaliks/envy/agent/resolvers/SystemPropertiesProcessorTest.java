package absaliks.envy.agent.resolvers;

import static absaliks.envy.agent.test.TestUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import absaliks.envy.agent.Agent;
import absaliks.envy.agent.services.ExpressionResolverService;
import absaliks.envy.agent.services.KubernetesService;
import absaliks.envy.agent.services.SystemPropertiesProcessor;
import absaliks.envy.agent.services.resolvers.KubernetesSecretsResolver;
import absaliks.envy.agent.services.resolvers.MapEntryResolver;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SystemPropertiesProcessorTest {

  private static final String K8S_NAMESPACE = "kns";

  private static final Map<String, String> ENVY_PROPERTIES =
      mapOf(
          "username", "Joe",
          "password", "pwd123456",
          "orders.base-url", "http://orders:8080",
          "credentials", "${username}:${password}",
          "username-alias", "$username",
          // -----[ Secrets ]----------------------------------
          "__env.k8s-namespace", K8S_NAMESPACE,
          "users-db.username", "$k8sSecret:udb-secrets.username",
          "users-db.password", "$k8sSecret:udb-secrets.password",
          "items-db.username", "$k8sSecret:idb-secrets.username",
          "items-db.password", "$k8sSecret:idb-secrets.password",
          "udb-user", "$users-db.username");

  private final KubernetesService kubernetesService = mock(KubernetesService.class);

  private final ExpressionResolverService expressionResolverService =
      new ExpressionResolverService(
          new MapEntryResolver(ENVY_PROPERTIES),
          new KubernetesSecretsResolver(kubernetesService, ENVY_PROPERTIES)); // TODO?

  @BeforeAll
  static void beforeAll() {
    Agent.premain("dev-eu,v", null);
  }

  @BeforeEach
  void setUp() {
    mockKubernetesService(
        Map.of(
            "udb-secrets.username", "u-username",
            "udb-secrets.password", "u-password",
            "idb-secrets.username", "i-username",
            "idb-secrets.password", "i-password"));
  }

  @Nested
  class MapEntryResolverTestCases {

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
    void nestedExpressions() {
      // ${credentials} resolves into "${username}:${password}", which resolves into "Joe:pwd123456"
      var systemProperties = Map.of("sys.auth", "Basic ${credentials}");

      var changed = SystemPropertiesProcessor.process(systemProperties, expressionResolverService);

      assertThat(changed).isEqualTo(Map.of("sys.auth", "Basic Joe:pwd123456"));
    }

    @Test
    void nestedExpression1() {
      var systemProperties = Map.of("sys.user", "Hey ${username-alias}");

      var changed = SystemPropertiesProcessor.process(systemProperties, expressionResolverService);

      assertThat(changed).isEqualTo(Map.of("sys.user", "Hey Joe"));
    }

    @Test
    void multipleExpressionsInOneString() {
      var systemProperties =
          Map.of(
              "instructions",
              "Hey, ${username}! Use ${credentials} credentials to call ${orders.base-url}");

      var changed = SystemPropertiesProcessor.process(systemProperties, expressionResolverService);

      assertThat(changed)
          .isEqualTo(
              Map.of(
                  "instructions",
                  "Hey, Joe! Use Joe:pwd123456 credentials to call http://orders:8080"));
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

  @Nested
  class SecretsResolverTestCases {

    @Test
    void oneSecret() {
      var systemProperties = Map.of("sys.password", "$users-db.password");

      var changed = SystemPropertiesProcessor.process(systemProperties, expressionResolverService);

      assertThat(changed).isEqualTo(Map.of("sys.password", "u-password"));
    }

    @Test
    void multipleSecrets() {
      var systemProperties =
          Map.of(
              "datasources.users.username", "$users-db.username",
              "datasources.users.password", "$users-db.password",
              "datasources.items.username", "$items-db.username",
              "datasources.items.password", "$items-db.password");

      var changed = SystemPropertiesProcessor.process(systemProperties, expressionResolverService);

      assertThat(changed)
          .isEqualTo(
              Map.of(
                  "datasources.users.username", "u-username",
                  "datasources.users.password", "u-password",
                  "datasources.items.username", "i-username",
                  "datasources.items.password", "i-password"));
    }
  }

  @Test
  void crossResolver() {
    // "${udb-user}" resolves to "$users-db.username",
    // which resolves to "$k8sSecret:udb-secrets.username"
    // which resolves to "u-username"
    var systemProperties =
        Map.of("url", "r2dbc:mysql://${udb-user}:${users-db.password}@hostname/schema");

    var changed = SystemPropertiesProcessor.process(systemProperties, expressionResolverService);

    assertThat(changed)
        .isEqualTo(Map.of("url", "r2dbc:mysql://u-username:u-password@hostname/schema"));
  }

  private void mockKubernetesService(Map<String, String> data) {
    var entriesBySecretName =
        data.entrySet().stream()
            .collect(Collectors.groupingBy(e -> e.getKey().substring(0, e.getKey().indexOf("."))));
    when(kubernetesService.getSecretEntries(eq(K8S_NAMESPACE), any()))
        .thenAnswer(
            i -> {
              Set<String> secretNames = i.getArgument(1);
              return secretNames.stream()
                  .flatMap(
                      secretName ->
                          entriesBySecretName.getOrDefault(secretName, List.of()).stream())
                  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            });
  }
}
