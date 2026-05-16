package absaliks.envy.agent.services.resolvers;

import absaliks.envy.agent.services.Resolvable;
import java.util.List;

public interface ExpressionsResolver {

  void resolve(List<Resolvable.Expression> expressions);
}
