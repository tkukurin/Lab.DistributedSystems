package co.kukurin.sensor;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ConfirmationPacket extends Packet {

  public ConfirmationPacket(Integer id) {
    super(id);
  }

  public static ConfirmationPacket fromPacket(Packet packet) {
    return new ConfirmationPacket(packet.getId());
  }
}
