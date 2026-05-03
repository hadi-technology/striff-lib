package com.hadi.striff.extractor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents the multiplicity that can exist between two classes, implied
 * context is a UML Class Diagram.
 */
public class ComponentAssociationMultiplicity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
