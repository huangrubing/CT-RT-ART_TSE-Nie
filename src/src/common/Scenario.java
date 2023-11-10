package common;

public class Scenario {

  public static String[] SIR_NAME = {"flex", "grep", "gzip", "sed", "make", "nanoxml"};
  public static int[][] SIR_PARA = {
      {4, 6, 8, 9},
      {4, 6, 8, 9},
      {6, 9, 12, 14},
      {5, 7, 9, 11},
      {4, 6, 8, 10},
      {4, 5, 6, 7}
  };
  public static double[][] SIR_CONS = {
    {0.0, 0.25, 0.5, 0.75, 1.0},
    {0.0, 0.25, 0.5, 0.75, 1.0},
    {0.0, 0.25, 0.5, 0.75, 1.0},
    {0.0, 0.25, 0.5, 0.75, 1.0},
    {0.0, 1.0},
    {0.0, 0.5, 1.0}
  };

  public static String[] LARGE_NAME = {"busybox", "linux", "drupal"};
  public static int[][] LARGE_PARA = {
      {28, 41, 55, 68},
      {42, 63, 84, 104},
      {19, 29, 38, 47}
  };
  public static double[][] LARGE_CONS = {
      {0.0, 0.25, 0.5, 0.75, 1.0},
      {0.0, 0.25, 0.5, 0.75, 1.0},
      {0.0, 0.25, 0.5, 0.75, 1.0}
  };

  public static String[] TECHNIQUE = {"ct", "rt", "art"};

}
