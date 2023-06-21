package tests;

import edu.ucr.cs.riple.taint.ucrtainting.UCRTaintingChecker;
import org.junit.runners.Parameterized.Parameters;
import tests.tools.SerializationTestHelper;

import java.io.File;
import java.util.List;

/**
 * Test runner for tests of the UCR Tainting Checker.
 *
 * <p>Tests appear as Java files in the {@code tests/ucrtainting} folder. To add a new test case,
 * create a Java file in that directory. The file contains "// ::" comments to indicate expected
 * errors and warnings; see
 * https://github.com/typetools/checker-framework/blob/master/checker/tests/README .
 */
public class EnhancedForSerializationTest extends SerializationTestHelper {
  public EnhancedForSerializationTest(List<File> testFiles) {
    super(
        testFiles,
        UCRTaintingChecker.class,
        "ucrtainting",
        "-Anomsgtext",
        "-AannotatedPackages=\"\"",
        "-AenableLibraryCheck=true",
        "-AenableValidationCheck=false",
        "-nowarn");
  }

  @Parameters
  public static String[] getTestDirs() {
    return new String[] {"enhancedforserialization"};
  }
}
