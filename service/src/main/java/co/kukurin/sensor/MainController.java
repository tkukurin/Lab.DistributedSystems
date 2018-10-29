package co.kukurin.sensor;

import co.kukurin.data.IpAddress;
import co.kukurin.sensor.entity.Sensor;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
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
    log.info(String.format("Location request for (%s)", username));
    Sensor forUsername = sensorRepository.findOneByUsername(username);
    return sensorService.nearest(forUsername.getLocation()).getIpAddress();
  }

  @PostMapping(path="/register")
  public boolean register(
      @Valid @NotNull @RequestBody SensorRegisterRequest sensorRegisterRequest) {
    log.info(String.format("Registering sensor for user %s", sensorRegisterRequest.getUsername()));
    sensorService.register(sensorRegisterRequest);
    return true;
  }

  @PostMapping(path="/store")
  public boolean storeMeasurement(
    @Valid @NotNull @RequestBody StoreMeasurementRequest storeMeasurementRequest) {
    log.info(String.format("Storing measurement %s", storeMeasurementRequest));
    sensorService.store(storeMeasurementRequest);
    return true;
  }

  @DeleteMapping(path="/delete")
  public void delete() {
    this.sensorRepository.deleteAll();
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
