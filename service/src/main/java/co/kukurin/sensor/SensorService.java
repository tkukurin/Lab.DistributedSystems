package co.kukurin.sensor;

import co.kukurin.data.IpAddress;
import co.kukurin.data.Location;
import co.kukurin.sensor.entity.Sensor;
import java.util.Comparator;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class SensorService {

  private final SensorRepository sensorRepository;

  public Sensor register(SensorRegisterRequest sensorRegisterRequest) {
    String username = sensorRegisterRequest.getUsername();
    Location location = new Location(
        sensorRegisterRequest.getLatitude(), sensorRegisterRequest.getLongitude());
    IpAddress ipAddress = new IpAddress(
        sensorRegisterRequest.getIpAddress(), sensorRegisterRequest.getPort());

    return this.sensorRepository.saveAndFlush(new Sensor(username, location, ipAddress));
  }

  public Sensor nearest(Location location) {
    Optional<Sensor> closest = this.sensorRepository.findAll().stream()
        .filter(si -> !si.getLocation().equals(location))
        .min(Comparator.comparingDouble(si -> si.getLocation().distance(location)));
    return closest.orElse(null);
  }

  public boolean store(StoreMeasurementRequest storeMeasurementRequest) {
    Sensor sensor = this.sensorRepository.findOneByUsername(storeMeasurementRequest.getUsername());
    sensor.addMeasurement(
        storeMeasurementRequest.getParameter(),
        storeMeasurementRequest.getAverageValue());
    return true;
  }
}
