package com.hadi.striff.extractor;

import com.hadi.striff.extractor.DiagramConstants.ComponentAssociation;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * A relationship to a repository type the model holds no component for.
 *
 * <p>A one-level analysis models the analysed files and the files they reference. A reference to
 * a type declared in the repository but not loaded, because it lies past the boundary or because a
 * budget held its file back, has no target component, so it cannot be a {@link ComponentRelation}
 * and is never drawn. It is kept here by name, with the component it starts from and the kind of
 * relation it would be, so it is not lost.
 *
 * <p>{@link Origin} says what such a relationship can tell a reader. One that starts from a
 * {@link Origin#FOCUS focus} component is a dependency of fully analysed code, known by name. One
 * that starts from a {@link Origin#BOUNDARY boundary} component is part of an outgoing reference set
 * that is incomplete by design; neither its presence nor the absence of others says anything
 * complete about that component.
 */
public final class NotLoadedRelation implements Comparable<NotLoadedRelation>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Whether the component a not-loaded relationship starts from was analysed in full. */
    public enum Origin {
        /** A component of an analysed file: its references are complete. */
        FOCUS,
        /** A component of a boundary file: its references past the boundary are incomplete. */
        BOUNDARY
    }

    private final String sourceComponent;
    private final String targetName;
    private final ComponentAssociation associationType;
    private final Origin origin;

    /**
     * Creates a not-loaded relationship.
     *
     * @param sourceComponent unique name of the base component (or synthetic module) it starts from
     * @param targetName      the name the reference gives its target
     * @param associationType the kind of relation it would be
     * @param origin          whether the starting component was analysed in full
     */
    public NotLoadedRelation(String sourceComponent, String targetName,
                             ComponentAssociation associationType, Origin origin) {
        this.sourceComponent = Objects.requireNonNull(sourceComponent, "sourceComponent");
        this.targetName = Objects.requireNonNull(targetName, "targetName");
        this.associationType = Objects.requireNonNull(associationType, "associationType");
        this.origin = Objects.requireNonNull(origin, "origin");
    }

    /** Returns the unique name of the component the relationship starts from. */
    public String sourceComponent() {
        return sourceComponent;
    }

    /** Returns the name of the repository type the relationship points to. */
    public String targetName() {
        return targetName;
    }

    /** Returns the kind of relation. */
    public ComponentAssociation associationType() {
        return associationType;
    }

    /** Returns whether the starting component was analysed in full or is a boundary component. */
    public Origin origin() {
        return origin;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof NotLoadedRelation)) {
            return false;
        }
        NotLoadedRelation that = (NotLoadedRelation) other;
        return sourceComponent.equals(that.sourceComponent) && targetName.equals(that.targetName)
                && associationType == that.associationType && origin == that.origin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceComponent, targetName, associationType, origin);
    }

    @Override
    public int compareTo(NotLoadedRelation other) {
        int bySource = sourceComponent.compareTo(other.sourceComponent);
        if (bySource != 0) {
            return bySource;
        }
        int byTarget = targetName.compareTo(other.targetName);
        if (byTarget != 0) {
            return byTarget;
        }
        int byType = associationType.compareTo(other.associationType);
        if (byType != 0) {
            return byType;
        }
        return origin.compareTo(other.origin);
    }

    @Override
    public String toString() {
        return sourceComponent + " -[" + associationType + ", not loaded, " + origin + "]-> " + targetName;
    }
}
