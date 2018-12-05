package co.kukurin.support;

import co.kukurin.common.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class Measurements {

  private final List<Measurement> measurements = new ArrayList<>();

  public Measurement getReading(long secs) {
    // formula from the document, adjusted to 0-index and skip header.
    return this.measurements.get((int) (secs % this.measurements.size()));
  }

  public static Measurements fromFile(String name) {
    Measurements result = new Measurements();
    InputStream stream = Utils.resourceInputStream(name);

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
      // skip header: "Temperature,Pressure,Humidity,CO,NO2,SO2,"
      reader.readLine();

      String line;
      while ((line = reader.readLine()) != null) {
        result.measurements.add(Measurement.parse(line));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  public static class Measurement {

    private double co;

    public static Measurement parse(String line) {
      return new Measurement(Double.parseDouble(line.split(",")[3]));
    }
  }

}


