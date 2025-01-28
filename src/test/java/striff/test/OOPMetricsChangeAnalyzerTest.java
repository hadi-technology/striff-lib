package striff.test;

import com.hadii.clarpse.compiler.ClarpseProject;
import com.hadii.clarpse.compiler.CompileException;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.striff.metrics.MetricChange;
import com.hadii.striff.metrics.OOPMetricsChangeAnalyzer;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

public class OOPMetricsChangeAnalyzerTest {

        @Test
        public void testMetricChangePrecision() {
                MetricChange metricChange = new MetricChange(
                                "TestClass",
                                1.12345, 2.12345,
                                3.12345, 4.12345,
                                5.12345, 6.12345,
                                7.12345, 8.12345,
                                9.12345, 10.12345, 9.12345, 10.12345);

                assertEquals(1.12, metricChange.oldNOC(), 0.001);
                assertEquals(2.12, metricChange.updatedNOC(), 0.001);
                assertEquals(3.12, metricChange.oldDIT(), 0.001);
                assertEquals(4.12, metricChange.updatedDIT(), 0.001);
                assertEquals(5.12, metricChange.oldWMC(), 0.001);
                assertEquals(6.12, metricChange.updatedWMC(), 0.001);
                assertEquals(7.12, metricChange.oldAC(), 0.001);
                assertEquals(8.12, metricChange.updatedAC(), 0.001);
                assertEquals(9.12, metricChange.oldEC(), 0.001);
                assertEquals(10.12, metricChange.updatedEC(), 0.001);
                assertEquals(9.12, metricChange.oldEncapsulation(), 0.001);
                assertEquals(10.12, metricChange.updatedEncapsulation(), 0.001);
        }

        @Test
        public void javaSingleClassNoMetricChange() throws CompileException {
                OOPSourceCodeModel oldModel = createSingleJavaClassModel("test", "ClassA");
                OOPSourceCodeModel newModel = createSingleJavaClassModel("test", "ClassA");
                Optional<MetricChange> maybeChange = analyzeChange(oldModel, newModel, new HashSet<>(), "test.ClassA");
                assertTrue("ClassA should be found in both models => some MetricChange object even if no differences.",
                                maybeChange.isPresent());
                assertTrue("Expected no change in NOC for ClassA => 0->0.",
                                maybeChange.get().toString().contains("NOC: 0.00 -> 0.00"));
        }

        @Test
        public void javaClassOnlyInNewModel() throws CompileException {
                OOPSourceCodeModel oldModel = createEmptyJavaModel();
                OOPSourceCodeModel newModel = createSingleJavaClassModel("test", "ClassA");
                Optional<MetricChange> maybeChange = analyzeChange(oldModel, newModel, new HashSet<>(), "test.ClassA");
                assertTrue("ClassA only in new => expect a MetricChange with old=0.0 metrics.",
                                maybeChange.isPresent());
        }

        @Test
        public void goSingleStructNoChanges() throws CompileException {
                OOPSourceCodeModel oldModel = createSingleGoStructModel("package main\ntype ClassA struct {}",
                                "ClassA");
                OOPSourceCodeModel newModel = createSingleGoStructModel("package main\ntype ClassA struct {}",
                                "ClassA");
                Optional<MetricChange> maybeChange = analyzeChange(oldModel, newModel, new HashSet<>(), "main.ClassA");
                if (!maybeChange.isPresent()) {
                        maybeChange = analyzeChange(oldModel, newModel, new HashSet<>(), "ClassA");
                }
                assertTrue("Expect a MetricChange object even if no difference in metrics.",
                                maybeChange.isPresent());
                assertTrue("Expected no children => NOC=0->0 for ClassA struct in Go.",
                                maybeChange.get().toString().contains("NOC: 0.00 -> 0.00"));
        }

