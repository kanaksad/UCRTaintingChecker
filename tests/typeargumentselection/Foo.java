package test;

import edu.ucr.cs.riple.taint.ucrtainting.qual.*;
import java.util.*;
import javax.servlet.http.*;
import java.util.Map.Entry;

public class Foo {

  public void testMap(String param) {
    Map<String, String> map = new HashMap<>();
    // :: error: assignment
    @RUntainted String s = map.get(param);
  }

  public void testMapOfList(List<String> key) {
    Map<List<String>, List<String>> mapOfList = new HashMap<>();
    // :: error: assignment
    @RUntainted String s = mapOfList.get(key).get(0);
  }

  public void testGenericFoo() {
    GenericFoo<String, String> gen = new GenericFoo<>();
    // :: error: assignment
    @RUntainted String s = gen.bar.getM();
    // :: error: assignment
    s = gen.bar.getL();
  }

  public void testGenericBar() {
    GenericBar<String, Map<String, String>> gen = new GenericBar<>();
    // :: error: assignment
    @RUntainted String s = gen.instance().bar.getM();
    // :: error: assignment
    s = gen.instanceWithL().getK().keySet().iterator().next();
    // :: error: assignment
    s = gen.instanceWithL().getK().values().iterator().next();
    // :: error: assignment
    Iterator<@RUntainted String> iter = gen.instanceWithL().getK().keySet().iterator();
  }

  public void rawUsedTypes(Item item1) {
    // :: error: assignment
    @RUntainted String name1 = (String) (item1.getItemProperty(null).getValue());
  }

  public @RUntainted String sameTypeVarWithDifferentOwners(String galleryType) {
    SortedMap<String, String> m_startGalleriesSettings = null;
    // :: error: return
    return m_startGalleriesSettings.get(galleryType);
  }

  public void multiple(){
    Iterator<Entry<String, String>> itEntries = null;
    // :: error: assignment
    @RUntainted Entry<@RUntainted String, @RUntainted String> entry = itEntries.next();
  }

  static class GenericFoo<T, K> {
    GenericBar<T, T> bar;

    K k;

    GenericBar<T, T> getBar() {
      return bar;
    }

    public K getK() {
      return k;
    }
  }

  static class GenericBar<M, L> {
    M getM() {
      return null;
    }

    L getL() {
      return null;
    }

    GenericFoo<M, L> instanceWithL() {
      return null;
    }

    GenericFoo<M, String> instance() {
      return null;
    }

    public void internal() {
      GenericBar<M, L> gen = new GenericBar<>();
      // :: error: assignment
      @RUntainted M m = gen.getM();
    }
  }

  public interface Item {
    public Property getItemProperty(Object id);
  }

  public interface Property<T> {
    public T getValue();
  }
}