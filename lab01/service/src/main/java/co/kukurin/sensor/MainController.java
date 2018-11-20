package co.kukurin.sensor;

import co.kukurin.data.IpAddress;
import co.kukurin.sensor.entity.Sensor;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.websocket.server.PathParam;
import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class MainController {
  private static final Logger log = Logger.getLogger(SensorService.class);

  private final SensorService sensorService;
  private final SensorRepository sensorRepository;

  @GetMapping("/nearest")
  public IpAddress getNearest(@RequestParam String username) {
    log.info(String.format("Location request for %s", username));
    Sensor forUsername = sensorRepository.findOneByUsername(username);
    if (forUsername == null) {
      throw new RuntimeException(String.format("Sensor not found for username %s", username));
    }
    return sensorService.nearest(forUsername.getLocation()).getIpAddress();
  }

  @PostMapping(path="/register")
  public boolean register(
      @Valid @NotNull @RequestBody SensorRegisterRequest sensorRegisterRequest) {
    log.info(String.format("Registering sensor %s", sensorRegisterRequest));
    return sensorService.register(sensorRegisterRequest) != null;
  }

  @PostMapping(path="/store")
  public boolean storeMeasurement(
    @Valid @NotNull @RequestBody StoreMeasurementRequest storeMeasurementRequest) {
    log.info(String.format("Storing measurement %s", storeMeasurementRequest));
    return sensorService.store(storeMeasurementRequest) != null;
  }

  @DeleteMapping(path="/{username}")
  public void delete(@PathVariable("username") String username) {
    log.info(String.format("Deleting %s", username));
    this.sensorRepository.deleteOneByUsername(username);
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse exceptionHandler(
      MethodArgumentNotValidException methodArgumentNotValidException) {
    BindingResult bindingResult = methodArgumentNotValidException.getBindingResult();
    List<ObjectError> validationErrors = bindingResult.getAllErrors();
    return ErrorResponse.fromValidationErrors(validationErrors);
  }

}
