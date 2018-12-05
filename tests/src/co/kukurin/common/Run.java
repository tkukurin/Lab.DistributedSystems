package co.kukurin.common;


public class Run implements Runnable {

  private final ThrowableRunnable runnable;

  public Run(ThrowableRunnable runnable) {
    this.runnable = runnable;
  }

  @Override
  public void run() {
    try {
      this.runnable.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

