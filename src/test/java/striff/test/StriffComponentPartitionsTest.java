package striff.test;

import com.hadii.clarpse.compiler.File;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.SourceFiles;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.DiagramSourceCodeModel;
import com.hadii.striff.diagram.partition.StriffComponentPartitions;
import com.hadii.striff.extractor.ComponentRelations;
import com.hadii.striff.parse.ParsedProject;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests to ensure component relations are being extracted correctly.
 */
public class StriffComponentPartitionsTest {


    /**
     * Test Classes Setup:
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
     *                              ^                              /\
     *                              |                              \/
     *                              |                              |
     *                              |                              |
     *                            (Uses)                      (Composes)
     *                              |                              |
     *                              |                              |
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
        final File file = new File("Class.java", CLASS_A + CLASS_B + CLASS_C + CLASS_D + CLASS_E + CLASS_F);
        final SourceFiles files = new SourceFiles(Lang.JAVA);
        files.insertFile(file);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(files).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        Map<Integer, Set<DiagramComponent>> componentPartitions = new StriffComponentPartitions(
                new HashSet<>(codeModel.components().values()).stream()
                        .filter(diagramComponent -> diagramComponent.componentType().isBaseComponent())
                        .collect(Collectors.toSet()), relations).partitions();
        assertEquals(2, componentPartitions.size());
    }

    @Test
    public void testEvenABCDEFComponentsGroupATypes() throws Exception {
        final File file = new File("Class.java", CLASS_A + CLASS_B + CLASS_C + CLASS_D + CLASS_E + CLASS_F);
        final SourceFiles files = new SourceFiles(Lang.JAVA);
        files.insertFile(file);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(files).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        Map<Integer, Set<DiagramComponent>> componentPartitions = new StriffComponentPartitions(
                new HashSet<>(codeModel.components().values()).stream()
                        .filter(diagramComponent -> diagramComponent.componentType().isBaseComponent())
                        .collect(Collectors.toSet()), relations).partitions();
        assertTrue(componentPartitions.get(0).containsAll(Arrays.asList(
                new DiagramComponent("ClassA"),
                new DiagramComponent("ClassB"),
                new DiagramComponent("ClassC")
        )));
    }

    @Test
    public void testEvenABCDEFComponentsGroupBTypes() throws Exception {
        final File file = new File("Class.java", CLASS_A + CLASS_B + CLASS_C + CLASS_D + CLASS_E + CLASS_F);
        final SourceFiles files = new SourceFiles(Lang.JAVA);
        files.insertFile(file);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(files).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        Map<Integer, Set<DiagramComponent>> componentPartitions = new StriffComponentPartitions(
                new HashSet<>(codeModel.components().values()).stream()
                        .filter(diagramComponent -> diagramComponent.componentType().isBaseComponent())
                        .collect(Collectors.toSet()), relations).partitions();
        assertTrue(componentPartitions.get(1).containsAll(Arrays.asList(
                new DiagramComponent("ClassD"),
                new DiagramComponent("ClassE"),
                new DiagramComponent("ClassF")
        )));
    }

    @Test
    public void testEvenABCDEFGHEvenComponentsSetPartitionsCount() throws Exception {
        final File file = new File("Class.java", CLASS_A + CLASS_B +  CLASS_C + CLASS_D + CLASS_E + CLASS_F + CLASS_G + CLASS_H);
        final SourceFiles files = new SourceFiles(Lang.JAVA);
        files.insertFile(file);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(files).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        Map<Integer, Set<DiagramComponent>> componentPartitions = new StriffComponentPartitions(
                new HashSet<>(codeModel.components().values()).stream()
                        .filter(diagramComponent -> diagramComponent.componentType().isBaseComponent())
                        .collect(Collectors.toSet()), relations).partitions();
        assertEquals(2, componentPartitions.size());
    }

    @Test
    public void testEvenABCDEFGHEvenComponentsGroupATypes() throws Exception {
        final File file = new File("Class.java", CLASS_A + CLASS_B +  CLASS_C + CLASS_D + CLASS_E + CLASS_F + CLASS_G + CLASS_H);
        final SourceFiles files = new SourceFiles(Lang.JAVA);
        files.insertFile(file);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(files).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        Map<Integer, Set<DiagramComponent>> componentPartitions = new StriffComponentPartitions(
                new HashSet<>(codeModel.components().values()).stream()
                        .filter(diagramComponent -> diagramComponent.componentType().isBaseComponent())
                        .collect(Collectors.toSet()), relations).partitions();
        assertTrue(componentPartitions.get(0).containsAll(Arrays.asList(
                new DiagramComponent("ClassA"),
                new DiagramComponent("ClassB"),
                new DiagramComponent("ClassC"),
                new DiagramComponent("ClassG")
        )));
    }

    @Test
    public void testEvenABCDEFGHEvenComponentsGroupBTypes() throws Exception {
        final File file = new File("Class.java", CLASS_A + CLASS_B +  CLASS_C + CLASS_D + CLASS_E + CLASS_F + CLASS_G + CLASS_H);
        final SourceFiles files = new SourceFiles(Lang.JAVA);
        files.insertFile(file);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(files).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        Map<Integer, Set<DiagramComponent>> componentPartitions = new StriffComponentPartitions(
                new HashSet<>(codeModel.components().values()).stream()
                        .filter(diagramComponent -> diagramComponent.componentType().isBaseComponent())
                        .collect(Collectors.toSet()), relations).partitions();
        assertTrue(componentPartitions.get(1).containsAll(Arrays.asList(
                new DiagramComponent("ClassD"),
                new DiagramComponent("ClassE"),
                new DiagramComponent("ClassF"),
                new DiagramComponent("ClassH")
        )));
    }

    @Test
    public void testOddABCDFGHComponentsSetPartitionsCount() throws Exception {
        // Don't include classE
        final File file = new File("Class.java", CLASS_A + CLASS_B +  CLASS_C + CLASS_D + CLASS_F + CLASS_G + CLASS_H);
        final SourceFiles files = new SourceFiles(Lang.JAVA);
        files.insertFile(file);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(files).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        Map<Integer, Set<DiagramComponent>> componentPartitions = new StriffComponentPartitions(
                new HashSet<>(codeModel.components().values()).stream()
                        .filter(diagramComponent -> diagramComponent.componentType().isBaseComponent())
                        .collect(Collectors.toSet()), relations).partitions();
        assertEquals(2, componentPartitions.size());
    }

    @Test
    public void testOddABCDFGHComponentsGroupATypes() throws Exception {
        // Don't include classE
        final File file = new File("Class.java", CLASS_A + CLASS_B +  CLASS_C + CLASS_D + CLASS_F + CLASS_G + CLASS_H);
        final SourceFiles files = new SourceFiles(Lang.JAVA);
        files.insertFile(file);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(files).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        Map<Integer, Set<DiagramComponent>> componentPartitions = new StriffComponentPartitions(
                new HashSet<>(codeModel.components().values()).stream()
                        .filter(diagramComponent -> diagramComponent.componentType().isBaseComponent())
                        .collect(Collectors.toSet()), relations).partitions();
        assertTrue(componentPartitions.get(0).containsAll(Arrays.asList(
                new DiagramComponent("ClassD"),
                new DiagramComponent("ClassE"),
                new DiagramComponent("ClassF"),
                new DiagramComponent("ClassH")
        )));
    }
}
