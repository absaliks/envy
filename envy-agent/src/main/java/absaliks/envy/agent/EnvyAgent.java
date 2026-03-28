package absaliks.envy.agent;

import java.lang.instrument.Instrumentation;

public class EnvyAgent {

  public static void premain(String agentArgs, Instrumentation inst) {
    System.out.println("Hello world!");
  }
}
