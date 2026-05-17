package absaliks.envy.agent.test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TestUtils {

  public static <T> Set<T> setOf(T... elements) {
    return new LinkedHashSet<>(Arrays.asList(elements));
  }

  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> mapOf(Object... keysAndValues) {
    var map = new LinkedHashMap<K, V>(keysAndValues.length / 2);
    for (int i = 0; i < keysAndValues.length; i += 2) {
      map.put((K) keysAndValues[i], (V) keysAndValues[i + 1]);
    }
    return map;
  }
}
