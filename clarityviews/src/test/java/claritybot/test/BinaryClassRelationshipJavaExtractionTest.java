package claritybot.test;

import com.clarity.binary.diagram.DiagramConstants.BinaryClassAssociation;
import com.clarity.binary.diagram.DiagramConstants.DefaultClassMultiplicities;
import com.clarity.binary.diagram.DiagramSourceCodeModel;
import com.clarity.binary.extractor.BinaryClassRelationship;
import com.clarity.binary.extractor.BinaryClassRelationshipExtractor;
import com.clarity.binary.parse.ParsedProject;
import com.clarity.compiler.Lang;
import com.clarity.compiler.RawFile;
import com.clarity.compiler.SourceFiles;
import com.clarity.sourcemodel.OOPSourceCodeModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Tests to ensure binary class relationships are being extracted properly.
 */
public class BinaryClassRelationshipJavaExtractionTest {

    private BinaryClassRelationship getRelationship(List<BinaryClassRelationship> relations, String classAUniqueName, String classB) throws Exception {
        for (BinaryClassRelationship br : relations) {
            if (br.getClassA().uniqueName().equals(classAUniqueName) && br.getClassB().uniqueName().equals(classB)) {
                return br;
            }
            if (br.getClassB().uniqueName().equals(classAUniqueName) && br.getClassA().uniqueName().equals(classB)) {
                return br;
            }
        }
        throw new Exception("Binary Relationship does not exist!");
    }
    @Test
    public void testFieldVarLevelRelationExists() throws Exception {
        final RawFile file = new RawFile("ClassA.java", "package com.sample;" + "import java.util.ArrayList;"
                + "public class ClassA {  private ArrayList<ClassB> b;}");
        final RawFile file2 = new RawFile("ClassB.java", "package com.sample; public class ClassB {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);

        reqCon.insertFile(file);
        reqCon.insertFile(file2);

        final OOPSourceCodeModel codeModel = new ParsedProject(reqCon).model();
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(new DiagramSourceCodeModel(codeModel));
        Assert.assertTrue("Relationship between ClassA and ClassB does not exist!", getRelationship(binaryRelationships, "com.sample.ClassA", "com.sample.ClassB") != null);
    }

    @Test
    public void testFieldVarLevelRelationTypes() throws Exception {
        final RawFile file = new RawFile("ClassA.java", "package com.sample;" + "import java.util.ArrayList;"
                + "public class ClassA {  private ArrayList<ClassB> b;}");
        final RawFile file2 = new RawFile("ClassB.java", "package com.sample; public class ClassB {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);

        reqCon.insertFile(file);
        reqCon.insertFile(file2);

        final OOPSourceCodeModel codeModel = new ParsedProject(reqCon).model();
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(new DiagramSourceCodeModel(codeModel));
        final BinaryClassRelationship A_B = getRelationship(binaryRelationships, "com.sample.ClassA", "com.sample.ClassB");
        Assert.assertTrue("Relationship from ClassA to ClassB should be 'Composition'!",
                A_B.getaSideAssociation() == BinaryClassAssociation.COMPOSITION);
        Assert.assertTrue("Relationship from ClassB to ClassA should be 'None'!",
                A_B.getbSideAssociation() == BinaryClassAssociation.NONE);
    }

    @Test
    public void testInterfaceLevelRelationExists() throws Exception {
        final RawFile file = new RawFile("ClassA.java",
                "package com.sample;" + "import java.util.ArrayList;" + "public class ClassA implements ClassC {}");
        final RawFile file2 = new RawFile("ClassC.java", "package com.sample; public interface ClassC {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);

        reqCon.insertFile(file);
        reqCon.insertFile(file2);

        final OOPSourceCodeModel codeModel = new ParsedProject(reqCon).model();
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(new DiagramSourceCodeModel(codeModel));
        final BinaryClassRelationship A_C = getRelationship(binaryRelationships, "com.sample.ClassA", "com.sample.ClassC");
        Assert.assertTrue("Relationship from ClassA to interfaceC should be 'Realization'",
                A_C.getaSideAssociation() == BinaryClassAssociation.REALIZATION);
        Assert.assertTrue("Relationship from InterfaceC to ClassA should be 'None'",
                A_C.getbSideAssociation() == BinaryClassAssociation.NONE);
    }

    @Test
    public void testInterfaceLevelRelationMultiplicities() throws Exception {
        final RawFile file = new RawFile("ClassA.java",
                "package com.sample;" + "import java.util.ArrayList;" + "public class ClassA implements ClassC {}");
        final RawFile file2 = new RawFile("ClassC.java", "package com.sample; public interface ClassC {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);

        reqCon.insertFile(file);
        reqCon.insertFile(file2);

        final OOPSourceCodeModel codeModel = new ParsedProject(reqCon).model();
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(new DiagramSourceCodeModel(codeModel));
        final BinaryClassRelationship A_C = getRelationship(binaryRelationships, "com.sample.ClassA", "com.sample.ClassC");
        Assert.assertTrue("Multiciplicity from ClassA to InterfaceC should be '0..0'!",
                A_C.getaSideMultiplicity().getValue().equals(DefaultClassMultiplicities.NONE.getValue()));
        Assert.assertTrue("Multiciplicity from InterfaceC to Class A should be '0..0'!",
                A_C.getbSideMultiplicity().getValue().equals(DefaultClassMultiplicities.NONE.getValue()));
    }

    @Test
    public void testExtensionLevelRelationExists() throws Exception {
        final RawFile file = new RawFile("ClassA.java",
                "package com.sample;" + " import java.util.ArrayList;" + "public class ClassA extends ClassD {}");
        final RawFile file2 = new RawFile("ClassD.java", "package com.sample; public class ClassD {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);

        reqCon.insertFile(file);
        reqCon.insertFile(file2);

        final OOPSourceCodeModel codeModel = new ParsedProject(reqCon).model();
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(new DiagramSourceCodeModel(codeModel));
        final BinaryClassRelationship A_D = getRelationship(binaryRelationships, "com.sample.ClassA", "com.sample.ClassD");
        Assert.assertTrue("Relationship between ClassA and ClassD does not exist!", A_D != null);
    }

    @Test
    public void testExtensionLevelRelationTypes() throws Exception {
        final RawFile file = new RawFile("ClassA.java", "package com.sample;  public class ClassA extends ClassD {}");
        final RawFile file2 = new RawFile("ClassD.java", "package com.sample; public class ClassD {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);

        reqCon.insertFile(file);
        reqCon.insertFile(file2);

        final OOPSourceCodeModel codeModel = new ParsedProject(reqCon).model();
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(new DiagramSourceCodeModel(codeModel));
        final BinaryClassRelationship A_D = getRelationship(binaryRelationships, "com.sample.ClassA", "com.sample.ClassD");
        Assert.assertTrue("Relationship from ClassA to ClassD should be 'Generalization'!",
                A_D.getaSideAssociation() == BinaryClassAssociation.GENERALISATION);
        Assert.assertTrue("Relationship from ClassD to ClassA should be 'Generalization'!",
                A_D.getbSideAssociation() == BinaryClassAssociation.NONE);
    }

    @Test
    public void testExtensionLevelRelationMultiplicities() throws Exception {
        final RawFile file = new RawFile("ClassA.java",
                "package com.sample;" + "import java.util.ArrayList;" + "public class ClassA extends ClassD {}");
        final RawFile file2 = new RawFile("ClassD.java", "package com.sample; public class ClassD {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);

        file.name("sample.java");
        reqCon.insertFile(file);
        reqCon.insertFile(file2);

        final OOPSourceCodeModel codeModel = new ParsedProject(reqCon).model();
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(new DiagramSourceCodeModel(codeModel));
        final BinaryClassRelationship A_D = getRelationship(binaryRelationships, "com.sample.ClassA", "com.sample.ClassD");
        Assert.assertTrue("Multiciplicity from ClassA to ClassD should be 'None'!",
                A_D.getaSideMultiplicity().getValue().equals(DefaultClassMultiplicities.NONE.getValue()));
        Assert.assertTrue("Multiciplicity from ClassD to Class A should be 'None'!",
                A_D.getbSideMultiplicity().getValue().equals(DefaultClassMultiplicities.NONE.getValue()));
    }

    @Test
    public void testMethodReturnTypeLevelRelationMultiplicities() throws Exception {
        final RawFile file = new RawFile("ClassA.java",
                "package com.sample;" + "import java.util.ArrayList;" + "public class ClassA { ClassE aMethod() {}}");
        final RawFile file2 = new RawFile("ClassE.java", "package com.sample; public class ClassE {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);

        file.name("sample.java");
        reqCon.insertFile(file);
        reqCon.insertFile(file2);

        final OOPSourceCodeModel codeModel = new ParsedProject(reqCon).model();
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(new DiagramSourceCodeModel(codeModel));
        final BinaryClassRelationship A_E = getRelationship(binaryRelationships, "com.sample.ClassA", "com.sample.ClassE");
        Assert.assertTrue(A_E.getaSideMultiplicity().getValue()
                .equals(com.clarity.binary.diagram.DiagramConstants.DefaultClassMultiplicities.NONE.getValue()));
        Assert.assertTrue(A_E.getbSideMultiplicity().getValue()
                .equals(com.clarity.binary.diagram.DiagramConstants.DefaultClassMultiplicities.NONE.getValue()));
    }

    @Test
    public void testMethodReturnTypeLevelRelationTypes() throws Exception {
        final RawFile file = new RawFile("ClassA.java",
                "package com.sample;" + "import java.util.ArrayList;" + "public class ClassA { ClassE aMethod() {}}");
        final RawFile file2 = new RawFile("ClassE.java", "package com.sample; public class ClassE {}");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);

        file.name("sample.java");
        reqCon.insertFile(file);
        reqCon.insertFile(file2);

        final OOPSourceCodeModel codeModel = new ParsedProject(reqCon).model();
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(new DiagramSourceCodeModel(codeModel));
        final BinaryClassRelationship A_E = getRelationship(binaryRelationships, "com.sample.ClassA", "com.sample.ClassE");
        Assert.assertTrue("Multiciplicity from ClassA to ClassE should be 'None'!",
                A_E.getaSideAssociation() == BinaryClassAssociation.ASSOCIATION);
        Assert.assertTrue("Multiciplicity from ClassA to ClassE should be 'None'!",
                A_E.getbSideAssociation() == BinaryClassAssociation.NONE);
    }

    /**
     * relationships from overridden methods should not be displayed as the
     * relation is already clear from the original implemented/extended class.
     */
    @Test
    public void testNoRelationsGeneratedFromOverridenMethods() throws Exception {
        final RawFile file = new RawFile("ClassA.java", "package test; public interface ClassA { ClassD aMethod();}");
        final RawFile file2 = new RawFile("ClassE.java",
                "package test; public class ClassE implements ClassA { public ClassD aMethod () {} }");
        final RawFile file3 = new RawFile("ClassD.java", "package test; public class ClassD { }");
        final SourceFiles reqCon = new SourceFiles(Lang.JAVA);

        reqCon.insertFile(file);
        reqCon.insertFile(file2);
        reqCon.insertFile(file3);

        final OOPSourceCodeModel codeModel = new ParsedProject((reqCon)).model();
        final BinaryClassRelationshipExtractor<?> bCAS = new BinaryClassRelationshipExtractor<>();
        final List<BinaryClassRelationship> binaryRelationships = bCAS
                .generateBinaryClassRelationships(new DiagramSourceCodeModel(codeModel));
        // relationship from class E to class D should not be shown because
        // class E implements class A which already has that relationship
        assertTrue(getRelationship(binaryRelationships, "test.ClassA", "test.ClassD") != null);
        assertTrue(getRelationship(binaryRelationships, "test.ClassE", "test.ClassD") == null);
    }

}
