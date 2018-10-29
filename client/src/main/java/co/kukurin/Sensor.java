package co.kukurin;

import co.kukurin.Main.SensorService;
import co.kukurin.Measurements.Measurement;
import co.kukurin.data.IpAddress;
import co.kukurin.sensor.StoreMeasurementRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lombok.Getter;
import org.apache.log4j.Logger;

public class Sensor {

  @Getter
  private final Client client;
  @Getter
  private final Server server;
  @Getter
  private final int port;

  private final long startTime;
  private final Logger log;
  private final Measurements measurements;

  Sensor(
      String name, int port, SensorService sensorService,
      ExecutorService executorService, Measurements measurements)
      throws IOException {
    this.client = new Client(name, sensorService);
    this.server = new Server(port, executorService);

    this.port = port;
    this.measurements = measurements;
    this.startTime = Utils.currentTimeSeconds();
    this.log = Logger.getLogger(name);
  }

  void shutdown() {
    this.server.running.set(false);
    this.client.measuring.set(false);
  }

  String getIp() {
    return this.server.serverSocket.getInetAddress().getHostAddress();
  }

  class Server implements Runnable {

    private ExecutorService executorService;
    private ServerSocket serverSocket;
    private AtomicBoolean running;

    public Server(int port, ExecutorService executorService) throws IOException {
      this.executorService = executorService;
      this.serverSocket = new ServerSocket(port);
      this.running = new AtomicBoolean(false);
    }

    @Override
    public void run() {
      log.debug("Starting server");

      this.running.set(true);
      while (this.running.get()) {
        try  {
          Socket client = this.serverSocket.accept();
          log.debug("Accepted request");

          Supplier<Measurement> measurement =
              () -> measurements.getReading(Utils.currentTimeSeconds() - startTime);
          this.executorService.execute(new Worker(
            measurement, client.getOutputStream(), client.getInputStream()));
        } catch (IOException e) {
          log.error("Server exception", e);
        }
      }
    }
  }

  class Client implements Runnable {

    private final String name;
    private SensorService sensorService;
    private AtomicBoolean measuring;

    public Client(String name, SensorService sensorService) {
      this.sensorService = sensorService;
      this.measuring = new AtomicBoolean(false);
      this.name = name;
    }

    @Override
    public void run() {
      this.measuring.set(true);
      try {
        IpAddress neighborIp = this.sensorService.nearest(this.name).execute().body();
        try (Socket socket =
            new Socket(InetAddress.getByName(neighborIp.getIp()), neighborIp.getPort())) {
          while (this.measuring.get()) {
            Measurement myMeasurement = measurements.getReading(
                Utils.currentTimeSeconds() - startTime);
            Measurement neighborMeasurement = getMeasurement(socket);
            Measurement average = Measurement.average(myMeasurement, neighborMeasurement);
            // TODO
            log.info("Sending store request");
            this.sensorService.store(new StoreMeasurementRequest(this.name, "co2", 1.0)).execute();
            Utils.sleepBestEffort(2000);
          }
        }
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    private Measurement getMeasurement(Socket socket) throws IOException {
      PrintWriter outToServer = new PrintWriter(new OutputStreamWriter(
          socket.getOutputStream()), true);
      BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
          socket.getInputStream()));

      outToServer.println("REQ");
      String received = inFromServer.readLine();
      log.debug(String.format("Received %s from server", received));

      return Measurement.parse(received);
    }
  }
}
