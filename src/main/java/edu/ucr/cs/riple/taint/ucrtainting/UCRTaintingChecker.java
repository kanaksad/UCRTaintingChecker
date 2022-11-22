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
        "Base64.astub",
        "File.astub",
        "Files.astub",
        "FileUtils.astub",
        "JdbcTemplate.astub",
        "Path.astub",
        "String.astub",
        "TempFileHelper.astub",
        "Statement.astub",
        "Object.astub",
        "ProcessBuilder.astub"
})
public class UCRTaintingChecker extends BaseTypeChecker {}
