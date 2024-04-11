package com.taint.tainting;

import org.checkerframework.common.accumulation.AccumulationAnalysis;
import org.checkerframework.common.basetype.BaseTypeChecker;

public class XTaintingAnalysis extends AccumulationAnalysis {
  /**
   * Constructs an AccumulationAnalysis.
   *
   * @param checker the checker
   * @param factory the type factory
   */
  public XTaintingAnalysis(BaseTypeChecker checker, XTaintingAnnotatedTypeFactory factory) {
    super(checker, factory);
  }
}
