package com.clarity.binary.extractor;

import com.clarity.binary.diagram.DiagramComponent;
import com.clarity.binary.diagram.DiagramConstants.BinaryClassAssociation;

import java.util.Set;

/**
 * Within a source class, there are many references or 'links' to external
 * classes (uni-directional)- either through class fields declarations or method
 * invocations. This class represents such a type of link.
 *
 * @author Muntazir Fadhel
 *
 */
public class ExternalClassLink {

    private final DiagramComponent orignalClass;
    private final DiagramComponent targetClass;
    // linkTargetMultiplicity of the link..
    private final BinaryClassMultiplicity targetClassMultiplicity;
    private final Set<String>             modifierContexts;
    private final BinaryClassAssociation  associationType;

    /**
     * @param originalClass
     *            original class
     * @param linkTargetClass
     *            target class
     * @param linkTargetClassMultiplicity
     *            multiplicity from origin to target
     * @param set
     *            modifiers
     * @param associationType
     */
    public ExternalClassLink(final DiagramComponent originalClass, final DiagramComponent linkTargetClass,
                             final BinaryClassMultiplicity linkTargetClassMultiplicity,
                             final Set<String> set, final BinaryClassAssociation associationType) {

        targetClassMultiplicity = (linkTargetClassMultiplicity);
        orignalClass = (originalClass);
        targetClass = (linkTargetClass);
        this.modifierContexts = set;
        this.associationType = (associationType);
    }

    public Set<String> getModifierContexts() {
        return modifierContexts;
    }

    public BinaryClassMultiplicity getTargetClassMultiplicity() {
        return targetClassMultiplicity;
    }


    public DiagramComponent getTargetClass() {
        return targetClass;
    }

    public DiagramComponent getOrignalClass() {
        return orignalClass;
    }

    public BinaryClassAssociation getAssociationType() {
        return associationType;
    }
}
