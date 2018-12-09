package co.kukurin.sensor.udp;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @Type(value = ConfirmationPacket.class, name = "confirmation"),
    @Type(value = MeasurementPacket.class, name = "measurement")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Packet {

  private Time time;

  private Integer id;

  private int port;

  public Packet(Time time, Integer id) {
    this.time = time;
    this.id = id;
  }
}
