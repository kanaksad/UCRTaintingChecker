package foo.bar;

import edu.ucr.cs.riple.taint.ucrtainting.qual.*;
import java.util.*;
import javax.servlet.http.*;

public class Test {

  @RPolyTainted
  String foo(@RPolyTainted String s0, String s1) {
    return s0;
  }

  protected @RUntainted String contentType = "text/plain";

  public void simple(String param) {
    @RUntainted String s = foo("foo", param);
    // :: error: assignment
    @RUntainted String s2 = foo(param, "bar");
  }

  public @RPolyTainted String thirdParty(@RPolyTainted int index, @RPolyTainted String s) {
    return s.substring(index);
  }

  public void test(HttpServletResponse oResponse, ActionInvocation invocation) {
    oResponse.setContentType(conditionalParse(contentType, invocation));
  }

  protected @RPolyTainted String conditionalParse(
      @RPolyTainted String param, ActionInvocation invocation) {
    return param;
  }

  class ActionInvocation {}
}
