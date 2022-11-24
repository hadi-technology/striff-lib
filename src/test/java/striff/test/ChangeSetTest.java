package striff.test;

import com.hadii.clarpse.reference.SimpleTypeReference;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.striff.ChangeSet;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.StriffCodeModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests to ensure Stiff Change Sets are generated correctly.
 */
public class ChangeSetTest {

    @Test
    public void testAddedFieldComponent() {
        final StriffCodeModel oldModel = new StriffCodeModel();
        DiagramComponent newDigramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.FIELD, "Animal.weight");
        final StriffCodeModel newModel = new StriffCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedFieldComponents() {
        DiagramComponent oldDiagramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.FIELD, "Animal.weight");
        StriffCodeModel oldModel = new StriffCodeModel(oldDiagramComponent);
        final StriffCodeModel newModel = new StriffCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedMethodComponent() {
        final StriffCodeModel oldModel = new StriffCodeModel();
        DiagramComponent newDigramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.METHOD, "Animal.test()");
        final StriffCodeModel newModel = new StriffCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedMethodComponents() {
        DiagramComponent oldDiagramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.METHOD, "Animal.test()");
        StriffCodeModel oldModel = new StriffCodeModel(oldDiagramComponent);
        final StriffCodeModel newModel = new StriffCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedConstructorComponent() {
        final StriffCodeModel oldModel = new StriffCodeModel();
        DiagramComponent newDigramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.CONSTRUCTOR, "Animal.test" +
                "()");
        final StriffCodeModel newModel = new StriffCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedConstructorComponents() {
        DiagramComponent oldDiagramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.CONSTRUCTOR, "Animal" +
                ".weight()");
        StriffCodeModel oldModel = new StriffCodeModel(oldDiagramComponent);
        final StriffCodeModel newModel = new StriffCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedClassComponent() {
        final StriffCodeModel oldModel = new StriffCodeModel();
        DiagramComponent newDigramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.CLASS, "Animal");
        final StriffCodeModel newModel = new StriffCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedClassComponents() {
        DiagramComponent oldDiagramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.CLASS, "Animal");
        StriffCodeModel oldModel = new StriffCodeModel(oldDiagramComponent);
        final StriffCodeModel newModel = new StriffCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedInterfaceComponent() {
        final StriffCodeModel oldModel = new StriffCodeModel();
        DiagramComponent newDigramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.INTERFACE, "Animal");
        final StriffCodeModel newModel = new StriffCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedInterfaceComponents() {
        DiagramComponent oldDiagramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.INTERFACE, "Animal");
        StriffCodeModel oldModel = new StriffCodeModel(oldDiagramComponent);
        final StriffCodeModel newModel = new StriffCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedStructComponent() {
        final StriffCodeModel oldModel = new StriffCodeModel();
        DiagramComponent newDigramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.FIELD, "Animal.weight");
        final StriffCodeModel newModel = new StriffCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedStructComponents() {
        DiagramComponent oldDiagramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.STRUCT, "Animal");
        StriffCodeModel oldModel = new StriffCodeModel(oldDiagramComponent);
        final StriffCodeModel newModel = new StriffCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedLocalVarComponent() {
        final StriffCodeModel oldModel = new StriffCodeModel();
        DiagramComponent newDigramComponent =
            setupDiagramComponent(
                OOPSourceModelConstants.ComponentType.LOCAL, "Animal.weight().test");
        final StriffCodeModel newModel = new StriffCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedLocalVarComponents() {
        DiagramComponent oldDiagramComponent =
            setupDiagramComponent(
                OOPSourceModelConstants.ComponentType.LOCAL, "Animal.weight().test");
        StriffCodeModel oldModel = new StriffCodeModel(oldDiagramComponent);
        final StriffCodeModel newModel = new StriffCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedMethodParamComponent() {
        final StriffCodeModel oldModel = new StriffCodeModel();
        DiagramComponent newDigramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.METHOD_PARAMETER_COMPONENT,
                                  "Animal.weight().test");
        final StriffCodeModel newModel = new StriffCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedMethodParamComponents() {
        DiagramComponent oldDiagramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.METHOD_PARAMETER_COMPONENT,
                                  "Animal.weight().test");
        StriffCodeModel oldModel = new StriffCodeModel(oldDiagramComponent);
        final StriffCodeModel newModel = new StriffCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testDeletedComponents() {
        DiagramComponent oldDiagramComponent =
            setupDiagramComponent(OOPSourceModelConstants.ComponentType.FIELD,
                                  "Animal.test");
        StriffCodeModel oldModel = new StriffCodeModel(oldDiagramComponent);
        final StriffCodeModel newModel = new StriffCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedRelations() {
        Component oldStrawBerryComponent = new Component();
        oldStrawBerryComponent.setComponentName("Strawberry");
        oldStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);

        Component oldAnimalFieldComponent = new Component();
        oldAnimalFieldComponent.setComponentName("Animal.weight");
        oldAnimalFieldComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);

        Component oldAnimalClassComponent = new Component();
        oldAnimalClassComponent.setComponentName("Animal");
        oldAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);

        StriffCodeModel oldModel = new StriffCodeModel(
                new DiagramComponent(oldAnimalFieldComponent, null),
                new DiagramComponent(oldAnimalClassComponent, null),
                new DiagramComponent(oldStrawBerryComponent, null)
        );

        Component newStrawBerryComponent = new Component();
        newStrawBerryComponent.setComponentName("Strawberry");
        newStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);

