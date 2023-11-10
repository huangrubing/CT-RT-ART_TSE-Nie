import common.CaseLARGE;
import common.CaseSIR;
import common.Scenario;

import java.io.File;
import java.util.Arrays;

public class Main {

  /*
   *  --name --strength || --name --strength --repeat --append
   */
  public static void main(String[] args) {
    // remove .ca files
    // > remove --name --strength
    if (args.length == 3 && args[0].equals("remove")) {
      remove(args);
      return;
    }

    TestSuiteGeneration tsg = new TestSuiteGeneration();
    tsg.createDir("testsuites");

    String name ;
    int strength ;
    int repeat = 50;
    boolean append = false;

    if (args.length == 2) {
      name = args[0];
      strength = Integer.valueOf(args[1]);
    }
    else if (args.length != 4) {
      name = args[0];
      strength = Integer.valueOf(args[1]);
      repeat = Integer.valueOf(args[2]);
      append = args[3].equals("append");
    }
    else {
      return;
    }

    int id1 = Arrays.asList(Scenario.SIR_NAME).indexOf(name);
    int id2 = Arrays.asList(Scenario.LARGE_NAME).indexOf(name);

    if (id1 != -1) {
      CaseSIR sir = new CaseSIR(name);
      tsg.TestSuite(name, sir, name, Scenario.SIR_PARA[id1], Scenario.SIR_CONS[id1], strength, repeat, append);
    }
    if (id2 != -1) {
      CaseLARGE lar = new CaseLARGE(name);
      tsg.TestSuite(name, lar, name, Scenario.LARGE_PARA[id2], Scenario.LARGE_CONS[id2], strength, repeat, append);
    }
  }

  private static void remove(String[] args) {
    String name = args[1];
    String strength = args[2];

    File folder = new File("testsuites/" + name);
    File[] listOfFiles = folder.listFiles();

    if (listOfFiles != null) {
      for (File file : listOfFiles) {
        if (file.isFile()) {
          String[] fn = file.toString().split("_");
          if (fn[2].equals(strength)) {
            System.out.print("delete " + file.toString() + " -> ");
            System.out.print(file.delete() + "\n");
          }
        }
      }
    }
  }

}
