package co.kukurin.sensor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StoreMeasurementRequest {

  private String username;

  private String parameter;

  private double averageValue;
}
