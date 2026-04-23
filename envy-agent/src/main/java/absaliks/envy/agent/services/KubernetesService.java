package absaliks.envy.agent.services;

import absaliks.envy.agent.utils.Utils;
import absaliks.envy.agent.utils.Log;
import absaliks.envy.agent.utils.Shell;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

public class KubernetesService {

  private static final String NAMESPACE = "prd1741";

  private final Shell shell;
  private String context;

  public KubernetesService(Shell shell) {
    this.shell = shell;
  }

  public void setContext(String name) {
    if (!name.equals(context)) {
      var result = shell.run("kubectx $name");
      if (result.isSuccess()) {
        context = name;
      } else {
        throw new RuntimeException("Couldn't set k8s context to '$name': ${result.output}");
      }
    }
  }

  /// Go template formating secret entries in <code>#&{secret-name}.{field}={value}\n</code> format. Weird #& prefix is
  /// to be able to distinguish secret entries from error messages more reliably, when response is partially successful.
  private static final String SECRET_ENTRIES_TEMPLATE =
      "{{$name := .metadata.name}}{{range $k,$v := .data}}#&{{$name}}.{{$k}}={{$v}}{{\"\\n\"}}{{end}}";

  /// Gets list of entries for the given secrets. Map key format: <code>{secret-name}.{field}</code>
  public Map<String, String> getSecretEntries(List<String> secrets) {
    var secretsStr = String.join(" ", secrets);
    var outputTemplate =
        secrets.size() == 1
            ? SECRET_ENTRIES_TEMPLATE
            : "{{range .items}}" + SECRET_ENTRIES_TEMPLATE + "{{end}}";
    var command =
        "kubectl get secret %s -n %s -o go-template='%s'"
            .formatted(secretsStr, NAMESPACE, outputTemplate);
    var result = shell.run(command);
    if (result.isSuccess()) {
      return parseKeyValuePairs(result.output());
    } else {
      // When requesting multiple secrets at once and result is partially successful, kubectl returns entries
      // for existing secrets along with errors for failed secrets.
      var errors = Utils.lines(result.output())
          .filter(line -> !isSecretEntry(line))
          .collect(Collectors.joining("\n"));
      throw new RuntimeException("Couldn't get entries for k8s secrets: " + secrets + "\n" + errors);
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
                  .orElseThrow(() ->
                      new RuntimeException("Unable to base64-decode value '" + encodedValue + "' for key " + key));
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private static boolean isSecretEntry(String line) {
    return line.startsWith("#&") && line.contains("=");
  }
}
