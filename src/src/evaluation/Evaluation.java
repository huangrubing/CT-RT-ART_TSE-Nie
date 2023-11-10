package evaluation;

import combinatorial.CTModel;
import combinatorial.TestSuite;
import common.CaseLARGE;
import common.CaseSIR;
import common.Scenario;
import common.Case;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Evaluate the fault detection ability and execution cost of CT, RT and ART
 * under different test scenarios.
 */
public class Evaluation {

  private static String dir = "data";

  /*
   * Evaluation.jar --name --strength
   */
  public static void main(String[] args) throws IOException {

    if (args.length != 2)
      return;

    File theDir = new File(dir);
    if (!theDir.exists()) {
      try {
        theDir.mkdir();
      } catch(SecurityException se){
        System.err.println(se.getMessage());
      }
    }

    String name = args[0];
    int strength = Integer.valueOf(args[1]);
    int repeat = 50;

    if (name.equals("busybox") || name.equals("linux") || name.equals("drupal")) {
      int i = Arrays.asList(Scenario.LARGE_NAME).indexOf(name);
      CaseLARGE lar = new CaseLARGE(Scenario.LARGE_NAME[i]);
      run(Scenario.LARGE_NAME[i], lar, Scenario.LARGE_PARA[i], Scenario.LARGE_CONS[i], strength, repeat);
    }
    else {
      int i = Arrays.asList(Scenario.SIR_NAME).indexOf(name);
      CaseSIR sir = new CaseSIR(Scenario.SIR_NAME[i]);
      run(Scenario.SIR_NAME[i], sir, Scenario.SIR_PARA[i], Scenario.SIR_CONS[i], strength, repeat);
    }
  }

  public static void run(String name, Case subject, final int[] PARA, final double[] CONS,
                         int strength, int repeat) throws IOException {

    if (!TestSuiteValidityChecker.check(name, subject, PARA, CONS, strength, repeat))
      return;

    PrintWriter writer = new PrintWriter(dir + "/" + name + "_" + strength + ".data");
    writer.println("PARAMETER " + Arrays.toString(PARA));
    writer.println("CONSTRAINT " + Arrays.toString(CONS));

    // get the complete fault list
    List<String> allFault = subject.getFaultList();
    writer.println("FAULT " + allFault);
    writer.println("");

    // for each combination of PARA and CONS
    for (int para : PARA) {
      for (double cons : CONS) {

        CTModel sub = subject.getSubModel(para, cons, strength);
        writer.println("# " + sub.parameter + " " + Arrays.toString(sub.value) + " " + sub.t_way + " " + String.format("%.2f", cons));

        Integer[][] size = new Integer[3][repeat];
        Long[][] cost = new Long[3][repeat];

        // index -> a technique, row -> a fault, column -> a repetition
        Integer[][][] matrix = new Integer[3][allFault.size()][repeat];

        // number of faults detected (only for console display)
        Integer[][] num = new Integer[3][repeat];

        // for each test technique
        for (int tech = 0 ; tech < 3 ; tech++ ) {
          String technique = Scenario.TECHNIQUE[tech];
          String filename = "testsuites/" + name + "/" + para + "_" + cons + "_" +strength + "_" + technique + ".ca";

          TestSuiteReader tsr = new TestSuiteReader(filename);
          for (int rep = 0 ; rep < repeat ; rep++ ) {
            TestSuite ts = tsr.allSuites.get(rep);
            size[tech][rep] = tsr.allSize.get(rep);
            cost[tech][rep] = tsr.allTime.get(rep);

            // get fault detection
            int[] fm = subject.computeFaultMatrix(ts.suite);
            int n = 0 ;
            for (int l = 0; l < fm.length; l++) {
              matrix[tech][l][rep] = fm[l];
              if (fm[l] == 1)
                n += 1;
            }
            num[tech][rep] = n;
          }
        }

        // write to data file
        writer.println("SIZE     = " + Arrays.toString(size[0]));
        writer.println("CT-COST  = " + Arrays.toString(cost[0]));
        writer.println("RT-COST  = " + Arrays.toString(cost[1]));
        writer.println("ART-COST = " + Arrays.toString(cost[2]));

        for (int aid = 0; aid < 3; aid++) {
          writer.println(Scenario.TECHNIQUE[aid].toUpperCase() + " MATRIX");
          for (Integer[] row : matrix[aid])
            writer.println("    " + Arrays.toString(row));
        }
        writer.println("");

        // print to console
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("[" + name.toUpperCase() + "] para = " + para + " cons = " + cons + " strength = " + strength);
        System.out.format("CT-SIZE = %.2f,  RT-SIZE = %.2f,  ART-SIZE = %.2f\n",
            average(size[0]), average(size[1]), average(size[2]));
        System.out.format("CT-COST = %.2f,  RT-COST = %.2f,  ART-COST = %.2f\n",
            average(cost[0]), average(cost[1]), average(cost[2]));
        System.out.format("CT-DETECTION  = %.2f,  RT-DETECTION  = %.2f,  ART-DETECTION  = %.2f\n",
            average(num[0]), average(num[1]), average(num[2]));
      }
    }

    writer.close();
    System.out.println("\nDone.");
  }

  private static <T extends Number> double average(final T[] array) {
    double sum = 0;
    for (T a : array)
      sum += a.doubleValue();
    return sum / (double) array.length;
  }

}
