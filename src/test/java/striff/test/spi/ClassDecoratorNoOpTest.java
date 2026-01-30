package striff.test.spi;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.CompileException;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.diagram.ComponentHelper;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.StriffDiagramModel;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.diagram.display.LightDiagramColorScheme;
import com.hadi.striff.diagram.plantuml.PUMLDiagramData;
import com.hadi.striff.diagram.plantuml.PUMLDiagramText;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ClassDecoratorNoOpTest {

    private static final String CLASS_DECORATOR_SERVICE =
            "META-INF/services/com.hadi.striff.spi.ClassDecorator";

    @Test
    public void noOpDecoratorDoesNotChangeOutput() throws Exception {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        ClassLoader filtered = new FilteringClassLoader(original, CLASS_DECORATOR_SERVICE);

        String withoutDecorator = buildPlantUmlString(filtered);
        String withDecorator = buildPlantUmlString(original);

        assertEquals(withoutDecorator, withDecorator);
    }

    private static String buildPlantUmlString(ClassLoader classLoader) throws Exception {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            ProjectFiles oldFiles = new ProjectFiles();
            oldFiles.insertFile(new ProjectFile("/A.java", "public class A { int x; }"));

            ProjectFiles newFiles = new ProjectFiles();
            newFiles.insertFile(new ProjectFile("/A.java", "public class A { int x; void m() {} }"));
            newFiles.insertFile(new ProjectFile("/B.java", "public class B { }"));

            CodeDiff diff = codeDiff(oldFiles, newFiles);
            StriffDiagramModel model = new StriffDiagramModel(diff, Set.of());
            Set<DiagramComponent> diagramCmps = model.diagramCmps();
            DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), cmpPkgs(diagramCmps));

            PUMLDiagramData data = new PUMLDiagramData(
                    model.diagramRels(),
                    diff.changeSet().addedRelations(),
                    diff.changeSet().deletedRelations(),
                    display,
                    diff.mergedModel(),
                    diff.changeSet().addedComponents(),
                    diff.changeSet().deletedComponents(),
                    diff.changeSet().modifiedComponents(),
                    diagramCmps);

            return PUMLDiagramText.build(data);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    private static CodeDiff codeDiff(ProjectFiles oldFiles, ProjectFiles newFiles) throws CompileException {
        OOPSourceCodeModel oldModel = new ClarpseProject(oldFiles, Lang.JAVA).result().model();
        OOPSourceCodeModel newModel = new ClarpseProject(newFiles, Lang.JAVA).result().model();
        return new CodeDiff(oldModel, newModel);
    }

    private static Set<String> cmpPkgs(Set<DiagramComponent> cmps) {
        return cmps.stream()
                .map(cmp -> ComponentHelper.packagePath(cmp.pkg()))
                .collect(Collectors.toSet());
    }

    private static final class FilteringClassLoader extends ClassLoader {
        private final String filteredResource;

        private FilteringClassLoader(ClassLoader parent, String filteredResource) {
            super(parent);
            this.filteredResource = filteredResource;
        }

        @Override
        public URL getResource(String name) {
            if (filteredResource.equals(name)) {
                return null;
            }
            return super.getResource(name);
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            if (filteredResource.equals(name)) {
                return Collections.emptyEnumeration();
            }
            return super.getResources(name);
        }
    }
}
