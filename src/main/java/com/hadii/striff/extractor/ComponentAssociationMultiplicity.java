package com.hadii.striff.extractor;

import com.hadii.striff.diagram.DiagramConstants;

/**
 * Represents the multiplicity that can exist between two classes, implied
 * context is a UML Class Diagram.
 *
 * @author Muntazir Fadhel
 *
 */
public class ComponentAssociationMultiplicity {

    /**
     *
     */
    private String value;

    /**
     * @param m DefaultClassMultiplicites.
     */
    public ComponentAssociationMultiplicity(final DiagramConstants.DefaultClassMultiplicities m) {
        this.value = m.value();
    }

    public final String value() {
        return this.value;
    }

    /**
     * @param multiplicityValue value.
     */
    public final void setValue(final String multiplicityValue) {
        this.value = multiplicityValue;
    }
}
