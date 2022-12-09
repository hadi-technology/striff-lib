package striff.test;

import com.hadii.clarpse.compiler.ClarpseProject;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.striff.diagram.DiagramComponent;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DiagramComponentTest {

    @Test
    public void diagramComponentChildList() throws Exception {
        final String code = "package main\ntype ClassA struct { \n string \n Name []byte \n Label" +
            " string }";
        final ProjectFiles rawData = new ProjectFiles(Lang.GOLANG);
        rawData.insertFile(new ProjectFile("main.go", code));
        rawData.insertFile(new ProjectFile("go.mod", "module module/module/module"));
        final ClarpseProject parseService = new ClarpseProject(rawData.files(), rawData.lang());
        final OOPSourceCodeModel codeModel = parseService.result().model();
        assertTrue(new DiagramComponent(codeModel.getComponent("main.ClassA").get(),
                                    codeModel).children().stream().anyMatch(s -> s.equals("main.ClassA.Name.Name : []byte")));
        assertTrue(new DiagramComponent(codeModel.getComponent("main.ClassA").get(),
                                        codeModel).children().stream().anyMatch(s -> s.equals("main.ClassA.Label.Label : string")));
    }

    @Test
    public void fieldDiagramComponentUniqueName() throws Exception {
        final String code = "package main\ntype ClassA struct { \n Name []byte }";
        final ProjectFiles rawData = new ProjectFiles(Lang.GOLANG);
        rawData.insertFile(new ProjectFile("main.go", code));
        rawData.insertFile(new ProjectFile("go.mod", "module module/module/module"));
        final ClarpseProject parseService = new ClarpseProject(rawData.files(), rawData.lang());
        final OOPSourceCodeModel codeModel = parseService.result().model();
        assertTrue(new DiagramComponent(codeModel.getComponent("main.ClassA.Name").get(), codeModel)
                                            .uniqueName().equals("main.ClassA.Name.Name : []byte"));
    }

    @Test
    public void fieldDiagramComponentPkg() throws Exception {
        final String code = "package main\ntype ClassA struct { \n Name []byte }";
        final ProjectFiles rawData = new ProjectFiles(Lang.GOLANG);
        rawData.insertFile(new ProjectFile("main.go", code));
        rawData.insertFile(new ProjectFile("go.mod", "module module/module/module"));
        final ClarpseProject parseService = new ClarpseProject(rawData.files(), rawData.lang());
        final OOPSourceCodeModel codeModel = parseService.result().model();
        assertTrue(new DiagramComponent(codeModel.getComponent("main.ClassA.Name").get(), codeModel)
                       .packagePath().equals("main"));
    }

    @Test
    public void fieldDiagramComponentParentComponent() throws Exception {
        final String code = "package main\ntype ClassA struct { \n Name []byte }";
        final ProjectFiles rawData = new ProjectFiles(Lang.GOLANG);
        rawData.insertFile(new ProjectFile("main.go", code));
        rawData.insertFile(new ProjectFile("go.mod", "module module/module/module"));
        final ClarpseProject parseService = new ClarpseProject(rawData.files(), rawData.lang());
        final OOPSourceCodeModel codeModel = parseService.result().model();
        assertTrue(new DiagramComponent(codeModel.getComponent("main.ClassA.Name").get(), codeModel)
                       .parentUniqueName().equals("main.ClassA"));
    }
}
