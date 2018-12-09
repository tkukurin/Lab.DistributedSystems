package co.kukurin.support;

import java.util.Random;

public class EmulatedSystemClock {
  private long delta;
  private long startTime;
  private double jitter; // percentage of deviation per 1 second

  public EmulatedSystemClock() {
    delta = 0;
    startTime = System.currentTimeMillis();
    Random r = new Random();
    jitter = (r.nextInt(400) - 200) / 1000d;
  }

  public long currentTimeMillis() {
    long current = System.currentTimeMillis();
    long diff = current - startTime;
    double coef = diff / 1000.0;
    return delta + startTime + Math.round(diff * Math.pow((1+jitter), coef));
  }

  public long updateClock(long otherTime) {
    if (otherTime <= currentTimeMillis()) {
      delta = otherTime - currentTimeMillis() + 1;
    }
    return currentTimeMillis();
  }
}