package striff.test;

import com.hadii.clarpse.reference.TypeExtensionReference;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.striff.diagram.StriffDiagramModel;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.parse.CodeDiff;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * StriffDiagramModel class tests.
 */
public class StriffDiagramModelTest {

        @Test
        public void testSelectedComponentsIncludeAddedComponents() {
                OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
                Component newStrawBerryComponent = new Component();
                newStrawBerryComponent.setComponentName("Strawberry");
                newStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
                OOPSourceCodeModel newModel = new OOPSourceCodeModel();
                oldModel.insertComponent(newStrawBerryComponent);

                assertTrue(new StriffDiagramModel(new CodeDiff(oldModel, newModel))
                                .diagramCmps()
                                .contains(new DiagramComponent("Strawberry")));
        }

        @Test
        public void testSelectedComponentsIncludeDeletedComponents() {
                OOPSourceCodeModel newModel = new OOPSourceCodeModel();
                Component oldStrawBerryComponent = new Component();
                oldStrawBerryComponent.setComponentName("Strawberry");
                oldStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
                OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
                oldModel.insertComponent(oldStrawBerryComponent);
                assertTrue(new StriffDiagramModel(new CodeDiff(oldModel, newModel))
                                .diagramCmps()
                                .contains(new DiagramComponent("Strawberry")));
        }

        @Test
        public void testSelectedComponentsIncludeKeyRelationContextComponents() {
                OOPSourceCodeModel newModel = new OOPSourceCodeModel();
                Component oldStrawBerryComponent = new Component();
                oldStrawBerryComponent.setComponentName("Strawberry");
                oldStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
                OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
                oldModel.insertComponent(oldStrawBerryComponent);
                assertTrue(new StriffDiagramModel(new CodeDiff(oldModel, newModel))
                                .diagramCmps()
                                .contains(new DiagramComponent("Strawberry")));
        }

        @Test
        public void testSelectedComponentsCoreAreBaseComponentsOnly() {
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

                OOPSourceCodeModel newModel = new OOPSourceCodeModel();
                assertEquals(new StriffDiagramModel(new CodeDiff(oldModel, newModel)).diagramCmps().size(), 2);
                assertTrue(new StriffDiagramModel(new CodeDiff(oldModel, newModel)).diagramCmps().contains(
                                new DiagramComponent("Animal")));
                assertTrue(new StriffDiagramModel(new CodeDiff(oldModel, newModel)).diagramCmps().contains(
                                new DiagramComponent("Strawberry")));
        }

        @Test
        public void testSelectedComponentsContext() {
                Component oldStrawBerryComponent = new Component();
                oldStrawBerryComponent.setComponentName("Strawberry");
                oldStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
                oldStrawBerryComponent.insertCmpRef(new TypeExtensionReference("Animal"));

                Component oldAnimalClassComponent = new Component();
                oldAnimalClassComponent.setComponentName("Animal");
                oldAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
                OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
                oldModel.insertComponent(oldAnimalClassComponent);
                oldModel.insertComponent(oldStrawBerryComponent);

                Component newStrawBerryComponent = new Component();
                newStrawBerryComponent.setComponentName("Strawberry");
                newStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
                newStrawBerryComponent.insertCmpRef(new TypeExtensionReference("Animal"));

                // Animal class has new field component
                Component newAnimalFieldComponent = new Component();
                newAnimalFieldComponent.setComponentName("Animal.weight");
                newAnimalFieldComponent.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);

                Component newAnimalClassComponent = new Component();
                newAnimalClassComponent.setComponentName("Animal");
                newAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
                OOPSourceCodeModel newModel = new OOPSourceCodeModel();
                newModel.insertComponent(newAnimalFieldComponent);
                newModel.insertComponent(newAnimalClassComponent);
                newModel.insertComponent(newStrawBerryComponent);

                // Although the Strawberry component directly references the Animal class which
                // is a core
                // component, it has no changes within it and should not be displayed
                assertEquals(new StriffDiagramModel(new CodeDiff(oldModel, newModel)).diagramCmps().size(), 1);
                assertTrue(new StriffDiagramModel(new CodeDiff(oldModel, newModel)).diagramCmps().contains(
                                new DiagramComponent("Animal")));
        }

        @Test
        public void testSelectedComponentsCoreWithSourceFilesFilter() {
                Component oldStrawBerryComponent = new Component();
                oldStrawBerryComponent.setComponentName("Strawberry");
                oldStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
                oldStrawBerryComponent.setSourceFilePath("strawbs.java");
                Component oldAnimalClassComponent = new Component();
                oldAnimalClassComponent.setComponentName("Animal");
                oldAnimalClassComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
                oldAnimalClassComponent.setSourceFilePath("animal.java");
                OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
                oldModel.insertComponent(oldAnimalClassComponent);
                oldModel.insertComponent(oldStrawBerryComponent);

                OOPSourceCodeModel newModel = new OOPSourceCodeModel();
                assertEquals(new StriffDiagramModel(new CodeDiff(oldModel, newModel),
                                Collections.singleton("animal.java"), false)
                                .diagramCmps().size(), 1);
                assertTrue(new StriffDiagramModel(new CodeDiff(oldModel, newModel),
                                Collections.singleton("animal.java"), false)
                                .diagramCmps()
                                .contains(new DiagramComponent("Animal")));
        }

        @Test
        public void OOPSourceCodeModelIsEmpty() {
                OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
                OOPSourceCodeModel newModel = new OOPSourceCodeModel();
                assertTrue(new CodeDiff(
                                oldModel, newModel).changeSet().addedComponents().isEmpty());
                assertTrue(new CodeDiff(
                                oldModel, newModel).changeSet().deletedComponents().isEmpty());
                assertTrue(new CodeDiff(
                                oldModel, newModel).changeSet().addedRelations().allRels().isEmpty());
                assertTrue(new CodeDiff(
                                oldModel, newModel).changeSet().deletedRelations().allRels().isEmpty());
        }
}
