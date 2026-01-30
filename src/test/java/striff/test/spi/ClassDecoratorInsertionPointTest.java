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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class ClassDecoratorInsertionPointTest {

    private static final String SERVICE_RESOURCE =
            "META-INF/services/com.hadi.striff.spi.ClassDecorator";

    @Test
    public void decoratorInsertionPointsRespected() throws Exception {
        ClassLoader classLoader = new ServiceResourceClassLoader(
                Thread.currentThread().getContextClassLoader());
        String output = buildPlantUmlString(classLoader);

        int classHeaderIndex = output.indexOf("class A");
        int classOpenIndex = output.indexOf("{", classHeaderIndex);
        int topIndex = output.indexOf("TOP_SENTINEL", classHeaderIndex);
        int bottomIndex = output.indexOf("BOTTOM_SENTINEL", classHeaderIndex);
        int classCloseIndex = output.indexOf("}\n", classOpenIndex);

        assertTrue(classHeaderIndex >= 0);
        assertTrue(topIndex > classOpenIndex);
        assertTrue(bottomIndex > topIndex);
        assertTrue(bottomIndex < classCloseIndex);
    }

    private static String buildPlantUmlString(ClassLoader classLoader) throws Exception {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            ProjectFiles oldFiles = new ProjectFiles();

            ProjectFiles newFiles = new ProjectFiles();
            newFiles.insertFile(new ProjectFile("/A.java", "public class A { int x; }"));

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

    private static final class ServiceResourceClassLoader extends ClassLoader {
        private final URL serviceUrl;

        private ServiceResourceClassLoader(ClassLoader parent) throws IOException {
            super(parent);
            Path tempDir = Files.createTempDirectory("spi-services");
            Path serviceFile = tempDir.resolve(SERVICE_RESOURCE);
            Files.createDirectories(serviceFile.getParent());
            Files.writeString(serviceFile,
                    String.join("\n", List.of(
                            "striff.test.spi.TopSentinelDecorator",
                            "striff.test.spi.BottomSentinelDecorator",
                            "")),
                    StandardCharsets.UTF_8);
            this.serviceUrl = serviceFile.toUri().toURL();
        }

        @Override
        public URL getResource(String name) {
            if (SERVICE_RESOURCE.equals(name)) {
                return serviceUrl;
            }
            return super.getResource(name);
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            if (SERVICE_RESOURCE.equals(name)) {
                return Collections.enumeration(List.of(serviceUrl));
            }
            return super.getResources(name);
        }
    }
}
