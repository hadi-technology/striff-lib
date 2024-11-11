package striff.test;

import com.hadii.clarpse.compiler.ClarpseProject;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants.ComponentType;
import com.hadii.striff.metrics.OOPMetricsProfile;

import static org.junit.Assert.assertThrows;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class OOPMetricsTest {

        @Test
        public void simpleDitTest() throws Exception {
                final ProjectFile file = new ProjectFile("/ClassA.java", "package test; public class " +
                                "ClassA { }");
                final ProjectFile file2 = new ProjectFile("/ClassB.java",
                                "package test; public class ClassB extends " +
                                                "ClassA { }");
                final ProjectFile file3 = new ProjectFile("/ClassC.java", "package test; public class " +
                                "ClassC extends ClassB { }");
                final ProjectFiles reqCon = new ProjectFiles();

                reqCon.insertFile(file);
                reqCon.insertFile(file2);
                reqCon.insertFile(file3);

                final OOPSourceCodeModel codeModel = new ClarpseProject(reqCon, Lang.JAVA).result().model();
                assertEquals(3.0, new OOPMetricsProfile(
                                codeModel).dit(codeModel.getComponent("test.ClassC").get()));
        }

        @Test
        public void interfaceDitShouldEqualOneTest() throws Exception {
                final ProjectFile file = new ProjectFile("/ClassA.java", "package test; public interface " +
                                "ClassA { }");
                final ProjectFile file2 = new ProjectFile("/ClassB.java",
                                "package test; public interface ClassB extends " +
                                                "ClassA { }");
                final ProjectFile file3 = new ProjectFile("/ClassC.java", "package test; public interface" +
                                " " +
                                "ClassC extends ClassA { }");
                final ProjectFile file4 = new ProjectFile("/ClassD.java", "package test; public interface" +
                                " " +
                                "ClassD extends  ClassB, ClassC { }");

                final ProjectFiles reqCon = new ProjectFiles();

                reqCon.insertFile(file);
                reqCon.insertFile(file2);
                reqCon.insertFile(file3);
                reqCon.insertFile(file4);

                final OOPSourceCodeModel codeModel = new ClarpseProject(reqCon, Lang.JAVA).result().model();
                assertEquals(1.0, new OOPMetricsProfile(
                                codeModel).dit(codeModel.getComponent("test.ClassD").get()));
        }

        @Test
        public void complexDitTest() throws Exception {
                final String code = "package main\ntype ClassA struct {}";
                final String codeF = "package main\ntype ClassF struct {}";
                final String codeE = "package main\ntype ClassE struct {ClassF}";
                final String codeB = "package main\ntype ClassB struct {ClassE}";
                final String codeC = "package main\ntype ClassC struct {ClassA}";
                final String codeD = "package main\ntype ClassD struct {\n*ClassB\n*ClassC\n}";
                final ProjectFiles rawData = new ProjectFiles();
                rawData.insertFile(new ProjectFile("/src/main/A.go", code));
                rawData.insertFile(new ProjectFile("/src/main/B.go", codeB));
                rawData.insertFile(new ProjectFile("/src/main/F.go", codeF));
                rawData.insertFile(new ProjectFile("/src/main/E.go", codeE));
                rawData.insertFile(new ProjectFile("/src/main/C.go", codeC));
                rawData.insertFile(new ProjectFile("/src/main/D.go", codeD));
                rawData.insertFile(new ProjectFile("/src/go.mod", "module module/module/module"));
                final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                final OOPSourceCodeModel codeModel = parseService.result().model();
                assertEquals(4.0, new OOPMetricsProfile(codeModel).dit(
                                codeModel.getComponent("main.ClassD").get()));
        }

        @Test
        public void simpleDitTestv2() throws Exception {
                final String code = "package main\ntype ClassA struct {}";
                final String codeE = "package main\ntype ClassE struct {}";
                final String codeB = "package main\ntype ClassB struct {ClassA}";
                final String codeC = "package main\ntype ClassC struct {ClassE}";
                final String codeD = "package main\ntype ClassD struct {\n*ClassB\n*ClassC\n}";
                final ProjectFiles rawData = new ProjectFiles();
                rawData.insertFile(new ProjectFile("/src/main/A.go", code));
                rawData.insertFile(new ProjectFile("/src/main/B.go", codeB));
                rawData.insertFile(new ProjectFile("/src/main/E.go", codeE));
                rawData.insertFile(new ProjectFile("/src/main/C.go", codeC));
                rawData.insertFile(new ProjectFile("/src/main/D.go", codeD));
                rawData.insertFile(new ProjectFile("/src/go.mod", "module module/module/module"));
                final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                final OOPSourceCodeModel codeModel = parseService.result().model();
                assertEquals(3.0, new OOPMetricsProfile(
                                codeModel).dit(codeModel.getComponent("main.ClassD").get()));
        }

        @Test
        public void simpleNocTest() throws Exception {
                final String code = "package main\ntype ClassA struct {}";
                final String codeE = "package main\ntype ClassE struct {}";
                final String codeB = "package main\ntype ClassB struct {ClassA}";
                final String codeC = "package main\ntype ClassC struct {ClassA}";
                final String codeD = "package main\ntype ClassD struct {\n*ClassA\n}";
                final ProjectFiles rawData = new ProjectFiles();
                rawData.insertFile(new ProjectFile("/src/main/A.go", code));
                rawData.insertFile(new ProjectFile("/src/main/B.go", codeB));
                rawData.insertFile(new ProjectFile("/src/main/E.go", codeE));
                rawData.insertFile(new ProjectFile("/src/main/C.go", codeC));
                rawData.insertFile(new ProjectFile("/src/main/D.go", codeD));
                rawData.insertFile(new ProjectFile("/src/go.mod", "module module/module/module"));
                final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                final OOPSourceCodeModel codeModel = parseService.result().model();
                assertEquals(3.0, new OOPMetricsProfile(
                                codeModel).noc(codeModel.getComponent("main.ClassA").get()));
        }

        @Test
        public void nocInterfaceTest() throws Exception {
                final String code = "package main\ntype plain interface\n{ }";
                final ProjectFiles rawData = new ProjectFiles();
                rawData.insertFile(new ProjectFile("/src/main/A.go", code));
                rawData.insertFile(new ProjectFile("/src/go.mod", "module module/module/module"));
                final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                final OOPSourceCodeModel codeModel = parseService.result().model();
                assertEquals(0.0, new OOPMetricsProfile(
                                codeModel).noc(codeModel.getComponent("main.plain").get()));
        }

        @Test
        public void structDitShouldBeOne() throws Exception {
                final String code = "package main\ntype ClassA struct {}";
                final ProjectFiles rawData = new ProjectFiles();
                rawData.insertFile(new ProjectFile("/src/main/A.go", code));
                rawData.insertFile(new ProjectFile("/src/go.mod", "module module/module/module"));
                final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                final OOPSourceCodeModel codeModel = parseService.result().model();
                assertEquals(1.0, new OOPMetricsProfile(codeModel).dit(
                                codeModel.getComponent("main.ClassA").get()));
        }

        @Test
        public void structNocShouldBeZero() throws Exception {
                final String code = "package main\ntype ClassA struct {}";
                final ProjectFiles rawData = new ProjectFiles();
                rawData.insertFile(new ProjectFile("/src/main/A.go", code));
                rawData.insertFile(new ProjectFile("/src/go.mod", "module module/module/module"));
                final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                final OOPSourceCodeModel codeModel = parseService.result().model();
                assertEquals(0.0, new OOPMetricsProfile(codeModel).noc(
                                codeModel.getComponent("main.ClassA").get()));
        }

        @Test
        public void afferentCouplingMetricTest() throws Exception {
                final String code = "package main\nimport \"test/math\"\ntype person struct {mathObj math" +
                                ".Person}";
                final ProjectFiles rawData = new ProjectFiles();
                rawData.insertFile(new ProjectFile("/person.go", code));
                rawData.insertFile(new ProjectFile("/go.mod", "module module/module/module"));
                final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                final OOPSourceCodeModel codeModel = parseService.result().model();
                assertEquals(1.0, new OOPMetricsProfile(codeModel).efferentCoupling(
                                codeModel.getComponent("main.person").get()));
        }

        @Test
        public void efferentCouplingMetricTest() throws Exception {
                final String codeA = "class Test { void test() { Cake.method(); } }";
                final String codeB = "class Tester { private Cake cake; }";
                final String codeD = "import java.util.List;  public class Cake { public " +
                                "static List<String> method() { return null; } }";
                final ProjectFiles rawData = new ProjectFiles();
                rawData.insertFile(new ProjectFile("/Test.java", codeA));
                rawData.insertFile(new ProjectFile("/Tester.java", codeB));
                rawData.insertFile(new ProjectFile("/Cake.java", codeD));
                final ClarpseProject parseService = new ClarpseProject(rawData, Lang.JAVA);
                OOPSourceCodeModel generatedSourceModel = parseService.result().model();
                assertEquals(2.0, new OOPMetricsProfile(generatedSourceModel).efferentCoupling(
                                generatedSourceModel.getComponent("Cake").get()));
        }

        @Test
        public void encapsulationMetricTest() throws Exception {
                final String codeA = "package main\n import \"test/math\"\ntype person struct " +
                                "{privField math.Person \n PubField math.Person} \n func (p person)" +
                                " PublicFunc() int {} \n func (p person) privateFunc() int {}";
                final ProjectFiles rawData = new ProjectFiles();
                rawData.insertFile(new ProjectFile("/person.go", codeA));
                rawData.insertFile(new ProjectFile("/go.mod", "module module/module/module"));
                final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                OOPSourceCodeModel generatedSourceModel = parseService.result().model();
                assertEquals(0.5, new OOPMetricsProfile(generatedSourceModel).encapsulation(
                                generatedSourceModel.getComponent("main.person").get()));
        }

        @Test
        public void wmcMetricTest() throws Exception {
                final String codeA = "package main\n" + //
                                "\n" + //
                                "import (\n" + //
                                "\t\"fmt\"\n" + //
                                ")\n" + //
                                "\n" + //
                                "// Sample struct to calculate WMC\n" + //
                                "type SampleStruct struct{}\n" + //
                                "\n" + //
                                "// Method1 with simple logic (cyclomatic complexity = 1)\n" + //
                                "func (s SampleStruct) Method1() {\n" + //
                                "\tfmt.Println(\"Executing Method1\")\n" + //
                                "}\n" + //
                                "\n" + //
                                "// Method2 with a loop and a condition (cyclomatic complexity = 3)\n" + //
                                "func (s SampleStruct) Method2(n int) {\n" + //
                                "\tfor i := 0; i < n; i++ {\n" + //
                                "\t\tif i%2 == 0 {\n" + //
                                "\t\t\tfmt.Println(\"Even number:\", i)\n" + //
                                "\t\t} else {\n" + //
                                "\t\t\tfmt.Println(\"Odd number:\", i)\n" + //
                                "\t\t}\n" + //
                                "\t}\n" + //
                                "}\n" + //
                                "\n" + //
                                "// Method3 with two nested conditions (cyclomatic complexity = 4)\n" + //
                                "func (s SampleStruct) Method3(n int) {\n" + //
                                "\tif n > 0 {\n" + //
                                "\t\tif n%2 == 0 {\n" + //
                                "\t\t\tfmt.Println(n, \"is positive and even\")\n" + //
                                "\t\t} else {\n" + //
                                "\t\t\tfmt.Println(n, \"is positive and odd\")\n" + //
                                "\t\t}\n" + //
                                "\t} else {\n" + //
                                "\t\tfmt.Println(n, \"is non-positive\")\n" + //
                                "\t}\n" + //
                                "}";
                final ProjectFiles rawData = new ProjectFiles();
                rawData.insertFile(new ProjectFile("/sample.go", codeA));
                rawData.insertFile(new ProjectFile("/go.mod", "module module/module/module"));
                final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                OOPSourceCodeModel generatedSourceModel = parseService.result().model();
                assertEquals(7.0, new OOPMetricsProfile(generatedSourceModel).weightedMethodComplexity(
                                generatedSourceModel.getComponent("main.SampleStruct").get()), 0.0);
        }

        @Test
        public void wmcMetricExceptionTest() throws Exception {
                assertThrows(IllegalArgumentException.class,
                                () -> {
                                        Component fakeCmp = new Component();
                                        fakeCmp.setComponentName("nonexistent");
                                        fakeCmp.setComponentType(ComponentType.CLASS);
                                        final String code = "package main\ntype ClassA struct {}";
                                        final ProjectFiles rawData = new ProjectFiles();
                                        rawData.insertFile(new ProjectFile("/sample.go", code));
                                        rawData.insertFile(new ProjectFile("/go.mod", "module module/module/module"));
                                        final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                                        OOPSourceCodeModel generatedSourceModel = parseService.result().model();
                                        new OOPMetricsProfile(generatedSourceModel).weightedMethodComplexity(fakeCmp);
                                });
        }

        @Test
        public void encapsulationMetricExceptionTest() throws Exception {
                assertThrows(IllegalArgumentException.class,
                                () -> {
                                        Component fakeCmp = new Component();
                                        fakeCmp.setComponentName("nonexistent");
                                        fakeCmp.setComponentType(ComponentType.CLASS);
                                        final String code = "package main\ntype ClassA struct {}";
                                        final ProjectFiles rawData = new ProjectFiles();
                                        rawData.insertFile(new ProjectFile("/sample.go", code));
                                        rawData.insertFile(new ProjectFile("/go.mod", "module module/module/module"));
                                        final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                                        OOPSourceCodeModel generatedSourceModel = parseService.result().model();
                                        new OOPMetricsProfile(generatedSourceModel).encapsulation(fakeCmp);
                                });
        }

        @Test
        public void efferentCouplingMetricExceptionTest() throws Exception {
                assertThrows(IllegalArgumentException.class,
                                () -> {
                                        Component fakeCmp = new Component();
                                        fakeCmp.setComponentName("nonexistent");
                                        fakeCmp.setComponentType(ComponentType.CLASS);
                                        final String code = "package main\ntype ClassA struct {}";
                                        final ProjectFiles rawData = new ProjectFiles();
                                        rawData.insertFile(new ProjectFile("/sample.go", code));
                                        rawData.insertFile(new ProjectFile("/go.mod", "module module/module/module"));
                                        final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                                        OOPSourceCodeModel generatedSourceModel = parseService.result().model();
                                        new OOPMetricsProfile(generatedSourceModel).efferentCoupling(fakeCmp);
                                });
        }

        @Test
        public void afferentCouplingMetricExceptionTest() throws Exception {
                assertThrows(IllegalArgumentException.class,
                                () -> {
                                        Component fakeCmp = new Component();
                                        fakeCmp.setComponentName("nonexistent");
                                        fakeCmp.setComponentType(ComponentType.CLASS);
                                        final String code = "package main\ntype ClassA struct {}";
                                        final ProjectFiles rawData = new ProjectFiles();
                                        rawData.insertFile(new ProjectFile("/sample.go", code));
                                        rawData.insertFile(new ProjectFile("/go.mod", "module module/module/module"));
                                        final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                                        OOPSourceCodeModel generatedSourceModel = parseService.result().model();
                                        new OOPMetricsProfile(generatedSourceModel).afferentCoupling(fakeCmp);
                                });
        }

}
