package foo.bar;

import edu.ucr.cs.riple.taint.ucrtainting.qual.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Foo {

  @SuppressWarnings("unchecked")
  public void testNewCollectionAsArgumentForRawType(Map map) {
    // We expect that checker must not crash here
    List<String> ret = new ArrayList<String>(map.keySet());
  }

  void acknowledgeAnnotOnReceiverOnToArrayMethod() {
    LinkedList<@RUntainted String> c1 = new LinkedList<>();
    @RUntainted Object[] array = c1.toArray();
  }

  void refraingFromApplyingUnannotatedCodeHandlerForToArrayMethod() {
    LinkedList<String> c1 = new LinkedList<>();
    // :: error: assignment
    @RUntainted Object[] array = c1.toArray();
  }

  void arraysAsListGenericTest() {
    LinkedList<String> c1 = new LinkedList<>();
    c1.addFirst("x");
    Object[] array = c1.toArray();
    // :: error: assignment
    List<@RUntainted Object> c2 = java.util.Arrays.asList(array);
  }
}
