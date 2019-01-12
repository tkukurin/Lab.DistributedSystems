import com.perfdynamics.pdq.Job;
import com.perfdynamics.pdq.Methods;
import com.perfdynamics.pdq.Node;
import com.perfdynamics.pdq.PDQ;
import com.perfdynamics.pdq.QDiscipline;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

  public static void main(String[] args) {
    double
        a = 0.2,
        b = 0.3,
        c = 0.5,
        d = 0.3,
        e = 0.7,
        f = 0.6,
        g = 0.2,
        h = 0.3;

    double Ss[] = {
        0.003,
        0.001,
        0.010,
        0.040,
        0.100,
        0.130,
        0.150,
    };

    double vs[] = {
        1.0,
        1 / (1 - d * f),
        (a + h * b) / ((1 - g * h) * (1 - d * f)),
        (b + g * a) / ((1 - g * h) * (1 - d * f)),
        c / (1 - d * f),
        d / (1 - d * f),
        e / (1 - d * f),
    };

    int n = vs.length;

    // generic queueing center
    final int node = Node.CEN;
    // first-come-first-serve
    final int fcfs = QDiscipline.FCFS;

    System.out.printf("Lambda;%s\n",
        IntStream.range(0, n)
            .mapToObj(i -> String.format("Node %d", i + 1))
            .collect(Collectors.joining(",")));

    final PDQ pdq = new PDQ();
    final double lambdaStep = 0.2;
    final double lambdaHi = 5.0;
    for (var lambda = 0.0; lambda < lambdaHi; lambda += lambdaStep) {
      pdq.Init("Homework");
      pdq.CreateOpen("Requests", lambda);
      IntStream.range(0, n).forEach(i -> {
        String name = String.format("S%d", i + 1);
        pdq.CreateNode(name, node, fcfs);
        pdq.SetVisits(name, "Requests", vs[i], Ss[i]);
      });

      pdq.Solve(Methods.CANON);
      System.out.printf("%.3f,%s\n", lambda, IntStream.range(0, n)
          .mapToObj(i -> String.format("S%d", i + 1))
          .map(name -> pdq.GetResidenceTime(name, "Requests", Job.TRANS))
          .map(dv -> String.format("%.3f", dv))
          .collect(Collectors.joining(",")));
    }
  }
}
