package com.hadii.striff.text;

final class DefaultText implements Text {

    private final String text;

    DefaultText(String text) {
        this.text = text;
    }

    @Override
    public String value() {
        return this.text;
    }
}
