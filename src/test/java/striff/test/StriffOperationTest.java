package striff.test;

import com.hadii.clarpse.compiler.SourceFiles;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.striff.NoStructuralChangesException;
import com.hadii.striff.StiffDiagram;
import com.hadii.striff.StriffOperation;
import com.hadii.striff.diagram.DiagramCodeModel;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.parse.DiffCodeModel;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.Test;

import java.util.List;

public class StriffOperationTest {

    @Test(expected = NoStructuralChangesException.class)
    public void noStructuralChangesFoundException() throws Exception {
        DiagramCodeModel oldModel = new DiagramCodeModel();
        DiagramCodeModel newModel = new DiagramCodeModel();
        List<StiffDiagram> stiffDiagrams = new StriffOperation(new DiffCodeModel(oldModel, newModel), 10, Arrays.asList(new SourceFiles[]{})).result();
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
        List<StiffDiagram> stiffDiagrams = new StriffOperation(new DiffCodeModel(oldModel, newModel), 3, Arrays.asList(new SourceFiles[]{})).result();
    }
}
