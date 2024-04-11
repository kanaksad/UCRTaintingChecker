package com.taint.tainting.handlers;

import com.taint.tainting.XTaintingAnnotatedTypeFactory;
import com.taint.tainting.serialization.Utility;

import javax.lang.model.element.Element;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

public class EnumHandler extends AbstractHandler {

  public EnumHandler(XTaintingAnnotatedTypeFactory typeFactory) {
    super(typeFactory);
  }

  @Override
  public void addAnnotationsFromDefaultForType(Element element, AnnotatedTypeMirror type) {
    if (Utility.isEnumConstant(element)) {
      typeFactory.makeUntainted(type);
    }
  }
}
