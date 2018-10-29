package co.kukurin;

import co.kukurin.data.IpAddress;
import co.kukurin.data.Location;
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
//    sensorRepository.save(new Sensor(
//        "user1", new Location(1.0, 1.0), new IpAddress("192.168.0.1", 8080)));
//    sensorRepository.save(new Sensor(
//        "user2", new Location(12.0, 12.0), new IpAddress("192.168.0.1", 8080)));
  }
}
