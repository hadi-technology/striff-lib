package claritybot.test;

import com.clarity.binary.diagram.DiagramComponent;
import com.clarity.binary.diagram.DiagramConstants.BinaryClassAssociation;
import com.clarity.binary.extractor.BinaryClassRelationship;
import com.clarity.binary.extractor.ExternalClassLink;
import com.clarity.sourcemodel.Component;
import com.clarity.sourcemodel.OOPSourceCodeModel;
import com.clarity.sourcemodel.OOPSourceModelConstants;
import org.junit.Test;

public class BinaryClassRelationshipTest {

    @Test
    public void equalBinaryClassRelationshipsTest() {
        Component cmpA = new Component();
        cmpA.setName("classA");
        cmpA.setComponentName("classA");
        cmpA.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        Component cmpB = new Component();
        cmpB.setName("classB");
        cmpB.setComponentName("classB");
        cmpB.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        ExternalClassLink link = new ExternalClassLink(new DiagramComponent(cmpA, new OOPSourceCodeModel()), new DiagramComponent(cmpB, new OOPSourceCodeModel()), null, null,
                BinaryClassAssociation.COMPOSITION);
        BinaryClassRelationship relA = new BinaryClassRelationship(link);
        BinaryClassRelationship relB = new BinaryClassRelationship(link);
        assert (relA.hashCode() == relB.hashCode());
    }

    @Test
    public void unEqualBinaryClassRelationshipsByName() {
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
        ExternalClassLink link = new ExternalClassLink(new DiagramComponent(cmpB, new OOPSourceCodeModel()), new DiagramComponent(cmpA, new OOPSourceCodeModel()), null, null,
                BinaryClassAssociation.COMPOSITION);
        BinaryClassRelationship relA = new BinaryClassRelationship(link);
        ExternalClassLink link2 = new ExternalClassLink(new DiagramComponent(cmpA, new OOPSourceCodeModel()), new DiagramComponent(cmpC, new OOPSourceCodeModel()), null, null,
                BinaryClassAssociation.COMPOSITION);
        BinaryClassRelationship relB = new BinaryClassRelationship(link2);
        assert (relA.hashCode() != relB.hashCode());
    }

    @Test
    public void unequalBinaryClassRelationshipsByAssociation() {
        Component cmpA = new Component();
        cmpA.setName("classA");
        cmpA.setComponentName("classA");
        cmpA.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        Component cmpB = new Component();
        cmpB.setName("classB");
        cmpB.setComponentName("classB");
        cmpB.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        ExternalClassLink link = new ExternalClassLink(new DiagramComponent(cmpB, new OOPSourceCodeModel()), new DiagramComponent(cmpA, new OOPSourceCodeModel()), null, null,
                BinaryClassAssociation.COMPOSITION);
        BinaryClassRelationship relA = new BinaryClassRelationship(link);
        ExternalClassLink link2 = new ExternalClassLink(new DiagramComponent(cmpB, new OOPSourceCodeModel()), new DiagramComponent(cmpA, new OOPSourceCodeModel()), null, null,
                BinaryClassAssociation.AGGREGATION);
        BinaryClassRelationship relB = new BinaryClassRelationship(link2);
        assert (relA.hashCode() != relB.hashCode());
    }
}
