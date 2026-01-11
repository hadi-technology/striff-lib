package com.hadii.striff.text;

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
        String wrapped = new LineBreakedText(
                new NormalizedSpaceText(
                        new PlantUMLFriendlyText(
                                new DocCommentCharacterStrippedText(
                                        new HtmlTagsStrippedText(
                                                new DefaultText(this.text.trim()))))), lineLength).value();
        String[] lines = wrapped.split("\\r?\\n");
        StringBuilder bolded = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            bolded.append("**").append(lines[i]).append("**");
            if (i < lines.length - 1) {
                bolded.append("\n");
            }
        }
        return bolded.toString();
    }
}
