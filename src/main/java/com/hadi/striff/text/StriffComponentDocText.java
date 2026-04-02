package com.hadi.striff.text;

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
        Text normalizedDocText = new PlantUMLFriendlyText(
                new HtmlTagsStrippedText(
                        new InlineCodeMarkedText(
                                new DocCommentCharacterStrippedText(
                                        new DefaultText(this.text.trim())))));
        return new InlineCodeBackgroundText(
                new BoldedLineText(
                        new LineBreakedText(
                                new NormalizedSpaceText(normalizedDocText),
                                lineLength)))
                .value();
    }
}
