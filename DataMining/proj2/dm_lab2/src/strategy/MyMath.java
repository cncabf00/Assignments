package strategy;

public class MyMath {
  private static final double log2 = Math.log(2);

  static final double log2(double x) {
    return Math.log(x) / log2;
  }
}
