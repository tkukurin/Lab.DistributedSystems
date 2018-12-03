package co.kukurin.sensor;

import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Time {
  private long[] vectorTime;
  private long scalarTime;

  public Time onSend(int myIndex) {
    this.vectorTime[myIndex]++;
    this.scalarTime++;
    return this;
  }

  public Time onReceive(Time other) {
    long scalarTime = Math.max(this.scalarTime, other.scalarTime);
    long[] vectorTime = IntStream.range(0, this.vectorTime.length)
        .mapToLong(i -> Math.max(this.vectorTime[i], other.vectorTime[i]))
        .toArray();
    return new Time(vectorTime, scalarTime);
  }

  public int compareScalarTime(Time other) {
    return Long.compare(this.scalarTime, other.scalarTime);
  }

  public int compareVectorTime(Time other) {
    int count = 0;
    for (int i = 0; i < this.vectorTime.length; i++) {
      count += Math.signum(Long.compare(vectorTime[i], other.vectorTime[i]));
    }

    return count == vectorTime.length ? -1
        : count == -vectorTime.length ? 1
          : 0;
  }
}
