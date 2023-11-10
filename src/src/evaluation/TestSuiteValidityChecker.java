package evaluation;

import combinatorial.CTModel;
import combinatorial.TestCase;
import common.CaseLARGE;
import common.CaseSIR;
import common.Scenario;
import common.Case;


public class TestSuiteValidityChecker {

  /*
   * Check whether every test suite is constraint satisfied with the sub test model
   * defined in a test scenario.
   */
  public static boolean check(String name, Case subject, final int[] PARA,
                              final double[] CONS, int strength, int repeat) {
    boolean pass = true;

    // for each combination of PARA and CONS
    for (int para : PARA) {
      for (double cons : CONS) {
        CTModel sub = subject.getSubModel(para, cons, 2);

        // for each test technique
        for (int tech = 0; tech < 3; tech++) {
          String technique = Scenario.TECHNIQUE[tech];
          String filename = "testsuites/" + name + "/" + para + "_" + cons + "_" + strength + "_" + technique + ".ca";
          System.out.println("checking " + filename);
          TestSuiteReader tr = new TestSuiteReader(filename);

          for (int rep = 0; rep < repeat; rep++) {
            for (TestCase x : tr.allSuites.get(rep).suite) {
              if (x.test.length != sub.parameter) {
                System.err.println("unequal length at " + technique + " repeat " + rep);
                pass = false;
              }
              if (!sub.isValid(x.test)) {
                System.err.println("invalid test at " + technique + " repeat " + rep);
                pass = false;
              }
            }
          }
        }
      }
    }
    System.out.println("\nCheck Done.");
    return pass;
  }

  private static void sir() {
    for (int id = 0 ; id < 6 ; id++) {
      CaseSIR sir = new CaseSIR(Scenario.SIR_NAME[id]);
      for (int t = 2 ; t <= 4 ; t++) {
        check(Scenario.SIR_NAME[id], sir, Scenario.SIR_PARA[id],
          Scenario.SIR_CONS[id], t, 50);
      }
    }
  }

  private static void large() {
    for (int id = 0 ; id < 3 ; id++) {
      CaseLARGE lar = new CaseLARGE(Scenario.LARGE_NAME[id]);
      for (int t = 2 ; t <= 4 ; t++) {
        check(Scenario.LARGE_NAME[id], lar, Scenario.LARGE_PARA[id],
          Scenario.LARGE_CONS[id], t, 50);
      }
    }
  }

  public static void main(String[] args) {
    int id = 0;
    CaseLARGE lar = new CaseLARGE(Scenario.LARGE_NAME[id]);
    check(Scenario.LARGE_NAME[id], lar, Scenario.LARGE_PARA[id],
      Scenario.LARGE_CONS[id], 2, 50);
  }
}
