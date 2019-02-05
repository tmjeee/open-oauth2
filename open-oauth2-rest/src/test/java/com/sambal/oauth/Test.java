package com.sambal.oauth;

public class Test {


  public int power(int a, int p) {
    if (p==0) {
      return 1;
    }
    return _power(a, a, p);
  }

  private int _power(int x, int a, int p) {
    if (p == 1) {
      return x;
    }
    return _power(x*a, a, p-1);
  }


  public static void main(String[] args) throws Exception {
    Test t = new Test();
    System.out.println("2^0="+t.power(2,0));
    System.out.println("2^1="+t.power(2,1));
    System.out.println("2^2="+t.power(2,2));
    System.out.println("2^3="+t.power(2,3));
    System.out.println("2^4="+t.power(2,4));
    System.out.println("2^5="+t.power(2,5));
  }
}
