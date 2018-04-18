package com.clarity.binary.extractor;

import com.clarity.binary.diagram.DiagramConstants.BinaryClassAssociation;

public class ColoredBinaryClassAssociation {

    private BinaryClassAssociation association;
    private String hexColor;

    public ColoredBinaryClassAssociation(BinaryClassAssociation association, String hexColor) {
        this.association = association;
        this.hexColor = hexColor;
    }

    public int getStrength() {
        return this.association.getStrength();
    }

    public String getAssociationLabel() {
        return this.association.getAssociationLabel();
    }

    public String getyumlLinkType() {
        if (this.hexColor != null && !this.hexColor.isEmpty()) {
            switch (association) {
            case AGGREGATION:
                return "--[" + hexColor + "]--";
            case COMPOSITION:
                return "--[" + hexColor + "]--";
            case GENERALISATION:
                return "--[" + hexColor + ",bold]--";
            case NONE:
                return "--[" + hexColor + "]--";
            case REALIZATION:
                return "--.-[" + hexColor + "]-";
            case WEAK_ASSOCIATION:
                return "--[" + hexColor + "]";
            case ASSOCIATION:
                return "--[" + hexColor + "]";
            default:
                return association.getyumlLinkType();
            }

        } else {
            return this.association.getyumlLinkType();
        }
    }

    public String getForwardLinkEndingType() {
        return this.association.getForwardLinkEndingType();
    }

    public String getBackwardLinkEndingType() {
        return this.association.getBackwardLinkEndingType();
    }
}
