package co.kukurin.sensor;

import co.kukurin.sensor.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Integer> {

  Sensor findOneByUsername(String username);

  @Transactional
  Integer deleteOneByUsername(String username);
}
