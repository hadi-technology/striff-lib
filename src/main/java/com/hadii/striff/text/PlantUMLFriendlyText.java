package com.hadii.striff.text;

/**
 * Represents text that can be displayed in a PlantUML diagram.
 */
final class PlantUMLFriendlyText implements Text {

    private final Text text;

    PlantUMLFriendlyText(Text text) {
        this.text = text;
    }

    @Override
    public String value() {
        return this.text.value().replaceAll("\\(", "[").replaceAll("\\)", "]");
    }
}
