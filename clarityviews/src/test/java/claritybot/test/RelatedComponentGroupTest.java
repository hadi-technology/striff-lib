package claritybot.test;

import com.clarity.binary.diagram.DiagramComponent;
import com.clarity.binary.diagram.DiagramSourceCodeModel;
import com.clarity.binary.diagram.RelatedBaseComponentsGroup;
import com.clarity.binary.extractor.BinaryClassRelationship;
import com.clarity.binary.extractor.BinaryClassRelationshipExtractor;
import com.clarity.binary.parse.ParsedProject;
import com.clarity.compiler.Lang;
import com.clarity.compiler.RawFile;
import com.clarity.compiler.SourceFiles;
import com.clarity.sourcemodel.Component;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * The {@link RelatedBaseComponentsGroup} class is used to determine what
 * {@link Component}s to be displayed on a diagram. These tests exist to ensure
 * the optimal {@link Component}s are being chosen based on the given code base
 * and desired.
 */
public class RelatedComponentGroupTest {

    @Test
    public void testA() throws Exception {
        final RawFile file = new RawFile("ClassA.java", "package com.sample;" + "import java.util.ArrayList;"
                + "public class ClassA {  private ArrayList<ClassB> b;}");
        final RawFile file2 = new RawFile("ClassB.java",
                "package com.sample; public class ClassB implements ClassD {}");
        final RawFile file3 = new RawFile("ClassD.java", "package com.sample; public class ClassD  {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);
        final ArrayList<SourceFiles> reqCons = new ArrayList<>();
        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCon.insertFile(file3);
        reqCons.add(reqCon);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(reqCon).model());
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(codeModel);
        assertTrue(new RelatedBaseComponentsGroup(codeModel.getComponents(), binaryRelationships,
                codeModel.getComponent("com.sample.ClassA")).components()
                        .contains(codeModel.getComponent("com.sample.ClassB")));
    }

    @Test
    public void testB() throws Exception {
        final RawFile file = new RawFile("ClassA.java", "package com.sample;" + "import java.util.ArrayList;"
                + "public class ClassA {  private ArrayList<ClassB> b;}");
        final RawFile file2 = new RawFile("ClassB.java",
                "package com.sample; public class ClassB implements ClassD {}");
        final RawFile file3 = new RawFile("ClassD.java", "package com.sample; public class ClassD  {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);
        final ArrayList<SourceFiles> reqCons = new ArrayList<SourceFiles>();
        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCon.insertFile(file3);
        reqCons.add(reqCon);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(reqCon).model());
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(codeModel);
        Set<DiagramComponent> cmps = new RelatedBaseComponentsGroup(codeModel.getComponents(), binaryRelationships,
                codeModel.getComponent("com.sample.ClassA")).components();
        assertTrue(cmps.contains(codeModel.getComponent("com.sample.ClassD")));
        assertTrue(cmps.size() == 3);
    }

    @Test
    public void testC() throws Exception {
        final RawFile file = new RawFile("ClassA.java", "package com.sample;" + "import java.util.ArrayList;"
                + "public class ClassA {  private ArrayList<ClassB> b;}");
        final RawFile file2 = new RawFile("ClassB.java", "package com.sample; public class ClassB extends ClassD {}");
        final RawFile file3 = new RawFile("ClassD.java", "package com.sample; public class ClassD  {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);
        final ArrayList<SourceFiles> reqCons = new ArrayList<SourceFiles>();
        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCon.insertFile(file3);
        reqCons.add(reqCon);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(reqCon).model());
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(codeModel);
        Set<DiagramComponent> cmps = new RelatedBaseComponentsGroup(codeModel.getComponents(), binaryRelationships,
                codeModel.getComponent("com.sample.ClassA")).components();
        assertTrue(cmps.contains(codeModel.getComponent("com.sample.ClassD")));
        assertTrue(cmps.size() == 3);
    }

    @Test
    public void testD() throws Exception {
        final RawFile file = new RawFile("ClassA.java", "package com.sample;" + " import java.util.ArrayList;"
                + "public class ClassA implements ClassD {  private ArrayList<ClassB> b;}");
        final RawFile file2 = new RawFile("ClassB.java", "package com.sample; public class ClassB {}");
        final RawFile file3 = new RawFile("ClassD.java", "package com.sample; public class ClassD  {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);
        final ArrayList<SourceFiles> reqCons = new ArrayList<SourceFiles>();
        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCon.insertFile(file3);
        reqCons.add(reqCon);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(reqCon).model());
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(codeModel);
        Set<DiagramComponent> cmps = new RelatedBaseComponentsGroup(codeModel.getComponents(), binaryRelationships,
                codeModel.getComponent("com.sample.ClassA")).components();
        assertTrue(cmps.contains(codeModel.getComponent("com.sample.ClassD")));
        assertTrue(cmps.size() == 3);
    }

