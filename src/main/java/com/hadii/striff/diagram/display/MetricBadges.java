package com.hadii.striff.diagram.display;

import io.github.dsibilio.badgemaker.core.BadgeFormatBuilder;
import io.github.dsibilio.badgemaker.core.BadgeMaker;
import io.github.dsibilio.badgemaker.model.BadgeFormat;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.transcoder.TranscoderException;
import com.hadii.striff.metrics.MetricChange;

public class MetricBadges {
    private final DiagramColorScheme colorScheme;
    public static final String BADGE_SEPARATOR = " ";

    public MetricBadges(DiagramColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public String generateBadge(String metricName, double oldValue, double newValue, boolean isDeleted,
            boolean isAdded) {
        double change = calculateChange(oldValue, newValue);
        String leftText = createLeftText(metricName, newValue);
        String rightText = formatChange(change, oldValue, newValue, isDeleted, isAdded);
        String messageColor = getColorForChange(change);
        BadgeFormat badgeFormat = new BadgeFormatBuilder(rightText)
                .withLabel(leftText)
                .withLabelColor(() -> colorScheme.classHeaderBackgroundColor())
                .withMessageColor(() -> messageColor)
                .build();
        return BadgeMaker.makeBadge(badgeFormat);
    }

    private String createLeftText(String metricName, double newValue) {
        if (newValue == (long) newValue) {
            return String.format("%s: %d", metricName, (long) newValue);
        }
        return String.format("%s: %.1f", metricName, newValue);
    }

    public String metricBadges(MetricChange metricChange, boolean isDeleted, boolean isAdded)
            throws IOException, TranscoderException {
        StringBuilder badges = new StringBuilder();
        if (shouldShowBadge(metricChange.oldNOC(), metricChange.updatedNOC())) {
            double nocValue = getMetricValue(metricChange.oldNOC(), metricChange.updatedNOC(), isDeleted,
                    isAdded);

            badges.append(touchUpSVG(generateBadge("NOC", metricChange.oldNOC(), nocValue, isDeleted, isAdded)))
                    .append(BADGE_SEPARATOR);
        }
        if (shouldShowBadge(metricChange.oldDIT(), metricChange.updatedDIT())) {
            double ditValue = getMetricValue(metricChange.oldDIT(), metricChange.updatedDIT(), isDeleted,
                    isAdded);
            badges.append(touchUpSVG(generateBadge("DIT", metricChange.oldDIT(), ditValue, isDeleted, isAdded)))
                    .append(BADGE_SEPARATOR);
        }

        double wmcValue = getMetricValue(metricChange.oldWMC(), metricChange.updatedWMC(), isDeleted,
                isAdded);
        badges.append(touchUpSVG(generateBadge("WMC", metricChange.oldWMC(), wmcValue, isDeleted, isAdded)))
                .append(BADGE_SEPARATOR);
        double encValue = getMetricValue(metricChange.oldEncapsulation(), metricChange.updatedEncapsulation(),
                isDeleted, isAdded);
        badges.append(
                touchUpSVG(generateBadge("ENC", metricChange.oldEncapsulation(), encValue, isDeleted, isAdded)))
                .append(BADGE_SEPARATOR);
        double acValue = getMetricValue(metricChange.oldAC(), metricChange.updatedAC(), isDeleted, isAdded);
        badges.append(touchUpSVG(generateBadge("AC", metricChange.oldAC(), acValue, isDeleted, isAdded)))
                .append(BADGE_SEPARATOR);
        double ecValue = getMetricValue(metricChange.oldEC(), metricChange.updatedEC(), isDeleted, isAdded);
        badges.append(touchUpSVG(generateBadge("EC", metricChange.oldEC(), ecValue, isDeleted, isAdded)));
        return badges.toString().trim() + "\n--\n";
    }

    private double calculateChange(double oldValue, double newValue) {
        if (oldValue == 0) {
            return 0;
        }
        return ((newValue - oldValue) / oldValue) * 100;
    }

    private String formatChange(double change, double oldValue, double newValue, boolean isDeleted, boolean isAdded) {
        if (isDeleted || isAdded || oldValue == newValue) {
            return new String(Character.toChars(0x2064));
        }
        return String.format("%+.0f%%", change);
    }

    private String getColorForChange(double change) {
        if (change > 0) {
            return colorScheme.addedComponentColor();
        }
        if (change < 0) {
            return colorScheme.deletedComponentColor();
        }
        return colorScheme.classHeaderBackgroundColor();
    }

    private boolean shouldShowBadge(double oldValue, double newValue) {
        return oldValue != newValue || oldValue > 1 || newValue > 1;
    }

    private double getMetricValue(double oldValue, double newValue, boolean isDeleted, boolean isAdded) {
        if (isDeleted) {
            return oldValue;
        }
        if (isAdded) {
            return newValue;
        }
        return newValue;
    }

    private String touchUpSVG(String svg) throws IOException, TranscoderException {
        svg = scaleSVG(cleanupPercentageText(removeEmptyImageTags(svg), "#000"), "0.9")
                .replace("clip-path=\"url(#r)\"", "");
        if (svg.contains(new String(Character.toChars(0x2064)))) {
            // No percentage change visible, remove right rectangle
            svg = removeRightRectangle(svg);
        }
        byte[] encodedBytes = Base64.getEncoder().encode(svg.getBytes(StandardCharsets.UTF_8));
        return "<img:data:image/svg+xml;base64," + new String(encodedBytes, StandardCharsets.UTF_8) + ">";
    }

    private String removeEmptyImageTags(String svg) {
        return svg.replaceAll("<image[^>]*xlink:href=['\"]['\"][^>]*>", "");
    }

    private String cleanupPercentageText(String svg, String rightTextColor) {
        Pattern pattern = Pattern.compile("(<text .*?>)(.*?)(</text>)");
        Matcher matcher = pattern.matcher(svg);
        StringBuffer result = new StringBuffer();
        int count = 0;
        while (matcher.find()) {
            String openingTag = matcher.group(1);
            String content = matcher.group(2);
            String closingTag = matcher.group(3);
            count++;
            if (count == 4) {
                openingTag = openingTag.replace("<text ", "<text fill=\"" + rightTextColor + "\" ");
            }
            if (count == 3) {
                content = "";
            }
            matcher.appendReplacement(result, openingTag + content + closingTag);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String scaleSVG(String svg, String scaleValue) {
        return svg.replace("transform=\"scale(1)\"", "transform=\"scale(" + scaleValue + ")\"");
    }

    private String removeRightRectangle(String svg) {
        // Remove text and rect blocks associated with the right rectangle on the badge
        return removeLastNTags(removeLastNTags(svg, "text", 2), "rect", 2);
    }

    private String removeLastNTags(String svg, String tagName, int n) {
        if (n <= 0 || tagName == null || tagName.isEmpty()) {
            return svg;
        }
        Pattern pattern = Pattern.compile(
                String.format("<%s\\b[^>]*?(/>|>.*?</%s>)", Pattern.quote(tagName), Pattern.quote(tagName)),
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(svg);
        StringBuilder sb = new StringBuilder(svg);
        int[][] tagPositions = new int[sb.length()][2];
        int count = 0;
        while (matcher.find()) {
            tagPositions[count][0] = matcher.start();
            tagPositions[count][1] = matcher.end();
            count++;
        }
        if (count < n) {
            return svg;
        }
        int tagsRemoved = 0;
        for (int i = count - 1; i >= 0 && tagsRemoved < n; i--) {
            int start = tagPositions[i][0];
            int end = tagPositions[i][1];
            sb.delete(start, end);
            tagsRemoved++;
        }
        return sb.toString();
    }
}
