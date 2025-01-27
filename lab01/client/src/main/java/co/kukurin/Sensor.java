package co.kukurin;

import co.kukurin.Measurements.Measurement;
import co.kukurin.data.IpAddress;
import co.kukurin.sensor.StoreMeasurementRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;

public class Sensor {

  private static final int TIMEOUT_MS = 100_000;

  private final long startTime;
  private final Logger log;
  private final Measurements measurements;

  @Getter private final Client client;
  @Getter private final Server server;

  Sensor(String name,
      SensorService sensorService,
      ExecutorService executorService,
      Measurements measurements) throws IOException {
    this.client = new Client(name, sensorService);
    this.server = new Server(0, executorService);

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

  int getPort() { return this.server.serverSocket.getLocalPort(); }

  class Server implements Runnable {

    private ExecutorService executorService;
    private ServerSocket serverSocket;
    private AtomicBoolean running;

    public Server(int port, ExecutorService executorService) throws IOException {
      this.executorService = executorService;
      this.serverSocket = new ServerSocket(port);
      this.serverSocket.setSoTimeout(TIMEOUT_MS);
      this.running = new AtomicBoolean(false);
    }

    @Override
    public void run() {
      Supplier<Measurement> measurementSupplier =
          () -> measurements.getReading(Utils.currentTimeSeconds() - startTime);

      this.running.set(true);
      while (this.running.get()) {
        try  {
          Socket client = this.serverSocket.accept();
          this.executorService.execute(new Worker(
              measurementSupplier,
              client.getOutputStream(),
              client.getInputStream()));
        } catch (IOException e) {
          log.throwing("Server exception", e.getMessage(), e);
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

        if (neighborIp == null) {
          log.log(Level.WARNING, "No neighbor returned from server.");
          return;
        }

        try (Socket socket = new Socket(neighborIp.getIp(), neighborIp.getPort())) {
          while (this.measuring.get()) {
            Measurement myMeasurement = measurements.getReading(
                Utils.currentTimeSeconds() - startTime);
            Measurement neighborMeasurement = getMeasurement(socket);

            Measurement.average(myMeasurement, neighborMeasurement)
                .streamAsKeyValue()
                .filter(m -> m.getValue() != null)
                .forEach(m -> {
                  try {
                    StoreMeasurementRequest request =
                        new StoreMeasurementRequest(this.name, m.getKey(), m.getValue());
                    this.sensorService.store(request).execute();
                  } catch (IOException e) {
                    log.log(Level.WARNING, "Error sending measurement", e);
                  }
                });

            Utils.sleepBestEffort(2000);
          }

          socket.getOutputStream().write(Worker.SERVER_SHUTDOWN);
        }
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    private Measurement getMeasurement(Socket socket) throws IOException {
      PrintWriter outToServer = new PrintWriter(new OutputStreamWriter(
          socket.getOutputStream()), true);
      outToServer.println("GIMMIE");

      BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
          socket.getInputStream()));
      String received = inFromServer.readLine();
      log.log(Level.INFO, String.format("Received %s from server", received));

      return Measurement.parse(received);
    }
  }
}
