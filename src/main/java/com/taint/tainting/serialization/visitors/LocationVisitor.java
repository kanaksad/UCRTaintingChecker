package com.taint.tainting.serialization.visitors;

import com.taint.tainting.serialization.location.ClassDeclarationLocation;
import com.taint.tainting.serialization.location.FieldLocation;
import com.taint.tainting.serialization.location.LocalVariableLocation;
import com.taint.tainting.serialization.location.MethodLocation;
import com.taint.tainting.serialization.location.MethodParameterLocation;
import com.taint.tainting.serialization.location.PolyMethodLocation;
import com.taint.tainting.serialization.location.SymbolLocation;

/**
 * A visitor of types, in the style of the visitor design pattern. When a visitor is passed to a
 * location's {@link SymbolLocation#accept accept} method, the <code>visit<i>Xyz</i></code> method
 * applicable to that location is invoked.
 */
public interface LocationVisitor<R, P> {

  /**
   * Visits a location for a method.
   *
   * @param method the location for a method
   * @param p a visitor-specified parameter
   * @return a visitor-specified result
   */
  R visitMethod(MethodLocation method, P p);

  /**
   * Visits a location for a field.
   *
   * @param field the location for a field
   * @param p a visitor-specified parameter
   * @return a visitor-specified result
   */
  R visitField(FieldLocation field, P p);

  /**
   * Visits a location for a parameter.
   *
   * @param parameter the location for a parameter
   * @param p a visitor-specified parameter
   * @return a visitor-specified result
   */
  R visitParameter(MethodParameterLocation parameter, P p);

  /**
   * Visits a location for a local variable.
   *
   * @param localVariable the location for a local variable
   * @param p a visitor-specified parameter
   * @return a visitor-specified result
   */
  R visitLocalVariable(LocalVariableLocation localVariable, P p);

  /**
   * Visits a location for a polymorphic method.
   *
   * @param polyMethodLocation the location for a polymorphic method
   * @param p a visitor-specified parameter
   * @return a visitor-specified result
   */
  R visitPolyMethod(PolyMethodLocation polyMethodLocation, P p);

  /**
   * Visits a location for a class declaration.
   *
   * @param classDeclarationLocation the location for a class declaration.
   * @param p a visitor-specified parameter
   * @return a visitor-specified result
   */
  R visitClassDeclaration(ClassDeclarationLocation classDeclarationLocation, P p);
}
