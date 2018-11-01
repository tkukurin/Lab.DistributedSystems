package co.kukurin;

import co.kukurin.data.Location;
import co.kukurin.sensor.SensorRegisterRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Main {

  private static final double LON_LO = 15.87;
  private static final double LON_HI = 16;
  private static final double LAT_LO = 45.75;
  private static final double LAT_HI = 45.85;

  public static void main(String[] args) throws IOException {
    LogManager.getLogManager().reset();
    Handler handler = new FileHandler("test.log");
    handler.setFormatter(new SimpleFormatter());
    Logger.getLogger("").addHandler(handler);
    new Thread(new Program()).start();
  }

  static class Program implements Runnable {
    ExecutorService executorService = Executors.newCachedThreadPool();

    ExecutorService clientService = executorService;
    ExecutorService serverService = executorService;
    ExecutorService httpClients = executorService;

    PrintStream out = System.out;
    Random random = new Random();
    Measurements measurements = Measurements.fromFile("readings.csv");
    OkHttpClient okHttpClient = new OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .build();
    SensorService service = new Retrofit.Builder()
        .baseUrl("http://localhost:8080")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
        .create(SensorService.class);

    Map<String, Sensor> nameToSensor = new HashMap<>();

    Program() throws IOException {
      Sensor s1 = new Sensor("u1", 8081, service, httpClients, measurements);
      serverService.execute(s1.getServer());
      nameToSensor.put("u1", s1);
      Sensor s2 = new Sensor("u2", 8082, service, httpClients, measurements);
      nameToSensor.put("u2", s2);
      serverService.execute(s2.getServer());
    }

    @Override
    public void run() {
      int port = 8181;
      try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
        while (true) {
          out.print("$ ");
          String[] components = in.readLine().trim().split("\\s+");

          if (components.length == 0) {
            out.println("Please enter command");
            continue;
          }

          String command = components[0];
          switch (command) {
            case "create":
              create(port++, components[1]);
              break;
            case "destroy":
              destroy(components[1]);
              break;
            case "clear":
              clearAll();
              break;
            case "measure":
              measure(components[1]);
              break;
            case "exit":
              System.exit(0);
            default:
              out.println("Unrecognized command");
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    private void create(int port, String name) throws IOException {
      if (nameToSensor.containsKey(name)) {
        out.println("Sensor with given name already exists.");
        return;
      }

      Location location = new Location(
          Utils.random(random, LAT_LO, LAT_HI),
          Utils.random(random, LON_LO, LON_HI));
      Sensor sensor = new Sensor(name, port, service, httpClients, measurements);
      SensorRegisterRequest request = new SensorRegisterRequest(
          name, location.getLat(), location.getLon(), sensor.getIp(), sensor.getPort());
      Boolean registered = service.register(request).execute().body();

      if (registered == null || !registered) {
        out.println("Error registering sensor.");
        return;
      }

      nameToSensor.put(name, sensor);
      serverService.execute(sensor.getServer());
    }

    private void destroy(String component) {
      Optional.ofNullable(nameToSensor.get(component))
          .ifPresent(sensor -> {
            try {
              service.delete(component).execute();
              nameToSensor.remove(component);
              sensor.shutdown();
            } catch (IOException e) {
              out.println("Error removing component");
            }
          });
    }

    private void measure(String name) {
      Optional.ofNullable(nameToSensor.get(name))
          .map(Sensor::getClient)
          .ifPresent(clientService::execute);
    }

    private void clearAll() {
      new ArrayList<>(nameToSensor.keySet()).forEach(this::destroy);
      out.println("Cleared all");
    }
  }
}
