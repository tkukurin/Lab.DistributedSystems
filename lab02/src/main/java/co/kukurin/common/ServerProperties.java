package co.kukurin.common;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServerProperties {

  private Properties properties;

  public ServerProperties(Properties properties) {
    this.properties = properties;
  }

  public String get(String key) {
    return properties.getProperty(key);
  }

  public <T> T get(String key, Function<String, T> parser) {
    return parser.apply(properties.getProperty(key));
  }

  public <T> List<T> getList(String key, Function<String, T> parser) {
    return Arrays.stream(properties.getProperty(key).split(","))
        .map(parser).collect(Collectors.toList());
  }

  public Object getOrDefault(String key, Object defaultValue) {
    return properties.getOrDefault(key, defaultValue);
  }

  public static ServerProperties load(String resourceName) throws IOException {
    Properties properties = new Properties();
    properties.load(Utils.resourceInputStream(resourceName));
    return new ServerProperties(properties);
  }

}
