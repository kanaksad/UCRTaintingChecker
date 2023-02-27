import com.test.thirdparty.LibraryCodeTestSupport;
import edu.ucr.cs.riple.taint.ucrtainting.qual.*;

// Test taint passing through custom library method invocation
class CustomLibraryTest {
  void untainted(@RUntainted String y) {
    LibraryCodeTestSupport dummy = new LibraryCodeTestSupport(y);
    @RUntainted String dummyStr = dummy.getVal();
  }

  void tainted(@RTainted String y) {
    LibraryCodeTestSupport dummy = new LibraryCodeTestSupport(y);
    // :: error: assignment
    @RUntainted String dummyStr = dummy.getVal();
  }
}
