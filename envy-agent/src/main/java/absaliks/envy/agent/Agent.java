package absaliks.envy.agent;

import static absaliks.envy.agent.utils.Printer.print;
import static absaliks.envy.agent.utils.Printer.printBanner;

import absaliks.envy.agent.resolvers.ExpressionResolver;
import absaliks.envy.agent.utils.Args;
import absaliks.envy.agent.config.Props;

import java.lang.instrument.Instrumentation;
import java.util.Objects;

public class Agent {

  private Agent() {}

  public static void premain(String argsString, Instrumentation inst) {
    printBanner();
    var args = Args.parse(argsString);
    print(args);

    var interpolationPrefix = args.prefix() + "{";
    var envProperties = Props.getAll(args.env());
    var resolver = new ExpressionResolver(envProperties);

    System.getProperties()
        .forEach(
            (k, v) -> {
              var rawValue = Objects.toString(v);
              var startIndex = rawValue.indexOf(interpolationPrefix);
              var endIndex =
                  startIndex == -1
                      ? -1
                      : rawValue.indexOf('}', startIndex + interpolationPrefix.length());
              if (startIndex > -1 && endIndex > -1) {
                var expression =
                    rawValue.substring(startIndex + interpolationPrefix.length(), endIndex);
                System.out.println(k + "=" + resolver.resolve(expression));
              }
            });
  }
}
