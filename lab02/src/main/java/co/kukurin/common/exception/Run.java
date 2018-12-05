package co.kukurin.common.exception;

import java.util.function.Consumer;
import lombok.Builder;

@Builder
public class Run implements Runnable {

  private final ThrowableRunnable runnable;
  private Consumer<Exception> handler;

  @Override
  public void run() {
    try {
      this.runnable.run();
    } catch (Exception e) {
      this.handler.accept(e);
    }
  }
}
