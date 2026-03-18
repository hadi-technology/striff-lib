package striff.test.model;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.CompileResult;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.compiler.typescript.NodeRuntime;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.striff.ChangeSet;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.StriffDiagramModel;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MultiLanguageModifiedComponentDetectionTest {

    private static final String TS_CONFIG = "{\n"
            + "  \"compilerOptions\": {\n"
            + "    \"target\": \"ES2020\",\n"
            + "    \"module\": \"NodeNext\",\n"
            + "    \"moduleResolution\": \"NodeNext\",\n"
            + "    \"strict\": true,\n"
            + "    \"skipLibCheck\": true\n"
            + "  },\n"
            + "  \"include\": [\"**/*.ts\"]\n"
            + "}";

    @Test
    public void pythonBodyOnlyMethodChange_marksMethodModifiedAndDisplaysParentClass() throws Exception {
        Assume.assumeTrue(NodeRuntime.isNodeAvailable());
        ProjectFiles oldFiles = new ProjectFiles();
        ProjectFiles newFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/module.py",
                "class Foo:\n"
                        + "    def run(self, value: int) -> int:\n"
                        + "        return value + 1\n"));
        newFiles.insertFile(new ProjectFile("/module.py",
                "class Foo:\n"
                        + "    def run(self, value: int) -> int:\n"
                        + "        total = value + 2\n"
                        + "        return total\n"));

        OOPSourceCodeModel oldModel = compileModel(oldFiles, Lang.PYTHON);
        OOPSourceCodeModel newModel = compileModel(newFiles, Lang.PYTHON);
        ChangeSet changeSet = new ChangeSet(oldModel, newModel);

        Component modifiedMethod = newModel.components()
                .filter(component -> component.componentType() == OOPSourceModelConstants.ComponentType.METHOD)
                .filter(component -> component.uniqueName().contains(".run("))
                .findFirst()
                .orElseThrow();

        Assert.assertTrue(changeSet.modifiedComponents().contains(modifiedMethod.uniqueName()));

        StriffDiagramModel diagramModel = new StriffDiagramModel(new CodeDiff(oldModel, newModel), java.util.Set.of(), false);
        Assert.assertTrue(diagramModel.diagramCmps().stream()
                .map(DiagramComponent::uniqueName)
                .anyMatch(uniqueName -> uniqueName.endsWith(".Foo")));
    }

    @Test
    public void typeScriptBodyOnlyMethodChange_marksMethodModifiedAndDisplaysParentClass() throws Exception {
        Assume.assumeTrue(NodeRuntime.isNodeAvailable());
        ProjectFiles oldFiles = new ProjectFiles();
        ProjectFiles newFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/module.ts",
                "export class Foo {\n"
                        + "  run(value: number): number {\n"
                        + "    return value + 1;\n"
                        + "  }\n"
                        + "}\n"));
        newFiles.insertFile(new ProjectFile("/module.ts",
                "export class Foo {\n"
                        + "  run(value: number): number {\n"
                        + "    const total = value + 2;\n"
                        + "    return total;\n"
                        + "  }\n"
                        + "}\n"));
        writeTsConfig(oldFiles);
        writeTsConfig(newFiles);

        OOPSourceCodeModel oldModel = compileModel(oldFiles, Lang.TYPESCRIPT);
        OOPSourceCodeModel newModel = compileModel(newFiles, Lang.TYPESCRIPT);
        ChangeSet changeSet = new ChangeSet(oldModel, newModel);

        Component modifiedMethod = newModel.components()
                .filter(component -> component.componentType() == OOPSourceModelConstants.ComponentType.METHOD)
                .filter(component -> component.uniqueName().contains(".run("))
                .findFirst()
                .orElseThrow();

        Assert.assertTrue(changeSet.modifiedComponents().contains(modifiedMethod.uniqueName()));

        StriffDiagramModel diagramModel = new StriffDiagramModel(new CodeDiff(oldModel, newModel), java.util.Set.of(), false);
        Assert.assertTrue(diagramModel.diagramCmps().stream()
                .map(DiagramComponent::uniqueName)
                .anyMatch(uniqueName -> uniqueName.endsWith(".Foo")));
    }

    private static OOPSourceCodeModel compileModel(final ProjectFiles files, final Lang lang) throws Exception {
        CompileResult result = new ClarpseProject(files, lang).result();
        Assert.assertTrue("Compile failures: " + result.failures(), result.failures().isEmpty());
        return result.model();
    }

    private static void writeTsConfig(final ProjectFiles files) throws Exception {
        Path repoRoot = Paths.get(files.projectDir());
        Files.createDirectories(repoRoot);
        Files.writeString(repoRoot.resolve("tsconfig.json"), TS_CONFIG, StandardCharsets.UTF_8);
    }
}
