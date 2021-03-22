package striff.test;

import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.DiagramConstants.ComponentAssociation;
import com.hadii.striff.diagram.DiagramConstants.DefaultClassMultiplicities;
import com.hadii.striff.diagram.DiagramCodeModel;
import com.hadii.striff.extractor.ComponentRelation;
import com.hadii.striff.extractor.ComponentRelations;
import com.hadii.striff.parse.ParsedProject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests to ensure component relations are being extracted correctly.
 */
public class ComponentRelationsTest {

    @Test
    public void testCompositionRelationExists() throws Exception {
        final ProjectFile fileA = new ProjectFile("ClassA.java", "package com.sample;" + " import java.util.ArrayList;"
                + " public class ClassA {  private ArrayList<ClassB> b;}");
        final ProjectFile fileB = new ProjectFile("ClassB.java", "package com.sample; public class ClassB {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(fileA);
        ProjectFiles.insertFile(fileB);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        assertTrue(classARelations.size() == 1 &&
                classARelations.get(0).originalComponent().name().equals("ClassA") &&
                classARelations.get(0).targetComponent().name().equals("ClassB") &&
                classARelations.get(0).associationType().equals(ComponentAssociation.COMPOSITION));
    }

    @Test
    public void testCompositionRelationMultiplicities() throws Exception {
        final ProjectFile fileA = new ProjectFile("ClassA.java", "package com.sample;" + " import java.util.ArrayList;"
                + " public class ClassA {  private ArrayList<ClassB> b;}");
        final ProjectFile fileB = new ProjectFile("ClassB.java", "package com.sample; public class ClassB {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(fileA);
        ProjectFiles.insertFile(fileB);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        assertEquals(classARelations.get(0).getTargetComponentRelationMultiplicity().value(), DefaultClassMultiplicities.NONE.value());
    }

    @Test
    public void testCompositionRelationTypes() throws Exception {
        final ProjectFile fileA = new ProjectFile("ClassA.java", "package com.sample;" + " import java.util.ArrayList;"
                + " public class ClassA {  private ArrayList<ClassB> b;}");
        final ProjectFile fileB = new ProjectFile("ClassB.java", "package com.sample; public class ClassB {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(fileA);
        ProjectFiles.insertFile(fileB);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        DiagramComponent componentB = codeModel.component("com.sample.ClassB");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        assertEquals(classARelations.get(0).originalComponent(), componentA);
        assertEquals(classARelations.get(0).targetComponent(), componentB);
    }

    @Test
    public void testRelationDoesNotExist() throws Exception {
        final ProjectFile fileA = new ProjectFile("ClassA.java", "package com.sample;" + " import java.util.ArrayList;"
                + " public class ClassA {  private ArrayList<ClassB> b;}");
        final ProjectFile fileB = new ProjectFile("ClassB.java", "package com.sample; public class ClassB {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(fileA);
        ProjectFiles.insertFile(fileB);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentB = codeModel.component("com.sample.ClassB");
        List<ComponentRelation> classBRelations = relations.componentRelations(componentB);
        assertNull(classBRelations);
    }

    @Test
    public void testRealizationRelationExists() throws Exception {
        final ProjectFile file = new ProjectFile("ClassA.java",
                "package com.sample;" + " import java.util.ArrayList;" + " public class ClassA implements ClassC {}");
        final ProjectFile file2 = new ProjectFile("ClassC.java", "package com.sample; public interface ClassC {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(file);
        ProjectFiles.insertFile(file2);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        Assert.assertSame(classARelations.get(0).associationType(), ComponentAssociation.REALIZATION);
    }

    @Test
    public void testRealizationRelationMultiplicities() throws Exception {
        final ProjectFile file = new ProjectFile("ClassA.java",
                "package com.sample;" + " import java.util.ArrayList;" + " public class ClassA implements ClassC {}");
        final ProjectFile file2 = new ProjectFile("ClassC.java", "package com.sample; public interface ClassC {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(file);
        ProjectFiles.insertFile(file2);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        assertEquals(classARelations.get(0).getTargetComponentRelationMultiplicity().value(), DefaultClassMultiplicities.NONE.value());
    }

    @Test
    public void testRealizationRelationTypes() throws Exception {
        final ProjectFile file = new ProjectFile("ClassA.java",
                "package com.sample;" + " import java.util.ArrayList;" + " public class ClassA implements ClassC {}");
        final ProjectFile file2 = new ProjectFile("ClassC.java", "package com.sample; public interface ClassC {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(file);
        ProjectFiles.insertFile(file2);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        DiagramComponent componentC = codeModel.component("com.sample.ClassC");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        assertEquals(classARelations.get(0).originalComponent(), componentA);
        assertEquals(classARelations.get(0).targetComponent(), componentC);
    }

    @Test
    public void testSpecializationRelationExists() throws Exception {
        final ProjectFile file = new ProjectFile("ClassA.java",
                "package com.sample;" + " import java.util.ArrayList;" + "public class ClassA extends ClassD {}");
        final ProjectFile file2 = new ProjectFile("ClassD.java", "package com.sample; public class ClassD {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(file);
        ProjectFiles.insertFile(file2);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        Assert.assertSame(classARelations.get(0).associationType(), ComponentAssociation.SPECIALIZATION);
        assertEquals("com.sample.ClassD", classARelations.get(0).targetComponent().uniqueName());
    }

    @Test
    public void testSpecializationRelationMultiplicity() throws Exception {
        final ProjectFile file = new ProjectFile("ClassA.java",
                "package com.sample;" + " import java.util.ArrayList;" + "public class ClassA extends ClassD {}");
        final ProjectFile file2 = new ProjectFile("ClassD.java", "package com.sample; public class ClassD {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(file);
        ProjectFiles.insertFile(file2);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        assertEquals(classARelations.get(0).getTargetComponentRelationMultiplicity().value(), DefaultClassMultiplicities.NONE.value());
    }

    @Test
    public void testSpecializationRelationTypes() throws Exception {
        final ProjectFile file = new ProjectFile("ClassA.java",
                "package com.sample;" + " import java.util.ArrayList;" + "public class ClassA extends ClassD {}");
        final ProjectFile file2 = new ProjectFile("ClassD.java", "package com.sample; public class ClassD {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(file);
        ProjectFiles.insertFile(file2);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        DiagramComponent componentD = codeModel.component("com.sample.ClassD");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        assertEquals(classARelations.get(0).originalComponent(), componentA);
        assertEquals(classARelations.get(0).targetComponent(), componentD);
    }

    @Test
    public void testAssociationRelationExists() throws Exception {
        final ProjectFile file = new ProjectFile("ClassA.java",
                "package com.sample;" + "import java.util.ArrayList;" + "public class ClassA { ClassE aMethod() {}}");
        final ProjectFile file2 = new ProjectFile("ClassE.java", "package com.sample; public class ClassE {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(file);
        ProjectFiles.insertFile(file2);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        assertTrue(classARelations.size() == 1 && classARelations.get(0).associationType() ==
                ComponentAssociation.ASSOCIATION);
    }

    @Test
    public void testAssociationRelationTypes() throws Exception {
        final ProjectFile file = new ProjectFile("ClassA.java",
                "package com.sample;" + "import java.util.ArrayList;" + "public class ClassA { ClassE aMethod() {}}");
        final ProjectFile file2 = new ProjectFile("ClassE.java", "package com.sample; public class ClassE {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(file);
        ProjectFiles.insertFile(file2);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        DiagramComponent componentE = codeModel.component("com.sample.ClassE");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        assertEquals(classARelations.get(0).originalComponent(), componentA);
        assertEquals(classARelations.get(0).targetComponent(), componentE);
    }

    @Test
    public void testLocalVarAssociationRelationExists() throws Exception {
        final ProjectFile file = new ProjectFile("ClassA.java",
                "package com.sample;" + " import java.util.ArrayList;" + "class ClassA { void test () { ClassD d; } }");
        final ProjectFile file2 = new ProjectFile("ClassD.java", "package com.sample; public class ClassD {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(file);
        ProjectFiles.insertFile(file2);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        Assert.assertSame(classARelations.get(0).associationType(), ComponentAssociation.ASSOCIATION);
        assertEquals("com.sample.ClassD", classARelations.get(0).targetComponent().uniqueName());
    }

    @Test
    public void testLocalVarAssociationRelationMultiplicity() throws Exception {
        final ProjectFile file = new ProjectFile("ClassA.java",
                "package com.sample;" + " import java.util.ArrayList;" + "class ClassA { void test () { ClassD d; } }");
        final ProjectFile file2 = new ProjectFile("ClassD.java", "package com.sample; public class ClassD {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(file);
        ProjectFiles.insertFile(file2);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        assertEquals(classARelations.get(0).getTargetComponentRelationMultiplicity().value(), DefaultClassMultiplicities.NONE.value());
    }

    @Test
    public void testLocalVarAssociationRelationTypes() throws Exception {
        final ProjectFile file = new ProjectFile("ClassA.java",
                "package com.sample;" + " import java.util.ArrayList;" + "class ClassA { void test () { ClassD d; } }");
        final ProjectFile file2 = new ProjectFile("ClassD.java", "package com.sample; public class ClassD {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(file);
        ProjectFiles.insertFile(file2);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("com.sample.ClassA");
        DiagramComponent componentD = codeModel.component("com.sample.ClassD");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        assertEquals(classARelations.get(0).originalComponent(), componentA);
        assertEquals(classARelations.get(0).targetComponent(), componentD);
    }


    @Test
    public void testComponentRelationWithSameComponentsAreEqual() {
        Component cmpA = new Component();
        cmpA.setName("classA");
        cmpA.setComponentName("classA");
        cmpA.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        Component cmpB = new Component();
        cmpB.setName("classB");
        cmpB.setComponentName("classB");
        cmpB.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        ComponentRelation linkA = new ComponentRelation(new DiagramComponent(cmpA, new OOPSourceCodeModel()), new DiagramComponent(cmpB, new OOPSourceCodeModel()), null,
                ComponentAssociation.COMPOSITION);
        ComponentRelation linkB = new ComponentRelation(new DiagramComponent(cmpA, new OOPSourceCodeModel()), new DiagramComponent(cmpB, new OOPSourceCodeModel()), null,
                ComponentAssociation.COMPOSITION);
        assert (linkA.hashCode() == linkB.hashCode());
    }

    @Test
    public void testComponentRelationWithSameComponentsDifferentAssociationsAreEqual() {
        Component cmpA = new Component();
        cmpA.setName("classA");
        cmpA.setComponentName("classA");
        cmpA.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        Component cmpB = new Component();
        cmpB.setName("classB");
        cmpB.setComponentName("classB");
        cmpB.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        ComponentRelation linkA = new ComponentRelation(new DiagramComponent(cmpA, new OOPSourceCodeModel()), new DiagramComponent(cmpB, new OOPSourceCodeModel()), null,
                ComponentAssociation.REALIZATION);
        ComponentRelation linkB = new ComponentRelation(new DiagramComponent(cmpA, new OOPSourceCodeModel()), new DiagramComponent(cmpB, new OOPSourceCodeModel()), null,
                ComponentAssociation.ASSOCIATION);
        assert (linkA.hashCode() == linkB.hashCode());
    }

    @Test
    public void testComponentRelationWithDifferentComponentsAreUnequal() {
        Component cmpA = new Component();
        cmpA.setName("classA");
        cmpA.setComponentName("classA");
        cmpA.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        Component cmpB = new Component();
        cmpB.setName("classB");
        cmpB.setComponentName("classB");
        cmpB.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        Component cmpC = new Component();
        cmpC.setName("classC");
        cmpC.setComponentName("classC");
        cmpC.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        ComponentRelation linkA = new ComponentRelation(new DiagramComponent(cmpB, new OOPSourceCodeModel()), new DiagramComponent(cmpA, new OOPSourceCodeModel()), null,
                ComponentAssociation.COMPOSITION);
        ComponentRelation linkB = new ComponentRelation(new DiagramComponent(cmpB, new OOPSourceCodeModel()), new DiagramComponent(cmpC, new OOPSourceCodeModel()), null,
                ComponentAssociation.COMPOSITION);
        assert (linkA.hashCode() != linkB.hashCode());
    }

    /**
     * Tests that even in the presence of multiple references between a pair of components, there is a single relation
     * that represents the relationship between them in any given direction.
     */
    @Test
    public void testRelationOverridingBetweenComponentPairs() throws Exception {
        // Class A uses Class B in two areas (Return type and method param). There however should only be ONE
        // ASSOCIATION relation from ClassA -> ClassE
        final ProjectFile file = new ProjectFile("ClassA.java", "public class ClassA { ClassB classB1;\n void aMethod(ClassB classB2) {}}");
        final ProjectFile file2 = new ProjectFile("ClassB.java", "public class ClassB {}");
        final ProjectFiles ProjectFiles = new ProjectFiles(Lang.JAVA);
        ProjectFiles.insertFile(file);
        ProjectFiles.insertFile(file2);
        final DiagramCodeModel codeModel = new DiagramCodeModel(new ParsedProject(ProjectFiles).model());
        final ComponentRelations relations = new ComponentRelations(codeModel);
        DiagramComponent componentA = codeModel.component("ClassA");
        List<ComponentRelation> classARelations = relations.componentRelations(componentA);
        assertTrue(classARelations.size() == 1 && classARelations.get(0).associationType() ==
                ComponentAssociation.AGGREGATION);
    }
}
