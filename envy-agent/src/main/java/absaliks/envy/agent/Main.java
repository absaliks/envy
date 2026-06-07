package absaliks.envy.agent;

import absaliks.envy.agent.config.Envs;
import absaliks.envy.agent.config.Props;
import absaliks.envy.agent.utils.Utils;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

  static void main(String[] args) {
    var env = Utils.get(args, 0);
    var envs = Envs.getAll();
    var propertiesString = env != null
        ? sortAndFormat(Props.forEnv(env))
        : sortAndFormat(Props.keySet(envs));
    System.out.printf(
        """
        This is a java agent jar implementing "premain" method, \
        it has to be attached to a java app before it starts. \
        This can be done with command line option: -javaagent:envy-agent.jar={environment}

        Available environments:
        %s

        Available properties:
        %s
        """,
        sortAndFormat(envs), propertiesString);
  }

  private static String sortAndFormat(Collection<String> items) {
    return items.stream().sorted().collect(Collectors.joining("\n- ", "- ", ""));
  }

  private static String sortAndFormat(Map<String, String> items) {
    var maxKeyLength = items.keySet().stream().mapToInt(String::length).max().orElse(0);
    return items.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> "- " + Utils.rightPad(entry.getKey(), maxKeyLength) + " = " + entry.getValue() + "\n")
        .collect(Collectors.joining());
  }
}
