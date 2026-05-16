package absaliks.envy.agent.utils;

import lombok.Getter;

/**
 * Reference holder that can be initialized only once and cannot be accessed without initialization.
 */
public class LateInit<T> {

  private volatile T value;
  @Getter private volatile boolean isInitialized;

  public synchronized void set(T value) {
    if (isInitialized) {
      throw new RuntimeException("Value has already been set to " + value);
    }
    this.value = value;
    isInitialized = true;
  }

  public T get() {
    if (!isInitialized) {
      throw new IllegalStateException("Value has not been initialized yet.");
    }
    return value;
  }

  @Override
  public String toString() {
    return isInitialized ? "LateInit{" + value + '}' : "LateInit";
  }
}
