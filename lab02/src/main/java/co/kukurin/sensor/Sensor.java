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
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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
  private final ConcurrentLinkedQueue<MeasurementPacket> measurementPackets =
      new ConcurrentLinkedQueue<>();

  private int lastPacketId;
  private Time time;

  public Sensor(Config config, Measurements measurements) {
    this.myId = config.getPort() - 9191; // TODO non-fixed port
    this.time = new Time(new long[config.getPorts().length], 0);
    this.config = config;
    this.measurements = measurements;
    this.otherNodePorts = Arrays.stream(config.getPorts())
        .filter(p -> p != config.getPort())
        .boxed()
        .collect(Collectors.toList());
    this.emulatedSystemClock =  new EmulatedSystemClock();
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
                    "Failed resending datagram %s to port %s",
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
            measurementPackets.offer(withTime(new MeasurementPacket(
                time, lastPacketId++, config.getPort(), measurement)));

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

            Utils.sleepBestEffort(1_000);
          }

          // we wish to compute everything on the same set of data, so let's lock
          // measurements while we do this.
          synchronized (measurementPackets) {
            clearOldPackets(measurementPackets, 10);
            List<MeasurementPacket> byScalar = measurementPackets.stream()
                .sorted((p1, p2) -> p1.getTime().compareScalarTime(p2.getTime()))
                .collect(Collectors.toList());
            List<MeasurementPacket> byVector = measurementPackets.stream()
                .sorted((p1, p2) -> p1.getTime().compareVectorTime(p2.getTime()))
                .collect(Collectors.toList());
            double averaged = measurementPackets.stream().collect(
                Collectors.averagingDouble(p -> p.getMeasurement().getCo()));
            System.out.println("Values:");
            output(byScalar);
            output(byVector);
            System.out.println(averaged);
          }
        }
      }
    }

    private void output(List<MeasurementPacket> list) {
      System.out
          .println(list.stream().map(i -> "" + i).collect(
              Collectors.joining(",\n", "[", "]")));
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
          DatagramPacket datagram = new DatagramPacket(data, data.length);
          socket.receive(datagram);
          Packet receivedPacket = withTime(
              objectMapper.readValue(datagram.getData(), Packet.class));

          log.info(new String(datagram.getData(), 0, datagram.getLength()));
          synchronized (this) {
            time = time.onReceive(receivedPacket.getTime());

            if (receivedPacket instanceof ConfirmationPacket) {
              idToPacket.remove(receivedPacket.getId());
            } else {
              MeasurementPacket measurement = (MeasurementPacket) receivedPacket;
              synchronized (measurementPackets) {
                measurementPackets.offer(measurement);
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
  }

  // this is just so we don't have to keep too many of them in memory.
  private <T extends Packet> T withTime(T readValue) {
    readValue.setReceivedTime(Date.from(Instant.now()));
    return readValue;
  }

  private ConcurrentLinkedQueue<MeasurementPacket> clearOldPackets(
      ConcurrentLinkedQueue<MeasurementPacket> measurementPackets, int nSeconds) {
    Date nSecondsEarlier = Date.from(Instant
        .now()
        .minusSeconds(nSeconds));
    while (!measurementPackets.isEmpty()
        && measurementPackets.peek().getReceivedTime().before(nSecondsEarlier)) {
      this.measurementPackets.poll();
    }
    return measurementPackets;
  }

}
