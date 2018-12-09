package co.kukurin.sensor.udp;

import co.kukurin.support.Measurements.Measurement;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true, includeFieldNames = false)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Getter
public class MeasurementPacket extends Packet {

  private Measurement measurement;

  public MeasurementPacket(Time time, Integer id, int port, Measurement measurement) {
    super(time, id, port);
    this.measurement = measurement;
  }
}
