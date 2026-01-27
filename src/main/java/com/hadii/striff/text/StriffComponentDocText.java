package com.hadii.striff.text;

/**
 * The component documentation in Striff Diagrams.
 */
public final class StriffComponentDocText implements Text {

    private final String text;
    private final int lineLength;

    public StriffComponentDocText(String text, int lineLength) {
        this.text = text;
        this.lineLength = lineLength;
    }

    @Override
    public String value() {
        return new BoldedLineText(
                new LineBreakedText(
                        new NormalizedSpaceText(
                                new PlantUMLFriendlyText(
                                        new DocCommentCharacterStrippedText(
                                                new HtmlTagsStrippedText(
                                                        new DefaultText(this.text.trim()))))), lineLength)).value();
    }
}
