package striff.test.model;

import com.hadi.clarpse.reference.SimpleTypeReference;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.StriffDiagramModel;
import com.hadi.striff.extractor.RelationsMap;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SyntheticModuleDiagramTest {

    @Test
    public void includesSyntheticModuleWhenModuleFunctionChanges() {
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        OOPSourceCodeModel newModel = new OOPSourceCodeModel();

        Component fn = new Component();
        fn.setComponentName("topLevelFn");
        fn.setComponentType(OOPSourceModelConstants.ComponentType.FUNCTION);
        fn.setModule("util");
        fn.setCodeHash(1);
        newModel.insertComponent(fn);

        StriffDiagramModel diagramModel = new StriffDiagramModel(new CodeDiff(oldModel, newModel));
        assertTrue(diagramModel.diagramCmps().contains(new DiagramComponent("module:util")));
        DiagramComponent synthetic = diagramModel.diagramCmps().stream()
                .filter(cmp -> cmp.uniqueName().equals("module:util"))
                .findFirst()
                .orElseThrow();
        assertTrue(synthetic.children().contains(fn.uniqueName()));
    }

    @Test
    public void syntheticModuleUsesSourceFileFromModuleComponents() {
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        OOPSourceCodeModel newModel = new OOPSourceCodeModel();

        Component fn = new Component();
        fn.setComponentName("topLevelFn");
        fn.setComponentType(OOPSourceModelConstants.ComponentType.FUNCTION);
        fn.setModule("util");
        fn.setSourceFilePath("util.py");
        fn.setCodeHash(1);
        newModel.insertComponent(fn);

        StriffDiagramModel diagramModel = new StriffDiagramModel(new CodeDiff(oldModel, newModel));
        DiagramComponent synthetic = diagramModel.diagramCmps().stream()
                .filter(cmp -> cmp.uniqueName().equals("module:util"))
                .findFirst()
                .orElseThrow();
        assertEquals("util.py", synthetic.sourceFile());
    }

    @Test
    public void moduleLevelFunctionRelationsFoldToSyntheticModule() {
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        OOPSourceCodeModel newModel = new OOPSourceCodeModel();

        Component target = new Component();
        target.setComponentName("Animal");
        target.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        newModel.insertComponent(target);

        Component fn = new Component();
        fn.setComponentName("topLevelFn");
        fn.setComponentType(OOPSourceModelConstants.ComponentType.FUNCTION);
        fn.setModule("util");
        fn.insertCmpRef(new SimpleTypeReference("Animal"));
        newModel.insertComponent(fn);

        CodeDiff diff = new CodeDiff(oldModel, newModel);
        RelationsMap relationsMap = diff.extractedRels();
        assertTrue(relationsMap.hasRels("module:util"));
        assertEquals(1, relationsMap.significantRels("module:util").size());
        assertTrue(relationsMap.significantRels("module:util").stream()
                .anyMatch(rel -> rel.targetComponent().uniqueName().equals("Animal")));
    }

    @Test
    public void moduleLevelFieldRelationsFoldToSyntheticModule() {
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        OOPSourceCodeModel newModel = new OOPSourceCodeModel();

        Component target = new Component();
        target.setComponentName("Zoo");
        target.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        newModel.insertComponent(target);

        Component field = new Component();
        field.setComponentName("topLevelField");
        field.setComponentType(OOPSourceModelConstants.ComponentType.MODULE_FIELD);
        field.setModule("util");
        field.insertCmpRef(new SimpleTypeReference("Zoo"));
        newModel.insertComponent(field);

        CodeDiff diff = new CodeDiff(oldModel, newModel);
        RelationsMap relationsMap = diff.extractedRels();
        assertTrue(relationsMap.hasRels("module:util"));
        assertTrue(relationsMap.significantRels("module:util").stream()
                .anyMatch(rel -> rel.targetComponent().uniqueName().equals("Zoo")));
    }
}
