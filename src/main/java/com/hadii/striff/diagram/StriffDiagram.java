package com.hadii.striff.diagram;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;
import com.hadii.striff.ChangeSet;
import com.hadii.striff.StriffConfig;
import com.hadii.striff.annotations.LogExecutionTime;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.diagram.plantuml.PUMLDiagram;
import com.hadii.striff.diagram.plantuml.PUMLDiagramData;
import com.hadii.striff.diagram.plantuml.PUMLDrawException;
import com.hadii.striff.extractor.RelationsMap;
import com.hadii.striff.parse.CodeDiff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Represents a Striff diagram.
 */

public class StriffDiagram {

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
        if (!config.metadataOnly()) {
            this.svgCode = new PUMLDiagram(new PUMLDiagramData(diagramRels, codeDiff.changeSet().addedRelations(),
                    codeDiff.changeSet().deletedRelations(), diagramDisplay,
                    codeDiff.mergedModel(), codeDiff.changeSet().addedComponents(),
                    codeDiff.changeSet().deletedComponents(), codeDiff.changeSet().modifiedComponents(),
                    diagramComponents)).svgText();
        }
        this.diagramRels = diagramRels;
        this.changeSet = codeDiff.changeSet();
    }

    @JsonIgnore
    public String svg() {
        return this.svgCode;
    }

    /**
     * Returns the compressed SVG code as a GZIP + Base64-encoded string.
     */
    @JsonProperty("compressedSVG")
    public String compressedSVG() throws IOException {
        if (svgCode == null) {
            return null;
        }
        // Compress the SVG string with GZIP
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
            gzipStream.write(svgCode.getBytes(Charsets.UTF_8));
        }
        // Encode the compressed bytes in Base64
        return Base64.getEncoder().encodeToString(byteStream.toByteArray());
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

    public static String decompressAndDecodeSvg(String compressedSvg) throws IOException {
        byte[] compressedBytes = Base64.getDecoder().decode(compressedSvg);
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressedBytes))) {
            StringBuilder decompressedSvg = new StringBuilder();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) > 0) {
                decompressedSvg.append(new String(buffer, 0, len, Charsets.UTF_8));
            }
            return decompressedSvg.toString();
        }
    }
}
