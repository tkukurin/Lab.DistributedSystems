package co.kukurin;

import co.kukurin.data.IpAddress;
import co.kukurin.data.Location;
import co.kukurin.sensor.SensorRepository;
import co.kukurin.sensor.entity.Sensor;
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
    sensorRepository.save(new Sensor(
        "u1", new Location(1.0, 1.0), new IpAddress("192.168.5.187", 8081)));
    sensorRepository.save(new Sensor(
        "u2", new Location(12.0, 12.0), new IpAddress("192.168.5.187", 8082)));
  }
}
