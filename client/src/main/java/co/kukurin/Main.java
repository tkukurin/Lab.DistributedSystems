package co.kukurin;

import co.kukurin.data.Location;
import co.kukurin.sensor.SensorRegisterRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    new Program().run();
  }

  static class Program {

    void run() throws IOException {
      ExecutorService clientService = Executors.newCachedThreadPool();
      ExecutorService serverService = Executors.newCachedThreadPool();
      ExecutorService httpClients = Executors.newCachedThreadPool();

      Random random = new Random();
      Measurements measurements = Measurements.fromFile("readings.csv");
      PrintStream out = System.out;
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
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
      int port = 8181;

      Map<String, Sensor> nameToSensor = new HashMap<>();
      Sensor s1 = new Sensor("u1", 8081, service, httpClients, measurements);
      serverService.execute(s1.getServer());
      nameToSensor.put("u1", s1);
      Sensor s2 = new Sensor("u2", 8082, service, httpClients, measurements);
      nameToSensor.put("u2", s2);
      serverService.execute(s2.getServer());

      while (true) {
        out.print("> ");
        String[] components = in.readLine().trim().split("\\s+");

        if (components.length == 0) {
          out.println("Please enter command");
          continue;
        }

        String command = components[0];
        switch (command) {
          case "create": {
            String name = components[1];

            if (nameToSensor.containsKey(name)) {
              out.println("Sensor with given name already exists.");
              continue;
            }

            Location location = new Location(
                Utils.random(random, LAT_LO, LAT_HI),
                Utils.random(random, LON_LO, LON_HI));
            Sensor sensor = new Sensor(name, port++, service, httpClients, measurements);
            SensorRegisterRequest request = new SensorRegisterRequest(
                name, location.getLat(), location.getLon(), sensor.getIp(), sensor.getPort());
            Boolean registered = service.register(request).execute().body();

            if (registered == null || !registered) {
              out.println("Error registering sensor.");
              continue;
            }

            nameToSensor.put(name, sensor);
            serverService.execute(sensor.getServer());
            break;
          }
          case "destroy": {
            String name = components[1];
            Optional.ofNullable(nameToSensor.get(name))
                .ifPresent(sensor -> {
                  sensor.shutdown();
                  nameToSensor.remove(name);
                });
            break;
          }
          case "clear": {
            nameToSensor.values().forEach(Sensor::shutdown);
            nameToSensor.clear();

            service.delete().execute();
            out.println("Cleared all");
            break;
          }
          case "measure": {
            String name = components[1];
            Optional.ofNullable(nameToSensor.get(name))
                .map(Sensor::getClient)
                .ifPresent(clientService::execute);
            break;
          }
          case "exit":
            System.exit(0);
          default:
            out.println("Unrecognized command");
            break;
        }
      }
    }
  }
}
