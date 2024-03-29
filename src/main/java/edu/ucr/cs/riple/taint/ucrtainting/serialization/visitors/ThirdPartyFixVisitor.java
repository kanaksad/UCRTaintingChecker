package edu.ucr.cs.riple.taint.ucrtainting.serialization.visitors;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Context;
import edu.ucr.cs.riple.taint.ucrtainting.FoundRequired;
import edu.ucr.cs.riple.taint.ucrtainting.UCRTaintingAnnotatedTypeFactory;
import edu.ucr.cs.riple.taint.ucrtainting.serialization.Fix;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreeUtils;

/**
 * Visitor for handling calls to third party libraries. This visitor gets to the required type by
 * making receiver and all arguments be untainted.
 */
public class ThirdPartyFixVisitor extends SpecializedFixComputer {

  private final boolean activation;

  public ThirdPartyFixVisitor(
      UCRTaintingAnnotatedTypeFactory factory, FixComputer fixComputer, Context context) {
    super(factory, fixComputer, context);
    this.activation = factory.libraryCheckIsEnabled();
  }

  public Set<Fix> visitMemberSelect(MemberSelectTree node, FoundRequired pair) {
    if (!activation) {
      return Set.of();
    }
    ExpressionTree receiver = TreeUtils.getReceiverTree(node);
    if (receiver == null) {
      return Set.of();
    }
    Element field = TreeUtils.elementFromUse(node);
    if (field == null || ElementUtils.isStatic(field)) {
      return Set.of();
    }
    AnnotatedTypeMirror receiverType = typeFactory.getAnnotatedType(receiver);
    AnnotatedTypeMirror requiredType = receiverType.deepCopy(true);
    typeFactory.makeUntainted(requiredType);
    return receiver.accept(fixComputer, new FoundRequired(receiverType, requiredType, pair.depth));
  }

  @Override
  public Set<Fix> visitMethodInvocation(MethodInvocationTree node, FoundRequired pair) {
    if (!activation) {
      return Set.of();
    }
    Element element = TreeUtils.elementFromUse(node);
    if (element == null) {
      return Set.of();
    }
    Symbol.MethodSymbol calledMethod = (Symbol.MethodSymbol) element;
    // check if the call is to a method defined in a third party library.
    if (!typeFactory.isThirdPartyMethod(calledMethod)) {
      return Set.of();
    }
    Set<Fix> fixes = new HashSet<>();
    // Add a fix for each passed argument.
    for (int i = 0; i < node.getArguments().size(); i++) {
      ExpressionTree argument = node.getArguments().get(i);
      AnnotatedTypeMirror formalParameterType =
          extractFormalParameterAnnotatedTypeMirror(calledMethod, i).deepCopy(true);
      makeUntainted(formalParameterType, typeFactory);
      AnnotatedTypeMirror actualParameterType = typeFactory.getAnnotatedType(argument);
      FoundRequired argFoundRequired = null;
      // check for varargs of called method, if the formal parameter is an array and the actual is
      // only a single element, we should match with the component type.
      if (i >= calledMethod.getParameters().size() - 1 && calledMethod.isVarArgs()) {
        if (formalParameterType instanceof AnnotatedTypeMirror.AnnotatedArrayType
            && !(actualParameterType instanceof AnnotatedTypeMirror.AnnotatedArrayType)) {
          argFoundRequired =
              FoundRequired.of(
                  actualParameterType,
                  ((AnnotatedTypeMirror.AnnotatedArrayType) formalParameterType).getComponentType(),
                  pair.depth);
        }
      }
      argFoundRequired =
          argFoundRequired == null
              ? FoundRequired.of(actualParameterType, formalParameterType, pair.depth)
              : argFoundRequired;
      fixes.addAll(argument.accept(fixComputer, argFoundRequired));
    }
    // Add the fix for the receiver if not static.
    if (calledMethod.isStatic()) {
      // No receiver for static method calls.
      return fixes;
    }
    // Build the fix for the receiver.
    ExpressionTree receiver = TreeUtils.getReceiverTree(node);
    if (receiver != null) {
      AnnotatedTypeMirror receiverType = typeFactory.getAnnotatedType(receiver);
      AnnotatedTypeMirror required = receiverType.deepCopy(true);
      typeFactory.makeUntainted(required);
      fixes.addAll(
          receiver.accept(fixComputer, new FoundRequired(receiverType, required, pair.depth)));
    }
    return fixes;
  }

  /**
   * Makes the given type untainted. If the type is an array, the component type is made untainted.
   * Do not use or change {@link UCRTaintingAnnotatedTypeFactory#makeUntainted} method.
   *
   * @param type The type to make untainted.
   * @param factory The type factory.
   */
  private void makeUntainted(AnnotatedTypeMirror type, UCRTaintingAnnotatedTypeFactory factory) {
    // for arrays, we need to pass collection of untainted data rather than tainted.
    if (type instanceof AnnotatedTypeMirror.AnnotatedArrayType) {
      ((AnnotatedTypeMirror.AnnotatedArrayType) type)
          .getComponentType()
          .replaceAnnotation(factory.rUntainted);
    } else {
      type.replaceAnnotation(factory.rUntainted);
    }
  }

  private AnnotatedTypeMirror extractFormalParameterAnnotatedTypeMirror(
      Symbol.MethodSymbol methodSymbol, int i) {
    return methodSymbol.isVarArgs() && i >= methodSymbol.getParameters().size()
        ? typeFactory.getAnnotatedType(
            methodSymbol.getParameters().get(methodSymbol.getParameters().size() - 1))
        : typeFactory.getAnnotatedType(methodSymbol.getParameters().get(i));
  }
}
