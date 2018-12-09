package co.kukurin.sensor;

import co.kukurin.common.Config;
import co.kukurin.common.Utils;
import co.kukurin.common.exception.Run;
import co.kukurin.common.exception.ThrowableRunnable;
import co.kukurin.sensor.udp.ConfirmationPacket;
import co.kukurin.sensor.udp.MeasurementPacket;
import co.kukurin.sensor.udp.Packet;
import co.kukurin.sensor.udp.Time;
import co.kukurin.support.EmulatedSystemClock;
import co.kukurin.support.Measurements;
import co.kukurin.support.Measurements.Measurement;
import co.kukurin.support.SimpleSimulatedDatagramSocket;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Sensor {
  private static final Logger log = Logger.getLogger(Sensor.class.toString());
  private static final int MAX_BUFFER_LENGTH = 100 * 1024;

  private final int myId;
  private final List<Integer> otherNodePorts;
  private final Config config;
  private final Measurements measurements;
  private final EmulatedSystemClock emulatedSystemClock;
  private final ObjectMapper objectMapper;
  private final ConcurrentHashMap<Integer, DatagramPacket> idToPacket =
      new ConcurrentHashMap<>();
  // we don't want to double-count packets
  // easiest way to do so is keeping some sort of set inmemory
  private final NavigableMap<Date, MeasurementPacket> measurementPackets =
      Collections.synchronizedNavigableMap(new TreeMap<>());

  private int lastPacketId;
  private Time time;

  public Sensor(Config config, Measurements measurements) {
    this.myId = config.getPort() - 9191;
    this.emulatedSystemClock =  new EmulatedSystemClock();
    this.time = new Time(
        new long[config.getPorts().length],
        emulatedSystemClock.currentTimeMillis() / 1000,
        emulatedSystemClock);
    this.config = config;
    this.measurements = measurements;
    this.otherNodePorts = Arrays.stream(config.getPorts())
        .filter(p -> p != config.getPort())
        .boxed()
        .collect(Collectors.toList());
    this.objectMapper = new ObjectMapper();
  }

  public void start() throws Exception {
    new Thread(Run.builder().runnable(new Server()).build()).start();
    new Client().run();
  }

  class Client implements ThrowableRunnable {

    @Override
    public void run() throws Exception {
      long startTime = emulatedSystemClock.currentTimeMillis();
      try (DatagramSocket simulatedDatagramSocket =
            new SimpleSimulatedDatagramSocket(
                config.getLossRate(), config.getAverageDelayMilliseconds())) {

        //noinspection InfiniteLoopStatement
        while (true) {
          // resend failed packets
          idToPacket.forEach((id, packet) ->
            Run.builder()
                .runnable(() -> simulatedDatagramSocket.send(packet))
                .handler(e -> log.warning(String.format(
                    "Exception resending datagram %s to port %s",
                    lastPacketId, packet.getPort())))
                .build()
                .run());

          for (int i = 0; i < 5; i++) {
            long secs = (emulatedSystemClock.currentTimeMillis() - startTime) / 1000;
            Measurement measurement = measurements.getReading(secs);

            synchronized (this) {
              time = time.onSend(myId);
            }

            // list is concurrent so we can get by without synchronizing here.
            // let's add our own measurement before others.
            measurementPackets.put(Date.from(Instant.now()), new MeasurementPacket(
                time, lastPacketId++, config.getPort(), measurement));

            for (Integer port : otherNodePorts) {
              MeasurementPacket packet = new MeasurementPacket(
                  time, lastPacketId, config.getPort(), measurement);
              byte[] data = objectMapper.writeValueAsString(packet).getBytes();

              SocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), port);
              DatagramPacket datagramPacket = new DatagramPacket(data, 0, data.length, address);
              try {
                simulatedDatagramSocket.send(datagramPacket);
                idToPacket.put(lastPacketId, datagramPacket);
                lastPacketId++;
              } catch (IOException e) {
                log.warning(String.format(
                    "Failed sending datagram %s to port %s",
                    lastPacketId, port));
              }
            }

            Utils.sleepBestEffort(1000);
          }

          // we wish to compute everything on the same set of data, so let's lock
          // measurements while we do this.
          synchronized (measurementPackets) {
            clearOldPackets(5);
            List<MeasurementPacket> byScalar = measurementPackets.values().stream()
                .sorted((p1, p2) -> Time.scalarComparator.compare(
                    p1.getTime(), p2.getTime()))
                .collect(Collectors.toList());
            List<MeasurementPacket> byVector = measurementPackets.values().stream()
                .sorted((p1, p2) -> Time.vectorComparator.compare(
                    p1.getTime(), p2.getTime()))
                .collect(Collectors.toList());
            double averaged = measurementPackets.values().stream().collect(
                Collectors.averagingDouble(p -> p.getMeasurement().getCo()));
            System.out.println("Values:");
            System.out.format("Scalar: %s\n\n", listFormat(byScalar));
            System.out.format("Vector: %s\n\n", listFormat(byVector));
            System.out.format("Average co2: %.2f\n", averaged);
          }
        }
      }
    }

    private String listFormat(List<MeasurementPacket> list) {
      return list.stream().map(i -> "" + i).collect(
          Collectors.joining(",\n", "[", "]"));
    }
  }

  class Server implements ThrowableRunnable {

    @Override
    public void run() throws Exception {
      try (DatagramSocket socket =
            new SimpleSimulatedDatagramSocket(
                config.getPort(), config.getLossRate(), config.getAverageDelayMilliseconds())) {
        socket.setSoTimeout(50000);

        //noinspection InfiniteLoopStatement
        while (true) {
          byte[] data = new byte[MAX_BUFFER_LENGTH];
          DatagramPacket datagram = new DatagramPacket(data, data.length);
          socket.receive(datagram);
          Packet receivedPacket = objectMapper.readValue(datagram.getData(), Packet.class);

          log.info(new String(datagram.getData(), 0, datagram.getLength()));
          synchronized (this) {
            time = time.onReceive(receivedPacket.getTime());
          }

          if (receivedPacket instanceof ConfirmationPacket) {
            idToPacket.remove(receivedPacket.getId());
          } else {
            MeasurementPacket measurement = (MeasurementPacket) receivedPacket;
            synchronized (measurementPackets) {
              measurementPackets.put(Date.from(Instant.now()), measurement);
            }
            byte[] confirmation = objectMapper.writeValueAsBytes(
                ConfirmationPacket.fromPacket(receivedPacket));
            socket.send(new DatagramPacket(
                confirmation, confirmation.length, datagram.getAddress(), measurement.getPort()));
          }
        }
      }
    }
  }

  // just so we don't have to keep too many of them in memory.
  private void clearOldPackets(int nSeconds) {
    Date nSecondsEarlier = Date.from(Instant.now().minusSeconds(nSeconds));
    while (!measurementPackets.isEmpty() && measurementPackets.firstKey().before(nSecondsEarlier)) {
      measurementPackets.pollFirstEntry();
    }
  }

}
