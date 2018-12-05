package co.kukurin;

import co.kukurin.common.Utils;
import co.kukurin.common.Config;
import co.kukurin.sensor.Sensor;
import co.kukurin.support.Measurements;
import java.util.Arrays;

public class Demo {

  public static void main(String[] args) throws Exception {
    // find first available port from allowed range for the new sensor.
    Config config = Config.from("config.json");
    config.setPort(Arrays.stream(config.getPorts()).filter(p -> !Utils.udpInUse(p))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("All designated ports in use")));

    Sensor sensor = new Sensor(
        config,
        Measurements.fromFile("readings.csv"));
    sensor.start();
  }
}
