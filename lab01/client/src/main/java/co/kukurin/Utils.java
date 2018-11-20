package co.kukurin;

import java.util.Random;

public class Utils {
  private Utils() {}

  public static double random(Random random, double rangeLow, double rangeHigh) {
    return rangeLow + random.nextDouble() * (rangeHigh - rangeLow);
  }

  public static long currentTimeSeconds() {
    return System.currentTimeMillis() / 1000;
  }

  public static void sleepBestEffort(int i) {
    try {
      Thread.sleep(i);
    } catch (InterruptedException ignore) {
    }
  }
}
