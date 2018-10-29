package co.kukurin;

import co.kukurin.Measurements.Measurement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Worker implements Runnable {

  private Supplier<Measurement> measurementSupplier;
  private OutputStream outputStream;
  private InputStream inputStream;

  @Override
  public void run() {
    try (PrintWriter writer = new PrintWriter(this.outputStream, true);
          BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream))) {
      String request = reader.readLine();
      writer.println(measurementSupplier.get().toString());
//      writer.print(measurementSupplier.get());
//      String input;
//      while ((input = reader.readLine()) != null) {
//        if (!input.equals("ping")) {
//          break;
//        }
//        writer.println(measurementSupplier.get());
//      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