        Component newAnimalFieldComponent = new Component();
        newAnimalFieldComponent.setComponentName("Animal.weight");
        newAnimalFieldComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        // New Reference to StrawBerry Component
        newAnimalFieldComponent.insertCmpRef(new SimpleTypeReference("Strawberry"));

        Component newAnimalClassComponent = new Component();
        newAnimalClassComponent.setComponentName("Animal");
        newAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        // New Reference to StrawBerry Component
        newAnimalClassComponent.insertCmpRef(new SimpleTypeReference("Strawberry"));

        StriffCodeModel newModel = new StriffCodeModel(
                new DiagramComponent(newAnimalFieldComponent, null),
                new DiagramComponent(newAnimalClassComponent, null),
                new DiagramComponent(newStrawBerryComponent, null)
        );

        assertEquals(new ChangeSet(oldModel, newModel).addedRelations().allRels().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedRelations().hasRels(
            new DiagramComponent(newAnimalClassComponent, null)));
    }

    @Test
    public void testDeletedRelations() {
        Component oldStrawBerryComponent = new Component();
        oldStrawBerryComponent.setComponentName("Strawberry");
        oldStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);

        Component oldAnimalFieldComponent = new Component();
        oldAnimalFieldComponent.setComponentName("Animal.weight");
        oldAnimalFieldComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        oldAnimalFieldComponent.insertCmpRef(new SimpleTypeReference("Strawberry"));

        Component oldAnimalClassComponent = new Component();
        oldAnimalClassComponent.setComponentName("Animal");
        oldAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        oldAnimalClassComponent.insertCmpRef(new SimpleTypeReference("Strawberry"));

        StriffCodeModel oldModel = new StriffCodeModel(
                new DiagramComponent(oldAnimalFieldComponent, null),
                new DiagramComponent(oldAnimalClassComponent, null),
                new DiagramComponent(oldStrawBerryComponent, null)
        );

        Component newStrawBerryComponent = new Component();
        newStrawBerryComponent.setComponentName("Strawberry");
        newStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);

        Component newAnimalFieldComponent = new Component();
        newAnimalFieldComponent.setComponentName("Animal.weight");
        newAnimalFieldComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        // New component does not have reference to StrawBerry Component

        Component newAnimalClassComponent = new Component();
        newAnimalClassComponent.setComponentName("Animal");
        newAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        // New component does not have reference to StrawBerry Component

        StriffCodeModel newModel = new StriffCodeModel(
                new DiagramComponent(newAnimalFieldComponent, null),
                new DiagramComponent(newAnimalClassComponent, null),
                new DiagramComponent(newStrawBerryComponent, null)
        );

        assertEquals(new ChangeSet(oldModel, newModel).deletedRelations().allRels().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedRelations().hasRels(
            new DiagramComponent(oldAnimalClassComponent, null)));
    }

    @Test
    public void testKeyContextComponents() {
        Component oldStrawBerryComponent = new Component();
        oldStrawBerryComponent.setComponentName("Strawberry");
        oldStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        Component oldAnimalFieldComponent = new Component();
        oldAnimalFieldComponent.setComponentName("Animal.weight");
        oldAnimalFieldComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        oldAnimalFieldComponent.insertCmpRef(new SimpleTypeReference("Strawberry"));
        Component oldAnimalClassComponent = new Component();
        oldAnimalClassComponent.setComponentName("Animal");
        oldAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        oldAnimalClassComponent.insertCmpRef(new SimpleTypeReference("Strawberry"));
        StriffCodeModel oldModel = new StriffCodeModel(
                new DiagramComponent(oldAnimalFieldComponent, null),
                new DiagramComponent(oldAnimalClassComponent, null),
                new DiagramComponent(oldStrawBerryComponent, null)
        );
        Component newStrawBerryComponent = new Component();
        newStrawBerryComponent.setComponentName("Strawberry");
        newStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        Component newAnimalFieldComponent = new Component();
        newAnimalFieldComponent.setComponentName("Animal.weight");
        newAnimalFieldComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        // New component does not have reference to StrawBerry Component
        Component newAnimalClassComponent = new Component();
        newAnimalClassComponent.setComponentName("Animal");
        newAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        // New component does not have reference to StrawBerry Component
        StriffCodeModel newModel = new StriffCodeModel(
                new DiagramComponent(newAnimalFieldComponent, null),
                new DiagramComponent(newAnimalClassComponent, null),
                new DiagramComponent(newStrawBerryComponent, null)
        );
        assertEquals(new ChangeSet(oldModel, newModel).keyRelationsComponents().size(), 2);
        assertTrue(new ChangeSet(oldModel, newModel).keyRelationsComponents().contains(
                new DiagramComponent(newAnimalClassComponent, null)
        ));
        assertTrue(new ChangeSet(oldModel, newModel).keyRelationsComponents().contains(
                new DiagramComponent(newStrawBerryComponent, null)
        ));
    }

    @Test
    public void testRelationComponentsMustExist() {
        StriffCodeModel oldModel = new StriffCodeModel();
        Component newAnimalFieldComponent = new Component();
        newAnimalFieldComponent.setComponentName("Animal.weight");
        newAnimalFieldComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        // Note strawberry component does not exist in this code base
        newAnimalFieldComponent.insertCmpRef(new SimpleTypeReference("Strawberry"));

        Component newAnimalClassComponent = new Component();
        newAnimalClassComponent.setComponentName("Animal");
        newAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        // Note strawberry component does not exist in this code base
        newAnimalClassComponent.insertCmpRef(new SimpleTypeReference("Strawberry"));

        StriffCodeModel newModel = new StriffCodeModel(
                new DiagramComponent(newAnimalFieldComponent, null),
                new DiagramComponent(newAnimalClassComponent, null)
        );
        assertEquals(new ChangeSet(oldModel, newModel).addedRelations().allRels().size(), 0);
    }

    private DiagramComponent setupDiagramComponent(OOPSourceModelConstants.ComponentType type,
                                                   String cmpName) {
        Component newComponent = new Component();
        newComponent.setComponentName(cmpName);
        newComponent.setComponentType(type);
        return new DiagramComponent(newComponent, null);
    }
}
