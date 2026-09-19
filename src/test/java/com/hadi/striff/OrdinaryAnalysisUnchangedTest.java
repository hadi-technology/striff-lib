package com.hadi.striff;

import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.striff.diagram.StriffDiagram;
import com.hadi.striff.diagram.StriffOutput;
import com.hadi.striff.extractor.ExtractedRelationships;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * An ordinary analysis (depth 0) produces exactly what it produced before one-level analysis was
 * added: the change set, every extracted relation, and each diagram's components and PlantUML
 * source, for a filtered and an unfiltered Java and Python change.
 *
 * <p>The golden file was produced by this test on the revision before one-level analysis, with
 * {@code -Dstriff.writeGolden=true}.</p>
 */
public class OrdinaryAnalysisUnchangedTest {

    private static final Path GOLDEN = Paths.get("src/test/resources/golden/ordinary-analysis.txt");

    private static ProjectFiles files(boolean head) {
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile("/app/A.java", head
                ? "package app; import lib.B; public class A { private B b; public void go(C c) { } }"
                : "package app; public class A { public void go() { } }"));
        files.insertFile(new ProjectFile("/app/C.java", "package app; public class C { }"));
        files.insertFile(new ProjectFile("/lib/B.java",
                "package lib; public class B extends Base implements Runnable { public void run() { } }"));
        files.insertFile(new ProjectFile("/lib/Base.java", "package lib; public abstract class Base { }"));
        files.insertFile(new ProjectFile("/app/Caller.java", "package app; public class Caller { private A a; }"));
        files.insertFile(new ProjectFile("/py/service.py", head
                ? "from py.helper import Helper\n\nclass Service:\n    def run(self, h: Helper):\n        return h\n"
                : "class Service:\n    def run(self):\n        return 1\n"));
        files.insertFile(new ProjectFile("/py/helper.py", "class Helper:\n    pass\n"));
        files.insertFile(new ProjectFile("/py/__init__.py", ""));
        return files;
    }

    private static String snapshot(String label, StriffConfig config) throws Exception {
        StriffOperation op = new StriffOperation(files(false), files(true), config);
        CodeDiff diff = op.codeDiff();
        StringBuilder out = new StringBuilder("## ").append(label).append('\n');
        out.append("added=").append(sorted(diff.changeSet().addedComponents())).append('\n');
        out.append("deleted=").append(sorted(diff.changeSet().deletedComponents())).append('\n');
        out.append("modified=").append(sorted(diff.changeSet().modifiedComponents())).append('\n');
        out.append("keyRelations=").append(sorted(diff.changeSet().keyRelationsComponents())).append('\n');
        out.append("addedRelations=").append(sorted(diff.changeSet().addedRelations().allRels().stream()
                .map(Object::toString).collect(Collectors.toList()))).append('\n');
        out.append("deletedRelations=").append(sorted(diff.changeSet().deletedRelations().allRels().stream()
                .map(Object::toString).collect(Collectors.toList()))).append('\n');
        // Read before rendering: the relations the diff holds, not what drawing reads from them.
        out.append("relations=").append(sorted(new ExtractedRelationships(diff.mergedModel()).result().allRels()
                .stream().map(Object::toString).collect(Collectors.toList()))).append('\n');
        StriffOutput output = op.result();
        for (StriffDiagram diagram : output.diagrams()) {
            out.append("components=").append(sorted(diagram.cmps().stream()
                    .map(cmp -> cmp.uniqueName()).collect(Collectors.toList()))).append('\n');
            out.append("puml=\n").append(diagram.pumlSource()).append('\n');
        }
        return out.toString();
    }

    private static TreeSet<String> sorted(Collection<String> values) {
        return new TreeSet<>(values);
    }

    private static String actual() throws Exception {
        return snapshot("unfiltered", new StriffConfig().setLanguages(List.of(Lang.JAVA, Lang.PYTHON)).setMetadataOnly(false))
                + snapshot("filtered", new StriffConfig().setLanguages(List.of(Lang.JAVA, Lang.PYTHON))
                        .setFilesFilter(List.of("/app/A.java", "/py/service.py")))
                + snapshot("filtered-expanded", new StriffConfig().setLanguages(List.of(Lang.JAVA, Lang.PYTHON))
                        .setFilesFilter(List.of("/app/A.java"))
                        .setExpandedFiles(List.of("/app/Caller.java")));
    }

    @Test
    public void ordinaryAnalysisOutputIsUnchanged() throws Exception {
        String actual = actual();
        if (Boolean.getBoolean("striff.writeGolden")) {
            Files.createDirectories(GOLDEN.getParent());
            Files.writeString(GOLDEN, actual, StandardCharsets.UTF_8);
        }
        assertEquals(Files.readString(GOLDEN, StandardCharsets.UTF_8), actual);
    }
}
