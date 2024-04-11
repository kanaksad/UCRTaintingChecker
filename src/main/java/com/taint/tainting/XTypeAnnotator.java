package com.taint.tainting;

import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.typeannotator.DefaultForTypeAnnotator;

public class XTypeAnnotator extends DefaultForTypeAnnotator {

  XTaintingAnnotatedTypeFactory factory;

  /**
   * Creates a new TypeAnnotator.
   *
   * @param atypeFactory the type factory
   */
  protected XTypeAnnotator(AnnotatedTypeFactory atypeFactory) {
    super(atypeFactory);
    this.factory = (XTaintingAnnotatedTypeFactory) atypeFactory;
  }

  @Override
  public Void visitDeclared(AnnotatedTypeMirror.AnnotatedDeclaredType type, Void unused) {
    return super.visitDeclared(type, unused);
  }

  @Override
  public Void visitArray(AnnotatedTypeMirror.AnnotatedArrayType type, Void unused) {
    factory.makeUntainted(type);
    return super.visitArray(type, unused);
  }
}
