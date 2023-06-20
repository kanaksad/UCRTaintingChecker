package edu.ucr.cs.riple.taint.ucrtainting.qual;

import org.checkerframework.framework.qual.InheritedAnnotation;
import org.checkerframework.framework.qual.PostconditionAnnotation;

import java.lang.annotation.*;

/**
 * A postcondition annotation to indicate that a method ensures certain expressions to be {@link
 * RUntainted}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@PostconditionAnnotation(qualifier = RUntainted.class)
@InheritedAnnotation
public @interface EnsuresRUntainted {
    String[] value();
}
