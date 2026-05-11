package absaliks.envy.agent.services;

import java.util.List;

import absaliks.envy.agent.utils.LateInit;

public record Resolvable(
    String key,
    String rawValue,
    List<Expression> expressions
) {

  public record Expression(
      String placeholder,
      String expression,
      LateInit<String> value
  ) {

    public Expression(String placeholder, String expression) {
      this(placeholder, expression, new LateInit<>());
    }
  }
}
