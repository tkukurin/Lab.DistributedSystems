package co.kukurin.sensor.entity;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Sensor {

  @Id
  @GeneratedValue
  @Null
  private Integer id;

  @Column
  @NotNull
  private String user;

  @OneToOne(cascade = CascadeType.ALL)
  @NotNull
  private Location location;

  @OneToOne(cascade = CascadeType.ALL)
  @NotNull
  private IpAddress ipAddress;

  @OneToMany
  private List<Double> measurements;

}
