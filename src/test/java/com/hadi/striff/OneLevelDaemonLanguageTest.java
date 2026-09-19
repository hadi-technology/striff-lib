package com.hadi.striff;

import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.extractor.NotLoadedRelation;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * One-level analysis of a language whose resolver reads the sources from disk, and the cleanup of
 * what that writes.
 *
 * <p>Fixture: {@code app.service} is analysed. In the head revision it uses {@code lib.helper.Helper},
 * which extends {@code deep.base.Base}.</p>
 */
public class OneLevelDaemonLanguageTest {

    private static final String SERVICE = "/app/service.py";
    private static final String HELPER = "/lib/helper.py";

    private static ProjectFiles files(boolean head) {
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile(SERVICE, head
                ? "from lib.helper import Helper\n\nclass Service:\n    def run(self, h: Helper):\n        return h\n"
                : "class Service:\n    def run(self):\n        return 1\n"));
        files.insertFile(new ProjectFile(HELPER, "from deep.base import Base\n\nclass Helper(Base):\n    pass\n"));
        files.insertFile(new ProjectFile("/deep/base.py", "class Base:\n    pass\n"));
        files.insertFile(new ProjectFile("/app/__init__.py", ""));
        files.insertFile(new ProjectFile("/lib/__init__.py", ""));
        files.insertFile(new ProjectFile("/deep/__init__.py", ""));
        return files;
    }

    private static StriffConfig oneLevel() {
        return new StriffConfig()
                .setLanguages(List.of(Lang.PYTHON))
                .setFilesFilter(List.of(SERVICE))
                .setAnalysisDepth(1);
    }

    private static Component component(OOPSourceCodeModel model, String name) {
        return model.component(name).orElseThrow(() -> new AssertionError(name + " is not in the model"));
    }

    /** Temporary directories of source copies this process owns. */
    private static Set<String> sourceCopies() {
        String prefix = "clarpse-src-" + ProcessHandle.current().pid() + "-";
        Set<String> names = new HashSet<>();
        File[] entries = new File(System.getProperty("java.io.tmpdir")).listFiles();
        if (entries != null) {
            for (File entry : entries) {
                if (entry.getName().startsWith(prefix)) {
                    names.add(entry.getName());
                }
            }
        }
        return names;
    }

    @Test
    public void pythonReferenceAddedInHeadLoadsItsTargetAsBoundaryInBothRevisions() throws Exception {
        StriffOperation op = new StriffOperation(files(false), files(true), oneLevel());
        CodeDiff diff = op.codeDiff();

        assertTrue(component(diff.oldModel(), "lib.helper.Helper").isBoundary());
        assertTrue(component(diff.newModel(), "lib.helper.Helper").isBoundary());
        assertFalse(component(diff.newModel(), "app.service.Service").isBoundary());
        assertFalse(diff.changeSet().addedComponents().contains("lib.helper.Helper"));
        assertTrue(diff.changeSet().addedRelations().allRels().stream()
                .anyMatch(rel -> rel.originalComponent().uniqueName().equals("app.service.Service")
                        && rel.targetComponent().uniqueName().equals("lib.helper.Helper")));
        assertFalse(diff.mergedModel().containsComponent("deep.base.Base"));
        assertTrue(diff.notLoadedRelations().toString(), diff.notLoadedRelations().stream()
                .anyMatch(rel -> rel.sourceComponent().equals("lib.helper.Helper")
                        && rel.targetName().equals("deep.base.Base")
                        && rel.origin() == NotLoadedRelation.Origin.BOUNDARY));
        assertEquals(Set.of(HELPER), op.analysisScope().levelOneFiles());
    }

    @Test
    public void pythonExtendPassPromotesTheHelper() throws Exception {
        StriffConfig config = oneLevel().setFocusExtender((base, head) -> Set.of(HELPER));
        CodeDiff diff = new StriffOperation(files(false), files(true), config).codeDiff();

        assertFalse(component(diff.newModel(), "lib.helper.Helper").isBoundary());
        assertTrue(component(diff.newModel(), "deep.base.Base").isBoundary());
        assertTrue(diff.notLoadedRelations().stream()
                .noneMatch(rel -> rel.sourceComponent().equals("lib.helper.Helper")));
    }

    @Test
    public void noSourceCopyOutlivesTheOperation() throws Exception {
        Set<String> before = sourceCopies();
        new StriffOperation(files(false), files(true),
                oneLevel().setFocusExtender((base, head) -> Set.of(HELPER))).result();
        Set<String> left = sourceCopies();
        left.removeAll(before);
        assertTrue("left behind: " + left, left.isEmpty());
    }

    @Test
    public void noSourceCopyOutlivesAFailedOperation() {
        Set<String> before = sourceCopies();
        StriffConfig config = oneLevel().setFocusExtender((base, head) -> {
            throw new IllegalStateException("extender failed");
        });
        assertThrows(IllegalStateException.class, () -> new StriffOperation(files(false), files(true), config));
        Set<String> left = sourceCopies();
        left.removeAll(before);
        assertTrue("left behind: " + left, left.isEmpty());
    }
}
