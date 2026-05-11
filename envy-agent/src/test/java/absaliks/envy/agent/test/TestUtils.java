package absaliks.envy.agent.test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class TestUtils {

  public static <T> Set<T> setOf(T... elements) {
    return new LinkedHashSet<>(Arrays.asList(elements));
  }
}
