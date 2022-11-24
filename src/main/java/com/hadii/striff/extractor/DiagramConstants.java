package com.hadii.striff.extractor;

public final class DiagramConstants {

    public enum ComponentAssociation {
        /**
         * Composition Association.
         */
        AGGREGATION(6, "-", "o", "o"),
        /**
         * Weak Binary Association.
         */
        COMPOSITION(6, "-", "*", "*"),
        /**
         * Generalization Association.
         */
        SPECIALIZATION(8, "--", "|>", "<|"),
        NONE(-1, "-", "", ""),
        /**
         * Realization association.
         */
        REALIZATION(8, "-.-", "|>", "<|"),
        /**
         * Dependency association.
         */
        WEAK_ASSOCIATION(1, "--", ">", "<"),
        /**
         * Basic association.
         */
        ASSOCIATION(3, "--", ">", "<");

        private final String forwardLinkEndingType;
        private final String backwardLinkEndingType;
        /**
         * type of link (dotted line, solid, etc....).
         */
        private final String yumlLinkType;
        private final int strength;

        ComponentAssociation(final int strength, final String linkType,
                             final String forwardLinkEndingType, String backwardLinkEndingType) {
            yumlLinkType = linkType;
            this.forwardLinkEndingType = forwardLinkEndingType;
            this.backwardLinkEndingType = backwardLinkEndingType;
            this.strength = strength;
        }

        public int strength() {
            return strength;
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

        NONE(""), ZEROTOMANY(
                "0" + MULTIPLICITY_STRING_SEPERATOR + "*"), ZEROTOONE("0" + MULTIPLICITY_STRING_SEPERATOR + "1");

        private final String value;

        DefaultClassMultiplicities(final String sValue) {
            value = sValue;
        }

        public String value() {
            return value;
        }
    }

    /**
     * private constructor for utility class.
     */
    private DiagramConstants() {

    }
}
