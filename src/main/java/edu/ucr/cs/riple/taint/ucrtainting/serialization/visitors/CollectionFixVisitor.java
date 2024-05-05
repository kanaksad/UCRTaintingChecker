package edu.ucr.cs.riple.taint.ucrtainting.serialization.visitors;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.Context;
import edu.ucr.cs.riple.taint.ucrtainting.FoundRequired;
import edu.ucr.cs.riple.taint.ucrtainting.UCRTaintingAnnotatedTypeFactory;
import edu.ucr.cs.riple.taint.ucrtainting.handlers.CollectionHandler;
import edu.ucr.cs.riple.taint.ucrtainting.serialization.Fix;
import edu.ucr.cs.riple.taint.ucrtainting.serialization.TypeIndex;
import edu.ucr.cs.riple.taint.ucrtainting.util.TypeUtils;
import java.util.Set;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.TreeUtils;

/**
 * Visitor for handling specific cases of Collection type. Generic {@code toArray} method and
 * constructors require special handling.
 */
public class CollectionFixVisitor extends SpecializedFixComputer {

  public CollectionFixVisitor(
      UCRTaintingAnnotatedTypeFactory typeFactory, FixComputer fixComputer, Context context) {
    super(typeFactory, fixComputer, context);
  }

  /**
   * If the invoked method is the generic {@link java.util.Collection#toArray(Object[])} method,
   * then the component type of the array can be untainted if the collection is of type {@code
   * Collection<@Untainted T>}. This method will locate the type argument of the collection and make
   * a corresponding fix.
   *
   * @param node The method invocation tree.
   * @param pair The found and required types.
   * @return A set of fixes to be applied to the tree.
   */
  @Override
  public Set<Fix> visitMethodInvocation(MethodInvocationTree node, FoundRequired pair) {
    ExpressionTree receiver = TreeUtils.getReceiverTree(node);
    // Locate the index of type argument passed to collection from type.
    Type type = TypeUtils.getType(receiver);
    Type current = type.tsym.type;
    int index = -1;
    while (current instanceof Type.ClassType && index == -1) {
      Type.ClassType classType = (Type.ClassType) current;
      if (classType.interfaces_field != null) {
        for (Type iFace : classType.interfaces_field) {
          if (iFace.tsym instanceof Symbol.ClassSymbol
              && ((Symbol.ClassSymbol) iFace.tsym)
                  .fullname
                  .toString()
                  .equals(CollectionHandler.COLLECTIONS_INTERFACE)) {
            String name = iFace.getTypeArguments().get(0).toString();
            for (int i = 0; i < type.tsym.type.getTypeArguments().size(); i++) {
              if (type.tsym.type.getTypeArguments().get(i).toString().equals(name)) {
                // found the type variable.
                index = i;
                break;
              }
            }
          }
        }
      }
      Type superType = ((Type.ClassType) current).supertype_field;
      if (!(superType instanceof Type.ClassType)) {
        break;
      }
      current = superType;
    }
    if (index != -1) {
      return receiver.accept(
          fixComputer,
          typeFactory.makeUntaintedPair(receiver, TypeIndex.setOf(index + 1, 0), pair.depth));
    }
    return Set.of();
  }

  /**
   * Adapts types of the new created collection by updating the type of the passed collection as
   * initializer. (e.g. to make {@code List<String>(l)} have type {@code List<@Untainted String>} it
   * will suggest making {@code l} of type {@code List<@Untainted String>})
   *
   * @param node the node being visited.
   * @param pair the found and required types.
   * @return a set of fixes to be applied to the tree.
   */
  @Override
  public Set<Fix> visitNewClass(NewClassTree node, FoundRequired pair) {
    AnnotatedTypeMirror.AnnotatedDeclaredType foundType =
        (AnnotatedTypeMirror.AnnotatedDeclaredType) pair.found;
    AnnotatedTypeMirror.AnnotatedDeclaredType requiredType =
        (AnnotatedTypeMirror.AnnotatedDeclaredType) pair.required;
    if (typeFactory.hasUntaintedAnnotation(requiredType.getTypeArguments().get(0))
        && !typeFactory.hasUntaintedAnnotation(foundType.getTypeArguments().get(0))) {
      if (node.getArguments().size() == 1) {
        ExpressionTree arg = node.getArguments().get(0);
        return arg.accept(
            fixComputer, typeFactory.makeUntaintedPair(arg, TypeIndex.setOf(1, 0), pair.depth));
      }
    }
    return Set.of();
  }
}