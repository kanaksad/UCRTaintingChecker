/*
 * MIT License
 *
 * Copyright (c) 2024 University of California, Riverside
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package edu.ucr.cs.riple.taint.ucrtainting.serialization.location;

import com.sun.tools.javac.code.Symbol;
import edu.ucr.cs.riple.taint.ucrtainting.serialization.TypeIndex;
import edu.ucr.cs.riple.taint.ucrtainting.util.SymbolUtils;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

/** abstract base class for {@link SymbolLocation}. */
public abstract class AbstractSymbolLocation implements SymbolLocation {

  /** Location kind of the targeted symbol */
  public final LocationKind kind;
  /** Path of the file containing the symbol, if available. */
  public final Path path;
  /** Enclosing class of the symbol. */
  public final Symbol.ClassSymbol enclosingClass;
  /** Target symbol. */
  public final Symbol target;
  /** Set of type indexes */
  // todo: make this field final
  public Set<TypeIndex> typeIndexSet;

  public AbstractSymbolLocation(LocationKind kind, Symbol target) {
    this.kind = kind;
    this.enclosingClass = target.enclClass();
    this.path = SymbolUtils.getPathFromSymbol(target);
    this.target = target;
  }

  @Override
  public void setTypeIndexSet(@Nullable Set<TypeIndex> typeIndexSet) {
    this.typeIndexSet =
        (typeIndexSet == null || typeIndexSet.isEmpty()) ? TypeIndex.topLevel() : typeIndexSet;
  }

  @Override
  public String toString() {
    return "kind="
        + kind
        + ", enclosingClass="
        + enclosingClass
        + ", target="
        + target
        + ", typeIndexSet="
        + typeIndexSet
        + ", path="
        + path
        + '}';
  }

  @Override
  public LocationKind getKind() {
    return kind;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbstractSymbolLocation)) {
      return false;
    }
    AbstractSymbolLocation that = (AbstractSymbolLocation) o;
    return getKind() == that.getKind()
        && Objects.equals(path, that.path)
        && Objects.equals(enclosingClass, that.enclosingClass)
        && Objects.equals(target, that.target)
        && Objects.equals(typeIndexSet, that.typeIndexSet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getKind(), path, enclosingClass, target, typeIndexSet);
  }

  @Override
  public Symbol getTarget() {
    return target;
  }

  @Override
  public Path path() {
    return path;
  }

  @Override
  public Set<TypeIndex> getTypeIndexSet() {
    return typeIndexSet;
  }

  public boolean isTopLevel() {
    return typeIndexSet.size() == 1 && typeIndexSet.contains(TypeIndex.TOP_LEVEL);
  }
}
