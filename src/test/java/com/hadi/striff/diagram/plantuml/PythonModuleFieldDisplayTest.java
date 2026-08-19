package com.hadi.striff.diagram.plantuml;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.compiler.typescript.NodeRuntime;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.SyntheticModuleSupport;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.diagram.display.LightDiagramColorScheme;
import com.hadi.striff.extractor.RelationsMap;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Tests that Python module-level variables (fields) are displayed in striff diagrams.
 */
public class PythonModuleFieldDisplayTest {

    @Test
    public void testModuleFieldsIncludedInFieldFilter() throws Exception {
        Assume.assumeTrue(NodeRuntime.isNodeAvailable());

        // Create a Python file with module-level fields (without initial values, like type hints)
        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile("/src/config.py",
                "# Module-level configuration variables\n"
                        + "DEFAULT_TIMEOUT: int\n"
                        + "MAX_RETRIES: int\n"
                        + "\n"
                        + "class Service:\n"
                        + "    timeout: int\n"
                        + "\n"
                        + "    def __init__(self, timeout: int = DEFAULT_TIMEOUT) -> None:\n"
                        + "        self.timeout = timeout\n"
                        + "\n"
                        + "def get_config() -> int:\n"
                        + "    return DEFAULT_TIMEOUT\n"));

        ClarpseProject project = new ClarpseProject(files, Lang.PYTHON);
        OOPSourceCodeModel model = project.result().model();

        // Find the module-level field components
        Component defaultTimeoutField = model.copyOfComponent("src.config.DEFAULT_TIMEOUT").orElse(null);
        Component maxRetriesField = model.copyOfComponent("src.config.MAX_RETRIES").orElse(null);

        Assert.assertNotNull("Module field DEFAULT_TIMEOUT should be parsed", defaultTimeoutField);
        Assert.assertEquals("DEFAULT_TIMEOUT should be a MODULE_FIELD",
                OOPSourceModelConstants.ComponentType.MODULE_FIELD, defaultTimeoutField.componentType());

        Assert.assertNotNull("Module field MAX_RETRIES should be parsed", maxRetriesField);
        Assert.assertEquals("MAX_RETRIES should be a MODULE_FIELD",
                OOPSourceModelConstants.ComponentType.MODULE_FIELD, maxRetriesField.componentType());

        // Now test that the synthetic module (which contains module-level fields) displays them
        // Create a synthetic module component
        Component syntheticModule = SyntheticModuleSupport.syntheticComponent("config",
                Set.of(defaultTimeoutField.uniqueName(), maxRetriesField.uniqueName()));
        syntheticModule.setComponentName("module:config");

        DiagramComponent syntheticModuleCmp = new DiagramComponent(syntheticModule, model);

        // Create PUMLDiagramData and render the synthetic module
        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), Set.of());
        PUMLDiagramData data = new PUMLDiagramData(
                new RelationsMap(),
                new RelationsMap(),
                new RelationsMap(),
                display,
                model,
                Set.of(defaultTimeoutField.uniqueName()),
                Set.of(),
                Set.of(),
                Set.of(syntheticModuleCmp));

        // This is the key test - PUMLClassFieldsCode should render module fields
        String puml = new PUMLClassFieldsCode(data).value(Set.of(syntheticModuleCmp));

        // Verify module fields are in the output
        assertTrue("Module field DEFAULT_TIMEOUT should be visible in PlantUML output",
                puml.contains("DEFAULT_TIMEOUT"));

        assertTrue("Module field MAX_RETRIES should be visible in PlantUML output",
                puml.contains("MAX_RETRIES"));
    }

    @Test
    public void testModuleFieldsWithComplexTypes() throws Exception {
        Assume.assumeTrue(NodeRuntime.isNodeAvailable());

        ProjectFiles files = new ProjectFiles();
        files.insertFile(new ProjectFile("/src/logger.py",
                "from typing import Optional\n"
                        + "\n"
                        + "# Compute log levels once at module load for better performance\n"
                        + "_SINK_ERROR_LOG_LEVEL: int\n"
                        + "_SINK_WARNING_LOG_LEVEL: int\n"
                        + "\n"
                        + "\n"
                        + "def get_sink_error_log_level() -> int:\n"
                        + "    return 40\n"
                        + "\n"
                        + "\n"
                        + "def get_sink_warning_log_level() -> int:\n"
                        + "    return 30\n"
                        + "\n"
                        + "\n"
                        + "class Logger:\n"
                        + "    level: int\n"
                        + "\n"
                        + "    def __init__(self, level: int = _SINK_WARNING_LOG_LEVEL) -> None:\n"
                        + "        self.level = level\n"));

        ClarpseProject project = new ClarpseProject(files, Lang.PYTHON);
        OOPSourceCodeModel model = project.result().model();

        // Find the module-level field components
        Component errorLevelField = model.copyOfComponent("src.logger._SINK_ERROR_LOG_LEVEL").orElse(null);
        Component warningLevelField = model.copyOfComponent("src.logger._SINK_WARNING_LOG_LEVEL").orElse(null);

        Assert.assertNotNull("Module field _SINK_ERROR_LOG_LEVEL should be parsed", errorLevelField);
        Assert.assertEquals("_SINK_ERROR_LOG_LEVEL should be a MODULE_FIELD",
                OOPSourceModelConstants.ComponentType.MODULE_FIELD, errorLevelField.componentType());

        Assert.assertNotNull("Module field _SINK_WARNING_LOG_LEVEL should be parsed", warningLevelField);
        Assert.assertEquals("_SINK_WARNING_LOG_LEVEL should be a MODULE_FIELD",
                OOPSourceModelConstants.ComponentType.MODULE_FIELD, warningLevelField.componentType());

        // Create a synthetic module component
        Component syntheticModule = SyntheticModuleSupport.syntheticComponent("logger",
                Set.of(errorLevelField.uniqueName(), warningLevelField.uniqueName()));
        syntheticModule.setComponentName("module:logger");

        DiagramComponent syntheticModuleCmp = new DiagramComponent(syntheticModule, model);

        // Create PUMLDiagramData and render the synthetic module
        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(), Set.of());
        PUMLDiagramData data = new PUMLDiagramData(
                new RelationsMap(),
                new RelationsMap(),
                new RelationsMap(),
                display,
                model,
                Set.of(errorLevelField.uniqueName()),
                Set.of(),
                Set.of(),
                Set.of(syntheticModuleCmp));

        String puml = new PUMLClassFieldsCode(data).value(Set.of(syntheticModuleCmp));

        // Verify module fields are in the output
        assertTrue("Module field _SINK_ERROR_LOG_LEVEL should be visible in PlantUML output",
                puml.contains("_SINK_ERROR_LOG_LEVEL"));

        assertTrue("Module field _SINK_WARNING_LOG_LEVEL should be visible in PlantUML output",
                puml.contains("_SINK_WARNING_LOG_LEVEL"));
    }
}
