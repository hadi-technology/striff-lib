package claritybot.test;

import com.clarity.binary.diagram.DiagramSourceCodeModel;
import com.clarity.binary.parse.ParsedProject;
import com.clarity.compiler.ClarpseProject;
import com.clarity.compiler.Lang;
import com.clarity.compiler.RawFile;
import com.clarity.compiler.SourceFiles;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class OOPMetricsTest {

    @Test
    public void simpleDitTest() throws Exception {
        final RawFile file = new RawFile("ClassA.java", "package test; public class ClassA { }");
        final RawFile file2 = new RawFile("ClassB.java",
                "package test; public class ClassB extends ClassA { }");
        final RawFile file3 = new RawFile("ClassC.java", "package test; public class ClassC extends ClassB { }");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);

        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCon.insertFile(file3);

        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject((reqCon)).model());
        assertTrue(codeModel.getComponent("test.ClassC").dit() == 3);
    }

    @Test
    public void interfaceDitShouldEqualOneTest() throws Exception {
        final RawFile file = new RawFile("ClassA.java", "package test; public interface ClassA { }");
        final RawFile file2 = new RawFile("ClassB.java",
                "package test; public interface ClassB extends ClassA { }");
        final RawFile file3 = new RawFile("ClassC.java", "package test; public interface ClassC extends ClassA { }");
        final RawFile file4 = new RawFile("ClassD.java", "package test; public interface ClassD extends  ClassB, ClassC { }");

        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);

        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCon.insertFile(file3);
        reqCon.insertFile(file4);

        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject((reqCon)).model());
        assertTrue(codeModel.getComponent("test.ClassD").dit() == 1);
    }


    @Test
    public void complexDitTest() throws Exception {
        final String code = "package main\ntype ClassA struct {}";
        final String codeF = "package main\ntype ClassF struct {}";
        final String codeE = "package main\ntype ClassE struct {ClassF}";
        final String codeB = "package main\ntype ClassB struct {ClassE}";
        final String codeC = "package main\ntype ClassC struct {ClassA}";
        final String codeD = "package main\ntype ClassD struct {\n*ClassB\n*ClassC\n}";
        final SourceFiles rawData = new SourceFiles(Lang.GOLANG);
        rawData.insertFile(new RawFile("/src/main/A.go", code));
        rawData.insertFile(new RawFile("/src/main/B.go", codeB));
        rawData.insertFile(new RawFile("/src/main/F.go", codeF));
        rawData.insertFile(new RawFile("/src/main/E.go", codeE));
        rawData.insertFile(new RawFile("/src/main/C.go", codeC));
        rawData.insertFile(new RawFile("/src/main/D.go", codeD));
        final ClarpseProject parseService = new ClarpseProject(rawData);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(parseService.result());
        assertTrue(codeModel.getComponent("main.ClassD").dit() == 4);
    }

    @Test
    public void simpleDitTestv2() throws Exception {
        final String code = "package main\ntype ClassA struct {}";
        final String codeE = "package main\ntype ClassE struct {}";
        final String codeB = "package main\ntype ClassB struct {ClassA}";
        final String codeC = "package main\ntype ClassC struct {ClassE}";
        final String codeD = "package main\ntype ClassD struct {\n*ClassB\n*ClassC\n}";
        final SourceFiles rawData = new SourceFiles(Lang.GOLANG);
        rawData.insertFile(new RawFile("/src/main/A.go", code));
        rawData.insertFile(new RawFile("/src/main/B.go", codeB));
        rawData.insertFile(new RawFile("/src/main/E.go", codeE));
        rawData.insertFile(new RawFile("/src/main/C.go", codeC));
        rawData.insertFile(new RawFile("/src/main/D.go", codeD));
        final ClarpseProject parseService = new ClarpseProject(rawData);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(parseService.result());
        assertTrue(codeModel.getComponent("main.ClassD").dit() == 3);
    }

    @Test
    public void simpleNocTest() throws Exception {
        final String code = "package main\ntype ClassA struct {}";
        final String codeE = "package main\ntype ClassE struct {}";
        final String codeB = "package main\ntype ClassB struct {ClassA}";
        final String codeC = "package main\ntype ClassC struct {ClassA}";
        final String codeD = "package main\ntype ClassD struct {\n*ClassA\n}";
        final SourceFiles rawData = new SourceFiles(Lang.GOLANG);
        rawData.insertFile(new RawFile("/src/main/A.go", code));
        rawData.insertFile(new RawFile("/src/main/B.go", codeB));
        rawData.insertFile(new RawFile("/src/main/E.go", codeE));
        rawData.insertFile(new RawFile("/src/main/C.go", codeC));
        rawData.insertFile(new RawFile("/src/main/D.go", codeD));
        final ClarpseProject parseService = new ClarpseProject(rawData);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(parseService.result());
        assertTrue(codeModel.getComponent("main.ClassA").noc() == 3);
    }

    @Test
    public void simpleNocInterfaceTest() throws Exception {
        final String code = "public interface interfaceA { void test(); }";
        final String codeB = "public interface interfaceB extends interfaceA {}";
        final SourceFiles rawData = new SourceFiles(Lang.JAVA);
        rawData.insertFile(new RawFile("/src/main/A.go", code));
        rawData.insertFile(new RawFile("/src/main/B.go", codeB));
        final ClarpseProject parseService = new ClarpseProject(rawData);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(parseService.result());
        assertTrue(codeModel.getComponent("interfaceA").noc() == 1);
    }

    @Test
    public void simpleStructDitShouldBeOne() throws Exception {
        final String code = "package main\ntype ClassA struct {}";
        final SourceFiles rawData = new SourceFiles(Lang.GOLANG);
        rawData.insertFile(new RawFile("/src/main/A.go", code));
        final ClarpseProject parseService = new ClarpseProject(rawData);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(parseService.result());
        assertTrue(codeModel.getComponent("main.ClassA").dit() == 1);
    }

    @Test
    public void simpleStructNocShouldBeZero() throws Exception {
        final String code = "package main\ntype ClassA struct {}";
        final SourceFiles rawData = new SourceFiles(Lang.GOLANG);
        rawData.insertFile(new RawFile("/src/main/A.go", code));
        final ClarpseProject parseService = new ClarpseProject(rawData);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(parseService.result());
        assertTrue(codeModel.getComponent("main.ClassA").noc() == 0);
    }
}