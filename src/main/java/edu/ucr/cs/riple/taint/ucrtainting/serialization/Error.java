package edu.ucr.cs.riple.taint.ucrtainting.serialization;

import com.google.common.collect.ImmutableSet;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import java.util.Set;
import javax.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

/** Represents the reporting error from the checker. */
public class Error implements JSONSerializable {
  /** Message key for the error. */
  public final String messageKey;
  /** Message for the error. */
  public final String message;
  /**
   * Set of fixes that can resolve the error. If the error is not fixable, this set will be empty.
   */
  public final ImmutableSet<Fix> resolvingFixes;

  /**
   * The class symbol of the region that contains the error. If the error is not in a region, this
   * will be null.
   */
  @Nullable public final Symbol.ClassSymbol regionClass;
  /**
   * The symbol of the region member that contains the error. If the error is not in a class, or
   * inside a static initializer block, this will be null.
   */
  @Nullable public final Symbol regionSymbol;

  public Error(String messageKey, String message, Set<Fix> resolvingFixes, TreePath path) {
    this.messageKey = messageKey;
    this.message = message;
    this.resolvingFixes = ImmutableSet.copyOf(resolvingFixes);
    this.regionClass = Utility.findRegionClassSymbol(path);
    this.regionSymbol = Utility.findRegionMemberSymbol(this.regionClass, path);
  }

  @Override
  public JSONObject toJSON() {
    JSONObject ans = new JSONObject();
    ans.put("messageKey", messageKey);
    ans.put("message", message);
    JSONObject region = new JSONObject();
    region.put("class", regionClass);
    region.put("symbol", regionSymbol);
    ans.put("region", region);
    ans.put("fixes", new JSONArray(this.resolvingFixes.stream().map(Fix::toJSON).toArray()));
    return ans;
  }
}
