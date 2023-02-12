import edu.ucr.cs.riple.taint.ucrtainting.qual.*;

// Test class constructor parameters for taint
class ClassTestInvocation {

    public class testClass {
        private @RUntainted String arg1;
        private @RTainted String arg2;
        testClass(@RUntainted String a1, String a2) {
            arg1=a1;
            arg2=a2;
        }
    }

    testClass obj=new testClass("a","b");

    void untaintedToUntainted() {
        @RUntainted String untaintedArg1=obj.arg1;
    }

    void taintedToUntainted() {
        @RTainted String taintedArg2=obj.arg2;
    }
    void taintedToTainted() {
        @RTainted String taintedArg1=obj.arg1;
    }

    void untaintedToTainted() {
        // :: error: (assignment)
        @RUntainted String untaintedArg2=obj.arg2;
    }

}
