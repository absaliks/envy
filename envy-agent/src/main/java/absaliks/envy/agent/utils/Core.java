package absaliks.envy.agent.utils;

public final class Core {

  private Core() {}

  public static <T> T get(T[] array, int i) {
    return array.length > i ? array[i] : null;
  }
}