        @Test
        public void goStructMissingInNewModel() throws CompileException {
                String oldCode = "package main\ntype OldStruct struct {}";
                String newCode = "package main\ntype DifferentStruct struct {}";
                OOPSourceCodeModel oldModel = createSingleGoStructModel(oldCode, "OldStruct");
                OOPSourceCodeModel newModel = createSingleGoStructModel(newCode, "DifferentStruct");
                Optional<MetricChange> maybeChange = analyzeChange(oldModel, newModel, new HashSet<>(),
                                "main.OldStruct");
                if (!maybeChange.isPresent()) {
                        maybeChange = analyzeChange(oldModel, newModel, new HashSet<>(), "OldStruct");
                }
                assertTrue("OldStruct missing in new => expect a valid MetricChange with new=0.0.",
                                maybeChange.isPresent());
        }

        @Test
        public void cornerCaseClassMissingInNewModel() throws CompileException {
                OOPSourceCodeModel oldModel = createSingleJavaClassModel("test", "ClassA");
                OOPSourceCodeModel newModel = createEmptyJavaModel();
                Optional<MetricChange> maybeChange = analyzeChange(oldModel, newModel, new HashSet<>(), "test.ClassA");
                assertTrue("ClassA only in old => MetricChange should exist with new=0.0 metrics.",
                                maybeChange.isPresent());
        }

        @Test
        public void cornerCaseClassMissingInBothModels() throws CompileException {
                OOPSourceCodeModel oldModel = createEmptyJavaModel();
                OOPSourceCodeModel newModel = createEmptyJavaModel();
                // Assert that IllegalArgumentException is thrown
                assertThrows(IllegalArgumentException.class, () -> {
                        analyzeChange(oldModel, newModel, new HashSet<>(), "test.ClassA");
                });
        }

        @Test
        public void cornerCaseClassPresentButNotInTargetSet() throws CompileException {
                OOPSourceCodeModel oldModel = createSingleJavaClassModel("test", "ClassA");
                OOPSourceCodeModel newModel = createSingleJavaClassModel("test", "ClassA");
                Set<String> target = Collections.singleton("test.NonExistent");
                // Assert that IllegalArgumentException is thrown
                assertThrows(IllegalArgumentException.class, () -> {
                        analyzeChange(oldModel, newModel, target, "test.ClassA");
                });
        }

        @Test
        public void cornerCaseInvalidClassName() throws CompileException {
                OOPSourceCodeModel oldModel = createSingleJavaClassModel("test", "ClassA");
                OOPSourceCodeModel newModel = createSingleJavaClassModel("test", "ClassA");
                // Assert that IllegalArgumentException is thrown
                assertThrows(IllegalArgumentException.class, () -> {
                        analyzeChange(oldModel, newModel, new HashSet<>(), "test.NoSuchClass");
                });
        }

        @Test
        public void detailedMetricChangesForClassB() throws CompileException {
                OOPSourceCodeModel oldModel = createDetailedOldModel();
                OOPSourceCodeModel newModel = createDetailedNewModel();
                Optional<MetricChange> maybeChange = analyzeChange(oldModel, newModel, new HashSet<>(), "test.ClassB");
                assertTrue("ClassB must exist in both old & new with changed metrics.",
                                maybeChange.isPresent());

                MetricChange change = maybeChange.get();

                // Example numeric checks: oldNOC=0->1, oldDIT=2->3, oldWMC=2->5, etc.
                assertEquals(0.0, change.oldNOC(), 1e-9);
                assertEquals(1.0, change.updatedNOC(), 1e-9);

                assertEquals(2.0, change.oldDIT(), 1e-9);
                assertEquals(3.0, change.updatedDIT(), 1e-9);

                assertEquals(2.0, change.oldWMC(), 1e-9);
                assertEquals(5.0, change.updatedWMC(), 1e-9);

                assertEquals(0.0, change.oldAC(), 1e-9);
                assertEquals(1.0, change.updatedAC(), 1e-9);

                assertEquals(3.0, change.oldEC(), 1e-9);
                assertEquals(3.0, change.updatedEC(), 1e-9);

                assertEquals(0.0, change.oldEncapsulation(), 1e-9);
                assertEquals(0.5, change.updatedEncapsulation(), 1e-9);
        }

