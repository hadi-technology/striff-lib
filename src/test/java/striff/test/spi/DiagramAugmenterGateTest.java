package striff.test.spi;

import com.hadii.clarpse.compiler.ClarpseProject;
import com.hadii.clarpse.compiler.CompileException;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.StriffDiagramModel;
import com.hadii.striff.parse.CodeDiff;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DiagramAugmenterGateTest {

    @Test
    public void augmentersCanBeDisabledViaConfig() throws Exception {
        String previous = System.getProperty(SentinelDiagramAugmenter.PROPERTY);
        System.setProperty(SentinelDiagramAugmenter.PROPERTY, "true");
        try {
            CodeDiff diff = codeDiff();

            StriffDiagramModel enabled = new StriffDiagramModel(diff, Set.of(), true);
            assertTrue(hasComponent(enabled, SentinelDiagramAugmenter.TARGET_COMPONENT));

            StriffDiagramModel disabled = new StriffDiagramModel(diff, Set.of(), false);
            assertFalse(hasComponent(disabled, SentinelDiagramAugmenter.TARGET_COMPONENT));
        } finally {
            if (previous == null) {
                System.clearProperty(SentinelDiagramAugmenter.PROPERTY);
            } else {
                System.setProperty(SentinelDiagramAugmenter.PROPERTY, previous);
            }
        }
    }

    private static boolean hasComponent(StriffDiagramModel model, String target) {
        return model.diagramCmps().stream()
                .map(DiagramComponent::uniqueName)
                .anyMatch(target::equals);
    }

    private static CodeDiff codeDiff() throws CompileException {
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/src/A.java",
                "package com.sample; public class A { int x; }"));
        oldFiles.insertFile(new ProjectFile("/src/B.java",
                "package com.sample; public class B { int y; }"));

        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/src/A.java",
                "package com.sample; public class A { int x; void m() {} }"));
        newFiles.insertFile(new ProjectFile("/src/B.java",
                "package com.sample; public class B { int y; }"));

        OOPSourceCodeModel oldModel = new ClarpseProject(oldFiles, Lang.JAVA).result().model();
        OOPSourceCodeModel newModel = new ClarpseProject(newFiles, Lang.JAVA).result().model();
        return new CodeDiff(oldModel, newModel);
    }
}
