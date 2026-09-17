package striff.test.model;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.CompileResult;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.compiler.typescript.NodeRuntime;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
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
import java.util.stream.Collectors;

/**
 * Relations whose target is a module-level function or field, which have no owning class and so
 * used to be dropped for want of a base component to land on.
 */
public class ModuleLevelRelationsTest {

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

    /**
     * The referrer is itself module-level, so the relation runs from one module to another.
     */
    @Test
    public void aModuleLevelFunctionReferencingAnothersDrawsAModuleToModuleRelation() throws Exception {
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile("/src/a.py", "def helper():\n    return 1\n"));
        files.insertFile(new ProjectFile("/src/b.py",
                "from src.a import helper\n\n\ndef useIt():\n    return helper()\n"));

        OOPSourceCodeModel model = compileModel(files, Lang.PYTHON);
        RelationsMap relations = new ExtractedRelationships(model).result();

        assertRelation(relations, model, "src.module:b", "src.module:a");
    }

    /**
     * The referrer is a class method, so the relation runs from the class to the target's module.
     */
    @Test
    public void aClassMethodReferencingAModuleLevelFunctionDrawsARelationToItsModule() throws Exception {
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile("/src/util.py", "def helper():\n    return 1\n"));
        files.insertFile(new ProjectFile("/src/consumer.py",
                "from src.util import helper\n\n\nclass Consumer:\n    def run(self):\n        return helper()\n"));

        OOPSourceCodeModel model = compileModel(files, Lang.PYTHON);
        RelationsMap relations = new ExtractedRelationships(model).result();

        assertRelation(relations, model, "src.consumer.Consumer", "src.module:util");
    }

    /**
     * A target that is genuinely absent from the model, as a type owned by an external library is,
     * keeps the previous behaviour: nothing is invented for it.
     */
    @Test
    public void aReferenceToATypeAbsentFromTheModelDrawsNothing() throws Exception {
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile("/src/c.py",
                "from thirdparty.lib import Missing\n\n\ndef useIt():\n    return Missing()\n"));

        OOPSourceCodeModel model = compileModel(files, Lang.PYTHON);
        RelationsMap relations = new ExtractedRelationships(model).result();

        Set<String> targets = targetsOf(relations, model, "src.module:c");
        Assert.assertTrue("A target outside the model should not be drawn, got: " + targets,
                targets.stream().noneMatch(target -> target.contains("Missing")));
    }

    /**
     * TypeScript: a module-level function relates its own module to a type it references.
     */
    @Test
    public void typeScriptModuleLevelFunctionRelatesItsModuleToAReferencedClass() throws Exception {
        Assume.assumeTrue(NodeRuntime.isNodeAvailable());
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile("/src/model.ts", "export class Order { id: number = 1; }\n"));
        files.insertFile(new ProjectFile("/src/use.ts",
                "import { Order } from \"./model\";\n"
                        + "export function build(): Order { return new Order(); }\n"));
        writeTsConfig(files);

        OOPSourceCodeModel model = compileModel(files, Lang.TYPESCRIPT);
        RelationsMap relations = new ExtractedRelationships(model).result();

        assertRelation(relations, model, "src.module:use", "src.model.Order");
    }

    /**
     * TypeScript: a call from one module-level function to another relates the two modules.
     *
     * <p>The same shape as the Python case above, and it resolves the same way: the callee is
     * recorded as a reference on the calling function, and neither end has an owning class, so each
     * resolves to the module that holds it. This is the ordinary shape of code written as functions
     * rather than classes, where a parser that recorded only what a call evaluates to would leave
     * every file in such a codebase unrelated to every other.
     */
    @Test
    public void aModuleLevelTypeScriptFunctionCallingAnotherDrawsAModuleToModuleRelation() throws Exception {
        Assume.assumeTrue(NodeRuntime.isNodeAvailable());
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile("/src/a.ts",
                "export function helper(): number { return 1; }\n"));
        files.insertFile(new ProjectFile("/src/b.ts",
                "import { helper } from \"./a\";\n"
                        + "export function useIt(): number { return helper(); }\n"));
        writeTsConfig(files);

        OOPSourceCodeModel model = compileModel(files, Lang.TYPESCRIPT);
        RelationsMap relations = new ExtractedRelationships(model).result();

        assertRelation(relations, model, "src.module:b", "src.module:a");
    }

    private static void assertRelation(final RelationsMap map, final OOPSourceCodeModel model,
                                       final String sourceUniqueName, final String targetUniqueName) {
        Set<String> targets = targetsOf(map, model, sourceUniqueName);
        Assert.assertTrue("Missing relation " + sourceUniqueName + " -> " + targetUniqueName
                        + "\n  targets of source: " + targets
                        + "\n  all relations: " + allRelations(map)
                        + "\n  model components: " + componentNames(model),
                targets.contains(targetUniqueName));
    }

    private static Set<String> targetsOf(final RelationsMap map, final OOPSourceCodeModel model,
                                         final String sourceUniqueName) {
        if (!map.hasRels(sourceUniqueName)) {
            return Set.of();
        }
        return map.significantRels(sourceUniqueName).stream()
                .map(rel -> rel.targetComponent().uniqueName())
                .collect(Collectors.toSet());
    }

    private static String allRelations(final RelationsMap map) {
        return map.relMap().entrySet().stream()
                .flatMap(entry -> entry.getValue().values().stream()
                        .flatMap(Set::stream)
                        .map(rel -> entry.getKey() + " -> " + rel.targetComponent().uniqueName()))
                .collect(Collectors.joining(", "));
    }

    private static String componentNames(final OOPSourceCodeModel model) {
        return model.components()
                .map(cmp -> cmp.uniqueName() + "[" + cmp.componentType()
                        + ", module=" + cmp.module() + ", refs="
                        + cmp.references().stream()
                                .map(ref -> ref.invokedComponent())
                                .collect(Collectors.toSet())
                        + "]")
                .collect(Collectors.joining("\n    "));
    }

    private static OOPSourceCodeModel compileModel(final ProjectFiles files, final Lang lang) throws Exception {
        CompileResult result = new ClarpseProject(files, lang).result();
        Assert.assertTrue("Compile failures: " + result.failures(), result.failures().isEmpty());
        return result.model();
    }

    private static void writeTsConfig(final ProjectFiles files) throws Exception {
        Path repoRoot = Paths.get(files.projectDir());
        Files.createDirectories(repoRoot);
        Path configPath = repoRoot.resolve("tsconfig.json");
        Files.writeString(configPath, TS_CONFIG, StandardCharsets.UTF_8);
    }
}
