package tests;

import edu.ucr.cs.riple.taint.ucrtainting.UCRTaintingChecker;
import java.io.File;
import java.util.List;
import org.junit.runners.Parameterized;
import tests.tools.SerializationTestHelper;

/**
 * Test runner for tests of the UCR Tainting Checker.
 *
 * <p>Tests appear as Java files in the {@code tests/ucrtainting} folder. To add a new test case,
 * create a Java file in that directory. The file contains "// ::" comments to indicate expected
 * errors and warnings; see
 * https://github.com/typetools/checker-framework/blob/master/checker/tests/README .
 */
public class ParameterExpressionSerializationTest extends SerializationTestHelper {
  public ParameterExpressionSerializationTest(List<File> testFiles) {
    super(
        testFiles,
        UCRTaintingChecker.class,
        "ucrtainting",
        "-Anomsgtext",
        "-AannotatedPackages=\"\"",
        "-AenableLibraryCheck",
        "-nowarn");
  }

  @Parameterized.Parameters
  public static String[] getTestDirs() {
    return new String[] {"parameterexpressionserialization"};
  }
}
