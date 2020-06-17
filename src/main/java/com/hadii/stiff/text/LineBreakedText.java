package com.hadii.stiff.text;

import org.apache.commons.lang.WordUtils;

/**
 * A piece of text with line breaks in it. Assumes input Text object does not
 * have any existing line breaks.
 *
 */
class LineBreakedText implements Text {

    private final Text text;
    private final int maxCharsPerLine;

    LineBreakedText(Text text, int maxCharsPerLine) {
        this.text = text;
        this.maxCharsPerLine = maxCharsPerLine;
    }

    @Override
    public String value() {
        return WordUtils.wrap(this.text.value(), this.maxCharsPerLine).trim();
    }
}
