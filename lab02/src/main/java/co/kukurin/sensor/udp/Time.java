package co.kukurin.sensor.udp;

import co.kukurin.support.EmulatedSystemClock;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "emulatedSystemClock")
public class Time {
  @Getter private long[] vectorTime;
  @Getter private long scalarTime;

  private EmulatedSystemClock emulatedSystemClock;

  public Time onSend(int myIndex) {
    long[] vectorTime = this.vectorTime.clone();
    vectorTime[myIndex]++;
    long scalarTime = emulatedSystemClock.currentTimeMillis();
    return new Time(vectorTime, scalarTime, emulatedSystemClock);
  }

  public Time onReceive(Time other) {
    long scalarTime = Math.max(this.scalarTime, other.scalarTime);
    long[] vectorTime = IntStream.range(0, this.vectorTime.length)
        .mapToLong(i -> Math.max(this.vectorTime[i], other.vectorTime[i]))
        .toArray();
    return new Time(vectorTime, scalarTime, emulatedSystemClock);
  }

  public int compareScalarTime(Time other) {
    return Long.compare(this.scalarTime, other.scalarTime);
  }

  public int compareVectorTime(Time other) {
    // counts stores number of times where the comparison has been
    // positive / neutral / negative between vector elements.
    // this way we can easily determine the order of events.
    int[] count = new int[3];
    int plus = 0;
    int minus = 2;

    for (int i = 0; i < this.vectorTime.length; i++) {
      int current = (int) Math.signum(Long.compare(vectorTime[i], other.vectorTime[i]));
      count[current + 1]++;
    }

    return count[plus] > 0 && count[minus] == 0 ? 1
        : count[minus] > 0 && count[plus] == 0 ? -1
        : 0;
  }
}
