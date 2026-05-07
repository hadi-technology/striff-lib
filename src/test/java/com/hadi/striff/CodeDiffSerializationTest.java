package com.hadi.striff;

import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CodeDiffSerializationTest {

    @Test
    public void roundTripPreservesComponentsRelationsAndChangeSet() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        oldFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { }"));

        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/ClassA.java",
                "package com.sample; public class ClassA { private ClassB b; }"));
        newFiles.insertFile(new ProjectFile("/ClassB.java",
                "package com.sample; public class ClassB { }"));

        StriffConfig config = new StriffConfig().setLanguages(Set.of(Lang.JAVA));
        CodeDiff original = new StriffOperation(oldFiles, newFiles, config).codeDiff();
        CodeDiff restored = roundTrip(original);

        assertEquals(original.oldModel().size(), restored.oldModel().size());
        assertEquals(original.newModel().size(), restored.newModel().size());
        assertEquals(original.mergedModel().size(), restored.mergedModel().size());
        assertEquals(edgeSet(original), edgeSet(restored));
        assertEquals(original.changeSet().addedComponents(), restored.changeSet().addedComponents());
        assertEquals(original.changeSet().deletedComponents(), restored.changeSet().deletedComponents());
        assertEquals(original.changeSet().modifiedComponents(), restored.changeSet().modifiedComponents());
        assertEquals(original.changeSet().keyRelationsComponents(), restored.changeSet().keyRelationsComponents());
        assertTrue(restored.changeSet().addedComponents().contains("com.sample.ClassB"));
    }

    private static CodeDiff roundTrip(CodeDiff original) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (CodeDiff) in.readObject();
        }
    }

    private static Set<String> edgeSet(CodeDiff diff) {
        return diff.extractedRels().allRels().stream()
                .map(rel -> rel.originalComponent().uniqueName()
                        + "->" + rel.targetComponent().uniqueName()
                        + ":" + rel.associationType().name())
                .collect(Collectors.toSet());
    }
}
