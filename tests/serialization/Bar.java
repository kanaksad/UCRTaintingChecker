import edu.ucr.cs.riple.taint.ucrtainting.qual.*;

class Bar {
  public Object field = new Object();

  public Baz baz = new Baz();

  public static Object staticF = new Object();

  Object getField() {
    return field;
  }
}
