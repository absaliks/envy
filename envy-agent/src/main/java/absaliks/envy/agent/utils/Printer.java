package absaliks.envy.agent.utils;

public class Printer {

  private static final String BORDER = "> ";

  public static void printBanner() {
    System.out.println(
        """
          █▀▀ █▀█ █ █ █ █
          █▀▀ █ █ ▀▄▀  █
          ▀▀▀ ▀ ▀  ▀   ▀
        ———————————————————""");
  }

  public static void print(Object string) {
    System.out.println(BORDER + string);
  }
}
