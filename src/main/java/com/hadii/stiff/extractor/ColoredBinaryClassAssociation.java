package com.hadii.stiff.extractor;

import com.hadii.stiff.diagram.DiagramConstants.ComponentAssociation;

public class ColoredBinaryClassAssociation {

    private final ComponentAssociation association;
    private final String hexColor;

    public ColoredBinaryClassAssociation(ComponentAssociation association, String hexColor) {
        this.association = association;
        this.hexColor = hexColor;
    }

    public String getyumlLinkType() {
        if (this.hexColor != null && !this.hexColor.isEmpty()) {
            switch (association) {
            case AGGREGATION:
                return "--[" + hexColor + "]--";
            case COMPOSITION:
                return "--[" + hexColor + "]--";
            case SPECIALIZATION:
                return "--[" + hexColor + "]--";
            case NONE:
                return "--[" + hexColor + "]--";
            case REALIZATION:
                return "--.-[" + hexColor + "]-";
            case ASSOCIATION:
                return "--[" + hexColor + "]";
            default:
                return association.getyumlLinkType();
            }
        } else {
            return this.association.getyumlLinkType();
        }
    }
}
