package com.hadi.striff.diagram;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hadi.striff.ChangeSet;
import com.hadi.striff.StriffConfig;
import com.hadi.striff.annotations.LogExecutionTime;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.diagram.plantuml.PUMLDiagram;
import com.hadi.striff.diagram.plantuml.PUMLDiagramData;
import com.hadi.striff.diagram.plantuml.PUMLDrawException;
import com.hadi.striff.extractor.RelationsMap;
import com.hadi.striff.parse.CodeDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Striff diagram.
 */

public class StriffDiagram {

    private static final Logger LOGGER = LoggerFactory.getLogger(StriffDiagram.class);
    private final Set<String> containedPkgs = new HashSet<>();
    private String svgCode;
    private final Set<DiagramComponent> diagramComponents;
    private RelationsMap diagramRels;
    private ChangeSet changeSet;

    @LogExecutionTime
    public StriffDiagram(CodeDiff codeDiff, Set<DiagramComponent> diagramComponents,
            RelationsMap diagramRels, DiagramDisplay diagramDisplay, StriffConfig config)
            throws IOException, PUMLDrawException {
        this.diagramComponents = diagramComponents;
        this.diagramComponents.forEach(
                cmp -> this.containedPkgs.add(ComponentHelper.packagePath(cmp.pkg())));
        boolean skipDiagramGeneration = config.metadataOnly();
        if (this.diagramComponents.size() > config.maxComponentsPerDiagram()) {
            LOGGER.warn("Skipping PlantUML rendering for diagram with {} components (limit: {}). "
                    + "Metadata will still be returned.",
                    this.diagramComponents.size(), config.maxComponentsPerDiagram());
            skipDiagramGeneration = true;
        }
        if (!skipDiagramGeneration) {
            this.svgCode = new PUMLDiagram(new PUMLDiagramData(diagramRels, codeDiff.changeSet().addedRelations(),
                    codeDiff.changeSet().deletedRelations(), diagramDisplay,
                    codeDiff.mergedModel(), codeDiff.changeSet().addedComponents(),
                    codeDiff.changeSet().deletedComponents(), codeDiff.changeSet().modifiedComponents(),
                    diagramComponents, config.layoutEngine())).svgText();
        }
        this.diagramRels = diagramRels;
        this.changeSet = codeDiff.changeSet();
    }

    @JsonIgnore
    public String svg() {
        return this.svgCode;
    }

    /**
     * Returns the SVG code as a Base64-encoded string.
     */
    @JsonProperty("base64encodedSVGCode")
    public String base64encodedSVGCode() {
        if (svgCode == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(svgCode.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Returns the contained packages.
     */
    @JsonProperty("packages")
    public Set<String> containedPkgs() {
        return this.containedPkgs;
    }

    /**
     * Returns the diagram components.
     */
    @JsonProperty("components")
    public Set<DiagramComponent> cmps() {
        return this.diagramComponents;
    }

    /**
     * Returns the relations.
     */
    @JsonProperty("relations")
    public RelationsMap relations() {
        return this.diagramRels;
    }

    /**
     * Returns the change set.
     */
    @JsonProperty("changeSet")
    public ChangeSet changeSet() {
        return this.changeSet;
    }

    /**
     * Returns the size of the diagram (number of components).
     */
    @JsonProperty("size")
    public int size() {
        return this.diagramComponents.size();
    }
}
