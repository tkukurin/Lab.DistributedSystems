package co.kukurin.sensor.entity;


import com.querydsl.core.types.dsl.NumberExpression;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
public class Location {

  @Id
  @GeneratedValue
  private Integer id;

  @Column
  private double lat;

  @Column
  private double lon;

  public double distance(Location l2) {
    double R = 6371;

    double dlon = l2.lon - this.lon;
    double dlat = l2.lon - this.lon;
    double a =
        Math.pow(Math.sin(dlat / 2), 2)
            + Math.cos(this.lat) * Math.cos(l2.lat)
            * Math.pow(Math.sin(dlon / 2), 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }
}

