package co.kukurin.sensor;

import co.kukurin.Demo.Run;
import co.kukurin.Demo.ThrowableRunnable;
import co.kukurin.common.Utils;
import co.kukurin.support.EmulatedSystemClock;
import co.kukurin.support.Measurements;
import co.kukurin.support.Measurements.Measurement;
import co.kukurin.support.SimpleSimulatedDatagramSocket;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Sensor {
  private static final int MAX_BUFFER_LENGTH = 4096;

  private int id;
  private Time time;
  private Config config;
  private Measurements measurements;
  private EmulatedSystemClock emulatedSystemClock = new EmulatedSystemClock();
  private ObjectMapper objectMapper = new ObjectMapper();

  private ConcurrentLinkedQueue<Integer> sentPackets = new ConcurrentLinkedQueue<>();
  private ConcurrentLinkedQueue<MeasurementPacket> measurementPackets =
      new ConcurrentLinkedQueue<>();

  public Sensor(Config config, Measurements measurements) {
    this.time = new Time(new long[] {0, 0, 0}, 0);
    this.config = config;
    this.measurements = measurements;
  }

  public void start() throws Exception {
    new Thread(new Run(new Server())).start();
    new Client().run();
  }

  class Client implements ThrowableRunnable {

    @Override
    public void run() throws Exception {
      long startTime = emulatedSystemClock.currentTimeMillis();
      try (DatagramSocket simulatedDatagramSocket =
            new SimpleSimulatedDatagramSocket(
                config.getLossRate(), config.getAverageDelayMilliseconds())) {
        while (true) {
          for (int i = 0; i < 5; i++) {
            long secs = (emulatedSystemClock.currentTimeMillis() - startTime) / 1000;
            Measurement measurement = measurements.getReading(secs);
            MeasurementPacket packet = new MeasurementPacket(id++, time, measurement);
            byte[] data = objectMapper.writeValueAsString(packet).getBytes();

            for (Integer port : config.getPorts()) {
              if (port == config.getPort()) {
                continue;
              }

              SocketAddress address = new InetSocketAddress("localhost", port);
              DatagramPacket datagramPacket = new DatagramPacket(data, 0, data.length, address);
              simulatedDatagramSocket.send(datagramPacket);
            }

            Utils.sleepBestEffort(1000);
          }

          List<MeasurementPacket> before5secs = measurementPackets.stream()
              .filter(p -> p.getReceivedTime().after(Date.from(Instant.now().minusSeconds(5))))
              .collect(Collectors.toList());
          List<MeasurementPacket> byScalar = before5secs.stream()
              .sorted((p1, p2) -> p1.getTime().compareScalarTime(p2.getTime()))
              .collect(Collectors.toList());
          List<MeasurementPacket> byVector = before5secs.stream()
              .sorted((p1, p2) -> p1.getTime().compareVectorTime(p2.getTime()))
              .collect(Collectors.toList());
          double averaged = before5secs.stream().collect(
              Collectors.averagingDouble(p -> p.getMeasurement().getCo()));
          System.out.println("Values:");
          System.out.println(byScalar.stream().map(i -> "" + i).collect(Collectors.joining(", ")));
          System.out.println(byVector.stream().map(i -> "" + i).collect(Collectors.joining(", ")));
          System.out.println(averaged);

          // TODO this is nowhere near threadsafe
          measurementPackets.clear();
        }
      }
    }
  }

  class Server implements ThrowableRunnable {

    @Override
    public void run() throws Exception {
      try (DatagramSocket socket =
            new SimpleSimulatedDatagramSocket(
                config.getPort(), config.getLossRate(), config.getAverageDelayMilliseconds())) {
        socket.setSoTimeout(50000);

        while (true) {
          byte[] data = new byte[MAX_BUFFER_LENGTH];
          DatagramPacket packet = new DatagramPacket(data, data.length);

          socket.receive(packet);
          Packet receivedPacket = objectMapper.readValue(packet.getData(), Packet.class);
          receivedPacket.setReceivedTime(
              new java.sql.Time(emulatedSystemClock.currentTimeMillis()));

          System.out.println(new String(packet.getData(), 0, packet.getLength()));

          if (receivedPacket instanceof ConfirmationPacket) {
            sentPackets.remove(receivedPacket.getId());
          } else {
            // packet data
            MeasurementPacket measurement = (MeasurementPacket) receivedPacket;
            measurementPackets.add(measurement);
            time = time.onReceive(measurement.getTime());
            packet.setData(objectMapper.writeValueAsBytes(
                ConfirmationPacket.fromPacket(receivedPacket)));
            socket.send(packet);
          }
        }
      }
    }
  }

}
