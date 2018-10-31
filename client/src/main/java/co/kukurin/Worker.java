package co.kukurin;

import co.kukurin.Measurements.Measurement;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Worker implements Runnable {
  public static final int SERVER_SHUTDOWN = 42;

  private Supplier<Measurement> measurementSupplier;
  private OutputStream outputStream;
  private InputStream inputStream;

  @Override
  public void run() {
    try (PrintWriter writer = new PrintWriter(this.outputStream, true);
          InputStreamReader reader = new InputStreamReader(this.inputStream)) {
      while (reader.read() != SERVER_SHUTDOWN) {
        writer.println(measurementSupplier.get().toString());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
