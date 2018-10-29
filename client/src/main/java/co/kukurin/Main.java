package co.kukurin;

import co.kukurin.data.IpAddress;
import co.kukurin.data.Location;
import co.kukurin.sensor.SensorRegisterRequest;
import co.kukurin.sensor.StoreMeasurementRequest;
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
import org.apache.log4j.Logger;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class Main {

  private static final double LON_LO = 15.87;
  private static final double LON_HI = 16;
  private static final double LAT_LO = 45.75;
  private static final double LAT_HI = 45.85;

  public static void main(String[] args) throws IOException {
    new Program().run();
//    ExecutorService clientService = Executors.newCachedThreadPool();
//    ExecutorService serverService = Executors.newCachedThreadPool();
//    ExecutorService httpClients = Executors.newCachedThreadPool();
//
//    clientService.execute(Main::mainProgram);
//    clientService.execute(Main::mainProgram);
//    clientService.execute(Main::mainProgram);
  }

  private static void mainProgram() {
    Logger log = Logger.getLogger(Thread.currentThread().getName());
    log.info("Started");
    while(true) {
      try {
        log.info("Sleeping");
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        log.error("Thread interrupt");
        break;
      }
    }
    log.info("Thread ended");
  }

  static class Program {

    public void run() throws IOException {
      Map<String, Sensor> nameToSensor = new HashMap<>();
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
            nameToSensor.keySet().forEach(nameToSensor::remove);
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

  interface SensorService {
    @GET("/nearest")
    Call<IpAddress> nearest(@Query("username") String username);

    @POST("/register")
    Call<Boolean> register(@Body SensorRegisterRequest request);

    @POST("/store")
    Call<Boolean> store(@Body StoreMeasurementRequest request);

    @DELETE("/delete")
    Call<Void> delete();
  }

}
