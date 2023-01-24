package edu.ucr.cs.riple.taint.ucrtainting;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.framework.source.SupportedOptions;

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
        "taintedMethods.astub"
})

@SupportedOptions({
        UCRTaintingChecker.ANNOTATED_PACKAGES,
        UCRTaintingChecker.UNANNOTATED_SUB_PACKAGES,
})

public class UCRTaintingChecker extends BaseTypeChecker {

        public static final String ANNOTATED_PACKAGES = "annotatedPackages";

        public static final String UNANNOTATED_SUB_PACKAGES = "unannotatedSubPackages";

}
