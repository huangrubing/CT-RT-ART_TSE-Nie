package evaluation;

import combinatorial.TestCase;
import combinatorial.TestSuite;

import java.io.*;
import java.util.ArrayList;

/**
 * Read test suites that correspond to the given technique and test scenario.
 */
public class TestSuiteReader {

  public ArrayList<TestSuite> allSuites ;
  public ArrayList<Integer> allSize ;
  public ArrayList<Long> allTime ;
  public int number ;

  public TestSuiteReader(String filename) {
    allSuites = new ArrayList<>();
    allSize = new ArrayList<>();
    allTime = new ArrayList<>();

    try {
      BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
      String line ;

      number = 0;
      while ((line = reader.readLine()) != null) {
        // a test suite
        if (line.startsWith("REPEAT")) {
          String[] parts = line.split(" ");
          assert Integer.valueOf(parts[2]) == number;

          allSize.add(Integer.valueOf(parts[5]));
          allTime.add(Long.valueOf(parts[8]));

          TestSuite ts = new TestSuite();
          while ((line = reader.readLine()).length() > 0) {
            String[] row = line.trim().split(" ");
            int[] tc = new int[row.length];
            for( int k = 0 ; k < row.length ; k++ )
              tc[k] = Integer.valueOf(row[k]);
            ts.suite.add(new TestCase(tc));
          }
          allSuites.add(ts);
          number += 1 ;
        }
      }

    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  public void show() {
    System.out.println("Repeat = " + allSuites.size());
    System.out.println("Average Size = " + allSize.stream().mapToDouble(x -> x).average() +
        " Time = " + allTime.stream().mapToDouble(x -> x).average().toString());
  }

  public static void main(String[] args) {
    TestSuiteReader a = new TestSuiteReader("testsuites/flex/9_0.75_2_art.ca");
    a.show();
  }

}
