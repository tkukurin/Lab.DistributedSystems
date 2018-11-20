package co.kukurin.sensor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class StoreMeasurementRequest {

  private String username;

  private String parameter;

  private double averageValue;
}