    @Test
    public void testE() throws Exception {
        final RawFile file = new RawFile("ClassA.java", "package com.sample;" + "import java.util.ArrayList;"
                + "public class ClassA {  private ArrayList<ClassB> b;}");
        final RawFile file2 = new RawFile("ClassB.java", "package com.sample; public class ClassB {}");
        final RawFile file3 = new RawFile("ClassD.java",
                "package com.sample; public class ClassD  { private ClassA classA;}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);
        final ArrayList<SourceFiles> reqCons = new ArrayList<SourceFiles>();
        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCon.insertFile(file3);
        reqCons.add(reqCon);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(reqCon).model());
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(codeModel);
        Set<DiagramComponent> cmps = new RelatedBaseComponentsGroup(codeModel.getComponents(), binaryRelationships,
                codeModel.getComponent("com.sample.ClassA")).components();
        assertTrue(cmps.contains(codeModel.getComponent("com.sample.ClassD")));
        assertTrue(cmps.contains(codeModel.getComponent("com.sample.ClassB")));
        assertTrue(cmps.size() == 3);
    }

    @Test
    public void testF() throws Exception {
        final RawFile file = new RawFile("ClassA.java",
                "package com.sample;" + "public class ClassA implements ClassD { }");
        final RawFile file2 = new RawFile("ClassB.java",
                "package com.sample; public class ClassB implements ClassA {}");
        final RawFile file3 = new RawFile("ClassD.java", "package com.sample; public class ClassD  { }");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);
        final ArrayList<SourceFiles> reqCons = new ArrayList<SourceFiles>();
        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCon.insertFile(file3);
        reqCons.add(reqCon);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(reqCon).model());
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(codeModel);
        Set<DiagramComponent> cmps = new RelatedBaseComponentsGroup(codeModel.getComponents(), binaryRelationships,
                codeModel.getComponent("com.sample.ClassA")).components();
        assertTrue(cmps.contains(codeModel.getComponent("com.sample.ClassD")));
        assertTrue(cmps.contains(codeModel.getComponent("com.sample.ClassB")));
        assertTrue(cmps.size() == 3);
    }

    @Test
    public void testG() throws Exception {
        final RawFile file = new RawFile("ClassA.java",
                "package com.sample;" + "public class ClassA implements ClassD { }");
        final RawFile file2 = new RawFile("ClassB.java", "package com.sample; public class ClassB extends ClassA {}");
        final RawFile file3 = new RawFile("ClassD.java", "package com.sample; public class ClassD  { }");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);
        final ArrayList<SourceFiles> reqCons = new ArrayList<SourceFiles>();
        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCon.insertFile(file3);
        reqCons.add(reqCon);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(reqCon).model());
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(codeModel);
        Set<DiagramComponent> cmps = new RelatedBaseComponentsGroup(codeModel.getComponents(), binaryRelationships,
                codeModel.getComponent("com.sample.ClassA")).components();
        assertTrue(cmps.contains(codeModel.getComponent("com.sample.ClassD")));
        assertTrue(cmps.contains(codeModel.getComponent("com.sample.ClassB")));
        assertTrue(cmps.size() == 3);
    }

    @Test
    public void testH() throws Exception {
        final RawFile file = new RawFile("ClassA.java", "package com.sample;" + " public class ClassA { }");
        final RawFile file2 = new RawFile("ClassB.java", "package com.sample; public class ClassB {}");
        final RawFile file3 = new RawFile("ClassD.java", "package com.sample; public class ClassD  {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);
        final ArrayList<SourceFiles> reqCons = new ArrayList<SourceFiles>();
        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCon.insertFile(file3);
        reqCons.add(reqCon);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(reqCon).model());
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(codeModel);
        Set<DiagramComponent> cmps = new RelatedBaseComponentsGroup(codeModel.getComponents(), binaryRelationships,
                codeModel.getComponent("com.sample.ClassA")).components();
        assertTrue(cmps.contains(codeModel.getComponent("com.sample.ClassA")));
        assertTrue(cmps.size() == 1);
    }

    @Test
    public void componentsMentionedThroughDocsArePlacedOnDiagram() throws Exception {
        final RawFile file = new RawFile("ClassA.java", "package com.sample;" + "\n /**\n {@link ClassB classB  } \n */ \n public class ClassA { }");
        final RawFile file2 = new RawFile("ClassB.java", "package com.sample; public class ClassB {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);
        final ArrayList<SourceFiles> reqCons = new ArrayList<SourceFiles>();
        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCons.add(reqCon);
        final DiagramSourceCodeModel codeModel = new DiagramSourceCodeModel(new ParsedProject(reqCon).model());
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(codeModel);
        Set<DiagramComponent> cmps = new RelatedBaseComponentsGroup(codeModel.getComponents(), binaryRelationships,
                codeModel.getComponent("com.sample.ClassA")).components();
        assertTrue(cmps.contains(codeModel.getComponent("com.sample.ClassB")));
        assertTrue(cmps.size() == 2);
    }
}
