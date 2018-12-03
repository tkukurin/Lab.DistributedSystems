package co.kukurin.sensor;

import co.kukurin.support.Measurements.Measurement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeasurementPacket extends Packet {

  private Time time;
  private Measurement measurement;

  public MeasurementPacket(Integer id, Time time, Measurement measurement) {
    super(id, null);
    this.time = time;
    this.measurement = measurement;
  }
}
