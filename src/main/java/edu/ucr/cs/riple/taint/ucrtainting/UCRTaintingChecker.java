package edu.ucr.cs.riple.taint.ucrtainting;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.StubFiles;

/**
 * This is the entry point for pluggable type-checking.
 */
@StubFiles({
        "Connection.astub",
        "apache.commons.io.astub",
        "apache.commons.lang.astub",
        "general.astub",
        "URL.astub",
        "File.astub",
        "Files.astub",
        "Path.astub",
        "Paths.astub",
        "System.astub",
})
public class UCRTaintingChecker extends BaseTypeChecker {}