        private Optional<MetricChange> analyzeChange(
                        OOPSourceCodeModel oldModel,
                        OOPSourceCodeModel newModel,
                        Set<String> targetComponents,
                        String className) {
                OOPMetricsChangeAnalyzer analyzer = new OOPMetricsChangeAnalyzer(oldModel, newModel, targetComponents);
                return analyzer.analyzeChanges(className);
        }

        private OOPSourceCodeModel createEmptyJavaModel() throws CompileException {
                ProjectFiles emptyFiles = new ProjectFiles();
                return new ClarpseProject(emptyFiles, Lang.JAVA).result().model();
        }

        private OOPSourceCodeModel createSingleJavaClassModel(String pkgName, String className)
                        throws CompileException {
                String javaSource = "package " + pkgName + ";\n" +
                                "public class " + className + " {\n" +
                                "}\n";

                ProjectFiles files = new ProjectFiles();
                files.insertFile(new ProjectFile("/" + className + ".java", javaSource));

                return new ClarpseProject(files, Lang.JAVA).result().model();
        }

        private OOPSourceCodeModel createSingleGoStructModel(String goSource, String fileHint) throws CompileException {
                ProjectFiles rawData = new ProjectFiles();
                rawData.insertFile(new ProjectFile("/src/main/" + fileHint + ".go", goSource));
                rawData.insertFile(new ProjectFile("/src/go.mod", "module module/module/module"));

                ClarpseProject parseService = new ClarpseProject(rawData, Lang.GOLANG);
                return parseService.result().model();
        }

        private OOPSourceCodeModel createDetailedOldModel() throws CompileException {
                ProjectFiles oldFiles = new ProjectFiles();

                String classA = "package test;\npublic class ClassA {}";
                oldFiles.insertFile(new ProjectFile("/ClassA.java", classA));

                String classC = "package test;\npublic class ClassC {}";
                oldFiles.insertFile(new ProjectFile("/ClassC.java", classC));

                String classB = "package test;\n" +
                                "public class ClassB extends ClassA {\n" +
                                "    public ClassB() {\n" +
                                "       ClassC c = new ClassC();\n" +
                                "    }\n" +
                                "    public void someMethod() {\n" +
                                "       if(true) System.out.println(\"Hello\");\n" +
                                "    }\n" +
                                "}";
                oldFiles.insertFile(new ProjectFile("/ClassB.java", classB));

                return new ClarpseProject(oldFiles, Lang.JAVA).result().model();
        }

        private OOPSourceCodeModel createDetailedNewModel() throws CompileException {
                ProjectFiles newFiles = new ProjectFiles();

                String classA = "package test;\npublic class ClassA extends ClassC {}";
                newFiles.insertFile(new ProjectFile("/ClassA.java", classA));

                String classC = "package test;\npublic class ClassC {}";
                newFiles.insertFile(new ProjectFile("/ClassC.java", classC));

                String classD = "package test;\n" +
                                "public class ClassD extends ClassB {\n" +
                                "    public ClassD() {\n" +
                                "       ClassC c = new ClassC();\n" +
                                "    }\n" +
                                "}";
                newFiles.insertFile(new ProjectFile("/ClassD.java", classD));

                String classB = "package test;\n" +
                                "public class ClassB extends ClassA {\n" +
                                "    private int hidden;\n" +
                                "    public ClassB() {\n" +
                                "       ClassC c = new ClassC();\n" +
                                "    }\n" +
                                "    public void someMethod() {\n" +
                                "       if(true)  System.out.println(\"Hello\");\n" +
                                "       if(false) System.out.println(\"World\");\n" +
                                "    }\n" +
                                "    protected void anotherMethod() {\n" +
                                "       for(int i=0; i<2; i++) { System.out.println(i); }\n" +
                                "    }\n" +
                                "}";
                newFiles.insertFile(new ProjectFile("/ClassB.java", classB));

                return new ClarpseProject(newFiles, Lang.JAVA).result().model();
        }
}
