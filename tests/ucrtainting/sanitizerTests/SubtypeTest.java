package sanitizerTests;

import edu.ucr.cs.riple.taint.ucrtainting.qual.RTainted;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted;

// Test basic subtyping relationships for the UCR Tainting Checker.
class SubtypeTest {
  void validationArg(@RTainted String y) {
    if (validator(y)) {
      sink(y);
    }
    sink(y);
  }

  void validationReceiver(@RTainted String y) {
    if (y.contains("ss")) {
      return;
    }
    sink(y);
  }

  void sanitization(@RTainted String y) {
    y = sanitizer(y);
    sink(y);
  }

  void sink(@RUntainted String s) {}

  boolean validator(@RTainted String a) {
    return false;
  }

  String sanitizer(@RTainted String a) {
    return a;
  }
}
