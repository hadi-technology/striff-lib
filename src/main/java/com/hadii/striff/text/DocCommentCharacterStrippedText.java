package com.hadii.striff.text;

final class DocCommentCharacterStrippedText implements Text {

    private final Text text;

    DocCommentCharacterStrippedText(Text text) {
        this.text = text;
    }

    @Override
    public String value() {
        return this.text.value()
            .replace("/*", "")
            .replace("*/", "")
            .replace("*", "");
    }
}
