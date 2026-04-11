package absaliks.envy.agent.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class Envs {

  private static final String ENVS_DIR = "/envs";

  private Envs() {}

  public static boolean exists(String env) {
    return Envs.class.getResource(ENVS_DIR + "/" + env + ".properties") != null;
  }

  public static List<String> getAll() {
    var resourceUrl = Envs.class.getResource(ENVS_DIR);
    if (resourceUrl == null) {
      return List.of();
    }
    try {
      var uri = resourceUrl.toURI();
      if ("jar".equals(uri.getScheme())) {
        // When running from a JAR, getResource("/envs") returns a "jar:file:..." URI.
        // Path.of() can't resolve it unless the JAR's FileSystem is opened first.
        try (var fileSystem = FileSystems.newFileSystem(uri, Map.of())) {
          return getAll(fileSystem.getPath(ENVS_DIR));
        }
      }
      // In the IDE, resources are on disk so the URI is "file:..."
      return getAll(Path.of(uri));
    } catch (URISyntaxException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static List<String> getAll(Path envsDir) throws IOException {
    try (var stream = Files.list(envsDir)) {
      return stream.map(Envs::filenameWithoutExtension).toList();
    }
  }

  private static String filenameWithoutExtension(Path path) {
    var fileName = path.getFileName();
    if (fileName == null) {
      throw new IllegalArgumentException("Bad path: " + path);
    }
    var filename = fileName.toString();
    var i = filename.lastIndexOf('.');
    return i == -1 ? filename : filename.substring(0, i);
  }
}
