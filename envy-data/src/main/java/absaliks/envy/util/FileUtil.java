package absaliks.envy.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class FileUtil {

  public static String readResource(String filename) {
    try (var stream = FileUtil.class.getResourceAsStream(filename)) {
      if (stream == null) {
        throw new RuntimeException("Resource not found: " + filename);
      }
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
