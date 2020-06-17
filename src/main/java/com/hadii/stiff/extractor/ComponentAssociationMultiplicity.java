package com.hadii.stiff.extractor;

import com.hadii.stiff.diagram.DiagramConstants;

/**
 * Represents the multiplicity that can exist between two classes, implied
 * context is a UML Class Diagram.
 */
public class ComponentAssociationMultiplicity {

    private String value;

    public ComponentAssociationMultiplicity(final DiagramConstants.DefaultClassMultiplicities multiplicity) {
        this.value = multiplicity.value();
    }

    public final String value() {
        return this.value;
    }

    public final void setValue(final String multiplicityValue) {
        this.value = multiplicityValue;
    }
}
