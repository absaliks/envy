package absaliks.envy.agent.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import absaliks.envy.agent.utils.Utils;

public final class Props {

  public static Map<String, String> forEnv(String env) {
    var resourcePath = "/envs/" + env + ".properties";
    try (var stream = Envs.class.getResourceAsStream(resourcePath)) {
      var props = new Properties();
      props.load(stream);
      return Utils.toMap(props);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Set<String> keySet(String env) {
    return forEnv(env).keySet();
  }

  public static Set<String> keySet(List<String> envsList) {
    var allKeys = new HashSet<String>();
    for (var env : envsList) {
      allKeys.addAll(forEnv(env).keySet());
    }
    return allKeys;
  }
}
