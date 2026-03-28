package absaliks.envy.data;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import org.yaml.snakeyaml.Yaml;

public class YamlToPropertiesConverter {

  public static void main(String[] args) throws IOException {
    var outputDir = System.getProperty("outputDir");
    if (outputDir == null) {
      throw new IllegalStateException("System property 'outputDir' is required");
    }

    try (var input = YamlToPropertiesConverter.class.getResourceAsStream("/data.yaml")) {
      if (input == null) {
        throw new FileNotFoundException("data.yaml not found on classpath");
      }
      Map<String, Object> yaml = new Yaml().load(input);
      var properties = new Properties();
      flatten("", yaml, properties);

      var outputPath = Path.of(outputDir, "data.properties");
      try (var writer = new FileWriter(outputPath.toFile())) {
        properties.store(writer, null);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static void flatten(String prefix, Map<String, Object> map, Properties target) {
    for (var entry : map.entrySet()) {
      var key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
      if (entry.getValue() instanceof Map<?, ?> nested) {
        flatten(key, (Map<String, Object>) nested, target);
      } else {
        target.setProperty(key, String.valueOf(entry.getValue()));
      }
    }
  }
}
