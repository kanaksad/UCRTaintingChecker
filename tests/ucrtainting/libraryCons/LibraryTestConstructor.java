import edu.ucr.cs.riple.taint.ucrtainting.qual.*;
import javax.swing.*;

// Test taint passing through library method invocation
class LibraryTestInvocation {
    void testClassInstanceTaint(@RUntainted String y) {
        JButton myButton = new JButton(y);
        myButton.setBounds(50, 100, 95, 30);
    }
}
