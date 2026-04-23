package absaliks.envy.agent;

import absaliks.envy.agent.utils.Utils;
import absaliks.envy.agent.config.Envs;
import absaliks.envy.agent.config.Props;
import java.util.Collection;
import java.util.stream.Collectors;

public class Main {

  static void main(String[] args) {
    var env = Utils.get(args, 0);
    var envs = Envs.getAll();
    var propertiesSet = env != null ? Props.keySet(env) : Props.keySet(envs);
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
        sortAndFormat(envs), sortAndFormat(propertiesSet));
  }

  private static String sortAndFormat(Collection<String> items) {
    return items.stream().sorted().collect(Collectors.joining("\n- ", "- ", ""));
  }
}
