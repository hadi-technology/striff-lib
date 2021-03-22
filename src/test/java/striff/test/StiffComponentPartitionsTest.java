package striff.test;

import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.striff.StriffCodeModel;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.DiagramCodeModel;
import com.hadii.striff.diagram.StiffComponentPartitions;
import com.hadii.striff.parse.DiffCodeModel;
import com.hadii.striff.parse.ParsedProject;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Tests to ensure component relations are being extracted correctly.
 */
public class StiffComponentPartitionsTest {

    /**
     * Test Components Setup:
     *
     *
     *                           ClassG
     *                             /\
     *                             \/
     *                              |
     *                              |
     *                         (Aggregates)
     *                              |
     *                              |
     *   ClassA--(Realizes)---|> ClassB----(Specializes)-------|>ClassC
     *    ^                         ^                              /\
     *    |                         |                              \/
     *    |                         |                              |
     *    |                         |                              |
     *  (Uses)                    (Uses)                      (Composes)
     *    |                         |                              |
     *    |                         |                              |
     *  ClassF---(Uses)--------->ClassE-------(Aggregates)-----<>ClassD
     *                                                             ^
     *                                                             |
     *                                                             |
     *                                                          (Uses)
     *                                                             |
     *                                                             |
     *                                                          ClassH
     *
     */

    final String CLASS_A="class ClassA implements ClassB { }\n";
    final String CLASS_B="class ClassB extends ClassC { }\n";
    final String CLASS_C="class ClassC { private ClassD classD; }\n";
    final String CLASS_D="class ClassD { public ClassE classE; }\n";
    final String CLASS_E="class ClassE { }\n";
    final String CLASS_F="class ClassF { public ClassF(ClassE classE){ ClassA classA; } }\n";
    final String CLASS_G="class ClassG { public ClassB classB; } \n";
    final String CLASS_H="class ClassH { public ClassH() {ClassD classD;} }\n";

    @Test
    public void testEvenABCDEFComponentsSetPartitionsCount() throws Exception {
        final ProjectFile file = new ProjectFile("Class.java", CLASS_A + CLASS_B + CLASS_C + CLASS_D + CLASS_E + CLASS_F);
        final ProjectFiles files = new ProjectFiles(Lang.JAVA);
        files.insertFile(file);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(files).model());
        List<Set<DiagramComponent>> componentPartitions = new StiffComponentPartitions(
               new StriffCodeModel(new DiffCodeModel(new DiagramCodeModel(), codeModel)), 3, 2).partitions();
        assertEquals(2, componentPartitions.size());
    }

    @Test
    public void testEvenABCDEFGHEvenComponentsSetPartitionsCount() throws Exception {
        final ProjectFile file = new ProjectFile("Class.java", CLASS_A + CLASS_B +  CLASS_C + CLASS_D + CLASS_E + CLASS_F + CLASS_G + CLASS_H);
        final ProjectFiles files = new ProjectFiles(Lang.JAVA);
        files.insertFile(file);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(files).model());
        List<Set<DiagramComponent>> componentPartitions = new StiffComponentPartitions(
                new StriffCodeModel(new DiffCodeModel(codeModel, new DiagramCodeModel())), 4, 2).partitions();
        assertEquals(2, componentPartitions.size());
    }

    @Test
    public void testOddABCDFGHComponentsSetPartitionsCount() throws Exception {
        // Don't include classE
        final ProjectFile file = new ProjectFile("Class.java", CLASS_A + CLASS_B +  CLASS_C + CLASS_D + CLASS_F + CLASS_G + CLASS_H);
        final ProjectFiles files = new ProjectFiles(Lang.JAVA);
        files.insertFile(file);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(files).model());
        List<Set<DiagramComponent>> componentPartitions = new StiffComponentPartitions(
                new StriffCodeModel(new DiffCodeModel(codeModel, new DiagramCodeModel())), 4, 2).partitions();
        assertEquals(2, componentPartitions.size());
    }

    /**
     * Ensure that no further partitions are generated when an initial set of components to be included in the diagram
     * are below the desired partition size. In the test below, because softMaxSizeLimit is equal to 3, and only two
     * components are being drawn, we expect there to be one partition.
     */
    @Test
    public void testNoAdditionalPartitionsWhenOriginalComponentSetSizeBelowSoftMaxSizeLimit() throws Exception {
        final ProjectFile file = new ProjectFile("Class.java", CLASS_A);
        final ProjectFile file2 = new ProjectFile("ClassB.java", CLASS_B);
        final ProjectFiles files = new ProjectFiles(Lang.JAVA);
        files.insertFile(file);
        files.insertFile(file2);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(files).model());
        List<Set<DiagramComponent>> componentPartitions = new StiffComponentPartitions(
                new StriffCodeModel(new DiffCodeModel(codeModel, new DiagramCodeModel())), 3, 2).partitions();
        assertEquals(1, componentPartitions.size());
    }
}
