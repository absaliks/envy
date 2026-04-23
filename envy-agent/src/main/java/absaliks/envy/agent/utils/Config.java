package absaliks.envy.agent.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import absaliks.envy.agent.config.Envs;

public record Config(String env, String namespace, boolean debug) {

  public static Config parse(String str) {
    var args = str == null ? new String[0] : str.split("[;,]+");
    Map<String, String> optionalArgs =
        args.length > 1 ? parseMap(Arrays.copyOfRange(args, 1, args.length)) : Map.of();

    var env = getEnv(args);
    var namespace = getParameter(optionalArgs, "ns", "namespace").orElse(null);
    var debug = getFlag(optionalArgs, "v", "verbose");
    return new Config(env, namespace, debug);
  }

  private static boolean getFlag(Map<String, String> map, String... aliases) {
    for (var alias : aliases) {
      if (map.containsKey(alias)) {
        if (map.get(alias) != null) {
          throw new IllegalArgumentException(alias + " flag does not accept values");
        }
        return true;
      }
    }
    return false;
  }

  private static Optional<String> getParameter(Map<String, String> map, String... aliases) {
    return Stream.of(aliases).filter(map::containsKey).map(map::get).findFirst();
  }

  private static String getEnv(String[] args) {
    if (args.length > 0) {
      var env = args[0];
      if (!Envs.exists(env)) {
        throw new IllegalArgumentException(
            "Unknown environment: '%s'. Available envs: %s".formatted(env, Envs.getAll()));
      }
      return env;
    }

    throw new IllegalArgumentException(
        """
            Missing environment argument.
            How to apply: -javaagent:envy-agent.jar={env}
            Available envs: %s"""
            .formatted(Envs.getAll()));
  }

  private static Map<String, String> parseMap(String[] args) {
    var result = new HashMap<String, String>(args.length);
    for (var arg : args) {
      var equalsIndex = arg.indexOf('=');
      switch (equalsIndex) {
        case -1:
          result.put(arg, null);
          break;
        case 0:
          throw new IllegalArgumentException("'=' cannot be the first character of an argument");
        default:
          var key = arg.substring(0, equalsIndex);
          var value = arg.substring(equalsIndex + 1); // value is empty string, if equals is the last char
          result.put(key, value);
      }
    }
    return result;
  }
}
