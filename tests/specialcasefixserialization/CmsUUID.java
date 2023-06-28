package test;

import com.vaadin.shared.ui.ContentMode;
import edu.ucr.cs.riple.taint.ucrtainting.qual.*;
import java.util.*;
import javax.servlet.http.*;
import org.safehaus.uuid.UUID;

public class CmsUUID {

  /** Internal UUID implementation. */
  private transient UUID m_uuid;
  /** Constant for the null UUID. */
  private static final @RUntainted CmsUUID NULL_UUID = new CmsUUID(UUID.getNullUUID());

  private static CmsUUID cms;

  private static final String BAZ = "property";
  private static final String PATH = "/";

  //  // Should not be error here.
  static final int CONCURRENCY_LEVEL = 8;
  public static final List<@RUntainted String> FOLDERS =
      Collections.unmodifiableList(Arrays.asList(BAZ));

  public CmsUUID(UUID uuid) {
    m_uuid = uuid;
  }

  public static final String IMAGE_MIMETYPECONFIG = "png:image/png|gif:image/gif|jpg:image/jpeg";
  public static final Map<String, @RUntainted String> IMAGE_MIMETYPES =
      Collections.unmodifiableMap(splitAsMap(IMAGE_MIMETYPECONFIG, "|", ":"));

  public static CmsUUID create(UUID uid) {
    return new CmsUUID(uid);
  }

  @RUntainted
  Object castTest() {
    if (this == NULL_UUID) {
      return NULL_UUID;
    }
    // :: error: return
    return new CmsUUID((UUID) m_uuid.clone());
  }

  void binaryExpressionTest() {
    // :: error: assignment
    @RUntainted boolean isDefault = (m_uuid != null) && Boolean.valueOf(m_uuid.toString());
  }

  void enumFromThirdPartyTest() {
    // should not be error here.
    @RUntainted ContentMode mode = ContentMode.HTML;
  }

  @RUntainted
  CmsUUID staticCallTest(UUID uid) {
    // :: error: return
    return CmsUUID.create(uid);
  }

  protected @RUntainted CmsPair<@RUntainted String, @RUntainted String> decode(
      String content, String encoding) {
    // :: error: return
    return CmsPair.create(content, encoding);
  }

  public void testFinalStaticString() {
    // should not be error here.
    @RUntainted String foo1 = BAZ;
    // should not be error here.
    @RUntainted String foo2 = CmsUUID.BAZ;
  }

  public enum BundleType {
    // :: error: assignment
    PROPERTY(cms);

    BundleType(CmsUUID s) {}

    public static @RUntainted BundleType toBundleType(String value) {

      if (null == value) {
        return null;
      }
      if (value.equals(PROPERTY.toString())) {
        // Should not get error here.
        return PROPERTY;
      }
      return null;
    }
  }

  public void testConstantForThirdpartyArgument() {
    @RUntainted List<String> list = new ArrayList<>();
    // :: error: (assignment)
    @RUntainted String s = list.get(0);
  }

  public void testMemberSelectOfFinalStatic(HttpServletResponse response) {
    sink(Boolean.TRUE);
  }

  public void bar() {
    try {
      // some code
    } catch (Exception e) {
      // Should not try to annotate "e" here.
      // :: error: assignment
      @RUntainted Exception dup = e;
    }
  }

  public void sink(@RUntainted boolean b) {}

  public void multipleAdditionTest() {
    class XMLPage {
      public String getRootPath() {
        return "";
      }
    }
    @RUntainted String path = "some path";
    String[] tokens = path.split("/");
    // :: error: assignment
    @RUntainted String name = tokens[1];
    XMLPage xmlPage = new XMLPage();
    @RUntainted
    String fullPath =
        // :: error: assignment
        xmlPage.getRootPath() + "/" + tokens[0] + "/" + name + "." + BAZ;
  }

  public void testUntaintedForAnyFinalStaticWithInitializer() {
    for (String folder : FOLDERS) {
      // Should not be an error here.
      @RUntainted String f = folder;
    }

    for (String folder : CmsUUID.FOLDERS) {
      // Should not be an error here.
      @RUntainted String f = folder;
    }
  }

  public static @RUntainted Map<@RUntainted String, @RUntainted String> splitAsMap(
      String source, String paramDelim, String keyValDelim) {

    int keyValLen = keyValDelim.length();
    // use LinkedHashMap to preserve the order of items
    Map<@RUntainted String, @RUntainted String> params =
        new LinkedHashMap<@RUntainted String, @RUntainted String>();
    Iterator<String> itParams = splitAsList(source, paramDelim, true).iterator();
    while (itParams.hasNext()) {
      String param = itParams.next();
      int pos = param.indexOf(keyValDelim);
      String key = param;
      String value = "";
      if (pos > 0) {
        key = param.substring(0, pos);
        if ((pos + keyValLen) < param.length()) {
          value = param.substring(pos + keyValLen);
        }
      }
      // :: error: argument
      params.put(key, value);
    }
    return params;
  }

  public static List<String> splitAsList(String source, String delimiter, boolean trim) {

    int dl = delimiter.length();

    List<String> result = new ArrayList<String>();
    int i = 0;
    int l = source.length();
    int n = source.indexOf(delimiter);
    while (n != -1) {
      // zero - length items are not seen as tokens at start or end:  ",," is one empty token but
      // not three
      if ((i < n) || ((i > 0) && (i < l))) {
        result.add(trim ? source.substring(i, n).trim() : source.substring(i, n));
      }
      i = n + dl;
      n = source.indexOf(delimiter, i);
    }
    // is there a non - empty String to cut from the tail?
    if (n < 0) {
      n = source.length();
    }
    if (i < n) {
      result.add(trim ? source.substring(i).trim() : source.substring(i));
    }
    return result;
  }

  public void testValueForStaticFinalMap() {
    @RUntainted String s = IMAGE_MIMETYPES.get("png");
  }

  public void recentCrash() {
    List<String> params = new LinkedList<String>();
    // :: error: argument
    ProcessBuilder pb = new ProcessBuilder(params.toArray(new String[params.size()]));
  }
}
