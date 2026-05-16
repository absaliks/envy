package absaliks.envy.agent.utils;

import static absaliks.envy.agent.Agent.config;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

public class Log {

  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

  public static void info(String string, Object... args) {
    log(string, args);
  }

  public static void debug(Supplier<String> message) {
    if (config().debug()) log(message.get());
  }

  public static void debug(String message, Object... args) {
    if (config().debug()) {
      log(message, args);
    }
  }

  public static void error(String message, Object... args) {
    log("ERROR " + message, args);
  }

  private static void log(String message, Object... args) {
    var time = LocalTime.now().format(TIME_FORMAT);
    var formatted = args.length == 0 ? message : message.formatted(args);
    System.out.println(time + " [ENVY] " + formatted);
  }
}
