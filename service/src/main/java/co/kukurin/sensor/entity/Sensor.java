package co.kukurin.sensor.entity;

import co.kukurin.data.IpAddress;
import co.kukurin.data.Location;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jdo.annotations.Element;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Sensor {

  @Id
  @GeneratedValue
  @Null
  private Integer id;

  @NotNull
  @Column(unique = true)
  private String username;

  @NotNull
  @Embedded
  private Location location;

  @NotNull
  @Embedded
  private IpAddress ipAddress;

  @ElementCollection
  private Map<String, Double> measurements;

  public Sensor(String username, Location location, IpAddress ipAddress) {
    this.username = username;
    this.location = location;
    this.ipAddress = ipAddress;
    this.measurements = new HashMap<>();
  }

  public void addMeasurement(String parameter, double averageValue) {
    this.measurements.put(parameter, averageValue);
  }
}
