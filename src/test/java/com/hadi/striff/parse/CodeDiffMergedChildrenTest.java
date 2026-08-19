package com.hadi.striff.parse;

import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.striff.StriffConfig;
import com.hadi.striff.StriffOperation;
import com.hadi.striff.diagram.StriffDiagram;
import com.hadi.striff.diagram.StriffOutput;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Pins what the merged model actually holds in a component's children list when the old revision
 * declares a child the new revision does not.
 *
 * <p>The merge loop in {@link CodeDiff} intends to union the two revisions' children onto the
 * merged component, so that a member deleted by the change still appears inside its surviving
 * parent. Whether it does so is the question these tests answer, and they are written to fail
 * loudly if the answer ever changes: the merged model is what every downstream consumer reads, so a
 * member missing from it is missing everywhere at once.
 */
public class CodeDiffMergedChildrenTest {

    private static CodeDiff diffOf(String oldSource, String newSource) throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/Widget.java", oldSource));
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/Widget.java", newSource));
        StriffConfig config = new StriffConfig().setLanguages(Set.of(Lang.JAVA));
        return new StriffOperation(oldFiles, newFiles, config).codeDiff();
    }

    private static final String OLD_SOURCE =
            "package com.sample; public class Widget {"
                    + " private String keptField;"
                    + " private String removedField;"
                    + " public void keptMethod() {}"
                    + " public void removedMethod() {}"
                    + " }";

    private static final String NEW_SOURCE =
            "package com.sample; public class Widget {"
                    + " private String keptField;"
                    + " public void keptMethod() {}"
                    + " }";

    @Test
    public void mergedParentListsChildrenDeletedByTheChange() throws Exception {
        CodeDiff diff = diffOf(OLD_SOURCE, NEW_SOURCE);

        Component oldWidget = diff.oldModel().copyOfComponent("com.sample.Widget").orElseThrow();
        Component newWidget = diff.newModel().copyOfComponent("com.sample.Widget").orElseThrow();
        Component mergedWidget = diff.mergedModel().copyOfComponent("com.sample.Widget").orElseThrow();

        // The premise: the old revision declares two children the new revision does not.
        assertTrue(oldWidget.children().contains("com.sample.Widget.removedField"));
        assertTrue(oldWidget.children().contains("com.sample.Widget.removedMethod()"));
        assertTrue(!newWidget.children().contains("com.sample.Widget.removedField"));
        assertTrue(!newWidget.children().contains("com.sample.Widget.removedMethod()"));

        // The deleted members themselves survive into the merged model as old-only components.
        assertTrue(diff.mergedModel().copyOfComponent("com.sample.Widget.removedField").isPresent());
        assertTrue(diff.mergedModel().copyOfComponent("com.sample.Widget.removedMethod()").isPresent());

        // What the merge loop is for: the surviving parent should list them too.
        assertTrue("merged Widget should list the deleted field among its children, but held "
                        + mergedWidget.children(),
                mergedWidget.children().contains("com.sample.Widget.removedField"));
        assertTrue("merged Widget should list the deleted method among its children, but held "
                        + mergedWidget.children(),
                mergedWidget.children().contains("com.sample.Widget.removedMethod()"));
    }

    @Test
    public void mergedParentKeepsTheNewRevisionsChildrenAndAddsNoDuplicates() throws Exception {
        CodeDiff diff = diffOf(OLD_SOURCE, NEW_SOURCE);
        List<String> children = diff.mergedModel().copyOfComponent("com.sample.Widget")
                .orElseThrow().children();

        assertTrue(children.contains("com.sample.Widget.keptField"));
        assertTrue(children.contains("com.sample.Widget.keptMethod()"));
        assertEquals("no child may be listed twice, but held " + children,
                Set.copyOf(children).size(), children.size());
    }

    @Test
    public void aDeletedMemberReachesTheRenderedDiagram() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/Widget.java", OLD_SOURCE));
        oldFiles.insertFile(new ProjectFile("/Peer.java",
                "package com.sample; public class Peer { private Widget widget; }"));
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/Widget.java", NEW_SOURCE));
        newFiles.insertFile(new ProjectFile("/Peer.java",
                "package com.sample; public class Peer { private Widget widget; }"));

        StriffOutput output = new StriffOperation(oldFiles, newFiles,
                new StriffConfig().setLanguages(Set.of(Lang.JAVA))).result();

        assertTrue("expected a diagram to render", !output.diagrams().isEmpty());
        StriffDiagram diagram = output.diagrams().get(0);
        assertTrue("a member this change deleted must appear in the diagram it is drawn on, but the "
                        + "PlantUML held:\n" + diagram.pumlSource(),
                diagram.pumlSource().contains("removedMethod"));
    }

    @Test
    public void oldOnlyComponentEntersTheMergedModelWithItsOwnChildren() throws Exception {
        CodeDiff diff = diffOf(
                "package com.sample; public class Widget { public void go() {} }"
                        + " class Gone { public void alsoGone() {} }",
                "package com.sample; public class Widget { public void go() {} }");

        Component gone = diff.mergedModel().copyOfComponent("com.sample.Gone").orElseThrow();
        assertTrue("an old-only component keeps its children, but held " + gone.children(),
                gone.children().contains("com.sample.Gone.alsoGone()"));
    }
}
