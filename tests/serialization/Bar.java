package foo.bar;

import edu.xxx.cs.yyyyy.taint.tainttyper.qual.*;
import javax.servlet.http.HttpServletResponse;

class Bar extends Foo {
  public String field = "";

  public Baz baz = new Baz();

  public static String staticF = "";

  String getField() {
    return field;
  }

  @Override
  protected void writeLoginPageLink(HttpServletResponse resp) {
    resp.setContentType(MIME_HTML_TEXT);
  }
}
