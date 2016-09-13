package timber.lint;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;

import org.intellij.lang.annotations.Language;

import java.util.List;

public class WrongTimberUsageDetectorTest extends LintDetectorTest {

  private static final String NO_WARNINGS = "No warnings.";

  @Override protected Detector getDetector() {
    return new WrongTimberUsageDetector();
  }

  @Override protected List<Issue> getIssues() {
    return new TimberIssueRegistry().getIssues();
  }

  public void testEmpty() throws Exception {
    @Language("JAVA")
    String file = "" +
            "package foo;\n" + //
            "public class EmptyClass {}\n";

    assertEquals(NO_WARNINGS, lintProject(java(file)));
  }

  public void testWrongArgs() throws Exception {
    @Language("JAVA")
    String file = "" +
            "package foo;\n" + //
            "import timber.log.Timber;\n" + //
            "public class FooClass {\n" + //
              "void foo() {\n" + //
                "Timber.d(\"Yo %s\");\n" + //
              "}\n" + //
            "}\n"; //

    String expectedError = "src/foo/FooClass.java:5: Error: Wrong argument count, format string " +
            "Yo " +
            "%s requires 1 but format call supplies 0 [TimberArgCount]\n" +
            "Timber.d(\"Yo %s\");\n" +
            "~~~~~~~~~~~~~~~~~\n" +
            "1 errors, 0 warnings\n";

    assertEquals(expectedError, lintProject(java(file)));
  }

  public void testAndroidLogUsage() throws Exception {
    @Language("JAVA")
    String file = "" +
            "package foo;\n" + //
            "import android.util.Log;\n" + //
            "public class Foo {\n" + //
              "void bar() {\n" + //
                "Log.d(\"TAG\", \"Wrong usage.\");\n" + //
              "}\n" + //
            "}\n";

    assertEquals(NO_WARNINGS, lintProject(java(file)));

  }

  public void testThrowablePosition() throws Exception {
    @Language("JAVA")
    String file = "" +
            "package foo;\n" + //
            "import timber.log.Timber;\n" + //
            "public class FooClass {\n" + //
              "void foo() {\n" + //
                "java.lang.RuntimeException e = new java.lang.RuntimeException();\n" + //
                "Timber.d(\"Yo %s, %s\", \"Hello world!\", e);\n" + //
              "}\n" + //
            "}";

    String expectedError = "src/foo/FooClass.java:6: Warning: Throwable should be first argument [ThrowableNotAtBeginning]\n"
        + "Timber.d(\"Yo %s, %s\", \"Hello world!\", e);\n"
        + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
        + "0 errors, 1 warnings\n";

    assertEquals(expectedError, lintProject(java(file)));
  }

  /**
   * Passing generics to Timber would cause a lint exception.
   * https://github.com/JakeWharton/timber/issues/163
   */
  public void testEjcError() throws Exception {
    @Language("JAVA")
    String file = "" +
            "package bar;\n" +
            "import timber.log.Timber;\n" +
            "public class EjcError<E> {\n" +
              "void foo(E e) {\n" +
                "Timber.d(\"%s\", e);\n" +
              "}\n" +
            "}\n";

    assertEquals(NO_WARNINGS, lintProject(java(file)));
  }

  @Override protected boolean allowCompilationErrors() {
    return true;
  }
}
