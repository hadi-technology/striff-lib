package stiff.test;

import com.hadii.clarpse.reference.SimpleTypeReference;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.stiff.ChangeSet;
import com.hadii.stiff.diagram.DiagramComponent;
import com.hadii.stiff.diagram.DiagramCodeModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests to ensure Striff Change Sets are generated correctly.
 */
public class ChangeSetTest {

    @Test
    public void testAddedFieldComponent() {
        final DiagramCodeModel oldModel = new DiagramCodeModel();
        Component newComponent = new Component();
        newComponent.setComponentName("Animal.weight");
        newComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        DiagramComponent newDigramComponent = new DiagramComponent(newComponent, null);
        final DiagramCodeModel newModel = new DiagramCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedFieldComponents() {
        Component oldComponent = new Component();
        oldComponent.setComponentName("Animal.weight");
        oldComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        DiagramComponent oldDiagramComponent = new DiagramComponent(oldComponent, null);
        DiagramCodeModel oldModel = new DiagramCodeModel(oldDiagramComponent);
        final DiagramCodeModel newModel = new DiagramCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedMethodComponent() {
        final DiagramCodeModel oldModel = new DiagramCodeModel();
        Component newComponent = new Component();
        newComponent.setComponentName("Animal.weight");
        newComponent.setComponentType(OOPSourceModelConstants.ComponentType.METHOD);
        DiagramComponent newDigramComponent = new DiagramComponent(newComponent, null);
        final DiagramCodeModel newModel = new DiagramCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedMethodComponents() {
        Component oldComponent = new Component();
        oldComponent.setComponentName("Animal.weight");
        oldComponent.setComponentType(OOPSourceModelConstants.ComponentType.METHOD);
        DiagramComponent oldDiagramComponent = new DiagramComponent(oldComponent, null);
        DiagramCodeModel oldModel = new DiagramCodeModel(oldDiagramComponent);
        final DiagramCodeModel newModel = new DiagramCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedConstructorComponent() {
        final DiagramCodeModel oldModel = new DiagramCodeModel();
        Component newComponent = new Component();
        newComponent.setComponentName("Animal.weight");
        newComponent.setComponentType(OOPSourceModelConstants.ComponentType.CONSTRUCTOR);
        DiagramComponent newDigramComponent = new DiagramComponent(newComponent, null);
        final DiagramCodeModel newModel = new DiagramCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedConstructorComponents() {
        Component oldComponent = new Component();
        oldComponent.setComponentName("Animal.weight");
        oldComponent.setComponentType(OOPSourceModelConstants.ComponentType.CONSTRUCTOR);
        DiagramComponent oldDiagramComponent = new DiagramComponent(oldComponent, null);
        DiagramCodeModel oldModel = new DiagramCodeModel(oldDiagramComponent);
        final DiagramCodeModel newModel = new DiagramCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedClassComponent() {
        final DiagramCodeModel oldModel = new DiagramCodeModel();
        Component newComponent = new Component();
        newComponent.setComponentName("Animal.weight");
        newComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        DiagramComponent newDigramComponent = new DiagramComponent(newComponent, null);
        final DiagramCodeModel newModel = new DiagramCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedClassComponents() {
        Component oldComponent = new Component();
        oldComponent.setComponentName("Animal.weight");
        oldComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        DiagramComponent oldDiagramComponent = new DiagramComponent(oldComponent, null);
        DiagramCodeModel oldModel = new DiagramCodeModel(oldDiagramComponent);
        final DiagramCodeModel newModel = new DiagramCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedInterfaceComponent() {
        final DiagramCodeModel oldModel = new DiagramCodeModel();
        Component newComponent = new Component();
        newComponent.setComponentName("Animal.weight");
        newComponent.setComponentType(OOPSourceModelConstants.ComponentType.INTERFACE);
        DiagramComponent newDigramComponent = new DiagramComponent(newComponent, null);
        final DiagramCodeModel newModel = new DiagramCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedInterfaceComponents() {
        Component oldComponent = new Component();
        oldComponent.setComponentName("Animal.weight");
        oldComponent.setComponentType(OOPSourceModelConstants.ComponentType.INTERFACE);
        DiagramComponent oldDiagramComponent = new DiagramComponent(oldComponent, null);
        DiagramCodeModel oldModel = new DiagramCodeModel(oldDiagramComponent);
        final DiagramCodeModel newModel = new DiagramCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedStructComponent() {
        final DiagramCodeModel oldModel = new DiagramCodeModel();
        Component newComponent = new Component();
        newComponent.setComponentName("Animal.weight");
        newComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        DiagramComponent newDigramComponent = new DiagramComponent(newComponent, null);
        final DiagramCodeModel newModel = new DiagramCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedStructComponents() {
        Component oldComponent = new Component();
        oldComponent.setComponentName("Animal.weight");
        oldComponent.setComponentType(OOPSourceModelConstants.ComponentType.STRUCT);
        DiagramComponent oldDiagramComponent = new DiagramComponent(oldComponent, null);
        DiagramCodeModel oldModel = new DiagramCodeModel(oldDiagramComponent);
        final DiagramCodeModel newModel = new DiagramCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }


    @Test
    public void testAddedLocalVarComponent() {
        final DiagramCodeModel oldModel = new DiagramCodeModel();
        Component newComponent = new Component();
        newComponent.setComponentName("Animal.weight");
        newComponent.setComponentType(OOPSourceModelConstants.ComponentType.LOCAL);
        DiagramComponent newDigramComponent = new DiagramComponent(newComponent, null);
        final DiagramCodeModel newModel = new DiagramCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedLocalVarComponents() {
        Component oldComponent = new Component();
        oldComponent.setComponentName("Animal.weight");
        oldComponent.setComponentType(OOPSourceModelConstants.ComponentType.LOCAL);
        DiagramComponent oldDiagramComponent = new DiagramComponent(oldComponent, null);
        DiagramCodeModel oldModel = new DiagramCodeModel(oldDiagramComponent);
        final DiagramCodeModel newModel = new DiagramCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testAddedMethodParamComponent() {
        final DiagramCodeModel oldModel = new DiagramCodeModel();
        Component newComponent = new Component();
        newComponent.setComponentName("Animal.weight");
        newComponent.setComponentType(OOPSourceModelConstants.ComponentType.METHOD_PARAMETER_COMPONENT);
        DiagramComponent newDigramComponent = new DiagramComponent(newComponent, null);
        final DiagramCodeModel newModel = new DiagramCodeModel(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedComponents().contains(newDigramComponent));
    }

    @Test
    public void testDeletedMethodParamComponents() {
        Component oldComponent = new Component();
        oldComponent.setComponentName("Animal.weight");
        oldComponent.setComponentType(OOPSourceModelConstants.ComponentType.METHOD_PARAMETER_COMPONENT);
        DiagramComponent oldDiagramComponent = new DiagramComponent(oldComponent, null);
        DiagramCodeModel oldModel = new DiagramCodeModel(oldDiagramComponent);
        final DiagramCodeModel newModel = new DiagramCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedComponents().contains(oldDiagramComponent));
    }

    @Test
    public void testDeletedComponents() {
        Component oldComponent = new Component();
        oldComponent.setComponentName("Animal.weight");
        oldComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        DiagramComponent oldDiagramComponent = new DiagramComponent(oldComponent, null);
        DiagramCodeModel oldModel = new DiagramCodeModel(oldDiagramComponent);
        final DiagramCodeModel newModel = new DiagramCodeModel();
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

        DiagramCodeModel oldModel = new DiagramCodeModel(
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
        newAnimalFieldComponent.insertComponentRef(new SimpleTypeReference("Strawberry"));

        Component newAnimalClassComponent = new Component();
        newAnimalClassComponent.setComponentName("Animal");
        newAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        // New Reference to StrawBerry Component
        newAnimalClassComponent.insertComponentRef(new SimpleTypeReference("Strawberry"));

        DiagramCodeModel newModel = new DiagramCodeModel(
                new DiagramComponent(newAnimalFieldComponent, null),
                new DiagramComponent(newAnimalClassComponent, null),
                new DiagramComponent(newStrawBerryComponent, null)
        );

        assertEquals(new ChangeSet(oldModel, newModel).addedRelations().relations().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedRelations().hasRelationsforComponent(new DiagramComponent(newAnimalClassComponent, null)));
    }

    @Test
    public void testDeletedRelations() {
        Component oldStrawBerryComponent = new Component();
        oldStrawBerryComponent.setComponentName("Strawberry");
        oldStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);

        Component oldAnimalFieldComponent = new Component();
        oldAnimalFieldComponent.setComponentName("Animal.weight");
        oldAnimalFieldComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        oldAnimalFieldComponent.insertComponentRef(new SimpleTypeReference("Strawberry"));

        Component oldAnimalClassComponent = new Component();
        oldAnimalClassComponent.setComponentName("Animal");
        oldAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        oldAnimalClassComponent.insertComponentRef(new SimpleTypeReference("Strawberry"));

        DiagramCodeModel oldModel = new DiagramCodeModel(
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

        DiagramCodeModel newModel = new DiagramCodeModel(
                new DiagramComponent(newAnimalFieldComponent, null),
                new DiagramComponent(newAnimalClassComponent, null),
                new DiagramComponent(newStrawBerryComponent, null)
        );

        assertEquals(new ChangeSet(oldModel, newModel).deletedRelations().relations().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedRelations().hasRelationsforComponent(new DiagramComponent(oldAnimalClassComponent, null)));
    }

    @Test
    public void testKeyContextComponents() {
        Component oldStrawBerryComponent = new Component();
        oldStrawBerryComponent.setComponentName("Strawberry");
        oldStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);

        Component oldAnimalFieldComponent = new Component();
        oldAnimalFieldComponent.setComponentName("Animal.weight");
        oldAnimalFieldComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        oldAnimalFieldComponent.insertComponentRef(new SimpleTypeReference("Strawberry"));

        Component oldAnimalClassComponent = new Component();
        oldAnimalClassComponent.setComponentName("Animal");
        oldAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        oldAnimalClassComponent.insertComponentRef(new SimpleTypeReference("Strawberry"));

        DiagramCodeModel oldModel = new DiagramCodeModel(
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

        DiagramCodeModel newModel = new DiagramCodeModel(
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

        DiagramCodeModel oldModel = new DiagramCodeModel();

        Component newAnimalFieldComponent = new Component();
        newAnimalFieldComponent.setComponentName("Animal.weight");
        newAnimalFieldComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        // Note strawberry component does not exist in this code base
        newAnimalFieldComponent.insertComponentRef(new SimpleTypeReference("Strawberry"));

        Component newAnimalClassComponent = new Component();
        newAnimalClassComponent.setComponentName("Animal");
        newAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        // Note strawberry component does not exist in this code base
        newAnimalClassComponent.insertComponentRef(new SimpleTypeReference("Strawberry"));

        DiagramCodeModel newModel = new DiagramCodeModel(
                new DiagramComponent(newAnimalFieldComponent, null),
                new DiagramComponent(newAnimalClassComponent, null)
        );

        assertEquals(new ChangeSet(oldModel, newModel).addedRelations().relations().size(), 0);
    }
}
