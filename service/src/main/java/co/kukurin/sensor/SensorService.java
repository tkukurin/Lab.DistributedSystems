package co.kukurin.sensor;

import co.kukurin.sensor.entity.IpAddress;
import co.kukurin.sensor.entity.Location;
import co.kukurin.sensor.entity.Sensor;
import java.util.Comparator;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class SensorService {

  private final SensorRepository sensorRepository;

  public boolean register(SensorRegisterRequest sensorRegisterRequest) {
    String username = sensorRegisterRequest.getUsername();

    Location location = new Location(
        null, sensorRegisterRequest.getLatitude(), sensorRegisterRequest.getLongitude());
    IpAddress ipAddress = new IpAddress(
        null, sensorRegisterRequest.getIpAddress(), sensorRegisterRequest.getPort());

    this.sensorRepository.saveAndFlush(new Sensor(null, username, location, ipAddress));
    return true;
  }

  public Sensor nearest(Location location) {
    Optional<Sensor> closest = this.sensorRepository.findAll().stream()
        .filter(si -> !si.getLocation().equals(location))
        .min(Comparator.comparingDouble(si -> si.getLocation().distance(location)));
    return closest.orElse(null);
  }
}
