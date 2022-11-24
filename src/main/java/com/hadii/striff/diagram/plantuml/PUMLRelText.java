package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.extractor.DiagramConstants.ComponentAssociation;

/**
 * PUML code to draw relationships.
 */
public class PUMLRelText {

    private final ComponentAssociation association;
    private final String hexColor;

    public PUMLRelText(ComponentAssociation association, String hexColor) {
        this.association = association;
        this.hexColor = hexColor;
    }

    public String text() {
        String relDescText = "";
        if (this.hexColor != null && !this.hexColor.isEmpty()) {
            relDescText += "[" + hexColor + "]";
        }
        switch (association) {
            case AGGREGATION:
            case SPECIALIZATION:
            case COMPOSITION:
            case NONE:
                return "--" + relDescText + "--";
            case REALIZATION:
                return "--.-" + relDescText + "-";
            case ASSOCIATION:
            case WEAK_ASSOCIATION:
                return "--" + relDescText + "";
            default:
                return association.getyumlLinkType();
        }
    }
}
