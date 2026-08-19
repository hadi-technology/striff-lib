package com.hadi.striff.parse;

import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.StriffConfig;
import com.hadi.striff.StriffOperation;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Records, with the measurements, why head does not reuse base's component objects for files the
 * change did not touch.
 *
 * <p>Reuse is the largest memory saving on the table: a pull request edits three files out of 452 and
 * both revisions are parsed in full, so copy-on-write would take the two models from twice the
 * repository to once plus a delta. The obstacle is whether a file's parsed component is a function of
 * that file. These tests establish that it is not, and they were written after a first set of
 * guesses about <em>why</em> turned out to be wrong -- which is why the specific mechanism is pinned
 * here rather than described.
 *
 * <h2>What is safe, measured, and not what was expected</h2>
 * <p>A reference's <b>target name</b> is resolved from the file's own text and does not move when
 * other files do. An unchanged consumer of a type that is renamed, moved to another package, or
 * deleted outright goes on naming the same target at head as at base; so does one importing by
 * wildcard, one naming a nested type, and one calling a method that appears on its superclass. Every
 * one of those was expected to differ and none does. clarpse resolves a simple name against the
 * file's imports and its own package speculatively, whether or not anything matches.
 *
 * <h2>What is not safe, and it is enough on its own</h2>
 * <p>A reference's <b>internal-or-external classification</b> is a function of the whole tree. A file
 * that names {@code Widget} with no import resolves it to {@code com.sample.Widget} at both revisions
 * -- but the reference is <b>external</b> at a revision where no file declares that type and
 * <b>internal</b> at one where some file does. {@link Component} keeps the two in separate sets, and
 * downstream everything turns on which set a reference is in: relationship extraction walks both but
 * the package graph and the structural facts derived from it are built from edges that point inside the
 * codebase, and an external reference is not one.
 *
 * <p>So the reuse case that looks safest is the one that breaks: a pull request that <b>adds a
 * class</b> which unchanged files already referenced. Reusing those unchanged files' components would
 * carry base's {@code external} classification into head, the new edges would be absent from head's
 * graph, and a genuinely added dependency would go unreported. Not an error -- a silent false
 * negative, in the product's core function, on the commonest shape of change there is.
 *
 * <p>Re-classifying every reused component's references against head's model would fix it and would
 * also defeat the point, since the reference sets are the bulk of a component's footprint. A sound
 * scheme needs provenance clarpse does not record: which of a reference's resolution steps consulted
 * the rest of the tree. Until it does, these tests are the standing statement of the problem, and any
 * future scheme has to make them pass rather than be weakened to accommodate one.
 */
public class CrossFileResolutionHazardTest {

    /** The file no revision below ever edits. What shifts underneath it is the point. */
    private static final String UNCHANGED_CONSUMER =
            "package com.sample; public class Consumer { private Widget widget; }";

    private static CodeDiff diff(final ProjectFiles oldFiles, final ProjectFiles newFiles)
            throws Exception {
        return new StriffOperation(oldFiles, newFiles,
                new StriffConfig().setLanguages(Set.of(Lang.JAVA))).codeDiff();
    }

    private static Set<String> internalTargets(final OOPSourceCodeModel model, final String cmp) {
        Set<String> targets = new TreeSet<>();
        model.copyOfComponent(cmp).orElseThrow().internalDependencies()
                .forEach(ref -> targets.add(ref.invokedComponent()));
        return targets;
    }

    private static Set<String> externalTargets(final OOPSourceCodeModel model, final String cmp) {
        Set<String> targets = new TreeSet<>();
        model.copyOfComponent(cmp).orElseThrow().externalDependencies()
                .forEach(ref -> targets.add(ref.invokedComponent()));
        return targets;
    }

    private static Set<String> allTargets(final OOPSourceCodeModel model, final String cmp) {
        Set<String> targets = new TreeSet<>(internalTargets(model, cmp));
        targets.addAll(externalTargets(model, cmp));
        return targets;
    }

