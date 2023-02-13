import edu.ucr.cs.riple.taint.ucrtainting.qual.*;
import com.test.SupportingTest;

// Test taint passing through library method invocation
class ReceiverTest {
    void untainted(@RUntainted String y) {
        SupportingTest dummy = new SupportingTest(y);
        @RUntainted String dummyStr = dummy.getVal();
    }

    void tainted(@RTainted String y) {
        SupportingTest dummy = new SupportingTest(y);
        // :: error: assignment
        @RUntainted String dummyStr = dummy.getVal();
    }
}
