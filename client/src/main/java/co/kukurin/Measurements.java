package co.kukurin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import lombok.Getter;

class Measurements {

  @Getter
  private List<String[]> measurements;

  @Getter
  private String[] headers;

  static Measurements fromFile(String location) {
    Measurements result = new Measurements();

    // headers: "Temperature,Pressure,Humidity,CO,NO2,SO2,"
    try (BufferedReader reader = new BufferedReader(new FileReader(location))) {
      result.headers = reader.readLine().split(",");

      while (true) {
        String line = reader.readLine();
        if (line == null) {
          break;
        }

        String[] components = line.trim().split(",");
        result.measurements.add(components);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }
}


