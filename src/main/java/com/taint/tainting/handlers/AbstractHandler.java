package com.taint.tainting.handlers;

import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.VariableTree;
import com.taint.tainting.XTaintingAnnotatedTypeFactory;
import javax.lang.model.element.Element;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

public abstract class AbstractHandler implements Handler {

  protected final XTaintingAnnotatedTypeFactory typeFactory;

  public AbstractHandler(XTaintingAnnotatedTypeFactory typeFactory) {
    this.typeFactory = typeFactory;
  }

  @Override
  public void addAnnotationsFromDefaultForType(Element element, AnnotatedTypeMirror type) {
    // no-op
  }

  @Override
  public void visitVariable(VariableTree variableTree, AnnotatedTypeMirror type) {
    // no-op
  }

  @Override
  public void visitMethodInvocation(MethodInvocationTree tree, AnnotatedTypeMirror type) {
    // no-op
  }

  @Override
  public void visitMemberSelect(MemberSelectTree tree, AnnotatedTypeMirror type) {
    // no-op
  }

  @Override
  public void visitNewClass(NewClassTree tree, AnnotatedTypeMirror type) {
    // no-op
  }

  @Override
  public LambdaHandler getLambdaHandler() {
    return null;
  }
}