    @Test
    public void addingATypeFlipsAnUnchangedFilesReferenceFromExternalToInternal() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/Consumer.java", UNCHANGED_CONSUMER));

        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/Consumer.java", UNCHANGED_CONSUMER));
        newFiles.insertFile(new ProjectFile("/Widget.java",
                "package com.sample; public class Widget { }"));

        CodeDiff diff = diff(oldFiles, newFiles);
        String field = "com.sample.Consumer.widget";

        assertEquals("the target name is the same at both revisions, so a name-only comparison would "
                        + "conclude this component is reusable",
                allTargets(diff.oldModel(), field), allTargets(diff.newModel(), field));

        assertTrue("at base, nothing declares Widget, so the reference is external",
                externalTargets(diff.oldModel(), field).contains("com.sample.Widget"));
        assertTrue("at head, a file declares it, so the very same reference is internal",
                internalTargets(diff.newModel(), field).contains("com.sample.Widget"));
        assertNotEquals("this is what makes an unchanged file's component revision-dependent, and it "
                        + "is why base's component may not be reused at head",
                internalTargets(diff.oldModel(), field), internalTargets(diff.newModel(), field));
    }

    @Test
    public void aRenamedTypeIsStillNamedIdenticallyByAnUnchangedConsumer() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/Consumer.java", UNCHANGED_CONSUMER));
        oldFiles.insertFile(new ProjectFile("/Widget.java",
                "package com.sample; public class Widget { }"));

        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/Consumer.java", UNCHANGED_CONSUMER));
        newFiles.insertFile(new ProjectFile("/Gadget.java",
                "package com.sample; public class Gadget { }"));

        CodeDiff diff = diff(oldFiles, newFiles);
        String field = "com.sample.Consumer.widget";

        assertEquals("the consumer names the old type at both revisions -- the rename does not move "
                        + "the reference, it only removes what the reference lands on",
                allTargets(diff.oldModel(), field), allTargets(diff.newModel(), field));
        assertTrue("and the diff reports the rename regardless",
                diff.changeSet().deletedComponents().contains("com.sample.Widget"));
        assertTrue(diff.changeSet().addedComponents().contains("com.sample.Gadget"));
    }

    @Test
    public void aTypeMovedBetweenPackagesIsStillNamedIdenticallyByAnUnchangedConsumer()
            throws Exception {
        String consumer = "package com.sample; import com.sample.inner.Widget;"
                + " public class Consumer { private Widget widget; }";

        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/Consumer.java", consumer));
        oldFiles.insertFile(new ProjectFile("/inner/Widget.java",
                "package com.sample.inner; public class Widget { }"));

        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/Consumer.java", consumer));
        newFiles.insertFile(new ProjectFile("/outer/Widget.java",
                "package com.sample.outer; public class Widget { }"));

        CodeDiff diff = diff(oldFiles, newFiles);
        String field = "com.sample.Consumer.widget";

        assertEquals("resolution follows the import statement, which the change did not touch",
                Set.of("com.sample.inner.Widget"), allTargets(diff.oldModel(), field));
        assertEquals(allTargets(diff.oldModel(), field), allTargets(diff.newModel(), field));
        assertTrue("the move still reads as a deletion and an addition",
                diff.changeSet().deletedComponents().contains("com.sample.inner.Widget"));
        assertTrue(diff.changeSet().addedComponents().contains("com.sample.outer.Widget"));
    }

    @Test
    public void deletingAStillReferencedTypeIsReportedAsADeletedRelation() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/Consumer.java", UNCHANGED_CONSUMER));
        oldFiles.insertFile(new ProjectFile("/Widget.java",
                "package com.sample; public class Widget { }"));

        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/Consumer.java", UNCHANGED_CONSUMER));

        CodeDiff diff = diff(oldFiles, newFiles);
        assertTrue(diff.changeSet().deletedComponents().contains("com.sample.Widget"));
        assertTrue("the merged model keeps the deleted type, for context",
                diff.mergedModel().copyOfComponent("com.sample.Widget").isPresent());
        assertTrue("and the edge into it is reported deleted",
                diff.changeSet().deletedRelations().allRels().stream()
                        .anyMatch(rel -> "com.sample.Widget".equals(
                                rel.targetComponent().uniqueName())));
    }
}
