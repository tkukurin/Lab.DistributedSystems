package co.kukurin.sensor;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.sql.Time;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type")
@JsonSubTypes({
    @Type(value = ConfirmationPacket.class, name = "confirmationPacket"),
    @Type(value = MeasurementPacket.class, name = "measurementPacket")
})
@EqualsAndHashCode
public class Packet {

  private Integer id;

  @Setter
  private Time receivedTime;

  public Packet(Integer id) {
    this.id = id;
  }
}
