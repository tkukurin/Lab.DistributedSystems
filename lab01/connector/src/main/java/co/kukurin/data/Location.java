package co.kukurin.data;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class Location {

  private double lat;

  private double lon;

  public double distance(Location l2) {
    double R = 6371;

    double dlon = l2.getLon() - this.getLon();
    double dlat = l2.getLat() - this.getLat();
    double a =
        Math.pow(Math.sin(dlat / 2), 2)
            + Math.cos(this.getLat()) * Math.cos(l2.getLat())
            * Math.pow(Math.sin(dlon / 2), 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }
}

