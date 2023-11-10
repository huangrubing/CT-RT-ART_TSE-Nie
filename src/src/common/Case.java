package common;

import combinatorial.CTModel;
import combinatorial.TestCase;

import java.util.ArrayList;
import java.util.List;

public interface Case {

  // get the complete number of parameters
  int getCompleteParameter();

  // get the list of constraint (disjunction representation)
  ArrayList<int[]> getCompleteConstraint();

  // get all faults
  List<String> getFaultList();

  // get the complete model
  CTModel getCompleteModel(int tway);

  // get the sub model of given PARA and CONS
  CTModel getSubModel(int para, double cons, int tway);

  // compute the fault detection matrix of a given test suite
  int[] computeFaultMatrix(ArrayList<TestCase> suite);

}
