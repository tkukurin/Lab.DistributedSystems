package co.kukurin.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;

public final class Utils {
  private Utils() {}

  public static void sleepBestEffort(long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException ignore) {
    }
  }

  public static InputStream resourceInputStream(String name) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
  }

  public static boolean udpInUse(int port) {
    try {
      new DatagramSocket(port).close();
      return false;
    } catch (IOException e) {
      return true;
    }
  }
}
