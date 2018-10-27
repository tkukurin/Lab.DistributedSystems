package co.kukurin.sensor.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@EqualsAndHashCode
@Getter
public class IpAddress {

  @Id
  @GeneratedValue
  private Integer id;

  @Column
  private String ip;

  @Column
  private int port;
}

