package com.hadii.stiff.text;

/**
 * The component documentation in Striff Diagrams.
 */
public final class StiffComponentDocText implements Text {

    private final String text;
    private final int lineLength;

    public StiffComponentDocText(String text, int lineLength) {
        this.text = text;
        this.lineLength = lineLength;
    }

    @Override
    public String value() {
        return new LineBreakedText(
                new NormalizedSpaceText(
                        new PlantUMLFriendlyText(
                                new DocCommentCharacterStrippedText(
                                        new HtmlTagsStrippedText(
                                                new DefaultText(this.text.trim()))))), lineLength).value();
    }
}
