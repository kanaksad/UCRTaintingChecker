package edu.ucr.cs.riple.taint.ucrtainting.handlers;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import edu.ucr.cs.riple.taint.ucrtainting.UCRTaintingAnnotatedTypeFactory;
import edu.ucr.cs.riple.taint.ucrtainting.util.SymbolUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.TreeUtils;

/**
 * Handler for static final fields. This handler will make static final fields with an untainted
 * initializer, untainted
 */
public class StaticFinalFieldHandler extends AbstractHandler {

  /**
   * Set of static final fields that are visited previously by this handler. Fields added to this
   * set will be considered as untainted.
   */
  private final Set<Element> staticFinalFields;
  /**
   * Map of visited initializers. The key is the initializer expression and the value is the state
   * of the initializer.
   */
  private final Map<ExpressionTree, InitializerState> visitedInitializers;

  /** Enum to represent the state of the initializer. */
  enum InitializerState {
    UNKNOWN,
    UNTAINTED,
    TAINTED
  }

  public StaticFinalFieldHandler(UCRTaintingAnnotatedTypeFactory typeFactory) {
    super(typeFactory);
    this.staticFinalFields = new HashSet<>();
    this.visitedInitializers = new HashMap<>();
  }

  @Override
  public void addAnnotationsFromDefaultForType(Element element, AnnotatedTypeMirror type) {
    if (staticFinalFields.contains(element)) {
      makeUntaintedCustom(type);
      return;
    }
    if (SymbolUtils.isStaticAndFinalField(element)) {
      if (typeFactory.isUnannotatedField((Symbol.VarSymbol) element)) {
        makeUntaintedCustom(type);
      } else {
        Tree decl = typeFactory.declarationFromElement(element);
        if (decl instanceof VariableTree) {
          ExpressionTree initializer = ((VariableTree) decl).getInitializer();
          if (isUntaintedInitializer(initializer)) {
            staticFinalFields.add(element);
            makeUntaintedCustom(type);
          }
        }
      }
    }
  }

  @Override
  public void visitVariable(VariableTree tree, AnnotatedTypeMirror type) {
    Element element = TreeUtils.elementFromDeclaration(tree);
    if (staticFinalFields.contains(element)) {
      makeUntaintedCustom(type);
      return;
    }
    // check if is final and static
    if (SymbolUtils.isStaticAndFinalField(element)) {
      ExpressionTree initializer = tree.getInitializer();
      if (isUntaintedInitializer(initializer)) {
        staticFinalFields.add(element);
        makeUntaintedCustom(type);
      }
    }
  }

  /**
   * Check if the initializer expression is untainted.
   *
   * @param initializer the initializer expression.
   * @return true if the initializer is untainted.
   */
  private boolean isUntaintedInitializer(ExpressionTree initializer) {
    if (visitedInitializers.containsKey(initializer)
        && visitedInitializers.get(initializer) == InitializerState.UNKNOWN) {
      // to prevent loop.
      return false;
    }
    if (visitedInitializers.containsKey(initializer)) {
      return visitedInitializers.get(initializer) == InitializerState.UNTAINTED;
    }
    visitedInitializers.put(initializer, InitializerState.UNKNOWN);
    boolean isUntaintedInitializer;
    try {
      isUntaintedInitializer = typeFactory.hasUntaintedAnnotation(initializer);
    } catch (Exception e) {
      isUntaintedInitializer = false;
    }
    visitedInitializers.put(
        initializer,
        isUntaintedInitializer ? InitializerState.UNTAINTED : InitializerState.TAINTED);
    return isUntaintedInitializer;
  }

  /**
   * Make the type untainted. If the type is an array, the component type will also be untainted.
   *
   * @param type the type to make untainted.
   */
  private void makeUntaintedCustom(AnnotatedTypeMirror type) {
    typeFactory.makeUntainted(type);
    if (type instanceof AnnotatedTypeMirror.AnnotatedArrayType) {
      typeFactory.makeUntainted(((AnnotatedTypeMirror.AnnotatedArrayType) type).getComponentType());
    }
  }
}
