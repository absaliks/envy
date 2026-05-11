package absaliks.envy.agent.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UtilsTest {

  public static Stream<Arguments> splitCommand() {
    return Stream.of(
        Arguments.of("pwd", List.of("pwd")),
        Arguments.of("ls -l", List.of("ls", "-l")),
        Arguments.of("ls -l", List.of("ls", "-l")),
        Arguments.of(
            "git reset --soft origin/XYZ-12345-hello",
            List.of("git", "reset", "--soft", "origin/XYZ-12345-hello")),
        Arguments.of(
            "kubectl get secret secrets1 -o jsonpath='{ .data }'",
            List.of("kubectl", "get", "secret", "secrets1", "-o", "jsonpath={ .data }")));
  }

  @ParameterizedTest
  @MethodSource
  void splitCommand(String source, List<String> expected) {
    assertThat(Utils.splitCommand(source)).isEqualTo(expected);
  }
}
