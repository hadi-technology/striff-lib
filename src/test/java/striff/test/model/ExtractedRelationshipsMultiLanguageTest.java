package striff.test.model;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.CompileResult;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.compiler.typescript.NodeRuntime;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.extractor.ComponentRelation;
import com.hadi.striff.extractor.DiagramConstants;
import com.hadi.striff.extractor.ExtractedRelationships;
import com.hadi.striff.extractor.RelationsMap;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class ExtractedRelationshipsMultiLanguageTest {

    private static final String TS_CONFIG = "{\n"
            + "  \"compilerOptions\": {\n"
            + "    \"target\": \"ES2020\",\n"
            + "    \"module\": \"NodeNext\",\n"
            + "    \"moduleResolution\": \"NodeNext\",\n"
            + "    \"strict\": true,\n"
            + "    \"skipLibCheck\": true\n"
            + "  },\n"
            + "  \"include\": [\"src/**/*.ts\"]\n"
            + "}";

    @Test
    public void javaRelationshipExtractionCoversCoreTypes() throws Exception {
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile("/src/main/java/com/sample/Parent.java",
                "package com.sample; public class Parent {}"));
        files.insertFile(new ProjectFile("/src/main/java/com/sample/Worker.java",
                "package com.sample; public interface Worker {}"));
        files.insertFile(new ProjectFile("/src/main/java/com/sample/OwnedDependency.java",
                "package com.sample; public class OwnedDependency {}"));
        files.insertFile(new ProjectFile("/src/main/java/com/sample/SharedDependency.java",
                "package com.sample; public class SharedDependency {}"));
        files.insertFile(new ProjectFile("/src/main/java/com/sample/CallDependency.java",
                "package com.sample; public class CallDependency {}"));
        files.insertFile(new ProjectFile("/src/main/java/com/sample/Child.java",
                "package com.sample; public class Child extends Parent implements Worker { "
                        + "private OwnedDependency owned; "
                        + "public SharedDependency shared; "
                        + "public Child(OwnedDependency owned, SharedDependency shared) { this.owned = owned; this.shared = shared; } "
                        + "public CallDependency run(CallDependency input) { return input; } }"));

        OOPSourceCodeModel model = compileModel(files, Lang.JAVA);
        RelationsMap relations = new ExtractedRelationships(model).result();

        assertRelationExists(relations, model, "com.sample.Child", "com.sample.Parent",
                DiagramConstants.ComponentAssociation.SPECIALIZATION);
        assertRelationExists(relations, model, "com.sample.Child", "com.sample.Worker",
                DiagramConstants.ComponentAssociation.REALIZATION);
        assertRelationExists(relations, model, "com.sample.Child", "com.sample.OwnedDependency",
                DiagramConstants.ComponentAssociation.COMPOSITION);
        assertRelationExists(relations, model, "com.sample.Child", "com.sample.SharedDependency",
                DiagramConstants.ComponentAssociation.AGGREGATION);
        assertRelationExists(relations, model, "com.sample.Child", "com.sample.CallDependency",
                DiagramConstants.ComponentAssociation.WEAK_ASSOCIATION);
    }

    @Test
    public void typeScriptRelationshipExtractionCoversCoreTypes() throws Exception {
        Assume.assumeTrue(NodeRuntime.isNodeAvailable());
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile("/src/index.ts",
                "export interface Runner {}\n"
                        + "export class Base {}\n"
                        + "export class OwnedDependency {}\n"
                        + "export class SharedDependency {}\n"
                        + "export class CallDependency {}\n"
                        + "export class Child extends Base implements Runner {\n"
                        + "  private owned: OwnedDependency;\n"
                        + "  public shared: SharedDependency;\n"
                        + "  constructor(owned: OwnedDependency, shared: SharedDependency) {\n"
                        + "    this.owned = owned;\n"
                        + "    this.shared = shared;\n"
                        + "  }\n"
                        + "  run(input: CallDependency): CallDependency {\n"
                        + "    return input;\n"
                        + "  }\n"
                        + "}"));
        writeTsConfig(files);

        OOPSourceCodeModel model = compileModel(files, Lang.TYPESCRIPT);
        RelationsMap relations = new ExtractedRelationships(model).result();

        assertRelationExists(relations, model, "src.index.Child", "src.index.Base",
                DiagramConstants.ComponentAssociation.SPECIALIZATION);
        assertRelationExists(relations, model, "src.index.Child", "src.index.Runner",
                DiagramConstants.ComponentAssociation.REALIZATION);
        assertRelationExists(relations, model, "src.index.Child", "src.index.OwnedDependency",
                DiagramConstants.ComponentAssociation.COMPOSITION);
        assertRelationExists(relations, model, "src.index.Child", "src.index.SharedDependency",
                DiagramConstants.ComponentAssociation.AGGREGATION);
        assertRelationExists(relations, model, "src.index.Child", "src.index.CallDependency",
                DiagramConstants.ComponentAssociation.WEAK_ASSOCIATION);
    }

    @Test
    public void csharpRelationshipExtractionCoversCoreTypes() throws Exception {
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile("/src/Models.cs",
                "namespace MyApp.Models {\n"
                        + "  public class Parent {}\n"
                        + "  public interface IWorker {}\n"
                        + "  public class OwnedDependency {}\n"
                        + "  public class SharedDependency {}\n"
                        + "  public class CallDependency {}\n"
                        + "  public class Child : Parent, IWorker {\n"
                        + "    private OwnedDependency _owned;\n"
                        + "    public SharedDependency Shared { get; set; }\n"
                        + "    public Child(OwnedDependency owned, SharedDependency shared) {\n"
                        + "      _owned = owned;\n"
                        + "      Shared = shared;\n"
                        + "    }\n"
                        + "    public CallDependency Run(CallDependency input) {\n"
                        + "      return input;\n"
                        + "    }\n"
                        + "  }\n"
                        + "}\n"));

        OOPSourceCodeModel model = compileModel(files, Lang.CSHARP);
        RelationsMap relations = new ExtractedRelationships(model).result();

        assertRelationExists(relations, model, "MyApp.Models.Child", "MyApp.Models.Parent",
                DiagramConstants.ComponentAssociation.SPECIALIZATION);
        assertRelationExists(relations, model, "MyApp.Models.Child", "MyApp.Models.IWorker",
                DiagramConstants.ComponentAssociation.REALIZATION);
        assertRelationExists(relations, model, "MyApp.Models.Child", "MyApp.Models.OwnedDependency",
                DiagramConstants.ComponentAssociation.COMPOSITION);
        assertRelationExists(relations, model, "MyApp.Models.Child", "MyApp.Models.SharedDependency",
                DiagramConstants.ComponentAssociation.AGGREGATION);
        assertRelationExists(relations, model, "MyApp.Models.Child", "MyApp.Models.CallDependency",
                DiagramConstants.ComponentAssociation.WEAK_ASSOCIATION);
    }

    @Test
    public void pythonRelationshipExtractionCoversCoreTypes() throws Exception {
        Assume.assumeTrue(NodeRuntime.isNodeAvailable());
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile("/src/sample.py",
                "class Parent:\n"
                        + "    pass\n\n"
                        + "class FieldDependency:\n"
                        + "    pass\n\n"
                        + "class CallDependency:\n"
                        + "    pass\n\n"
                        + "class Child(Parent):\n"
                        + "    dep: FieldDependency\n\n"
                        + "    def __init__(self, dep: FieldDependency) -> None:\n"
                        + "        self.dep = dep\n\n"
                        + "    def run(self, input: CallDependency) -> CallDependency:\n"
                        + "        return input\n"));

        OOPSourceCodeModel model = compileModel(files, Lang.PYTHON);
        RelationsMap relations = new ExtractedRelationships(model).result();

        assertRelationExists(relations, model, "src.sample.Child", "src.sample.Parent",
                DiagramConstants.ComponentAssociation.SPECIALIZATION);
        assertRelationExists(relations, model, "src.sample.Child", "src.sample.FieldDependency",
                DiagramConstants.ComponentAssociation.AGGREGATION);
        assertRelationExists(relations, model, "src.sample.Child", "src.sample.CallDependency",
                DiagramConstants.ComponentAssociation.WEAK_ASSOCIATION);
    }

    private static OOPSourceCodeModel compileModel(final ProjectFiles files, final Lang lang) throws Exception {
        CompileResult result = new ClarpseProject(files, lang).result();
        Assert.assertTrue("Compile failures: " + result.failures(), result.failures().isEmpty());
        return result.model();
    }

    private static void assertRelationExists(final RelationsMap map,
                                             final OOPSourceCodeModel model,
                                             final String sourceUniqueName,
                                             final String targetUniqueName,
                                             final DiagramConstants.ComponentAssociation expectedType) {
        Component source = model.copyOfComponent(sourceUniqueName).orElseThrow();
        Set<ComponentRelation> relations = map.rels(source);
        Assert.assertTrue("Missing relation " + sourceUniqueName + " -> " + targetUniqueName + " (" + expectedType
                        + "), got: " + relations,
                relations.stream().anyMatch(rel ->
                        rel.targetComponent().uniqueName().equals(targetUniqueName)
                                && rel.associationType() == expectedType));
    }

    private static void writeTsConfig(final ProjectFiles files) throws Exception {
        Path repoRoot = Paths.get(files.projectDir());
        Files.createDirectories(repoRoot);
        Path configPath = repoRoot.resolve("tsconfig.json");
        Files.writeString(configPath, TS_CONFIG, StandardCharsets.UTF_8);
    }
}
