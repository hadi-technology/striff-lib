package com.hadi.striff;

import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.extractor.NotLoadedRelation;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Kotlin through the whole pipeline: a one-level analysis loads what an analysed Kotlin file
 * references as boundary, a Java file's reference to a Kotlin type is a relation of the diff, and a
 * diagram holding a Kotlin file class renders.
 *
 * <p>Fixture: {@code app.Service} is analysed. In the head revision it uses {@code lib.Helper}, which
 * extends {@code deep.Base}, and calls the top-level function {@code lib.format}, which compiles into
 * the file class {@code lib.UtilsKt}.</p>
 */
public class KotlinAnalysisTest {

    private static final String SERVICE = "/app/Service.kt";
    private static final String HELPER = "/lib/Helper.kt";

    private static ProjectFiles kotlinFiles(boolean head) {
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile(SERVICE, head
                ? "package app\n\nimport lib.Helper\nimport lib.format\n\nclass Service {\n"
                        + "    fun run(h: Helper): String = format(h)\n}\n"
                : "package app\n\nclass Service {\n    fun run(): Int = 1\n}\n"));
        files.insertFile(new ProjectFile(HELPER, "package lib\n\nimport deep.Base\n\nopen class Helper : Base()\n"));
        files.insertFile(new ProjectFile("/lib/utils.kt", "package lib\n\nfun format(h: Helper): String = h.toString()\n"));
        files.insertFile(new ProjectFile("/deep/Base.kt", "package deep\n\nopen class Base\n"));
        return files;
    }

    private static Component component(OOPSourceCodeModel model, String name) {
        return model.component(name).orElseThrow(() -> new AssertionError(name + " is not in the model"));
    }

    private static boolean added(CodeDiff diff, String from, String to) {
        return diff.changeSet().addedRelations().allRels().stream()
                .anyMatch(rel -> rel.originalComponent().uniqueName().equals(from)
                        && rel.targetComponent().uniqueName().equals(to));
    }

    @Test
    public void kotlinReferenceAddedInHeadLoadsItsTargetAsBoundaryInBothRevisions() throws Exception {
        StriffConfig config = new StriffConfig()
                .setLanguages(List.of(Lang.KOTLIN))
                .setFilesFilter(List.of(SERVICE))
                .setAnalysisDepth(1);
        StriffOperation op = new StriffOperation(kotlinFiles(false), kotlinFiles(true), config);
        CodeDiff diff = op.codeDiff();

        assertTrue(component(diff.oldModel(), "lib.Helper").isBoundary());
        assertTrue(component(diff.newModel(), "lib.Helper").isBoundary());
        assertFalse(component(diff.newModel(), "app.Service").isBoundary());
        assertTrue(added(diff, "app.Service", "lib.Helper"));
        assertTrue(added(diff, "app.Service", "lib.UtilsKt"));
        assertFalse(diff.mergedModel().containsComponent("deep.Base"));
        assertTrue(diff.notLoadedRelations().toString(), diff.notLoadedRelations().stream()
                .anyMatch(rel -> rel.sourceComponent().equals("lib.Helper")
                        && rel.targetName().equals("deep.Base")
                        && rel.origin() == NotLoadedRelation.Origin.BOUNDARY));
        assertEquals(Set.of(HELPER, "/lib/utils.kt"), op.analysisScope().levelOneFiles());
    }

    @Test
    public void aJavaFilesReferenceToAKotlinTypeIsARelationOfTheDiff() throws Exception {
        ProjectFiles base = new ProjectFiles();
        base.insertFile(new ProjectFile("/src/app/Checkout.java", "package app;\npublic class Checkout { }\n"));
        base.insertFile(new ProjectFile("/src/app/Order.kt", "package app\n\ndata class Order(val id: Long)\n"));
        ProjectFiles head = new ProjectFiles();
        head.insertFile(new ProjectFile("/src/app/Checkout.java",
                "package app;\npublic class Checkout {\n    private Order order;\n}\n"));
        head.insertFile(new ProjectFile("/src/app/Order.kt", "package app\n\ndata class Order(val id: Long)\n"));

        StriffConfig config = new StriffConfig().setLanguages(List.of(Lang.JAVA, Lang.KOTLIN));
        CodeDiff diff = new StriffOperation(base, head, config).codeDiff();

        assertTrue(diff.mergedModel().containsComponent("app.Order"));
        assertTrue(diff.changeSet().addedRelations().allRels().toString(), added(diff, "app.Checkout", "app.Order"));
    }

    @Test
    public void aDiagramHoldingAKotlinFileClassRenders() throws Exception {
        StriffConfig config = new StriffConfig().setLanguages(List.of(Lang.KOTLIN));
        StriffOperation op = new StriffOperation(kotlinFiles(false), kotlinFiles(true), config);

        assertFalse(op.result().diagrams().isEmpty());
        assertTrue(op.result().diagrams().stream().anyMatch(diagram -> diagram.svg().contains("UtilsKt")));
    }
}
