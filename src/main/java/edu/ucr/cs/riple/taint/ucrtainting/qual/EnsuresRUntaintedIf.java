package edu.ucr.cs.riple.taint.ucrtainting.qual;

import org.checkerframework.framework.qual.ConditionalPostconditionAnnotation;
import org.checkerframework.framework.qual.InheritedAnnotation;

import java.lang.annotation.*;

/**
 * Denotes a reference that is untainted, i.e. can be trusted.
 *
 * @checker_framework.manual #tainting-checker Tainting Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.FIELD})
@ConditionalPostconditionAnnotation(qualifier = RUntainted.class)
@InheritedAnnotation
@Repeatable(EnsuresRUntaintedIf.List.class)
public @interface EnsuresRUntaintedIf {
    /**
     * Returns Java expression(s) that are untainted after the method returns the given result.
     *
     * @return Java expression(s) that are untainted after the method returns the given result
     * @checker_framework.manual #java-expressions-as-arguments Syntax of Java expressions
     */
    String[] expression();

    /**
     * Returns the return value of the method under which the postcondition holds.
     *
     * @return the return value of the method under which the postcondition holds
     */
    boolean result();

    /**
     * * A wrapper annotation that makes the {@link EnsuresRUntaintedIf} annotation repeatable.
     *
     * <p>Programmers generally do not need to write this. It is created by Java when a programmer
     * writes more than one {@link EnsuresRUntaintedIf} annotation at the same location.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.FIELD})
    @ConditionalPostconditionAnnotation(qualifier = RUntainted.class)
    @InheritedAnnotation
    public static @interface List {
        /**
         * Returns the repeatable annotations.
         *
         * @return the repeatable annotations
         */
        EnsuresRUntaintedIf[] value();
    }
}
