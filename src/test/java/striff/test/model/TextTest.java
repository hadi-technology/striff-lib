package striff.test.model;

import com.hadi.striff.text.DefaultText;
import com.hadi.striff.text.LineBreakedText;
import com.hadi.striff.text.StriffComponentDocText;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class TextTest {


    @Test
    public void lineBreakedTextTest() {
        assert(new LineBreakedText(new DefaultText("a test string that can be broken up."), 10))
        .value().equals("a test\n" +
                            "string\n" +
                            "that can\n" +
                            "be broken\n" +
                            "up.");
    }

    @Test
    public void striffComponentDocTextTest() throws Exception {
        String value = new StriffComponentDocText("/**\n" +
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
                                                    " */".trim(), 80).value();
        assertTrue(value.contains("<back:#E6E6E6>TestCase</back>"));
        assertTrue(value.contains("{@link #setUp[]}"));
        assertTrue(value.contains("public class MathTest extends"));
        assertTrue(value.contains("@see TestResult @see TestSuite"));
        assertFalse(value.contains("<code>"));
        assertFalse(value.contains("<pre>"));
    }

    @Test
    public void striffComponentDocTextHighlightsInlineCode() {
        assertTrue(new StriffComponentDocText("/** Uses `foo()` and `bar` here. */", 80).value().equals(
                "**Uses <back:#E6E6E6>foo[]</back> and <back:#E6E6E6>bar</back> here.**"));
    }

    @Test
    public void striffComponentDocTextHighlightsJavadocCodeTag() {
        assertTrue(new StriffComponentDocText("/** Uses {@code foo()} here. */", 80).value().equals(
                "**Uses <back:#E6E6E6>foo[]</back> here.**"));
    }

    @Test
    public void striffComponentDocTextHighlightsHtmlCodeTags() {
        assertTrue(new StriffComponentDocText("/** Uses <code>foo()</code> and <c>bar</c> here. */", 80).value().equals(
                "**Uses <back:#E6E6E6>foo[]</back> and <back:#E6E6E6>bar</back> here.**"));
    }

    @Test
    public void striffComponentDocTextHighlightsRestCodeForms() {
        assertTrue(new StriffComponentDocText("/** Uses ``foo()`` and :code:`bar` here. */", 80).value().equals(
                "**Uses <back:#E6E6E6>foo[]</back> and <back:#E6E6E6>bar</back> here.**"));
    }

    @Test
    public void striffComponentDocTextHighlightsDoxygenInlineCodeCommands() {
        assertTrue(new StriffComponentDocText("/** Use \\c foo and \\p bar here. */", 80).value().equals(
                "**Use <back:#E6E6E6>foo</back> and <back:#E6E6E6>bar</back> here.**"));
    }
}
