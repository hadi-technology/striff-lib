package striff.test;

import com.hadii.clarpse.compiler.ClarpseProject;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.striff.metrics.OOPMetricsProfile;
import com.hadii.striff.parse.ParsedProject;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class OOPMetricsTest {

    @Test
    public void simpleDitTest() throws Exception {
        final ProjectFile file = new ProjectFile("ClassA.java", "package test; public class " +
                "ClassA { }");
        final ProjectFile file2 = new ProjectFile("ClassB.java",
                                                  "package test; public class ClassB extends " +
                                                          "ClassA { }");
        final ProjectFile file3 = new ProjectFile("ClassC.java", "package test; public class " +
                "ClassC extends ClassB { }");
        final ProjectFiles reqCon = new ProjectFiles(Lang.JAVA);

        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCon.insertFile(file3);

        final OOPSourceCodeModel codeModel = new ParsedProject((reqCon)).model();
        assertTrue(new OOPMetricsProfile(codeModel.getComponent("test.ClassC").get(), codeModel).dit() == 3);
    }

    @Test
    public void interfaceDitShouldEqualOneTest() throws Exception {
        final ProjectFile file = new ProjectFile("ClassA.java", "package test; public interface " +
                "ClassA { }");
        final ProjectFile file2 = new ProjectFile("ClassB.java",
                                                  "package test; public interface ClassB extends " +
                                                          "ClassA { }");
        final ProjectFile file3 = new ProjectFile("ClassC.java", "package test; public interface " +
                "ClassC extends ClassA { }");
        final ProjectFile file4 = new ProjectFile("ClassD.java", "package test; public interface " +
                "ClassD extends  ClassB, ClassC { }");

        final ProjectFiles reqCon = new ProjectFiles(Lang.JAVA);

        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCon.insertFile(file3);
        reqCon.insertFile(file4);

        final OOPSourceCodeModel codeModel = new ParsedProject((reqCon)).model();
        assertTrue(new OOPMetricsProfile(codeModel.getComponent("test.ClassD").get(), codeModel).dit() == 1);
    }

    @Test
    public void complexDitTest() throws Exception {
        final String code = "package main\ntype ClassA struct {}";
        final String codeF = "package main\ntype ClassF struct {}";
        final String codeE = "package main\ntype ClassE struct {ClassF}";
        final String codeB = "package main\ntype ClassB struct {ClassE}";
        final String codeC = "package main\ntype ClassC struct {ClassA}";
        final String codeD = "package main\ntype ClassD struct {\n*ClassB\n*ClassC\n}";
        final ProjectFiles rawData = new ProjectFiles(Lang.GOLANG);
        rawData.insertFile(new ProjectFile("/src/main/A.go", code));
        rawData.insertFile(new ProjectFile("/src/main/B.go", codeB));
        rawData.insertFile(new ProjectFile("/src/main/F.go", codeF));
        rawData.insertFile(new ProjectFile("/src/main/E.go", codeE));
        rawData.insertFile(new ProjectFile("/src/main/C.go", codeC));
        rawData.insertFile(new ProjectFile("/src/main/D.go", codeD));
        final ClarpseProject parseService = new ClarpseProject(rawData);
        final OOPSourceCodeModel codeModel = parseService.result();

        assertTrue(new OOPMetricsProfile(codeModel.getComponent("main.ClassD").get(), codeModel).dit() == 4);
    }

    @Test
    public void simpleDitTestv2() throws Exception {
        final String code = "package main\ntype ClassA struct {}";
        final String codeE = "package main\ntype ClassE struct {}";
        final String codeB = "package main\ntype ClassB struct {ClassA}";
        final String codeC = "package main\ntype ClassC struct {ClassE}";
        final String codeD = "package main\ntype ClassD struct {\n*ClassB\n*ClassC\n}";
        final ProjectFiles rawData = new ProjectFiles(Lang.GOLANG);
        rawData.insertFile(new ProjectFile("/src/main/A.go", code));
        rawData.insertFile(new ProjectFile("/src/main/B.go", codeB));
        rawData.insertFile(new ProjectFile("/src/main/E.go", codeE));
        rawData.insertFile(new ProjectFile("/src/main/C.go", codeC));
        rawData.insertFile(new ProjectFile("/src/main/D.go", codeD));
        final ClarpseProject parseService = new ClarpseProject(rawData);
        final OOPSourceCodeModel codeModel = parseService.result();
        assertTrue(new OOPMetricsProfile(codeModel.getComponent("main.ClassD").get(), codeModel).dit() == 3);
    }

    @Test
    public void simpleNocTest() throws Exception {
        final String code = "package main\ntype ClassA struct {}";
        final String codeE = "package main\ntype ClassE struct {}";
        final String codeB = "package main\ntype ClassB struct {ClassA}";
        final String codeC = "package main\ntype ClassC struct {ClassA}";
        final String codeD = "package main\ntype ClassD struct {\n*ClassA\n}";
        final ProjectFiles rawData = new ProjectFiles(Lang.GOLANG);
        rawData.insertFile(new ProjectFile("/src/main/A.go", code));
        rawData.insertFile(new ProjectFile("/src/main/B.go", codeB));
        rawData.insertFile(new ProjectFile("/src/main/E.go", codeE));
        rawData.insertFile(new ProjectFile("/src/main/C.go", codeC));
        rawData.insertFile(new ProjectFile("/src/main/D.go", codeD));
        final ClarpseProject parseService = new ClarpseProject(rawData);
        final OOPSourceCodeModel codeModel = parseService.result();
        assertTrue(new OOPMetricsProfile(
                codeModel.getComponent("main.ClassA").get(),
                codeModel).noc() == 3);
    }

    @Test
    public void simpleNocInterfaceTest() throws Exception {
        final String code = "public interface interfaceA { void test(); }";
        final String codeB = "public interface interfaceB extends interfaceA {}";
        final ProjectFiles rawData = new ProjectFiles(Lang.JAVA);
        rawData.insertFile(new ProjectFile("/src/main/A.go", code));
        rawData.insertFile(new ProjectFile("/src/main/B.go", codeB));
        final ClarpseProject parseService = new ClarpseProject(rawData);
        final OOPSourceCodeModel codeModel = parseService.result();
        assertTrue(new OOPMetricsProfile(
                codeModel.getComponent("interfaceA").get(), codeModel).noc() == 0);
    }

    @Test
    public void simpleStructDitShouldBeOne() throws Exception {
        final String code = "package main\ntype ClassA struct {}";
        final ProjectFiles rawData = new ProjectFiles(Lang.GOLANG);
        rawData.insertFile(new ProjectFile("/src/main/A.go", code));
        final ClarpseProject parseService = new ClarpseProject(rawData);
        final OOPSourceCodeModel codeModel = parseService.result();
        assertTrue(new OOPMetricsProfile(codeModel.getComponent("main.ClassA").get(), codeModel).dit() == 1);
    }

    @Test
    public void simpleStructNocShouldBeZero() throws Exception {
        final String code = "package main\ntype ClassA struct {}";
        final ProjectFiles rawData = new ProjectFiles(Lang.GOLANG);
        rawData.insertFile(new ProjectFile("/src/main/A.go", code));
        final ClarpseProject parseService = new ClarpseProject(rawData);
        final OOPSourceCodeModel codeModel = parseService.result();
        assertTrue(new OOPMetricsProfile(codeModel.getComponent("main.ClassA").get(), codeModel).noc() == 0);
    }


    @Test
    public void simpleFanOutTest() throws Exception {
        final String code = "package main\nimport \"test/math\"\ntype person struct {mathObj math" +
                ".Person}";
        final ProjectFiles rawData = new ProjectFiles(Lang.GOLANG);
        rawData.insertFile(new ProjectFile("person.go", code));
        final ClarpseProject parseService = new ClarpseProject(rawData);
        final OOPSourceCodeModel codeModel = parseService.result();
        assertTrue(new OOPMetricsProfile(codeModel.getComponent("main.person").get(), codeModel).fanout() == 1);
    }
}
