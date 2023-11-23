package foo.bar;

import edu.ucr.cs.riple.taint.ucrtainting.qual.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.digester3.CallMethodRule;
import org.apache.commons.digester3.Digester;
import org.xml.sax.Attributes;

class Foo {

  String field = "";
  Bar bar = new Bar();

  static final String staticField = "";
  protected static final String MIME_HTML_TEXT = "text/html";
  private static final Pattern PATTERN_CRLF = Pattern.compile("(\\r|\\n)");

  void bar(String x, @RUntainted String y, boolean b) {
    String localVar = x;
    final String finalLocalVar = x;
    // :: error: assignment
    @RUntainted String c = field;
    // :: error: assignment
    c = x;
    // :: error: assignment
    c = b ? x : y;
    // :: error: assignment
    c = x + y;
    // :: error: assignment
    c = b ? x + y : localVar;
    if (b) {
      // :: error: assignment
      c = bar.getField();
    } else {
      // :: error: assignment
      c = bar.field;
      // :: error: assignment
      c = x;
      // :: error: assignment
      c = localVar;
      // :: error: assignment
      c = bar.baz.field;
    }
    class LocalClass {
      @RUntainted String field;
      String f2;

      void foo() {
        // :: error: assignment
        field = finalLocalVar;
        // :: error: assignment
        field = f2;
        String localVar = f2;
        List<String> argsList = new ArrayList<>();
        String[] argsArray = new String[10];
        class LocalInnerClass {
          // :: error: assignment
          @RUntainted String baz = localVar + argsList.get(0) + argsArray[0];
        }
      }
    }
  }

  void requireUntainted(@RUntainted Object param) {}

  public void testAndOr(boolean op1, boolean op2) {
    // :: error: assignment
    @RUntainted boolean x = op1 && op2;
    // :: error: assignment
    x = op1 || op2;
  }

  protected void writeLoginPageLink(HttpServletResponse resp) {
    resp.setContentType(MIME_HTML_TEXT);
    List<String> param = null;
    // :: error: assignment
    @RUntainted String s = doInSystemTransaction(param);
  }

  protected <T> T doInSystemTransaction(List<T> list) {
    return null;
  }

  private @RUntainted String sanitize(String redirectUrl) {
    if (redirectUrl != null) {
      // :: error: return
      return PATTERN_CRLF.matcher(redirectUrl).replaceAll("");
    }
    return null;
  }

  public void test1(String op1) {
    @RUntainted String s = "hello";
    // :: error: compound.assignment
    s += op1;
  }

  public void testBinaryEqual(@RUntainted String a, String b) {
    // :: error: assignment
    @RUntainted boolean c = (a == b);
  }

  public void testUntaintedForAnnotationMethodCalls(InputConfig annot) {
    @RUntainted String a = annot.methodName();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public @interface InputConfig {
    String methodName() default "";
  }

  public void shouldNotReportErrorForOptional(Digester digester) {
    digester.addRule(
        null,
        new CallMethodRule(null, 15, new Class[] {}) {
          @Override
          public void begin(String namespace, String name, Attributes attributes) throws Exception {
            getDigester().peekParams()[14] = Optional.empty();
          }
        });
  }
}
