package edu.ucr.cs.riple.taint.ucrtainting.handlers;

import com.google.common.collect.ImmutableSet;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.util.Context;
import edu.ucr.cs.riple.taint.ucrtainting.UCRTaintingAnnotatedTypeFactory;
import javax.lang.model.element.Element;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

public class CompositHandler implements Handler {

  /** Set of handlers to be used to add annotations from default for type. */
  private final ImmutableSet<Handler> handlers;

  public CompositHandler(UCRTaintingAnnotatedTypeFactory typeFactory, Context context) {
    this.handlers =
        ImmutableSet.<Handler>builder()
            .add(
                new StaticFinalFieldHandler(typeFactory),
                new EnumHandler(typeFactory),
                new ThirdPartyHandler(typeFactory),
                new CollectionHandler(typeFactory, context),
                new AnnotationHandler(typeFactory))
            .build();
  }

  @Override
  public void addAnnotationsFromDefaultForType(Element element, AnnotatedTypeMirror type) {
    this.handlers.forEach(handler -> handler.addAnnotationsFromDefaultForType(element, type));
  }

  @Override
  public void visitVariable(VariableTree variableTree, AnnotatedTypeMirror type) {
    this.handlers.forEach(handler -> handler.visitVariable(variableTree, type));
  }

  @Override
  public void visitMethodInvocation(MethodInvocationTree tree, AnnotatedTypeMirror type) {
    this.handlers.forEach(handler -> handler.visitMethodInvocation(tree, type));
  }
}
