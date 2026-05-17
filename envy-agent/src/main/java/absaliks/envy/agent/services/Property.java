package absaliks.envy.agent.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Property {

  public final String key;
  public final String originalValue;
  public final ArrayList<Expression> expressions;
  public String value;
  public boolean isSensitive;

  public Property(String key, String value, List<Expression> expressions) {
    this.key = key;
    this.originalValue = value;
    this.expressions = new ArrayList<>(expressions);
    this.value = value;
  }

  public boolean isValueChanged() {
    return !Objects.equals(originalValue, value);
  }

  public void resolve(Map<String, String> data, boolean isSensitive) {
    for (var i = 0; i < expressions.size(); i++) {
      var expression = expressions.get(i);
      if (data.containsKey(expression.expression)) {
        this.isSensitive = this.isSensitive || isSensitive;
        expressions.remove(i--);
        var resolvedValue = data.get(expression.expression);
        value = value.replace(expression.placeholder(), resolvedValue);
        var nestedExpressions = ExpressionFinder.findExpressions(resolvedValue);
        expressions.addAll(nestedExpressions);
      }
    }
  }

  public record Expression(String placeholder, String expression) {}
}
