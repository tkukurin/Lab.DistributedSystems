package co.kukurin;

import co.kukurin.Measurements.Measurement;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Worker implements Runnable {

  private Measurement measurement;
  private OutputStream outputStream;

  @Override
  public void run() {
    try (PrintWriter writer = new PrintWriter(this.outputStream)) {
      writer.println("HTTP/1.1 200 OK\r\n\r\n");
    }
  }
}
