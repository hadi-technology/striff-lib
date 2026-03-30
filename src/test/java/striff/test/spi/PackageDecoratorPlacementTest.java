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

public class PackageDecoratorPlacementTest {

    private static final String SERVICE_RESOURCE =
            "META-INF/services/com.hadi.striff.spi.PackageDecorator";

    @Test
    public void packageDecoratorRendersInsideTargetPackageBlock() throws Exception {
        ClassLoader classLoader = new ServiceResourceClassLoader(
                Thread.currentThread().getContextClassLoader());
        String output = buildPlantUmlString(classLoader);

        String packageBlock = packageBlock(output, "com.sample.b");
        int packageStart = output.indexOf("package \"com.sample.b\"");
        int noteIndex = packageBlock.indexOf("note right of B");
        int sentinelIndex = packageBlock.indexOf("PACKAGE_SENTINEL");

        assertTrue(packageStart >= 0);
        assertTrue(noteIndex >= 0);
        assertTrue(sentinelIndex > noteIndex);
    }

    private static String buildPlantUmlString(ClassLoader classLoader) throws Exception {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            ProjectFiles oldFiles = new ProjectFiles();

            ProjectFiles newFiles = new ProjectFiles();
            newFiles.insertFile(new ProjectFile("/A.java", "package com.sample.a; public class A { int x; }"));
            newFiles.insertFile(new ProjectFile("/B.java", "package com.sample.b; public class B { int y; }"));

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
            Path tempDir = Files.createTempDirectory("spi-package-services");
            Path serviceFile = tempDir.resolve(SERVICE_RESOURCE);
            Files.createDirectories(serviceFile.getParent());
            Files.writeString(serviceFile,
                    String.join("\n", List.of(
                            "striff.test.spi.PackageSentinelDecorator",
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

    private static String packageBlock(String puml, String packageName) {
        String packageKeyword = "package \"" + packageName + "\"";
        int start = puml.indexOf(packageKeyword);
        if (start < 0) {
            throw new AssertionError("Package block not found for " + packageName);
        }
        int open = puml.indexOf('{', start);
        int depth = 1;
        for (int i = open + 1; i < puml.length(); i++) {
            char ch = puml.charAt(i);
            if (ch == '{') {
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0) {
                    return puml.substring(start, i + 1);
                }
            }
        }
        throw new AssertionError("Unclosed package block for " + packageName);
    }
}
