package absaliks.envy.env;

import java.util.List;

record EnvProperties(
  String name,
  String k8sContext,
  List<String> resolutionPath,
  List<EnvProperties> children
) {}
