package absaliks.envy.util;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Reference holder that can be initialized only once and cannot be accessed without initialization. */
@NullMarked
public class LateInit<T extends @Nullable Object> {

  private T value;
  private boolean isInitialized;

  public synchronized void set(T value) {
    if (isInitialized) {
      throw new RuntimeException("Value has already been set to " + value);
    }
    this.value = value;
    isInitialized = true;
  }

  public synchronized T get() {
    if (!isInitialized) {
      throw new IllegalStateException("Value has not been initialized yet.");
    }
    return value;
  }
}
