package edu.ucr.cs.riple.taint.ucrtainting.serialization.visitors;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import edu.ucr.cs.riple.taint.ucrtainting.FoundRequired;
import edu.ucr.cs.riple.taint.ucrtainting.UCRTaintingAnnotatedTypeFactory;
import edu.ucr.cs.riple.taint.ucrtainting.handlers.CollectionHandler;
import edu.ucr.cs.riple.taint.ucrtainting.serialization.Fix;
import edu.ucr.cs.riple.taint.ucrtainting.serialization.Serializer;
import edu.ucr.cs.riple.taint.ucrtainting.serialization.TypeIndex;
import edu.ucr.cs.riple.taint.ucrtainting.util.SymbolUtils;
import edu.ucr.cs.riple.taint.ucrtainting.util.TypeUtils;
import java.util.Set;
import javax.lang.model.element.Element;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.TreeUtils;

/**
 * General Fix visitor.This visitor determines the approach for resolving the error upon visiting
 * specific nodes that may impact the algorithm selection.
 */
public class FixComputer extends SimpleTreeVisitor<Set<Fix>, FoundRequired> {

  /**
   * The type factory of the checker. Used to get the type of the tree and generate a fix only if is
   * {@link edu.ucr.cs.riple.taint.ucrtainting.qual.RTainted}.
   */
  protected final UCRTaintingAnnotatedTypeFactory typeFactory;

  protected final Types types;
  protected final Context context;
  protected final BasicVisitor basicVisitor;
  protected final SpecializedFixComputer thirdPartyFixVisitor;
  protected final SpecializedFixComputer methodTypeArgumentFixVisitor;

  public FixComputer(UCRTaintingAnnotatedTypeFactory factory, Types types, Context context) {
    this.typeFactory = factory;
    this.context = context;
    this.types = types;
    this.basicVisitor = new BasicVisitor(factory, this, context);
    this.thirdPartyFixVisitor = new UnannotatedCodeFixVisitor(typeFactory, this, context);
    this.methodTypeArgumentFixVisitor = new GenericMethodFixVisitor(typeFactory, this, context);
  }

  @Override
  public Set<Fix> defaultAction(Tree node, FoundRequired pair) {
    return node.accept(basicVisitor, pair);
  }

  @Override
  public Set<Fix> visitMemberSelect(MemberSelectTree tree, FoundRequired pair) {
    if (tree instanceof JCTree.JCFieldAccess) {
      ExpressionTree receiver = TreeUtils.getReceiverTree(tree);
      if (receiver != null) {
        Symbol symbol = (Symbol) TreeUtils.elementFromUse(tree);
        if (symbol.getKind().isField()
            && typeFactory.isUnannotatedField((Symbol.VarSymbol) symbol)) {
          return thirdPartyFixVisitor.visitMemberSelect(tree, pair);
        }
      }
    }
    return defaultAction(tree, pair);
  }

  /**
   * Visitor for method invocations. In method invocations, we might choose different approaches:
   *
   * <ol>
   *   <li>If in stub files, exit
   *   <li>If method has type args, and by changing the parameter types of parameters, we can
   *       achieve required type, we annotate the parameters.
   *   <li>If return type of method has type arguments and the call has a valid receiver, we
   *       annotate the receiver.
   *   <li>If method is in third party library, we annotate the receiver and parameters.
   *   <li>Annotate the called method directly
   * </ol>
   *
   * @param node The given tree.
   * @return Void null.
   */
  @Override
  public Set<Fix> visitMethodInvocation(MethodInvocationTree node, FoundRequired pair) {
    Element element;
    try {
      element = TreeUtils.elementFromUse(node);
    } catch (Exception e) {
      Serializer.log("Error in finding the element from invocation: " + node);
      return Set.of();
    }
    if (element == null) {
      return Set.of();
    }
    Symbol.MethodSymbol calledMethod = (Symbol.MethodSymbol) element;
    // Locate method receiver.
    ExpressionTree receiver = TreeUtils.getReceiverTree(node);
    boolean isInAnnotatedPackage = !typeFactory.isUnannotatedMethod(calledMethod);
    boolean isTypeVar = TypeUtils.containsTypeArgument(calledMethod.getReturnType());
    boolean hasReceiver =
        !(calledMethod.isStatic() || receiver == null || SymbolUtils.isThisIdentifier(receiver));
    AnnotatedTypeMirror.AnnotatedExecutableType methodAnnotatedType =
        typeFactory.getAnnotatedType(calledMethod);
    boolean hasPolyTaintedAnnotation =
        methodAnnotatedType != null
            && typeFactory.hasPolyTaintedAnnotation(methodAnnotatedType.getReturnType());
    boolean methodHasTypeArgs = !calledMethod.getTypeParameters().isEmpty();
    if (hasPolyTaintedAnnotation) {
      Set<Fix> polyFixes = node.accept(new PolyMethodVisitor(typeFactory, this, context), pair);
      if (!polyFixes.isEmpty()) {
        return polyFixes;
      }
    }
    if (CollectionHandler.isToArrayWithTypeArgMethod(calledMethod, types)) {
      // update the type argument to match that.
      Type type = TypeUtils.getType(receiver);
      Type current = type.tsym.type;
      int index = -1;
      while (current instanceof Type.ClassType) {
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
                  index = i;
                  break;
                }
              }
            }
          }
        }
        if (index != -1) {
          break;
        }
        Type superType = ((Type.ClassType) current).supertype_field;
        if (!(superType instanceof Type.ClassType)) {
          break;
        }
        current = superType;
      }
      if (index != -1) {
        AnnotatedTypeMirror receiverType = typeFactory.getAnnotatedType(receiver);
        AnnotatedTypeMirror required = receiverType.deepCopy(true);
        typeFactory.makeUntainted(required, TypeIndex.setOf(index + 1, 0));
        return receiver.accept(this, FoundRequired.of(receiverType, required, pair.depth));
      }
    }
    if (methodHasTypeArgs) {
      Set<Fix> fixes = node.accept(methodTypeArgumentFixVisitor, pair);
      if (!fixes.isEmpty()) {
        return fixes;
      }
    }
    // The method has a receiver, if the method contains a type argument, we should annotate the
    // receiver and leave the called method untouched. Annotation on the declaration on the type
    // argument, will be added on the method automatically.
    if (isTypeVar && hasReceiver) {
      Set<Fix> fixes = node.accept(new TypeArgumentFixVisitor(typeFactory, this, context), pair);
      if (!fixes.isEmpty()) {
        return fixes;
      }
    }
    // check if the call is to a method defined in a third party library. If the method has a type
    // var return type and has a receiver, we should annotate the receiver.
    if (!isInAnnotatedPackage && basicVisitor.requireFix(pair)) {
      return node.accept(thirdPartyFixVisitor, pair);
    }
    // The method has a receiver, if the method contains a type argument, we should annotate the
    // receiver and leave the called method untouched. Annotation on the declaration on the type
    // argument, will be added on the method automatically.
    return defaultAction(node, pair);
  }

  public void reset(TreePath currentPath) {
    basicVisitor.reset(currentPath);
  }
}
