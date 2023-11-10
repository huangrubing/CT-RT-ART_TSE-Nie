package evaluation;

import common.CaseLARGE;
import common.CaseSIR;
import common.Scenario;

import java.io.IOException;

public class EvaluationAll {

  public static void main(String[] args) throws IOException {
    int repeat = 50 ;

    // sir subjects
    for (int strength = 2; strength <= 4 ; strength++ ) {
      for (int i = 0; i < 6; i++) {
        CaseSIR sir = new CaseSIR(Scenario.SIR_NAME[i]);
        Evaluation.run(Scenario.SIR_NAME[i], sir, Scenario.SIR_PARA[i], Scenario.SIR_CONS[i], strength, repeat);
      }
    }

    // large subjects
    for (int strength = 2; strength <= 4 ; strength++ ) {
      for (int i = 0; i < 3; i++) {
        CaseLARGE lar = new CaseLARGE(Scenario.LARGE_NAME[i]);
        Evaluation.run(Scenario.LARGE_NAME[i], lar, Scenario.LARGE_PARA[i], Scenario.LARGE_CONS[i], strength, repeat);
      }
    }
  }

}
