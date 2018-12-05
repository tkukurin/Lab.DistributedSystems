package co.kukurin.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Config {

  @Setter
  private int port;
  private int averageDelayMilliseconds;
  private double lossRate;
  private int[] ports;

  public static Config from(String fileName) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    InputStream resource = Utils.resourceInputStream(fileName);
    return mapper.readValue(resource, Config.class);
  }
}
