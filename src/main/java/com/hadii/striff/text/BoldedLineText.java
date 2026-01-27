package com.hadii.striff.text;

final class BoldedLineText implements Text {

    private final Text text;

    BoldedLineText(Text text) {
        this.text = text;
    }

    @Override
    public String value() {
        String[] lines = text.value().split("\\r?\\n");
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
