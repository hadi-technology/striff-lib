package striff.test;

import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.striff.NoStructuralChangesException;
import com.hadii.striff.StriffDiagram;
import com.hadii.striff.StriffOperation;
import com.hadii.striff.diagram.DiagramCodeModel;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.parse.DiffCodeModel;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class StriffOperationTest {

    @Test(expected = NoStructuralChangesException.class)
    public void noStructuralChangesFoundException() throws Exception {
        DiagramCodeModel oldModel = new DiagramCodeModel();
        DiagramCodeModel newModel = new DiagramCodeModel();
        new StriffOperation(new DiffCodeModel(
                oldModel, newModel), 10, Arrays.asList(new String[]{})).result();
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
        new StriffOperation(new DiffCodeModel(
                oldModel, newModel), 3, Arrays.asList(new String[]{})).result();
    }
}
