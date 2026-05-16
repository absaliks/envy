package absaliks.envy.agent.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Shell {

  public Result run(String command) {
    Log.debug("<shell> %s", command);
    Process process = null;
    try {
      process = new ProcessBuilder(Utils.splitCommand(command)).redirectErrorStream(true).start();

      var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      var exitCode = process.waitFor();
      return new Result(output, exitCode);
    } catch (InterruptedException | IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
  }

  public record Result(String output, int exitCode) {
    public boolean isSuccess() {
      return exitCode == 0;
    }
  }
}
