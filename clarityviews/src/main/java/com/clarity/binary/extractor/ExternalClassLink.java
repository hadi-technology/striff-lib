package com.clarity.binary.extractor;

import com.clarity.binary.diagram.DiagramConstants.BinaryClassAssociation;
import com.clarity.sourcemodel.Component;

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

    private final Component               orignalClass;
    private final Component               targetClass;
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
     *            association type
     * @param set
     */
    public ExternalClassLink(final Component originalClass, final Component linkTargetClass,
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


    public Component getTargetClass() {
        return targetClass;
    }

    public Component getOrignalClass() {
        return orignalClass;
    }

    public BinaryClassAssociation getAssociationType() {
        return associationType;
    }
}
