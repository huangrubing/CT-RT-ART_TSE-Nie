package common;

import combinatorial.CTModel;
import combinatorial.TestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;

public class CaseLARGE implements Case {

  /*
   * A fault and its corresponding fault introducing combinations.
   */
  class Fault {
    int id;                    // f.x
    String path;               // path
    List<Set<String>> combs;   // fault introducing combinations
    Set<String> involved;      // set of involved parameters

    Fault(int id, String path, List<Set<String>> combs) {
      this.id = id;
      this.path = path;
      this.combs = combs;
      involved = new HashSet<>();
      combs.forEach(x ->
        x.forEach(y -> involved.add(y.startsWith("!") ? y.substring(1) : y))
      );
    }

    String getIdentifier() {
      return String.format("f.%d", id);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("f." + id + " ");
      for (Set<String> x : combs)
        sb.append(x).append(" ");
      return sb.toString();
    }
  }

  public int parameter;
  public ArrayList<String> parameterName;
  public int[] value;

  // constraint (disjunction representation)
  public ArrayList<int[]> constraint;

  // default test case
  public int[] defaultTestCase;
  public int[] defaultPV;

  // assign each parameter-values to an unique PV value, which is used
  // in the CNF representation of constraint. (start from 1)
  public int[][] relation;
  public Map<Integer, String> relationName;  // key = PV, value = name

  // the set of faults
  public ArrayList<Fault> allFault;
  public Map<String, Fault> mapFault;  // key = f.x, value = fault

  private boolean INFO = false;
  public void setINFO(boolean info) {
    this.INFO = info;
  }

