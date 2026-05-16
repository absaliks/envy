package absaliks.envy.agent.services;

import absaliks.envy.agent.utils.Log;
import absaliks.envy.agent.utils.Shell;
import absaliks.envy.agent.utils.Utils;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class KubernetesService {

  private final Shell shell;

  public KubernetesService(Shell shell) {
    this.shell = shell;
  }

  public void setContext(String name) {
    var result = shell.run("kubectx " + name);
    if (!result.isSuccess()) {
      throw new RuntimeException("Couldn't set k8s context to '%s': %s".formatted(name, result));
    }
  }

  /**
   * Go template formating secret entries in <code>#&{secret-name}.{field}={value}\n</code> format.
   * Weird #& prefix is to be able to distinguish secret entries from error messages more reliably,
   * when response is partially successful.
   */
  private static final String SECRET_ENTRIES_TEMPLATE =
      "{{$name := .metadata.name}}{{range $k,$v := .data}}#&{{$name}}.{{$k}}={{$v}}{{\"\\n\"}}{{end}}";

  /// Gets list of entries for the given secrets. Map key format: <code>{secret-name}.{field}</code>
  public Map<String, String> getSecretEntries(String namespace, Set<String> secretNames) {
    var secretNamesStr = String.join(" ", secretNames);
    var outputTemplate =
        secretNames.size() == 1
            ? SECRET_ENTRIES_TEMPLATE
            : "{{range .items}}" + SECRET_ENTRIES_TEMPLATE + "{{end}}";
    var command =
        "kubectl get secret %s -n %s -o go-template='%s'"
            .formatted(secretNamesStr, namespace, outputTemplate);
    var result = shell.run(command);
    if (result.isSuccess()) {
      return parseKeyValuePairs(result.output());
    } else {
      // When requesting multiple secrets at once and the result is partially successful, kubectl
      // returns entries
      // for existing secrets along with errors for failed secrets. We don't want to print secret
      // values.
      var errors =
          Utils.lines(result.output())
              .filter(line -> !isSecretEntry(line))
              .collect(Collectors.joining("\n"));
      throw new RuntimeException(
          "Couldn't get entries for k8s secrets: " + secretNames + "\n" + errors);
    }
  }

  private Map<String, String> parseKeyValuePairs(String output) {
    return Utils.lines(output)
        .filter(KubernetesService::isSecretEntry)
        .map(
            line -> {
              var eqIndex = line.indexOf('=');
              if (eqIndex == -1) {
                Log.error("Unexpected line: " + line);
                return null;
              }
              var key = line.substring(2, eqIndex); // 2 to trim "#&" prefix before each entry
              var encodedValue = line.substring(eqIndex + 1);
              return Utils.tryBase64Decode(encodedValue)
                  .map(value -> Map.entry(key, value))
                  .orElseThrow(
                      () ->
                          new RuntimeException(
                              "Unable to base64-decode value '"
                                  + encodedValue
                                  + "' for key "
                                  + key));
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private static boolean isSecretEntry(String line) {
    return line.startsWith("#&") && line.contains("=");
  }
}
