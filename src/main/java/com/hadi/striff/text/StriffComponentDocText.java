package com.hadi.striff.text;

/**
 * The component documentation in Striff Diagrams.
 */
public final class StriffComponentDocText implements Text {

    private final String text;
    private final int lineLength;
    private final int maxChars;

    public StriffComponentDocText(String text, int lineLength) {
        this(text, lineLength, Integer.MAX_VALUE);
    }

    /**
     * @param maxChars the most visible characters kept; a longer comment is cut at a word boundary
     *                 and ends in an ellipsis, before any markup is added.
     */
    public StriffComponentDocText(String text, int lineLength, int maxChars) {
        this.text = text;
        this.lineLength = lineLength;
        this.maxChars = maxChars;
    }

    @Override
    public String value() {
        Text normalizedDocText = new PlantUMLFriendlyText(
                new HtmlTagsStrippedText(
                        new JavadocLinkMarkedText(
                        new InlineCodeMarkedText(
                                new DocCommentCharacterStrippedText(
                                        new DefaultText(this.text.trim()))))));
        return new InlineCodeBackgroundText(
                new BoldedLineText(
                        new LineBreakedText(
                                new TruncatedText(new NormalizedSpaceText(normalizedDocText), maxChars),
                                lineLength)))
                .value();
    }
}