  public CaseLARGE(String name) {
    if (!Arrays.asList(Scenario.LARGE_NAME).contains(name))
      return;

    try {
      readModel("subject/" + name + ".model");
      readBugReport("subject-bugs/" + name + ".bug");
      showCompleteModel();
      checkSatisfied();

    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  private void showCompleteModel() {
    System.out.println("parameter = " + parameter + " constraint = " + constraint.size());

    for (int i = 0 ; i < parameterName.size() ; i++) {
      System.out.print(i + " " + parameterName.get(i) + " [");
      System.out.print(relation[i][0] + " " + relation[i][1] + "]\n");
    }

    Set<String> unique = new HashSet<>();
    for (int[] cons : constraint) {
      System.out.println(Arrays.toString(cons));
      for (int e : cons)
        unique.add(parameterName.get(index(e)));
    }
    System.out.println("# parameters involved in constraints = " + unique.size());

    unique.clear();
    System.out.println("\nbugs: ");
    for (Fault f : allFault) {
      System.out.println(f.id + ": " + f.path + " <- " + f.combs);
      unique.addAll(f.involved);
    }
    System.out.println("# parameters involved in bugs = " + unique.size());
  }

  /*
   * Get the index of the parameter according to a PV value.
   * For example, parameter P5 (starts from 0) has PV = 11 (P5 = 0)
   * and 12 (P5 = 1).
   */
  private static int index(int pv) {
    return (int) ceil(abs(pv) / 2.0) - 1;
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
    for (int i = 0 ; i < parameter ; i++) {
      if (!parts[i + 1].equals("2")) {
        System.err.println("incorrect value " + parts[i + 1]);
        return;
      }
      value[i] = 2;
    }

    parameterName = new ArrayList<>();
    relation = new int[parameter][2];
    relationName = new HashMap<>();

    int start = 1;
    for (int i = 0 ; i < parameter ; i++) {
      parts = br.readLine().trim().split(" ");
      parameterName.add(parts[1]);
      value[i] = 2;
      relation[i][0] = start++ ;
      relation[i][1] = start++ ;
      relationName.put(relation[i][0], "!" + parts[1]);
      relationName.put(relation[i][1], parts[1]);
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
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line;
    int bug_index = 0 ;
    while ((line = reader.readLine()) != null) {
      String[] parts = line.split(" : ");
      String bug_path = parts[0] ;
      List<Set<String>> bug_combs = new ArrayList<>();

      // deal with the combination
      String presenceCondition = parts[1].replaceAll("\\s", "");
      String[] options = presenceCondition.split("\\)\\|\\|\\(");
      for (String option : options){
        String[] macros = option.split("&&");
        Set<String> each = new HashSet<>();
        for( String e : macros ) {
          e = e.replaceAll("\\(", "");
          e = e.replaceAll("\\)","");
          e = e.replaceAll("CONFIG_", "");
          each.add(e);
        }
        bug_combs.add(each);
      }

      // add a new fault
      Fault fault = new Fault(bug_index, bug_path, bug_combs);
      allFault.add(fault);
      mapFault.put(fault.getIdentifier(), fault);
      bug_index += 1;
    }
  }

  /*
   * Determine whether a test case can trigger a particular fault.
   * The test case should be a valid test case.
   */
  private boolean hitting(final int[] test, Fault fault) {
    ArrayList<String> tc = getStringTest(amend(test));
    for(Set<String> each : fault.combs) {
      int cover = ((int) tc.stream().filter(each::contains).count());
      if (cover == each.size())
        return true;
    }
    return false;
  }

  /*
   * Get the string representation of a conventional test case.
   * Namely, convert [0, 1, ...] into [!ENABLE_A, ENABLE_B, ...]
   */
  private ArrayList<String> getStringTest(final int[] test) {
    assert test.length == parameter;

    ArrayList<String> out = new ArrayList<>();
    for( int i = 0 ; i < parameter ; i++ ) {
      out.add(test[i] == 1 ? parameterName.get(i) : "!" + parameterName.get(i));
    }
    return out;
  }

  /*
   * Amend a test case of sub-model to a test case of complete model.
   */
  public int[] amend(final int[] t) {
    if (t.length == parameter)
      return t.clone();

    int[] tc = new int[parameter];
    int len = t.length;
    System.arraycopy(t, 0, tc, 0, len);
    System.arraycopy(defaultTestCase, len, tc, len, parameter - len);
    return tc;
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
    sut.setParameterName(parameterName);
    sut.setConstraint(constraint);
    return sut;
  }

  /**
   * Get the sub-model of given PARA and CONS.
   */
  @Override
  public CTModel getSubModel(int para, double cons, int tway) {
    // use the first para parameters
    int[] val = new int[para];
    System.arraycopy(value, 0, val, 0, val.length);
    List<String> subName = new ArrayList<>();
    for (int k = 0 ; k < para ; k++)
      subName.add(parameterName.get(k));

    CTModel sut = new CTModel(para, val, tway);
    sut.setParameterName(subName);

    // find all constraints that correspond to the selected parameters
    List<int[]> cs = new ArrayList<>();
    int lastPV = relation[para - 1][value[para - 1] - 1];

    Set<Integer> fixed = new HashSet<>();
    for (int i = para; i < parameter; i++)
      fixed.add(defaultPV[i]);

    for (int[] cp : constraint) {
      List<Integer> in = new ArrayList<>();
      List<Integer> out = new ArrayList<>();
      for (int each : cp) {
        if (abs(each) <= lastPV)
          in.add(each);
        else
          out.add(each);
      }

      if (out.size() == 0) {
        // full constraint
        if (!ALG.inList(cs, cp))
          cs.add(cp.clone());
      } else if (in.size() != 0) {
        // compute value of OUT based on fixed
        boolean outV = false;
        for (int e : out) {
          if (fixed.contains(abs(e))) {
            if (e > 0)
              outV = true;
          } else {
            if (e < 0)
              outV = true;
          }
        }

        // if OUT is false, then some cnf in IN should be added (IN V OUT)
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
    allFault.forEach(x -> all.add(x.getIdentifier()));
    return all;
  }

  /*
   * Return the set of faults that can be detected by a given test suite.
   * Each fault is represented as an integer (fault id), indicating its
   * index in the allFault list.
   */
  private Set<Integer> computeFaultDetected(List<TestCase> suite) {
    CTModel ground = getCompleteModel(2);
    Set<Integer> detected = new HashSet<>();
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
          if (INFO) System.out.print(fault.getIdentifier() + " ");
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
    Set<Integer> detected = computeFaultDetected(suite);

    for (int i = 0; i < fm.length; i++) {
      int fid = allFault.get(i).id ;
      if (detected.contains(fid))
        fm[i] = 1;
    }
    return fm;
  }

  /*
   * Check whether default test case is valid, and whether all fault introducing
   * combinations are constraint satisfied.
   */
  private void checkSatisfied() {
    CTModel sut = getCompleteModel(2);
    ArrayList<Integer> invalid = new ArrayList<>();
    for (Fault f : allFault) {
      for (Set<String> x : f.combs) {
        int[] test = new int[parameter];
        for( int k = 0 ; k < parameter ; k++ )
          test[k] = -1;

        for (String e : x) {
          int idx = e.startsWith("!") ? parameterName.indexOf(e.substring(1)) : parameterName.indexOf(e);
          test[idx] = e.startsWith("!") ? 0 : 1 ;
        }

        if (!sut.isValid(test)) {
          invalid.add(f.id);
          System.out.println(">>> invalid fault: " + f.id);
          System.out.println("    combination: " + x);
          System.out.println("    tuple: " + Arrays.toString(test));
        }
      }
    }
    System.out.println();
    if (invalid.size() == 0)
      System.out.println("all faults are satisfied");
    System.out.println("default test case validity: " + sut.isValid(defaultTestCase));
    ArrayList<TestCase> temp = new ArrayList<>();
    temp.add(new TestCase(defaultTestCase));
    Set<Integer> dec = computeFaultDetected(temp);
    System.out.println("default test case hits: " + dec);
    //System.out.println(Arrays.toString(defaultTestCase));
    //System.out.println(getStringTest(defaultTestCase));
  }


}
