package striff.test.model;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.diagram.ComponentHelper;
import com.hadi.striff.diagram.DiagramComponent;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DiagramComponentTest {

    @Test
    public void diagramComponentChildList() throws Exception {
        final String code = "package main\ntype ClassA struct { \n string \n Name []byte \n Label" +
                " string }";
        final ProjectFiles rawData = new ProjectFiles();
        rawData.insertFile(new ProjectFile("/main.go", code));
        rawData.insertFile(new ProjectFile("/go.mod", "module module/module/module"));
        final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
        final OOPSourceCodeModel codeModel = parseService.result().model();
        assertTrue(new DiagramComponent(codeModel.getComponent("main.ClassA").get(),
                codeModel).children().stream().anyMatch(s -> s.equals("main.ClassA.Name")));
        assertTrue(new DiagramComponent(codeModel.getComponent("main.ClassA").get(),
                codeModel).children().stream().anyMatch(s -> s.equals("main.ClassA.Label")));
    }

    @Test
    public void fieldDiagramComponentUniqueName() throws Exception {
        final String code = "package main\ntype ClassA struct { \n Name []byte }";
        final ProjectFiles rawData = new ProjectFiles();
        rawData.insertFile(new ProjectFile("/main.go", code));
        rawData.insertFile(new ProjectFile("/go.mod", "module module/module/module"));
        final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
        final OOPSourceCodeModel codeModel = parseService.result().model();
        assertTrue(new DiagramComponent(codeModel.getComponent("main.ClassA.Name").get(), codeModel)
                .uniqueName().equals("main.ClassA.Name"));
    }

    @Test
    public void fieldDiagramComponentPkg() throws Exception {
        final String code = "package main\ntype ClassA struct { \n Name []byte }";
        final ProjectFiles rawData = new ProjectFiles();
        rawData.insertFile(new ProjectFile("/main.go", code));
        rawData.insertFile(new ProjectFile("/go.mod", "module module/module/module"));
        final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
        final OOPSourceCodeModel codeModel = parseService.result().model();
        assertTrue(ComponentHelper
                .packagePath(new DiagramComponent(codeModel.getComponent("main.ClassA.Name").get(), codeModel).pkg())
                .equals("main"));
    }

    @Test
    public void fieldDiagramComponentParentComponent() throws Exception {
        final String code = "package main\ntype ClassA struct { \n Name []byte }";
        final ProjectFiles rawData = new ProjectFiles();
        rawData.insertFile(new ProjectFile("/main.go", code));
        rawData.insertFile(new ProjectFile("/go.mod", "module module/module/module"));
        final ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
        final OOPSourceCodeModel codeModel = parseService.result().model();
        assertTrue(new DiagramComponent(codeModel.getComponent("main.ClassA.Name").get(), codeModel)
                .parentUniqueName().equals("main.ClassA"));
    }
}
