package co.kukurin.sensor.udp;

import co.kukurin.support.Measurements.Measurement;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true, includeFieldNames = false)
public class MeasurementPacket extends Packet {

  private Measurement measurement;

  public MeasurementPacket(Time time, Integer id, int port, Measurement measurement) {
    super(time, id, null, port);
    this.measurement = measurement;
  }

  public MeasurementPacket(Time time, Integer id, Date receivedTime, int port,
      Measurement measurement) {
    super(time, id, receivedTime, port);
    this.measurement = measurement;
  }
}
