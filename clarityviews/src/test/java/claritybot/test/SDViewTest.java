package claritybot.test;

import com.clarity.binary.diagram.Diagram;
import com.clarity.binary.diagram.DiagramSourceCodeModel;
import com.clarity.binary.diagram.scheme.LightDiagramColorScheme;
import com.clarity.binary.diagram.view.EmptySDException;
import com.clarity.binary.diagram.view.SDView;
import com.clarity.compiler.Lang;
import com.clarity.invocation.TypeDeclaration;
import com.clarity.sourcemodel.Component;
import com.clarity.sourcemodel.OOPSourceCodeModel;
import com.clarity.sourcemodel.OOPSourceModelConstants;
import org.junit.Test;

import java.io.PrintWriter;

public class SDViewTest {

    @Test
    public void manualTest() throws Exception {
        DiagramSourceCodeModel oldModel = ClarityTestUtil.getGitHubRepoModel("clarity-org",
                "clarpse", "1eb8b0d9ce08250f5b1b29189885ddb3a6485d40", "01fe37630d630dfecbd504faed22688d6de0aab6", Lang.JAVA);
        DiagramSourceCodeModel newModel = ClarityTestUtil.getGitHubRepoModel(
                "clarity-org", "clarpse", "master", "01fe37630d630dfecbd504faed22688d6de0aab6", Lang.JAVA);
        Diagram view = new SDView(new LightDiagramColorScheme(), oldModel, newModel, 400).view();
        PrintWriter writer = new PrintWriter("/home/zir0/Desktop/sdTest.svg", "UTF-8");
        writer.println(view.svgText());
        writer.close();
    }

    @Test(expected = EmptySDException.class)
    public void testEmptySDException() throws Exception {
        DiagramSourceCodeModel oldModel = new DiagramSourceCodeModel(new OOPSourceCodeModel());
        DiagramSourceCodeModel newModel = new DiagramSourceCodeModel(new OOPSourceCodeModel());
        new SDView(new LightDiagramColorScheme(), oldModel, newModel, 400).view();
    }

    @Test
    public void newRelationshipTest() throws Exception {
        Component a = new Component();

        a.setPackageName("com.test");
        a.setComponentName("classA");
        a.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        a.setName("classA");
        Component aField = new Component();
        aField.setPackageName("com.test");
        aField.setComponentName("classA.aField");
        aField.setComponentType(OOPSourceModelConstants.ComponentType.FIELD);
        aField.setName("aField");
        a.insertChildComponent("com.test.classA.aField");
        Component b = new Component();
        b.setPackageName("com.test");
        b.setComponentName("classB");
        b.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        b.setName("classB");
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(a);
        oldModel.insertComponent(aField);
        oldModel.insertComponent(b);
        // add type declaration to new model.
        Component newAField = new Component(aField);
        newAField.insertComponentInvocation(new TypeDeclaration("com.test.classB"));
        OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(a);
        newModel.insertComponent(newAField);
        newModel.insertComponent(b);
        // expect no Exception thrown and a valid sd is generated...
        new SDView(new LightDiagramColorScheme(), new DiagramSourceCodeModel(oldModel), new DiagramSourceCodeModel(newModel), 400).view();
    }

}
