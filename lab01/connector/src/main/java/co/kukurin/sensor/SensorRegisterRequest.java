package co.kukurin.sensor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class SensorRegisterRequest {

  private String username;

  private double latitude;

  private double longitude;

  private String ipAddress;

  private int port;
}
