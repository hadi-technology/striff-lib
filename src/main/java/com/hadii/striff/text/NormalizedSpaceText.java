package com.hadii.striff.text;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents text with normalized spacing.
 */
final class NormalizedSpaceText implements Text {

    private final Text text;

    NormalizedSpaceText(Text text) {
        this.text = text;
    }

    @Override
    public String value() {
        return StringUtils.normalizeSpace(this.text.value());
    }
}
