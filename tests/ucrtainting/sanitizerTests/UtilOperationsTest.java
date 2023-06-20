package sanitizerTests;

import edu.ucr.cs.riple.taint.ucrtainting.qual.EnsuresRUntaintedIf;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RTainted;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted;

class UtilOperationsTest {

  @EnsuresRUntaintedIf(result = true, expression = "#1")
  public boolean isUntaintedString(@RTainted String s) {
    // if the first parameter is tainted, and the result is true, then the expression is tainted
    // if the first parameter is tainted, and the result is false, then the expression is untainted
    // so the post condition here is that ifTheParameterIsNotTaintedMakeSureItGetsUntainted?
    if(s.equals("abc")) {
      return false;
    }
    return true;
  }

  public void checkUntaintedString(@RTainted String s) {
    if(isUntaintedString(s)) {
      sink(s);
    }
  }

  public void sink(@RUntainted String s) {}
}
