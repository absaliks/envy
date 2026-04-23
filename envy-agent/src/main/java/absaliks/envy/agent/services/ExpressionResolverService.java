package absaliks.envy.agent.services;

import absaliks.envy.agent.services.resolvers.ExpressionResolver;
import absaliks.envy.agent.utils.Log;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExpressionResolverService {

  private final List<ExpressionResolver> resolvers;

  /** Attempts to resolve expression. Returns empty, if unable. */
  public Optional<String> resolve(String key, String expression) {
    var trimmedExpression = expression.strip();
    for (var resolver : resolvers) {
      var resolved = resolver.resolve(trimmedExpression);
      if (resolved != null) {
        Log.info("Resolved %s = %s from expression: %s", key, resolved, trimmedExpression);
        return Optional.of(resolved);
      }
    }
    Log.info("Unable to resolve expression '" + trimmedExpression + "' for key " + key);
    return Optional.empty();
  }
}
