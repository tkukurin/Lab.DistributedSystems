package co.kukurin;

import co.kukurin.sensor.entity.IpAddress;
import co.kukurin.sensor.entity.Location;
import co.kukurin.sensor.entity.Sensor;
import co.kukurin.sensor.SensorRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@AllArgsConstructor
public class Application implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  private SensorRepository sensorRepository;

  @Override
  public void run(String... args) {

    sensorRepository.save(
        new Sensor(
            null,
            "user1",
            new Location(null, 1.0, 1.0),
            new IpAddress(null, "192.168.0.1", 8080)));

    sensorRepository.save(
        new Sensor(
            null,
            "user2",
            new Location(null, 12.0, 12.0),
            new IpAddress(null, "192.168.0.1", 8080)));
  }
}
