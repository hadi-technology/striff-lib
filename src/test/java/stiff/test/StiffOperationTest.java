package stiff.test;

import com.hadii.clarpse.compiler.SourceFiles;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.stiff.NoStructuralChangesException;
import com.hadii.stiff.StiffDiagram;
import com.hadii.stiff.StiffOperation;
import com.hadii.stiff.diagram.DiagramCodeModel;
import com.hadii.stiff.diagram.DiagramComponent;
import com.hadii.stiff.parse.DiffCodeModel;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.Test;

import java.util.List;

public class StiffOperationTest {

    @Test(expected = NoStructuralChangesException.class)
    public void noStructuralChangesFoundException() throws Exception {
        DiagramCodeModel oldModel = new DiagramCodeModel();
        DiagramCodeModel newModel = new DiagramCodeModel();
        List<StiffDiagram> stiffDiagrams = new StiffOperation(new DiffCodeModel(oldModel, newModel), 10, Arrays.asList(new SourceFiles[]{})).result();
    }

    @Test(expected = IllegalArgumentException.class)
    public void requestedDiagramSizeTooSmall() throws Exception {
        DiagramCodeModel oldModel = new DiagramCodeModel();
        Component newStrawBerryComponent = new Component();
        newStrawBerryComponent.setComponentName("Strawberry");
        newStrawBerryComponent.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        DiagramCodeModel newModel = new DiagramCodeModel(
                new DiagramComponent(newStrawBerryComponent, null)
        );
        List<StiffDiagram> stiffDiagrams = new StiffOperation(new DiffCodeModel(oldModel, newModel), 3, Arrays.asList(new SourceFiles[]{})).result();
    }
}
