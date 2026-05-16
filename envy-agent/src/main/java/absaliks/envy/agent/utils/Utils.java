package absaliks.envy.agent.utils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public final class Utils {

  private Utils() {}

  public static <T> T get(T[] array, int i) {
    return array.length > i ? array[i] : null;
  }

  public static Map<String, String> toMap(Properties props) {
    var result = new HashMap<String, String>();
    props.forEach(
        (k, v) -> {
          String key = k.toString();
          String value = v.toString();
          result.put(key, value);
        });
    return result;
  }

  public static Optional<String> tryBase64Decode(String encodedValue) {
    try {
      var bytes = Base64.getDecoder().decode(encodedValue);
      return Optional.of(new String(bytes));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public static Stream<String> lines(String text) {
    return text == null ? Stream.empty() : text.lines();
  }

  /** Split command in parts for ProcessBuilder (command name and separate arguments). */
  public static List<String> splitCommand(String command) {
    var tokens = new ArrayList<String>();
    var token = new StringBuilder();
    var inQuotes = false;
    var quoteChar = '\0';

    for (int i = 0; i < command.length(); i++) {
      var ch = command.charAt(i);
      if (inQuotes) {
        if (ch == quoteChar) {
          inQuotes = false;
        } else {
          token.append(ch);
        }
      } else if (ch == '\'' || ch == '"') {
        inQuotes = true;
        quoteChar = ch;
      } else if (ch == ' ' || ch == '\t') {
        if (!token.isEmpty()) {
          tokens.add(token.toString());
          token.setLength(0);
        }
      } else {
        token.append(ch);
      }
    }
    if (!token.isEmpty()) {
      tokens.add(token.toString());
    }
    return tokens;
  }
}
