package absaliks.envy.env;

import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import absaliks.envy.util.LateInit;
import lombok.Builder;

@Builder
@NullMarked
public record Env(
    String name,
    @Nullable String k8sContext,
    List<String> resolutionPath,
    List<Env> children,
    LateInit<@Nullable Env> parent
) {

  public boolean hasParent() {
    return parent.get() != null;
  }

  public boolean isLeaf() {
    return children.isEmpty();
  }

  @Override
  public String toString() {
    // replacing parent with its name to avoid StackOverflow due to circular dependencies
    var parent = this.parent.get();
    var parentName = parent != null ? parent.name() : null;
    return "Env{" +
        "name='" + name + '\'' +
        ", k8sContext='" + k8sContext + '\'' +
        ", resolutionPath=" + resolutionPath +
        ", children=" + children +
        ", parent=" + parentName +
        '}';
  }
}