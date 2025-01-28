package striff.test;

import com.hadii.clarpse.reference.SimpleTypeReference;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.striff.ChangeSet;
import com.hadii.striff.diagram.DiagramComponent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests to ensure Stiff Change Sets are generated correctly.
 */
public class ChangeSetTest {

    @Test
    public void testAddedFieldComponent() {
        final OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        Component newDigramComponent = setupComponent(OOPSourceModelConstants.ComponentType.FIELD, "Animal.weight");
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inAddedComponents(newDigramComponent.uniqueName()));
    }

    @Test
    public void testDeletedFieldComponents() {
        Component oldComponent = setupComponent(OOPSourceModelConstants.ComponentType.FIELD, "Animal.weight");
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(oldComponent);
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inDeletedComponents(oldComponent.uniqueName()));
    }

    @Test
    public void testAddedMethodComponent() {
        final OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        Component newDigramComponent = setupComponent(OOPSourceModelConstants.ComponentType.METHOD, "Animal.test()");
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inAddedComponents(newDigramComponent.uniqueName()));
    }

    @Test
    public void testDeletedMethodComponents() {
        Component oldComponent = setupComponent(OOPSourceModelConstants.ComponentType.METHOD, "Animal.test()");
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(oldComponent);
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inDeletedComponents(oldComponent.uniqueName()));
    }

    @Test
    public void testAddedConstructorComponent() {
        final OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        Component newDigramComponent = setupComponent(OOPSourceModelConstants.ComponentType.CONSTRUCTOR, "Animal.test" +
                "()");
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inAddedComponents(newDigramComponent.uniqueName()));
    }

    @Test
    public void testDeletedConstructorComponents() {
        Component oldComponent = setupComponent(OOPSourceModelConstants.ComponentType.CONSTRUCTOR, "Animal" +
                ".weight()");
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(oldComponent);
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inDeletedComponents(oldComponent.uniqueName()));
    }

    @Test
    public void testAddedClassComponent() {
        final OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        Component newDigramComponent = setupComponent(OOPSourceModelConstants.ComponentType.CLASS, "Animal");
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inAddedComponents(newDigramComponent.uniqueName()));
    }

    @Test
    public void testDeletedClassComponents() {
        Component oldComponent = setupComponent(OOPSourceModelConstants.ComponentType.CLASS, "Animal");
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(oldComponent);
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inDeletedComponents(oldComponent.uniqueName()));
    }

    @Test
    public void testAddedInterfaceComponent() {
        final OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        Component newDigramComponent = setupComponent(OOPSourceModelConstants.ComponentType.INTERFACE, "Animal");
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inAddedComponents(newDigramComponent.uniqueName()));
    }

    @Test
    public void testDeletedInterfaceComponents() {
        Component oldComponent = setupComponent(OOPSourceModelConstants.ComponentType.INTERFACE, "Animal");
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(oldComponent);
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inDeletedComponents(oldComponent.uniqueName()));
    }

    @Test
    public void testAddedStructComponent() {
        final OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        Component newDigramComponent = setupComponent(OOPSourceModelConstants.ComponentType.FIELD, "Animal.weight");
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inAddedComponents(newDigramComponent.uniqueName()));
    }

    @Test
    public void testDeletedStructComponents() {
        Component oldComponent = setupComponent(OOPSourceModelConstants.ComponentType.STRUCT, "Animal");
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(oldComponent);
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inDeletedComponents(oldComponent.uniqueName()));
    }

    @Test
    public void testAddedLocalVarComponent() {
        final OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        Component newDigramComponent = setupComponent(
                OOPSourceModelConstants.ComponentType.LOCAL, "Animal.weight().test");
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inAddedComponents(newDigramComponent.uniqueName()));
    }

    @Test
    public void testDeletedLocalVarComponents() {
        Component oldComponent = setupComponent(
                OOPSourceModelConstants.ComponentType.LOCAL, "Animal.weight().test");
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(oldComponent);
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inDeletedComponents(oldComponent.uniqueName()));
    }

    @Test
    public void testAddedMethodParamComponent() {
        final OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        Component newDigramComponent = setupComponent(OOPSourceModelConstants.ComponentType.METHOD_PARAMETER_COMPONENT,
                "Animal.weight().test");
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(newDigramComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inAddedComponents(newDigramComponent.uniqueName()));
    }

    @Test
    public void testDeletedMethodParamComponents() {
        Component oldComponent = setupComponent(OOPSourceModelConstants.ComponentType.METHOD_PARAMETER_COMPONENT,
                "Animal.weight().test");
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(oldComponent);
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inDeletedComponents(oldComponent.uniqueName()));
    }

    @Test
    public void testDeletedComponents() {
        Component oldComponent = setupComponent(OOPSourceModelConstants.ComponentType.FIELD,
                "Animal.test");
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(oldComponent);
        final OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        assertEquals(new ChangeSet(oldModel, newModel).deletedComponents().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).inDeletedComponents(oldComponent.uniqueName()));
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

        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(oldAnimalFieldComponent);
        oldModel.insertComponent(oldAnimalClassComponent);
        oldModel.insertComponent(oldStrawBerryComponent);
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

        OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(newAnimalFieldComponent);
        newModel.insertComponent(newAnimalClassComponent);
        newModel.insertComponent(newStrawBerryComponent);

        assertEquals(new ChangeSet(oldModel, newModel).addedRelations().allRels().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).addedRelations().hasRels(
                newAnimalClassComponent.uniqueName()));
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

        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(oldAnimalFieldComponent);
        oldModel.insertComponent(oldAnimalClassComponent);
        oldModel.insertComponent(oldStrawBerryComponent);
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

        OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(newAnimalFieldComponent);
        newModel.insertComponent(newAnimalClassComponent);
        newModel.insertComponent(newStrawBerryComponent);
        assertEquals(new ChangeSet(oldModel, newModel).deletedRelations().allRels().size(), 1);
        assertTrue(new ChangeSet(oldModel, newModel).deletedRelations().hasRels(
                oldAnimalClassComponent.uniqueName()));
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
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(oldAnimalFieldComponent);
        oldModel.insertComponent(oldAnimalClassComponent);
        oldModel.insertComponent(oldStrawBerryComponent);
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
        OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(newAnimalFieldComponent);
        newModel.insertComponent(newAnimalClassComponent);
        newModel.insertComponent(newStrawBerryComponent);
        assertEquals(new ChangeSet(oldModel, newModel).keyRelationsComponents().size(), 2);
        assertTrue(new ChangeSet(oldModel, newModel).keyRelationsComponents().contains(
                new DiagramComponent(newAnimalClassComponent, newModel).uniqueName()));
        assertTrue(new ChangeSet(oldModel, newModel).keyRelationsComponents().contains(
                new DiagramComponent(newStrawBerryComponent, newModel).uniqueName()));
    }

    @Test
    public void testRelationComponentsMustExist() {
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
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

        OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(newAnimalFieldComponent);
        newModel.insertComponent(newAnimalClassComponent);
        assertEquals(new ChangeSet(oldModel, newModel).addedRelations().allRels().size(), 0);
    }

    private Component setupComponent(OOPSourceModelConstants.ComponentType type,
            String cmpName) {
        Component newComponent = new Component();
        newComponent.setComponentName(cmpName);
        newComponent.setComponentType(type);
        return newComponent;
    }
}
