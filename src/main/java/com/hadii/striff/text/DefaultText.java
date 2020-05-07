package com.hadii.striff.text;

public class DefaultText implements Text {

    private final String text;

    public DefaultText(String text) {
        this.text = text;
    }

    @Override
    public String value() {
        return this.text;
    }
}
