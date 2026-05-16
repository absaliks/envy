package absaliks.envy.agent;

import static absaliks.envy.agent.utils.Utils.toMap;

import absaliks.envy.agent.config.Props;
import absaliks.envy.agent.services.ExpressionResolverService;
import absaliks.envy.agent.services.KubernetesService;
import absaliks.envy.agent.services.SystemPropertiesProcessor;
import absaliks.envy.agent.services.resolvers.KubernetesSecretsResolver;
import absaliks.envy.agent.services.resolvers.MapEntryResolver;
import absaliks.envy.agent.utils.Config;
import absaliks.envy.agent.utils.Log;
import absaliks.envy.agent.utils.Shell;
import java.lang.instrument.Instrumentation;
import java.util.List;

public class Agent {

  private Agent() {}

  private static Config config;

  public static Config config() {
    return config;
  }

  public static void premain(String argsString, Instrumentation inst) {
    System.out.println(
        """
          █▀▀ █▀█ █ █ █ █
          █▀▀ █ █ ▀▄▀  █
          ▀▀▀ ▀ ▀  ▀   ▀
        ———————————————————""");
    config = Config.parse(argsString);
    Log.info(config.toString());

    var k8sService = new KubernetesService(new Shell());
    var envProperties = Props.forEnv(config.env());
    var resolver =
        new ExpressionResolverService(
            List.of(
                new KubernetesSecretsResolver(k8sService, envProperties),
                new MapEntryResolver(envProperties)));

    var systemProperties = toMap(System.getProperties());
    var changedProperties = SystemPropertiesProcessor.process(systemProperties, resolver);
    changedProperties.forEach(System::setProperty);
  }
}
