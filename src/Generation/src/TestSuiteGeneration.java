import combinatorial.CTModel;
import combinatorial.TestSuite;
import evaluation.TestSuiteReader;
import generator.ART;
import generator.CT;
import generator.RT;
import common.Case;

import java.io.*;
import java.util.ArrayList;

/**
 * Generate all required test suites (same size as CT).
 */
public class TestSuiteGeneration {

  private static String dir = "testsuites/";

  public void TestSuite(String name, Case subject, String directory, final int[] PARA,
                        final double[] CONS, int strength, int repeat, boolean append) {

    // create a directory if there does not have such one
    String path = dir + directory;
    createDir(path);

    // iterative each sub-model
    for (int para : PARA) {
      for (double cons : CONS) {
        String label = path + "/" + para + "_" + cons + "_" + strength + "_";
        System.out.println("\n-------------------------------------------------");
        System.out.println("[" + name.toUpperCase() + "] para = " + para + ", cons = " + cons + ", t-way = " + strength);

        // all test suites
        ArrayList<TestSuite> CTs = new ArrayList<>();
        ArrayList<TestSuite> RTs = new ArrayList<>();
        ArrayList<TestSuite> ARTs = new ArrayList<>();

        // test model
        CTModel model = subject.getSubModel(para, cons, strength);
        model.show();

        // show selection of constraint
        CTModel tp = subject.getSubModel(para, -1, strength);
        System.out.println("full constraint = " + tp.constraint.size() + " * " + cons + " = selected " + model.constraint.size());

        CT CTGen = new CT();
        RT RTGen = new RT();
        ART ARTGen = new ART();

        // the number of repetition
        int need = repeat ;

        // append mode:
        // the number of existing suites is determined according to "ct.ca"
        if (append) {
          TestSuiteReader tr = new TestSuiteReader(label + "ct.ca");
          need = repeat - tr.allSuites.size();
        }

        // run each technique N times
        for (int n = 0; n < need; n++) {
          TestSuite cts = new TestSuite();
          CTGen.generation(model, cts);
          CTs.add(cts);

          TestSuite rts = new TestSuite();
          RTGen.generation(model, rts, cts.getTestSuiteSize());
          RTs.add(rts);

          TestSuite arts = new TestSuite();
          ARTGen.generation(model, arts, cts.getTestSuiteSize());
          ARTs.add(arts);
        }

        if (append) {
          appendFile(CTs, label + "ct.ca", repeat - need);
          appendFile(RTs, label + "rt.ca", repeat - need);
          appendFile(ARTs, label + "art.ca", repeat - need);
        } else {
          writeFile(CTs, label + "ct.ca");
          writeFile(RTs, label + "rt.ca");
          writeFile(ARTs, label + "art.ca");
        }
      }
    }
  }

  private void writeFile(ArrayList<TestSuite> all, String filename) {
    try {
      PrintWriter writer = new PrintWriter(filename);
      double aveSize = 0, aveTime = 0;
      int index = 0;

      for (TestSuite ts : all) {
        writer.printf("REPEAT = %d SIZE = %d TIME = %d\n", index++, ts.getTestSuiteSize(), ts.getTestSuiteTime());
        ts.suite.forEach(x -> {
          for (int e : x.test)
            writer.print(e + " ");
          writer.print("\n");
        });
        writer.println();
        aveSize += (double) ts.getTestSuiteSize();
        aveTime += (double) ts.getTestSuiteTime();
      }
      writer.flush();
      writer.close();

      String tech = filename.substring(filename.lastIndexOf("_") + 1);
      Double repeat = (double) all.size();
      System.out.printf("%s (average): size = %.2f time = %.2f\n", tech, aveSize / repeat, aveTime / repeat);

    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  private void appendFile(ArrayList<TestSuite> all, String filename, int start) {
    try {
      FileWriter fw = new FileWriter(filename, true);
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter out = new PrintWriter(bw);

      int index = start;
      for (TestSuite ts : all) {
        out.printf("REPEAT = %d SIZE = %d TIME = %d\n", index++, ts.getTestSuiteSize(), ts.getTestSuiteTime());
        ts.suite.forEach(x -> {
          for (int e : x.test)
            out.print(e + " ");
          out.print("\n");
        });
        out.println();
      }
      out.flush();
      out.close();

      System.out.println("append " + (index - start) + " arrays into file " + filename);

    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  public void createDir(String path) {
    File theDir = new File(path);
    if (!theDir.exists()) {
      try{
        theDir.mkdir();
      }
      catch(SecurityException se){
        System.err.println(se.getMessage());
      }
    }
  }

}
