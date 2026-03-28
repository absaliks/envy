package absaliks.envy.env;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EnvironmentsLoaderTest {

  @Test
  void fromFile() {
    var envs = EnvironmentsLoader.fromFile("/envs.yaml");
    envs.forEach((k, v) -> System.out.println(k + ": " + v));
  }
}
