import edu.ucr.cs.riple.taint.ucrtainting.qual.*;
import org.apache.commons.lang3.text.WordUtils;

// Test taint passing through library method invocation
class LibraryTestInvocation {
    void untaintedToUntainted(@RUntainted String y) {
        @RUntainted String z = WordUtils.capitalize(y);
        Number s = new Integer(3);
    }

    void untaintedToTainted(@RUntainted String y) {
        @RTainted String z = WordUtils.capitalize(y);
    }

    void taintedToUntainted(@RTainted String y) {
        // :: error: assignment
        @RUntainted String z = WordUtils.capitalize(y);
    }

    void taintedToTainted(@RTainted String y) {
        @RTainted String z = WordUtils.capitalize(y);
    }

    int foo() {
        return 0;
    }
}
