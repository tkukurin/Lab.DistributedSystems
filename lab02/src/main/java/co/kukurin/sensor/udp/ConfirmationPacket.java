package co.kukurin.sensor.udp;

import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@Builder
public class ConfirmationPacket extends Packet {

  public ConfirmationPacket(Time time, Integer id) {
    super(time, id);
  }

  public static ConfirmationPacket fromPacket(Packet packet) {
    return new ConfirmationPacket(packet.getTime(), packet.getId());
  }
}
