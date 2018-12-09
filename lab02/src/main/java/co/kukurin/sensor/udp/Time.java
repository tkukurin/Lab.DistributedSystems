package co.kukurin.sensor.udp;

import co.kukurin.support.EmulatedSystemClock;
import java.util.Comparator;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "emulatedSystemClock")
public class Time {

  public static final Comparator<Time> scalarComparator =
      Comparator.comparingLong(t -> t.scalarTime);
  public static final Comparator<Time> vectorComparator = (fst, snd) -> {
    if (fst.vectorTime.length != snd.vectorTime.length) {
      throw new RuntimeException("Vector time lengths need to match for comparison.");
    }

    // counts stores number of times where the comparison has been
    // positive / neutral / negative between vector elements.
    // this way we can easily determine the order of events.
    int[] count = new int[3];
    int plus = 0;
    int minus = 2;

    for (int i = 0; i < fst.vectorTime.length; i++) {
      int current = (int) Math.signum(Long.compare(fst.vectorTime[i], snd.vectorTime[i]));
      count[current + 1]++;
    }

    return count[plus] > 0 && count[minus] == 0 ? -1
        : count[minus] > 0 && count[plus] == 0 ? 1
            : 0;
  };

  @Getter private long[] vectorTime;
  @Getter private long scalarTime;

  private EmulatedSystemClock emulatedSystemClock;

  public Time onSend(int myIndex) {
    long[] vectorTime = this.vectorTime.clone();
    vectorTime[myIndex]++;
    long scalarTime = emulatedSystemClock.currentTimeMillis();
    return new Time(vectorTime, scalarTime, emulatedSystemClock);
  }

  public Time onReceive(int myIndex, Time other) {
    // update according to clock drift
    // maintains consistency of events following this one.
    long scalarTime = emulatedSystemClock.updateClock(other.scalarTime);
    long[] vectorTime = IntStream.range(0, this.vectorTime.length)
        .mapToLong(i -> Math.max(this.vectorTime[i], other.vectorTime[i]))
        .toArray();
    vectorTime[myIndex]++;
    return new Time(vectorTime, scalarTime, emulatedSystemClock);
  }
}
