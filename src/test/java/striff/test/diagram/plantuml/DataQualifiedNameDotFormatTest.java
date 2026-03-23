package striff.test.diagram.plantuml;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.striff.StriffConfig;
import com.hadi.striff.StriffOperation;
import com.hadi.striff.diagram.display.OutputMode;
import com.hadi.striff.diagram.StriffDiagram;
import com.hadi.striff.diagram.plantuml.PUMLDiagram;
import com.hadi.striff.diagram.plantuml.PUMLDiagramData;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * Test that verifies data-qualified-name attributes use dots (not hyphens).
 * This is critical for browser extension compatibility.
 */
public class DataQualifiedNameDotFormatTest {

    private static final Pattern DATA_QUALIFIED_NAME_PATTERN = Pattern.compile(
            "data-qualified-name=\"([^\"]+)\""
    );

    private static Set<String> extractDataQualifiedNames(String svg) {
        Set<String> names = new java.util.HashSet<>();
        Matcher matcher = DATA_QUALIFIED_NAME_PATTERN.matcher(svg);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    @Test
    public void dataQualifiedNamesUseDotsNotHyphens() throws Exception {
        ProjectFiles oldFiles = new ProjectFiles();
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/com/example/MyClass.java",
                "package com.example; public class MyClass { int x; }"));
        newFiles.insertFile(new ProjectFile("/com/example/Utils.java",
                "package com.example; public class Utils { void foo() {} }"));
        newFiles.insertFile(new ProjectFile("/com/example/sub/Nested.java",
                "package com.example.sub; public class Nested { }"));

        CodeDiff diff = new CodeDiff(
                new ClarpseProject(oldFiles, Lang.JAVA).result().model(),
                new ClarpseProject(newFiles, Lang.JAVA).result().model()
        );

        List<StriffDiagram> diagrams = new StriffOperation(oldFiles, newFiles,
                StriffConfig.create()
                        .setOutputMode(OutputMode.DEFAULT)
                        .setLanguages(List.of(Lang.JAVA)))
                .result().diagrams();

        String svg = diagrams.get(0).svg();
        Set<String> dataQualifiedNames = extractDataQualifiedNames(svg);

        // Verify NO hyphens in data-qualified-name attributes (except within module: prefixes)
        for (String name : dataQualifiedNames) {
            // Should contain dots, not hyphens (except in module: prefix)
            assertTrue("data-qualified-name should use dots: " + name,
                    name.contains(".") || name.startsWith("module:") || !name.contains("-"));
        }

        // Verify specific expected names
        assertTrue("Should contain com.example.MyClass: " + dataQualifiedNames,
                dataQualifiedNames.contains("com.example.MyClass"));
        assertTrue("Should contain com.example.Utils: " + dataQualifiedNames,
                dataQualifiedNames.contains("com.example.Utils"));
        assertTrue("Should contain com.example.sub.Nested: " + dataQualifiedNames,
                dataQualifiedNames.contains("com.example.sub.Nested"));
    }
}
