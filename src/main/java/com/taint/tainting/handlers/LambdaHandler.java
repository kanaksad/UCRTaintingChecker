package com.taint.tainting.handlers;

import com.sun.tools.javac.code.Symbol;
import com.taint.tainting.XTaintingAnnotatedTypeFactory;

import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

public class LambdaHandler extends AbstractHandler {

  public Set<Symbol.VarSymbol> lambdaParameters = new HashSet<>();

  public LambdaHandler(XTaintingAnnotatedTypeFactory typeFactory) {
    super(typeFactory);
  }

  @Override
  public void addAnnotationsFromDefaultForType(Element element, AnnotatedTypeMirror type) {
    if (element instanceof Symbol.VarSymbol && lambdaParameters.contains(element)) {
      typeFactory.makeUntainted(type);
    }
  }

  public void addLambdaParameter(Symbol.VarSymbol varSymbol) {
    lambdaParameters.add(varSymbol);
  }
}
