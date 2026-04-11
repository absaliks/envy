package absaliks.envy.agent.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public final class Props {

  public static Map<String, String> getAll(String env) {
    var resourcePath = "/envs/" + env + ".properties";
    try (var stream = Envs.class.getResourceAsStream(resourcePath)) {
      var props = new Properties();
      props.load(stream);
      return toMap(props);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Map<String, String> toMap(Properties props) {
    var result = new HashMap<String, String>();
    for (var entry : props.entrySet()) {
      result.put(entry.getKey().toString(), entry.getValue().toString());
    }
    return result;
  }

  public static Set<String> keySet(String env) {
    return getAll(env).keySet();
  }

  public static Set<String> keySet(List<String> envsList) {
    var allKeys = new HashSet<String>();
    for (var env : envsList) {
      allKeys.addAll(getAll(env).keySet());
    }
    return allKeys;
  }
}
