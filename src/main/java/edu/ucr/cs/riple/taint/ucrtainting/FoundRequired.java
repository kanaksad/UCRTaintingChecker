package edu.ucr.cs.riple.taint.ucrtainting;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

/**
 * Class that creates string representations of {@link AnnotatedTypeMirror}s which are only verbose
 * if required to differentiate the two types.
 */
public class FoundRequired {
  public final String foundString;
  public final String requiredString;
  public final AnnotatedTypeMirror found;
  public final AnnotatedTypeMirror required;
  public int depth;
  public static final int MAX_DEPTH = 5;

  public FoundRequired(
      @Nonnull AnnotatedTypeMirror found, @Nonnull AnnotatedTypeMirror required, int depth) {
    this.found = found;
    this.required = required;
    this.foundString = found.toString(true);
    this.requiredString = required.toString(true);
    this.depth = depth;
  }

  /**
   * Creates string representations of {@link AnnotatedTypeMirror}s which are only verbose if
   * required to differentiate the two types.
   */
  public static FoundRequired of(
      AnnotatedTypeMirror found, AnnotatedTypeMirror required, int depth) {
    return new FoundRequired(found, required, depth);
  }

  /**
   * Increments the depth of the found required pair. This is used to prevent infinite recursion.
   * While visiting a new method body, the depth is incremented.
   */
  public void incrementDepth() {
    this.depth++;
  }

  /**
   * Returns true if the depth of the found required pair is greater than or equal to the max depth.
   */
  public boolean isMaxDepth() {
    return this.depth >= MAX_DEPTH;
  }

  /**
   * Decrements the depth of the found required pair. This is used to decrease depth when exiting a
   * method body.
   */
  public void decrementDepth() {
    this.depth--;
  }

  @Override
  public String toString() {
    return "found='" + foundString + '\'' + ", required='" + requiredString + ", depth: " + depth;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FoundRequired)) {
      return false;
    }
    FoundRequired that = (FoundRequired) o;
    return Objects.equals(found, that.found) && Objects.equals(required, that.required);
  }

  @Override
  public int hashCode() {
    return Objects.hash(found, required);
  }
}
