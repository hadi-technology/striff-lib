package com.hadii.striff.text;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;

final class HtmlTagsStrippedText implements Text {

    private final Text   text;
    OutputSettings settings = new OutputSettings();

    HtmlTagsStrippedText(Text text) {
        this.text = text;
        this.settings.prettyPrint(false);
    }

    @Override
    public String value() {
        return Jsoup.parse(this.text.value()).text();
    }
}