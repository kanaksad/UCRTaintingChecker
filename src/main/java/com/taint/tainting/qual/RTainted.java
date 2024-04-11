package com.taint.tainting.qual;

import java.lang.annotation.*;

import com.taint.tainting.XTaintingChecker;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Denotes a possibly-tainted value: at run time, the value might be tainted or might be untainted.
 *
 * @see RUntainted
 * @see XTaintingChecker
 * @checker_framework.manual #tainting-checker Tainting Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@DefaultQualifierInHierarchy
@SubtypeOf({})
public @interface RTainted {}
