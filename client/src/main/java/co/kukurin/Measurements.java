package co.kukurin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.log4j.Logger;

class Measurements {

  private static final Logger log = Logger.getLogger(Measurements.class);

  private final List<Measurement> measurements = new ArrayList<>();

  public Measurement getReading(long secs) {
    log.debug(String.format("Retrieving measurement at second %d", secs));
    // formula from the document, adjusted to 0-index and skip header.
    return this.measurements.get((int)(secs % this.measurements.size()));
  }

  static Measurements fromFile(String resourceLocation) {
    Measurements result = new Measurements();
    InputStream stream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceLocation);

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
  @AllArgsConstructor
  static class Measurement {
    private String temperature;
    private String pressure;
    private String humidity;
    private String co;
    private String no2;
    private String so2;

    @Override
    public String toString() {
      return String.format("%s,%s,%s,%s,%s,%s", temperature, pressure, humidity, co, no2, so2);
    }

    public static Measurement parse(String line) {
      List<String> components = Arrays.stream(line.trim().split(",")).collect(Collectors.toList());
      return new Measurement(
          atIndexOrEmpty(components, 0),
          atIndexOrEmpty(components, 1),
          atIndexOrEmpty(components, 2),
          atIndexOrEmpty(components, 3),
          atIndexOrEmpty(components, 4),
          atIndexOrEmpty(components, 5));
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

    public Stream<HashMap.SimpleEntry<String, Double>> streamAsKeyValue() {
      return Stream.<HashMap.SimpleEntry<String, Double>>builder()
          .add(new HashMap.SimpleEntry<>("temperature", doubleOrEmpty(this.getTemperature())))
          .add(new HashMap.SimpleEntry<>("pressure", doubleOrEmpty(this.getPressure())))
          .add(new HashMap.SimpleEntry<>("humidity", doubleOrEmpty(this.getHumidity())))
          .add(new HashMap.SimpleEntry<>("co", doubleOrEmpty(this.getCo())))
          .add(new HashMap.SimpleEntry<>("no2", doubleOrEmpty(this.getNo2())))
          .add(new HashMap.SimpleEntry<>("so2", doubleOrEmpty(this.getSo2())))
          .build();
    }
  }

  private static String atIndexOrEmpty(List<String> components, int i) {
    if (components.size() <= i) {
      return "";
    }
    return components.get(i);
  }

  private static Double doubleOrEmpty(String s) {
    try {
      return Double.parseDouble(s);
    } catch (Exception e) {
      return null;
    }
  }
}


