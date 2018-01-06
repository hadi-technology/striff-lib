package com.clarity.binary.extractor;

import com.clarity.binary.diagram.DiagramConstants.BinaryClassAssociation;
import com.clarity.binary.diagram.DiagramConstants.DefaultClassMultiplicities;
import com.clarity.sourcemodel.Component;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Represents the relationship between two classes, implied context is a UML
 * Class Diagram.
 * <p>
 * We maintain two arbitrary sides that exists on the ends of the relationship,
 * sides A and B. At each side in a binary class relationship, there exits a
 * class, association, multiplicity, etc.. - these are tracked and updated as
 * external link objects are received, see
 * resolveExtClassLinkWithBinaryRelationship().
 */
public class BinaryClassRelationship implements Serializable {

    private static final long serialVersionUID = -9031833990867424972L;

    private static String classNameSplitter = "<<-->>";

    /**
     * @param externalClassLink the external class link object from which to create the binary
     *                          class relationship.
     */
    public BinaryClassRelationship(final ExternalClassLink externalClassLink) {

        classA = externalClassLink.getOrignalClass();
        classB = externalClassLink.getTargetClass();
        aSideMultiplicity = new BinaryClassMultiplicity(DefaultClassMultiplicities.NONE);
        bSideMultiplicity = externalClassLink.getTargetClassMultiplicity();
        aSideAssociation = externalClassLink.getAssociationType();
        bSideAssociation = BinaryClassAssociation.NONE;
        aSideAction = aSideAssociation.getAssociationLabel();
        bSideAction = "";
        name = (generateRelationshipName(classA, classB));
    }

    public static String getClassNameSplitter() {
        return classNameSplitter;
    }

    private final Component classA;
    private final Component classB;
    private BinaryClassMultiplicity aSideMultiplicity;
    private BinaryClassMultiplicity bSideMultiplicity;
    private BinaryClassAssociation aSideAssociation;
    private BinaryClassAssociation bSideAssociation;
    private final String name;
    private String aSideAction;
    private String bSideAction;

    /**
     * @param originaClass original class
     * @param targetClass  target class.
     * @return relationship name.
     */
    public static String generateRelationshipName(final Component originaClass, final Component targetClass) {

        return originaClass.name() + classNameSplitter + targetClass.name();
    }

    public final String getaSideAction() {
        return aSideAction;
    }

    public final BinaryClassAssociation getaSideAssociation() {
        return aSideAssociation;
    }

    public final BinaryClassMultiplicity getaSideMultiplicity() {
        return aSideMultiplicity;
    }

    /**
     * @param forwardDirection true if association goes from A to B (represents direction of
     *                         arrow between to classes on a UML Class Diagram, false
     *                         otherwise.
     * @return association type of the binary class relationship.
     */
    private BinaryClassAssociation getAssociation(final boolean forwardDirection) {

        if (forwardDirection) {
            return aSideAssociation;
        } else {
            return bSideAssociation;
        }
    }

    public final String getbSideAction() {
        return bSideAction;
    }

    public final BinaryClassAssociation getbSideAssociation() {
        return bSideAssociation;
    }

    public final BinaryClassMultiplicity getbSideMultiplicity() {
        return bSideMultiplicity;
    }

    public final Component getClassA() {
        return classA;
    }

    public final Component getClassB() {
        return classB;
    }

    public final String getName() {
        return name;
    }

    /**
     * @param forwardDir         true if relationship association A -> B, false otherwise.
     * @param association        association type.
     * @param targetMultiplicity multiplicity type on the target/end side.
     */
    private void overwriteSideRelationship(final boolean forwardDir, final BinaryClassAssociation association,
                                           final BinaryClassMultiplicity targetMultiplicity) {
        if (forwardDir) {
            aSideAssociation = association;
            bSideMultiplicity = targetMultiplicity;
            aSideAction = association.getAssociationLabel();
        } else {
            bSideAssociation = association;
            aSideMultiplicity = targetMultiplicity;
            bSideAction = association.getAssociationLabel();
        }
    }

    /**
     * Updates the existing binary relationship b/w two classes to reflect the
     * newly found external class link.
     *
     * @param isDirForward         The direction of the external class link
     * @param existingRelationship The existing binary class relationship
     * @param incomingLink         the incoming external class link we need to incorporate into
     *                             the binary class relationship
     * @return
     */
    public final void resolveExtClassLink(final boolean isDirForward, final ExternalClassLink incomingLink) {

        final BinaryClassAssociation currentSideAssociation = getAssociation(isDirForward);
        if (currentSideAssociation.getStrength() < incomingLink.getAssociationType().getStrength()) {

            overwriteSideRelationship(isDirForward, incomingLink.getAssociationType(),
                    incomingLink.getTargetClassMultiplicity());
        }
    }

    @Override
    public boolean equals(Object o) {
        BinaryClassRelationship testRelationship = (BinaryClassRelationship) o;
        if (testRelationship.classA.uniqueName().equals(this.classA.uniqueName())
                && testRelationship.classB.uniqueName().equals(this.classB.uniqueName())
                && testRelationship.aSideAssociation.equals(this.aSideAssociation)
                && testRelationship.bSideAssociation.equals(this.bSideAssociation)) {
            return true;
        } else {
            return testRelationship.classA.uniqueName().equals(this.classB.uniqueName())
                    && testRelationship.classB.uniqueName().equals(this.classA.uniqueName())
                    && testRelationship.aSideAssociation.equals(this.bSideAssociation)
                    && testRelationship.bSideAssociation.equals(this.aSideAssociation);
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.classA.uniqueName()).toHashCode()
                + new HashCodeBuilder().append(this.classB.uniqueName()).toHashCode()
                + new HashCodeBuilder().append(this.bSideAction).toHashCode()
                + new HashCodeBuilder().append(this.aSideAction).toHashCode() + new HashCodeBuilder()
                .append(this.classB.uniqueName().length() + this.classA.uniqueName().length()).toHashCode();
    }
}
