package com.hadi.striff;

import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.extractor.DiagramConstants.ComponentAssociation;
import com.hadi.striff.extractor.NotLoadedRelation;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * One-level analysis through the full pipeline: the base/head union, the focus extension pass,
 * not-loaded relationships, context files and budgets.
 *
 * <p>Fixture: {@code app.A} is analysed. In the head revision it references {@code lib.B}, which
 * references {@code deep.C}. {@code app.Caller} and {@code app.Caller2} use {@code app.A};
 * {@code app.Caller} also uses {@code lib.E}.</p>
 */
public class OneLevelAnalysisTest {

    private static final String A = "/app/A.java";
    private static final String B = "/lib/B.java";
    private static final String C = "/deep/C.java";
    private static final String E = "/lib/E.java";
    private static final String CALLER = "/app/Caller.java";
    private static final String CALLER2 = "/app/Caller2.java";

    private static final String A_WITHOUT_B = "package app; public class A { }";
    private static final String A_WITH_B = "package app; import lib.B; public class A { private B b; }";

    private static ProjectFiles files(String aSource) {
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile(A, aSource));
        files.insertFile(new ProjectFile(B, "package lib; import deep.C; public class B { private C c; }"));
        files.insertFile(new ProjectFile(C, "package deep; public class C { }"));
        files.insertFile(new ProjectFile(E, "package lib; public class E { }"));
        files.insertFile(new ProjectFile(CALLER,
                "package app; import lib.E; public class Caller { private A a; private A other; private E e; }"));
        files.insertFile(new ProjectFile(CALLER2, "package app; public class Caller2 { private A a; }"));
        return files;
    }

    private static StriffConfig oneLevel() {
        return new StriffConfig()
                .setLanguages(List.of(Lang.JAVA))
                .setFilesFilter(List.of(A))
                .setAnalysisDepth(1);
    }

    private static Component component(OOPSourceCodeModel model, String name) {
        return model.component(name).orElseThrow(() -> new AssertionError(name + " is not in the model"));
    }

    private static Set<String> edges(CodeDiff diff) {
        return diff.extractedRels().allRels().stream()
                .map(rel -> rel.originalComponent().uniqueName() + "->" + rel.targetComponent().uniqueName())
                .collect(Collectors.toSet());
    }

    @Test
    public void referenceAddedInHeadLoadsItsTargetInBothRevisions() throws Exception {
        StriffOperation op = new StriffOperation(files(A_WITHOUT_B), files(A_WITH_B), oneLevel());
        CodeDiff diff = op.codeDiff();

        assertTrue(component(diff.oldModel(), "lib.B").isBoundary());
        assertTrue(component(diff.newModel(), "lib.B").isBoundary());
        assertFalse("lib.B only became referenced; it was not added",
                diff.changeSet().addedComponents().stream().anyMatch(name -> name.startsWith("lib.B")));
        assertTrue(diff.changeSet().addedRelations().allRels().stream()
                .anyMatch(rel -> rel.originalComponent().uniqueName().equals("app.A")
                        && rel.targetComponent().uniqueName().equals("lib.B")));
        assertTrue(op.analysisScope().levelOneFiles().contains(B));
    }

    @Test
    public void referenceRemovedInHeadDoesNotDeleteItsTarget() throws Exception {
        CodeDiff diff = new StriffOperation(files(A_WITH_B), files(A_WITHOUT_B), oneLevel()).codeDiff();

        assertTrue(component(diff.newModel(), "lib.B").isBoundary());
        assertFalse("lib.B only stopped being referenced; it was not deleted",
                diff.changeSet().deletedComponents().stream().anyMatch(name -> name.startsWith("lib.B")));
        assertTrue(diff.changeSet().deletedRelations().allRels().stream()
                .anyMatch(rel -> rel.originalComponent().uniqueName().equals("app.A")
                        && rel.targetComponent().uniqueName().equals("lib.B")));
    }

    @Test
    public void boundaryReferencesPastLevelOneAreNotLoadedRelations() throws Exception {
        CodeDiff diff = new StriffOperation(files(A_WITHOUT_B), files(A_WITH_B), oneLevel()).codeDiff();

        assertFalse(diff.mergedModel().containsComponent("deep.C"));
        assertTrue(diff.notLoadedRelations().toString(), diff.notLoadedRelations().contains(
                new NotLoadedRelation("lib.B", "deep.C", ComponentAssociation.COMPOSITION,
                        NotLoadedRelation.Origin.BOUNDARY)));
        assertFalse("not-loaded relationships are never drawn", edges(diff).contains("lib.B->deep.C"));
    }

    @Test
    public void focusExtenderPromotesALevelOneFile() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        StriffConfig config = oneLevel().setFocusExtender((base, head) -> {
            calls.incrementAndGet();
            assertTrue("the extender sees the first pass", component(head, "lib.B").isBoundary());
            return Set.of(B);
        });
        StriffOperation op = new StriffOperation(files(A_WITHOUT_B), files(A_WITH_B), config);
        CodeDiff diff = op.codeDiff();

        assertEquals(1, calls.get());
        assertFalse(component(diff.newModel(), "lib.B").isBoundary());
        assertTrue(component(diff.newModel(), "deep.C").isBoundary());
        assertTrue(edges(diff).contains("lib.B->deep.C"));
        assertTrue("B's reference to C is no longer not-loaded", diff.notLoadedRelations().stream()
                .noneMatch(rel -> rel.sourceComponent().equals("lib.B")));
        assertEquals(Set.of(B), op.analysisScope().extendedFocus());
    }

    @Test
    public void renderingLeavesTheDiffsRelationsIntact() throws Exception {
        CodeDiff diff = new StriffOperation(files(A_WITHOUT_B), files(A_WITH_B),
                oneLevel().setFocusExtender((base, head) -> Set.of(B))).codeDiff();
        // deep.C is not drawn, and drawing must not take its relation out of the diff.
        assertEquals(edges(diff), new com.hadi.striff.extractor.ExtractedRelationships(diff.mergedModel())
                .result().allRels().stream()
                .map(rel -> rel.originalComponent().uniqueName() + "->" + rel.targetComponent().uniqueName())
                .collect(Collectors.toSet()));
    }

    @Test
    public void focusExtenderNamingNothingKeepsTheFirstCompile() throws Exception {
        StriffConfig config = oneLevel().setFocusExtender((base, head) -> Set.of(A));
        StriffOperation op = new StriffOperation(files(A_WITHOUT_B), files(A_WITH_B), config);

        assertTrue(component(op.codeDiff().newModel(), "lib.B").isBoundary());
        assertTrue(op.analysisScope().extendedFocus().isEmpty());
    }

    @Test
    public void budgetHeldTargetsOfAnalysedCodeStayNamedAsFocusRelations() throws Exception {
        StriffConfig config = oneLevel().setLevelOneBudget(0).setContextBudget(0);
        StriffOperation op = new StriffOperation(files(A_WITHOUT_B), files(A_WITH_B), config);
        CodeDiff diff = op.codeDiff();

        assertFalse(diff.mergedModel().containsComponent("lib.B"));
        assertTrue(diff.notLoadedRelations().toString(), diff.notLoadedRelations().contains(
                new NotLoadedRelation("app.A", "lib.B", ComponentAssociation.COMPOSITION,
                        NotLoadedRelation.Origin.FOCUS)));
        assertTrue(op.analysisScope().levelOneHeldByBudget().contains(B));
    }

    @Test
    public void contextFilesAreBoundaryWithoutTheirOwnLevelOne() throws Exception {
        StriffConfig config = oneLevel().setExpandedFiles(List.of(CALLER));
        StriffOperation op = new StriffOperation(files(A_WITHOUT_B), files(A_WITH_B), config);
        CodeDiff diff = op.codeDiff();

        assertTrue(component(diff.newModel(), "app.Caller").isBoundary());
        assertFalse("a context file's own references are not followed",
                diff.mergedModel().containsComponent("lib.E"));
        assertTrue(edges(diff).contains("app.Caller->app.A"));
        assertEquals(Set.of(CALLER), op.analysisScope().contextFiles());
        assertFalse(op.analysisScope().levelOneFiles().contains(CALLER));
    }

    @Test
    public void contextBudgetKeepsTheContextFilesNamingTheAnalysedFilesMost() throws Exception {
        StriffConfig config = oneLevel().setExpandedFiles(List.of(CALLER, CALLER2)).setContextBudget(1);
        StriffOperation op = new StriffOperation(files(A_WITHOUT_B), files(A_WITH_B), config);

        assertEquals(Set.of(CALLER), op.analysisScope().contextFiles());
        assertEquals(Set.of(CALLER2), op.analysisScope().contextHeldByBudget());
        assertFalse(op.codeDiff().mergedModel().containsComponent("app.Caller2"));
    }

    @Test
    public void contextNeverDisplacesLevelOne() throws Exception {
        StriffConfig config = oneLevel().setExpandedFiles(List.of(CALLER)).setLevelOneBudget(1);
        StriffOperation op = new StriffOperation(files(A_WITHOUT_B), files(A_WITH_B), config);

        assertTrue(op.codeDiff().mergedModel().containsComponent("lib.B"));
        assertTrue(op.analysisScope().contextFiles().isEmpty());
        assertEquals(Set.of(CALLER), op.analysisScope().contextHeldByBudget());
    }

    @Test
    public void serialisationKeepsBoundaryFlagsAndNotLoadedRelations() throws Exception {
        CodeDiff original = new StriffOperation(files(A_WITHOUT_B), files(A_WITH_B), oneLevel()).codeDiff();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }
        CodeDiff restored;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (CodeDiff) in.readObject();
        }
        assertTrue(component(restored.mergedModel(), "lib.B").isBoundary());
        assertTrue(component(restored.newModel(), "lib.B").isBoundary());
        assertFalse(component(restored.mergedModel(), "app.A").isBoundary());
        assertEquals(original.notLoadedRelations(), restored.notLoadedRelations());
        assertFalse(restored.notLoadedRelations().isEmpty());
    }

    @Test
    public void boundaryComponentsAreDrawnAsContextAndSerialisedAsBoundary() throws Exception {
        StriffOperation op = new StriffOperation(files(A_WITHOUT_B), files(A_WITH_B), oneLevel());
        Map<String, Boolean> boundaryByName = op.result().diagrams().get(0).cmps().stream()
                .collect(Collectors.toMap(cmp -> cmp.uniqueName(), cmp -> cmp.boundary()));

        assertEquals(Boolean.TRUE, boundaryByName.get("lib.B"));
        assertEquals(Boolean.FALSE, boundaryByName.get("app.A"));
        String json = new com.fasterxml.jackson.databind.ObjectMapper()
                .writeValueAsString(op.result().diagrams().get(0).cmps());
        assertTrue(json.contains("\"boundary\":true"));
        assertFalse(json.contains("\"boundary\":false"));
    }

    @Test
    public void boundaryComponentsAreTrimmedFirstWhenTheDiagramIsOverTheLimit() throws Exception {
        StriffConfig config = oneLevel().setExpandedFiles(List.of(CALLER)).setMaxComponentsPerDiagram(2);
        StriffOperation op = new StriffOperation(files(A_WITHOUT_B), files(A_WITH_B), config);
        Set<String> drawn = op.result().diagrams().get(0).cmps().stream()
                .map(cmp -> cmp.uniqueName()).collect(Collectors.toSet());

        assertTrue(drawn.toString(), drawn.contains("app.A"));
        assertTrue("level one on a changed relation outlasts context", drawn.contains("lib.B"));
        assertFalse("context goes first", drawn.contains("app.Caller"));
        assertNotNull(op.result().diagrams().get(0).svg());
    }

    @Test
    public void ordinaryAnalysisHasNoScopeAndNoNotLoadedRelations() throws Exception {
        StriffConfig config = new StriffConfig().setLanguages(List.of(Lang.JAVA)).setFilesFilter(List.of(A));
        StriffOperation op = new StriffOperation(files(A_WITHOUT_B), files(A_WITH_B), config);

        assertEquals(AnalysisScope.none(), op.analysisScope());
        assertTrue(op.codeDiff().notLoadedRelations().isEmpty());
        assertFalse(op.codeDiff().mergedModel().components().anyMatch(Component::isBoundary));
    }

    @Test
    public void depthIsZeroOrOne() {
        assertThrows(IllegalArgumentException.class, () -> new StriffConfig().setAnalysisDepth(2));
        assertThrows(IllegalArgumentException.class, () -> new StriffConfig().setAnalysisDepth(-1));
        assertThrows(IllegalArgumentException.class, () -> new StriffConfig().setLevelOneBudget(-1));
        assertThrows(IllegalArgumentException.class, () -> new StriffConfig().setContextBudget(-1));
    }

    @Test
    public void incrementalOperationRejectsOneLevel() {
        StriffConfig config = oneLevel();
        assertThrows(IllegalArgumentException.class, () ->
                new StriffOperation(new OOPSourceCodeModel(), files(A_WITH_B), Set.of(A), config));
    }

    @Test
    public void aRelationBetweenTwoBoundaryComponentsIsNotAKeyRelation() throws Exception {
        OOPSourceCodeModel base = compile("package lib; public class B { }");
        OOPSourceCodeModel head = compile("package lib; import deep.C; public class B { private C c; }");
        assertTrue("control: an ordinary relation change is key",
                new ChangeSet(base, head).keyRelationsComponents().containsAll(Set.of("lib.B", "deep.C")));

        base.components().forEach(cmp -> cmp.setBoundary(true));
        head.components().forEach(cmp -> cmp.setBoundary(true));
        ChangeSet changeSet = new ChangeSet(base, head);
        assertFalse(changeSet.addedRelations().allRels().isEmpty());
        assertTrue(changeSet.keyRelationsComponents().isEmpty());
    }

    private static OOPSourceCodeModel compile(String bSource) throws Exception {
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile(B, bSource));
        files.insertFile(new ProjectFile(C, "package deep; public class C { }"));
        return new com.hadi.clarpse.compiler.ClarpseProject(files, Lang.JAVA).result().model();
    }
}
