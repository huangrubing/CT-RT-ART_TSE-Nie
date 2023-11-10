package common;

import combinatorial.CTModel;
import combinatorial.TestCase;

import java.io.*;
import java.util.*;

import static java.lang.Math.abs;

public class CaseSIR implements Case {

  /*
   * A fault and its corresponding fault introducing test cases.
   */
  class Fault {
    String id;         // f.v.x
    List<int[]> set;   // fault introducing test cases

    Fault(String id, List<int[]> set) {
      this.id = id;
      this.set = set;
    }

    @Override
    public String toString() {
      return id + "\t -> # " + set.size() ;
    }
  }

  public int parameter;
  public int[] value;

  // constraint (disjunction representation)
  public ArrayList<int[]> constraint;

  // default test case
  public int[] defaultTestCase;
  public int[] defaultPV;

  // assign each parameter-values to an unique index value, which is used
  // in the CNF representation of constraint. (to use mapping, start from 0)
  public int[][] relation;

  // the set of faults
  public ArrayList<Fault> allFault;
  public Map<String, Fault> mapFault;  // key = f.v.x, value = fault

  private boolean INFO = false;
  public void setINFO(boolean info) {
    this.INFO = info;
  }

  public CaseSIR(String name) {
    if (!Arrays.asList(Scenario.SIR_NAME).contains(name))
      return;

    try {
      readModel("subject/" + name + ".model");
      readBugReport("subject-bugs/" + name + ".bug");
      showCompleteModel();
      CTModel sut = getCompleteModel(2);
      System.out.println("default test case validity: " + sut.isValid(defaultTestCase));

    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  private void showCompleteModel() {
    System.out.println("parameter = " + parameter + " constraint = " + constraint.size());

    Set<Integer> unique = new HashSet<>();
    for (int[] cons : constraint) {
      System.out.println(Arrays.toString(cons));
      for (int e : cons)
        unique.add(index(e));
    }
    System.out.println("# parameters involved in constraints = " + unique.size());

    System.out.println("\n# faults = " + allFault.size());
    for (Fault fault : allFault)
      System.out.println(fault);
  }

  /*
   * Get the index of the parameter according to a PV value.
   */
  private int index(int pv) {
    for (int i = 0 ; i < parameter ; i++) {
      for (int j = 0 ; j < value[i] ; j++) {
        if (relation[i][j] == abs(pv))
          return i ;
      }
    }
    return -1;
  }

  /*
   * Read model file.
   */
  private void readModel(String filename) throws IOException {
    File file = new File(filename);
    BufferedReader br = new BufferedReader(new FileReader(file));

    // <Parameter>
    String[] parts = br.readLine().trim().split(" ");
    parameter = Integer.valueOf(parts[1]);

    // <Value>
    value = new int[parameter];
    parts = br.readLine().trim().split(" ");
    for (int i = 0 ; i < parameter ; i++)
      value[i] = Integer.valueOf(parts[i + 1]);

    relation = new int[parameter][];
    int start = 1;
    for (int i = 0 ; i < parameter ; i++) {
      relation[i] = new int[value[i]];
      for (int j = 0 ; j < value[i] ; j++)
        relation[i][j] = start++ ;
    }

    // <Default>
    br.readLine();
    br.readLine();
    parts = br.readLine().trim().split(" ");
    defaultTestCase = new int[parameter];
    defaultPV = new int[parameter];
    for (int k = 0 ; k < parameter ; k++ ) {
      defaultTestCase[k] = Integer.valueOf(parts[k]);
      defaultPV[k] = relation[k][defaultTestCase[k]];
    }

    // <Constraints>
    constraint = new ArrayList<>();
    br.readLine();
    int consNum = Integer.valueOf(br.readLine().trim().split(" ")[1]);
    String line;
    while ((line = br.readLine()) != null) {
      parts = line.trim().split(" ");
      int[] cons = new int[parts.length];
      for (int k = 0 ; k < parts.length ; k++)
        cons[k] = Integer.valueOf(parts[k]);
      constraint.add(cons);
    }
    if (constraint.size() != consNum)
      System.err.println("incorrect constraint size");
  }

  /*
   * Read bug report file.
   */
  private void readBugReport(String filename) throws IOException {
    allFault = new ArrayList<>();
    mapFault = new HashMap<>();

    File file = new File(filename);
    BufferedReader br = new BufferedReader(new FileReader(file));

    String line;
    while ((line = br.readLine()) != null) {
      if (line.startsWith("f")) {
        String id = line.trim();
        List<int[]> set = new ArrayList<>();
        while (!(line = br.readLine()).equals("")) {
          String[] parts = line.split(" ");
          if (parts.length != parameter) {
            System.out.println("incorrect faulty test case");
            return;
          }
          int[] tc = new int[parameter];
          for (int x = 0 ; x < parameter ; x++)
            tc[x] = Integer.valueOf(parts[x]);
          set.add(tc);
        }

        // add a new fault
        Fault fault = new Fault(id, set);
        allFault.add(fault);
        mapFault.put(fault.id, fault);
      }
    }
  }

  /*
   * Amend a test case of sub-model to a test case of full model.
   */
  private int[] amend(int[] t) {
    if (t.length == parameter)
      return t.clone();

    int[] tc = new int[parameter];
    int len = t.length;
    System.arraycopy(t, 0, tc, 0, len);
    System.arraycopy(defaultTestCase, len, tc, len, parameter - len);
    return tc;
  }

  /*
   * Determine whether a test case can trigger a particular fault.
   * The test case should be a valid test case.
   */
  private boolean hitting(final int[] test, Fault fault) {
    int[] tc = amend(test);
    for (int[] fs : fault.set) {
      if (Arrays.equals(fs, tc))
        return true;
    }
    return false;
  }

  /**
   * Get the number of complete parameters.
   */
  @Override
  public int getCompleteParameter() {
    return this.parameter;
  }

  /**
   * Get the list of complete constraints.
   */
  @Override
  public ArrayList<int[]> getCompleteConstraint() {
    return this.constraint;
  }

  /**
   * Get the complete model.
   */
  @Override
  public CTModel getCompleteModel(int tway) {
    CTModel sut = new CTModel(parameter, value, tway);
    sut.setConstraint(constraint);
    return sut;
  }

  /**
   * Get the sub-model of given PARA and CONS.
   */
  @Override
  public CTModel getSubModel(int para, double cons, int tway) {
    // use the first par parameters
    int[] val = new int[para];
    System.arraycopy(value, 0, val, 0, val.length);
    CTModel sut = new CTModel(para, val, tway);

    // find all cnf that correspond to the selected parameters
    ArrayList<int[]> cs = new ArrayList<>();
    int lastPV = relation[para - 1][value[para - 1] - 1];

    HashSet<Integer> fixed = new HashSet<>();
    for (int i = para; i < parameter; i++)
      fixed.add(defaultPV[i]);

    for (int[] cp : constraint) {
      ArrayList<Integer> in = new ArrayList<>();
      ArrayList<Integer> out = new ArrayList<>();

      for (int each : cp) {
        if (Math.abs(each) <= lastPV)
          in.add(each);
        else
          out.add(each);
      }

      // full constraint
      if (out.size() == 0) {
        if (!ALG.inList(cs, cp))
          cs.add(cp.clone());
      } else if (in.size() != 0) {
        // compute value of OUT based on fixed
        boolean outV = false;
        for (int e : out) {
          if (fixed.contains(Math.abs(e))) {
            if (e > 0)
              outV = true;
          } else {
            if (e < 0)
              outV = true;
          }
        }

        // if OUT is false, then some cnf in IN (IN V OUT)
        if (!outV) {
          int[] tcp = new int[in.size()];
          for (int k = 0; k < in.size(); k++)
            tcp[k] = in.get(k);
          if (!ALG.inList(cs, tcp))
            cs.add(tcp);
        }
      }
    }

    // only keep a proportion of cnf
    if (cons != -1) {
      int prop = (int) Math.round((double) cs.size() * cons);
      for (int k = cs.size(); k > prop; k--)
        cs.remove(k - 1);
    }

    // set constraint
    sut.setConstraint(cs);
    return sut;
  }

  /**
   * Get all faults.
   */
  @Override
  public List<String> getFaultList() {
    ArrayList<String> all = new ArrayList<>();
    allFault.forEach(x -> all.add(x.id));
    return all;
  }

  /*
   * Return the set of faults that can be detected by a given test suite.
   * Each fault is represented as v.[n].[k], where [n] is the version and
   * [k] is the index of faulty variant.
   */
  private Set<String> computeFaultDetected(ArrayList<TestCase> suite) {
    CTModel ground = getCompleteModel(2);
    Set<String> detected = new HashSet<>();
    int row = 0;
    for (TestCase tc : suite) {
      int[] at = amend(tc.test);
      if (INFO) System.out.print((row++) + ": " + Arrays.toString(tc.test) + " -> ");

      // constraint violation prevents fault detection
      if (!ground.isValid(at)) {
        if (INFO) System.out.print("invalid\n");
        continue;
      }

      for (Fault fault : allFault) {
        if (hitting(at, fault)) {
          detected.add(fault.id);
          if (INFO) System.out.print(fault.id + " ");
        }
      }
      if(INFO) System.out.print("\n");
    }
    return detected;
  }

  /**
   * Return fault detection matrix for a given test suite, where 1 indicates
   * hitting. Here the order of faults is as that in allFault list.
   */
  @Override
  public int[] computeFaultMatrix(ArrayList<TestCase> suite) {
    int[] fm = new int[allFault.size()];
    Set<String> detected = computeFaultDetected(suite);

    for (int i = 0; i < fm.length; i++) {
      String f = allFault.get(i).id;
      if (detected.contains(f))
        fm[i] = 1;
    }
    return fm;
  }
}