package co.kukurin.sensor.entity;

import co.kukurin.data.IpAddress;
import co.kukurin.data.Location;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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

  // TODO
//  @OneToMany(cascade = CascadeType.ALL)
//  private List<Double> measurements;

  public Sensor(String username, Location location, IpAddress ipAddress) {
    this.username = username;
    this.location = location;
    this.ipAddress = ipAddress;
  }
}
