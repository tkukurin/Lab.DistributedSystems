package co.kukurin;

import co.kukurin.data.IpAddress;
import co.kukurin.sensor.SensorRegisterRequest;
import co.kukurin.sensor.StoreMeasurementRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

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

