import edu.ucr.cs.riple.taint.ucrtainting.qual.*;
import com.test.SupportingTest;

// Test taint passing through library method invocation
class ReceiverTest {
    void untaintedToUntainted(@RUntainted String y) {
        SupportingTest dummy = new SupportingTest(y);
        @RUntainted String dummyStr = dummy.getVal();
    }
}
