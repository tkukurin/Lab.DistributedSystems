package co.kukurin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

class Measurements {

  @AllArgsConstructor
  @Getter
  static class Measurement {
    String temperature;
    String pressure;
    String humidity;
    String co;
    String no2;
    String so2;

    @Override
    public String toString() {
      return String.format("%s,%s,%s,%s,%s,%s", temperature, pressure, humidity, co, no2, so2);
    }

    public static Measurement parse(String line) {
//      List<Optional<Double>> components = Arrays.stream(line.trim().split(","))
//          .map(Measurements::doubleOrEmpty)
//          .collect(Collectors.toList());
      List<String> components = Arrays.stream(line.trim().split(",")).collect(Collectors.toList());
      return new Measurement(
          guard(components, 0),
          guard(components, 1),
          guard(components, 2),
          guard(components, 3),
          guard(components, 4),
          guard(components, 5));
    }

    public static Measurement average(Measurement m1, Measurement m2) {
      return new Measurement(
          averageOrSingle(m1.getTemperature(), m2.getTemperature()),
          averageOrSingle(m1.getPressure(), m2.getPressure()),
          averageOrSingle(m1.getHumidity(), m2.getHumidity()),
          averageOrSingle(m1.getCo(), m2.getCo()),
          averageOrSingle(m1.getNo2(), m2.getNo2()),
          averageOrSingle(m1.getSo2(), m2.getSo2()));
    }

    private static String averageOrSingle(String s1, String s2) {
      if (s1 == null || s1.isEmpty()) {
        return s2;
      }
      if (s2 == null || s2.isEmpty()) {
        return s1;
      }
      double d1 = Double.parseDouble(s1);
      double d2 = Double.parseDouble(s2);
      return Double.toString((d1 + d2) / 2.0);
    }
  }

  private List<Measurement> measurements = new ArrayList<>();

  public Measurement getReading(long secs) {
    // formula from the document, adjusted to 0-index and skip header.
    return this.measurements.get((int)(secs % 100));
  }

  static Measurements fromFile(String resourceLocation) {
    Measurements result = new Measurements();
    InputStream stream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceLocation);

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
      // skip header: "Temperature,Pressure,Humidity,CO,NO2,SO2,"
      reader.readLine();

      while (true) {
        String line = reader.readLine();
        if (line == null) {
          break;
        }

        result.measurements.add(Measurement.parse(line));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }

  private static String guard(List<String> components, int i) {
    if (components.size() <= i) {
      return "";
    }
    return components.get(i);
  }

  private static Optional<Double> doubleOrEmpty(String s) {
    try {
      return Optional.of(Double.parseDouble(s));
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}


