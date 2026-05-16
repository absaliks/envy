package absaliks.envy.agent.resolvers;

import static absaliks.envy.agent.test.TestUtils.setOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import absaliks.envy.agent.services.KubernetesService;
import absaliks.envy.agent.utils.Shell;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class KubernetesServiceTest {

  private static final int EXIT_CODE_SUCCESS = 0;
  private final Shell shell = mock(Shell.class);

  private final KubernetesService kubernetesService = new KubernetesService(shell);

  @Test
  void getSecretEntries_oneSecret() {
    var expectedCommand =
        """
        kubectl get secret db-secrets -n k8s-ns1 -o go-template='{{$name := .metadata.name}}{{range $k,$v := .data}}#&{{$name}}.{{$k}}={{$v}}{{"\\n"}}{{end}}'""";
    when(shell.run(expectedCommand))
        .thenReturn(
            new Shell.Result(
                """
                #&db-secrets._raw=eyJkYXRhIjp7InVzZXJuYW1lIjoiam9obi5kb2UiLCJwYXNzd29yZCI6InB3ZDEyMzQ1NiJ9LCJtZXRhZGF0YSI6eyJjcmVhdGVkX3RpbWUiOiIyMDI1LTEyLTE3VDE3OjI4OjEzLjUyNFoiLCJ2ZXJzaW9uIjoxfX0=
                #&db-secrets.username=am9obi5kb2U=
                #&db-secrets.password=cHdkMTIzNDU2
                """,
                EXIT_CODE_SUCCESS));
    var entries = kubernetesService.getSecretEntries("k8s-ns1", setOf("db-secrets"));

    assertThat(entries)
        .isEqualTo(
            Map.of(
                "db-secrets._raw",
                "{\"data\":{\"username\":\"john.doe\",\"password\":\"pwd123456\"},\"metadata\":{\"created_time\":\"2025-12-17T17:28:13.524Z\",\"version\":1}}",
                "db-secrets.username",
                "john.doe",
                "db-secrets.password",
                "pwd123456"));
  }

  @Test
  void getSecretEntries_twoSecrets() {
    var expectedCommand =
        """
        kubectl get secret db-secrets api-secrets -n k8s-ns2 -o go-template='{{range .items}}{{$name := .metadata.name}}{{range $k,$v := .data}}#&{{$name}}.{{$k}}={{$v}}{{"\\n"}}{{end}}{{end}}'""";
    when(shell.run(expectedCommand))
        .thenReturn(
            new Shell.Result(
                """
                #&db-secrets._raw=eyJkYXRhIjp7InVzZXJuYW1lIjoiam9obi5kb2UiLCJwYXNzd29yZCI6InB3ZDEyMzQ1NiJ9LCJtZXRhZGF0YSI6eyJjcmVhdGVkX3RpbWUiOiIyMDI1LTEyLTE3VDE3OjI4OjEzLjUyNFoiLCJ2ZXJzaW9uIjoxfX0=
                #&db-secrets.username=am9obi5kb2U=
                #&db-secrets.password=cHdkMTIzNDU2
                #&api-secrets._raw=eyJkYXRhIjp7InRva2VuIjoiYWJyYWNhZGFicmEifSwibWV0YWRhdGEiOnsiY3JlYXRlZF90aW1lIjoiMjAyNi0wMy0wN1QxMjo0NTozNC4xMjFaIiwidmVyc2lvbiI6Mn19
                #&api-secrets.token=YWJyYWNhZGFicmE=
                """,
                EXIT_CODE_SUCCESS));

    var entries = kubernetesService.getSecretEntries("k8s-ns2", setOf("db-secrets", "api-secrets"));

    assertThat(entries)
        .isEqualTo(
            Map.of(
                "db-secrets._raw",
                "{\"data\":{\"username\":\"john.doe\",\"password\":\"pwd123456\"},\"metadata\":{\"created_time\":\"2025-12-17T17:28:13.524Z\",\"version\":1}}",
                "db-secrets.username",
                "john.doe",
                "db-secrets.password",
                "pwd123456",
                "api-secrets._raw",
                "{\"data\":{\"token\":\"abracadabra\"},\"metadata\":{\"created_time\":\"2026-03-07T12:45:34.121Z\",\"version\":2}}",
                "api-secrets.token",
                "abracadabra"));
  }

  @Test
  void getSecretEntries_twoSecrets_oneNotFound() {
    var expectedCommand =
        """
        kubectl get secret db-secrets api-secrets -n k8s-ns3 -o go-template='{{range .items}}{{$name := .metadata.name}}{{range $k,$v := .data}}#&{{$name}}.{{$k}}={{$v}}{{"\\n"}}{{end}}{{end}}'""";
    when(shell.run(expectedCommand))
        .thenReturn(
            new Shell.Result(
                """
                #&db-secrets._raw=eyJkYXRhIjp7InVzZXJuYW1lIjoiam9obi5kb2UiLCJwYXNzd29yZCI6InB3ZDEyMzQ1NiJ9LCJtZXRhZGF0YSI6eyJjcmVhdGVkX3RpbWUiOiIyMDI1LTEyLTE3VDE3OjI4OjEzLjUyNFoiLCJ2ZXJzaW9uIjoxfX0=
                #&db-secrets.username=am9obi5kb2U=
                #&db-secrets.password=cHdkMTIzNDU2
                #&api-secrets._raw=eyJkYXRhIjp7InRva2VuIjoiYWJyYWNhZGFicmEifSwibWV0YWRhdGEiOnsiY3JlYXRlZF90aW1lIjoiMjAyNi0wMy0wN1QxMjo0NTozNC4xMjFaIiwidmVyc2lvbiI6Mn19
                #&api-secrets.token=YWJyYWNhZGFicmE=
                """,
                EXIT_CODE_SUCCESS));

    var entries = kubernetesService.getSecretEntries("k8s-ns3", setOf("db-secrets", "api-secrets"));

    assertThat(entries)
        .isEqualTo(
            Map.of(
                "db-secrets._raw",
                "{\"data\":{\"username\":\"john.doe\",\"password\":\"pwd123456\"},\"metadata\":{\"created_time\":\"2025-12-17T17:28:13.524Z\",\"version\":1}}",
                "db-secrets.username",
                "john.doe",
                "db-secrets.password",
                "pwd123456",
                "api-secrets._raw",
                "{\"data\":{\"token\":\"abracadabra\"},\"metadata\":{\"created_time\":\"2026-03-07T12:45:34.121Z\",\"version\":2}}",
                "api-secrets.token",
                "abracadabra"));
  }

  @Test
  void encode() {
    Stream.of(
            """
        {"data":{"token":"abracadabra"},"metadata":{"created_time":"2026-03-07T12:45:34.121Z","version":2}}""",
            "abracadabra")
        .forEach(
            str -> {
              var bytes = Base64.getEncoder().encode(str.getBytes());
              var encoded = new String(bytes, StandardCharsets.UTF_8);
              System.out.println("---");
              System.out.println(str);
              System.out.println(encoded);
            });
  }
}
