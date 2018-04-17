package com.clarity.binary.diagram;

public final class DiagramConstants {

    public enum BinaryClassAssociation {
        /**
         * Composition Association.
         */
        AGGREGATION(5, "Aggregates", "-", "o", "o"),
        /**
         * Weak Binary Association.
         */
        COMPOSITION(6, "Contains", "-", "*", "*"),
        /**
         * Generalization Association.
         */
        GENERALISATION(7, "Extends", "-", "|>", "<|"),
        /**
         * Dependency association.
         */
        NONE(0, "", "-", "", ""), REALIZATION(8, "Implements", "-.-", "|>", "<|"),
        /**
        *
        */
        WEAK_ASSOCIATION(2, "Uses", "--", ">", "<"), ASSOCIATION(4, "Uses", "--", ">", "<");

        /**
         * label that appears at the top of class box in the class diagram.
         */
        private final String associationLabel;
        private final String forwardLinkEndingType;
        private final String backwardLinkEndingType;
        /**
         * type of link (dotted line, solid, etc....).
         */
        private final String yumlLinkType;
        private final int strength;

        BinaryClassAssociation(final int strength, final String label, final String linkType,
                final String forwardLinkEndingType, String backwardLinkEndingType) {
            associationLabel = label;
            yumlLinkType = linkType;
            this.forwardLinkEndingType = forwardLinkEndingType;
            this.backwardLinkEndingType = backwardLinkEndingType;
            this.strength = strength;
        }

        public int getStrength() {
            return strength;
        }

        public String getAssociationLabel() {
            return associationLabel;
        }

        public String getyumlLinkType() {
            return yumlLinkType;
        }

        public String getForwardLinkEndingType() {
            return forwardLinkEndingType;
        }

        public String getBackwardLinkEndingType() {
            return backwardLinkEndingType;
        }
    }

    public static final String MULTIPLICITY_STRING_SEPERATOR = "..";

    /**
     * Represents the default class diagram multiplicities.
     *
     * @author Muntazir Fadhel
     */
    public enum DefaultClassMultiplicities {

        NONE(""), ONETOONE("1" + MULTIPLICITY_STRING_SEPERATOR + "1"), ZEROTOMANY(
                "0" + MULTIPLICITY_STRING_SEPERATOR + "*"), ZEROTOONE("0" + MULTIPLICITY_STRING_SEPERATOR + "1");

        private final String value;

        DefaultClassMultiplicities(final String sValue) {
            value = sValue;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * private constructor for utility class.
     */
    private DiagramConstants() {

    }
}
