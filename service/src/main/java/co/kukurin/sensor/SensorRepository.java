package co.kukurin.sensor;

import co.kukurin.sensor.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository
    extends JpaRepository<Sensor, Integer>, QueryDslPredicateExecutor<Sensor> {

  Sensor findOneByUsername(String username);

}
