package com.hadii.striff.text;

public class JavaDocSymbolStrippedText implements Text {

    private final Text text;

    public JavaDocSymbolStrippedText(Text text) {
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
