package co.kukurin.support;

import java.util.Random;

public class EmulatedSystemClock {
  private long startTime;
  private double jitter; // percentage of deviation per 1 second

  public EmulatedSystemClock() {
    startTime = System.currentTimeMillis();
    Random r = new Random();
    jitter = (r.nextInt(400) - 200) / 1000d;
  }

  public long currentTimeMillis() {
    long current = System.currentTimeMillis();
    long diff = current - startTime;
    double coef = diff / 1000.0;
    return startTime + Math.round(diff * Math.pow((1+jitter), coef));
  }
}