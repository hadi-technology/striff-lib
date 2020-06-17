package stiff.test;

import com.hadii.stiff.text.StiffComponentDocText;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class TextTest {

    @Test
    public void StriffComponentDocTextTest() throws Exception {
        assertTrue(new StiffComponentDocText("/**\n" +
                                                    " * A test case defines the fixture to run multiple tests. To define a test case<br/>\n" +
                                                    " * <ol>\n" +
                                                    " *   <li>implement a subclass of <code>TestCase</code></li>\n" +
                                                    " *   <li>define instance variables that store the state of the fixture</li>\n" +
                                                    " *   <li>initialize the fixture state by overriding {@link #setUp()}</li>\n" +
                                                    " *   <li>clean-up after a test by overriding {@link #tearDown()}.</li>\n" +
                                                    " * </ol>\n" +
                                                    " * Each test runs in its own fixture so there\n" +
                                                    " * can be no side effects among test runs.\n" +
                                                    " * Here is an example:\n" +
                                                    " * <pre>\n" +
                                                    " * public class MathTest extends TestCase {\n" +
                                                    " *    protected double fValue1;\n" +
                                                    " *    protected double fValue2;\n" +
                                                    " *\n" +
                                                    " *    protected void setUp() {\n" +
                                                    " *       fValue1= 2.0;\n" +
                                                    " *       fValue2= 3.0;\n" +
                                                    " *    }\n" +
                                                    " * }\n" +
                                                    " * </pre>\n" +
                                                    " *\n" +
                                                    " * For each test implement a method which interacts\n" +
                                                    " * with the fixture. Verify the expected results with assertions specified\n" +
                                                    " * by calling {@link junit.framework.Assert#assertTrue(String, boolean)} with a boolean.\n" +
                                                    " * <pre>\n" +
                                                    " *    public void testAdd() {\n" +
                                                    " *       double result= fValue1 + fValue2;\n" +
                                                    " *       assertTrue(result == 5.0);\n" +
                                                    " *    }\n" +
                                                    " * </pre>\n" +
                                                    " *\n" +
                                                    " * Once the methods are defined you can run them. The framework supports\n" +
                                                    " * both a static type safe and more dynamic way to run a test.\n" +
                                                    " * In the static way you override the runTest method and define the method to\n" +
                                                    " * be invoked. A convenient way to do so is with an anonymous inner class.\n" +
                                                    " * <pre>\n" +
                                                    " * TestCase test= new MathTest(\"add\") {\n" +
                                                    " *    public void runTest() {\n" +
                                                    " *       testAdd();\n" +
                                                    " *    }\n" +
                                                    " * };\n" +
                                                    " * test.run();\n" +
                                                    " * </pre>\n" +
                                                    " * The dynamic way uses reflection to implement {@link #runTest()}. It dynamically finds\n" +
                                                    " * and invokes a method.\n" +
                                                    " * In this case the name of the test case has to correspond to the test method\n" +
                                                    " * to be run.\n" +
                                                    " * <pre>\n" +
                                                    " * TestCase test= new MathTest(\"testAdd\");\n" +
                                                    " * test.run();\n" +
                                                    " * </pre>\n" +
                                                    " *\n" +
                                                    " * The tests to be run can be collected into a TestSuite. JUnit provides\n" +
                                                    " * different <i>test runners</i> which can run a test suite and collect the results.\n" +
                                                    " * A test runner either expects a static method <code>suite</code> as the entry\n" +
                                                    " * point to get a test to run or it will extract the suite automatically.\n" +
                                                    " * <pre>\n" +
                                                    " * public static Test suite() {\n" +
                                                    " *    suite.addTest(new MathTest(\"testAdd\"));\n" +
                                                    " *    suite.addTest(new MathTest(\"testDivideByZero\"));\n" +
                                                    " *    return suite;\n" +
                                                    " * }\n" +
                                                    " * </pre>\n" +
                                                    " *\n" +
                                                    " * @see TestResult\n" +
                                                    " * @see TestSuite\n" +
                                                    " */".trim(), 80).value().equals(
                                                        "A test case defines the fixture to run multiple tests. To define a test case\n" +
                                                                "implement a subclass of TestCase define instance variables that store the state\n" +
                                                                "of the fixture initialize the fixture state by overriding {@link #setUp[]}\n" +
                                                                "clean-up after a test by overriding {@link #tearDown[]}. Each test runs in its\n" +
                                                                "own fixture so there can be no side effects among test runs. Here is an example:\n" +
                                                                "public class MathTest extends TestCase { protected double fValue1; protected\n" +
                                                                "double fValue2; protected void setUp[] { fValue1= 2.0; fValue2= 3.0; } } For\n" +
                                                                "each test implement a method which interacts with the fixture. Verify the\n" +
                                                                "expected results with assertions specified by calling {@link\n" +
                                                                "junit.framework.Assert#assertTrue[String, boolean]} with a boolean. public void\n" +
                                                                "testAdd[] { double result= fValue1 + fValue2; assertTrue[result == 5.0]; } Once\n" +
                                                                "the methods are defined you can run them. The framework supports both a static\n" +
                                                                "type safe and more dynamic way to run a test. In the static way you override the\n" +
                                                                "runTest method and define the method to be invoked. A convenient way to do so is\n" +
                                                                "with an anonymous inner class. TestCase test= new MathTest[\"add\"] { public void\n" +
                                                                "runTest[] { testAdd[]; } }; test.run[]; The dynamic way uses reflection to\n" +
                                                                "implement {@link #runTest[]}. It dynamically finds and invokes a method. In this\n" +
                                                                "case the name of the test case has to correspond to the test method to be run.\n" +
                                                                "TestCase test= new MathTest[\"testAdd\"]; test.run[]; The tests to be run can be\n" +
                                                                "collected into a TestSuite. JUnit provides different test runners which can run\n" +
                                                                "a test suite and collect the results. A test runner either expects a static\n" +
                                                                "method suite as the entry point to get a test to run or it will extract the\n" +
                                                                "suite automatically. public static Test suite[] { suite.addTest[new\n" +
                                                                "MathTest[\"testAdd\"]]; suite.addTest[new MathTest[\"testDivideByZero\"]]; return\n" +
                                                                "suite; } @see TestResult @see TestSuite"));
    }
}