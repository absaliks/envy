package absaliks.envy.env;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import absaliks.envy.util.FileUtil;

public final class EnvironmentsLoader {

  public static Map<String, Env> fromFile(String filename) {
    var text = FileUtil.readResource(filename);
    var props = new Yaml().<List<EnvProperties>> load(text);
    var envs = props.stream()
        .map(EnvironmentsLoader::createEnv)
        .toList();
    setParents(envs);
    var envIndex = indexEnvs(new HashMap<>(), envs);
    validateResolutionPaths(envIndex);
    return envIndex;
  }

  private static void setParents(List<Env> envs) {
    setParents(envs, null);
  }

  private static void setParents(List<Env> envs, Env parent) {
    for (var env : envs) {
      env.parent().set(parent);
      setParents(env.children(), env);
    }
  }

  private static Env createEnv(EnvProperties props) {
    var children = props.children().stream()
        .map(EnvironmentsLoader::createEnv)
        .toList();
    return Env.builder()
        .name(props.name())
        .k8sContext(props.k8sContext())
        .resolutionPath(props.resolutionPath())
        .children(children)
        .build();
  }

  private static Map<String, Env> indexEnvs(Map<String, Env> map, List<Env> envs) {
    envs.forEach(env -> {
      if (map.put(env.name(), env) == null) {
        throw new IllegalStateException("Environment '${env.name}' defined multiple times");
      }
      indexEnvs(map, env.children());
    });
    return map;
  }

  private static void validateResolutionPaths(Map<String, Env> map) {
    for (Env env : map.values())
      for (String pathItem : env.resolutionPath())
        if (!map.containsKey(pathItem))
          throw new IllegalStateException(
              "Environment "
                  + env.name()
                  + " refers undefined resolution path element: "
                  + pathItem);
  }

}
