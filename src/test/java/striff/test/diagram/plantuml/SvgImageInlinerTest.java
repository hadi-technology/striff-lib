package striff.test.diagram.plantuml;

import com.hadi.striff.diagram.plantuml.SvgImageInliner;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SvgImageInlinerTest {

    private String encodeSvg(String svg) {
        return Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void inlinesImageWithXlinkHref() {
        String innerSvg = "<svg height=\"20\" width=\"102\" xmlns=\"http://www.w3.org/2000/svg\">"
                + "<rect width=\"59\" height=\"20\" fill=\"#24292E\"/>"
                + "</svg>";
        String base64 = encodeSvg(innerSvg);

        String input = "<svg><image height=\"20\" width=\"102\" x=\"10\" y=\"20\" "
                + "xlink:href=\"data:image/svg+xml;base64," + base64 + "\"/></svg>";

        String result = SvgImageInliner.inlineSvgImages(input);

        assertFalse("Should not contain <image element", result.contains("<image"));
        assertTrue("Should contain <g transform", result.contains("<g transform=\"translate(10,20)"));
        assertTrue("Should contain inner rect", result.contains("fill=\"#24292E\""));
    }

    @Test
    public void inlinesImageWithHref() {
        String innerSvg = "<svg height=\"20\" width=\"100\" xmlns=\"http://www.w3.org/2000/svg\">"
                + "<text x=\"50\" y=\"14\">Hello</text>"
                + "</svg>";
        String base64 = encodeSvg(innerSvg);

        String input = "<svg><image height=\"20\" width=\"100\" x=\"5\" y=\"10\" "
                + "href=\"data:image/svg+xml;base64," + base64 + "\"/></svg>";

        String result = SvgImageInliner.inlineSvgImages(input);

        assertFalse("Should not contain <image element", result.contains("<image"));
        assertTrue("Should contain <g transform", result.contains("<g transform=\"translate(5,10)"));
        assertTrue("Should contain inner text", result.contains("Hello"));
    }

    @Test
    public void deduplicatesInnerIds() {
        String innerSvg = "<svg height=\"20\" width=\"102\" xmlns=\"http://www.w3.org/2000/svg\">"
                + "<linearGradient id=\"s\" x2=\"0\" y2=\"100%\"><stop offset=\"0\"/>"
                + "<stop offset=\"1\"/></linearGradient>"
                + "<rect fill=\"url(#s)\" width=\"59\" height=\"20\"/>"
                + "</svg>";
        String base64 = encodeSvg(innerSvg);

        String input = "<svg><image x=\"0\" y=\"0\" width=\"102\" height=\"20\" "
                + "xlink:href=\"data:image/svg+xml;base64," + base64 + "\"/></svg>";

        String result = SvgImageInliner.inlineSvgImages(input);

        assertTrue("ID should be de-duplicated", result.contains("id=\"i_0_s\""));
        assertTrue("url reference should be updated", result.contains("url(#i_0_s)"));
        assertFalse("Should not contain original id=\"s\" standalone",
                result.matches(".*\\bid\\s*=\\s*\"s\".*"));
    }

    @Test
    public void handlesMultipleImages() {
        String innerSvg1 = "<svg height=\"20\" width=\"50\"><rect id=\"r\" width=\"50\" "
                + "height=\"20\" fill=\"red\"/></svg>";
        String innerSvg2 = "<svg height=\"20\" width=\"60\"><rect id=\"r\" width=\"60\" "
                + "height=\"20\" fill=\"blue\"/></svg>";
        String base64_1 = encodeSvg(innerSvg1);
        String base64_2 = encodeSvg(innerSvg2);

        String input = "<svg>"
                + "<image x=\"10\" y=\"20\" width=\"50\" height=\"20\" "
                + "xlink:href=\"data:image/svg+xml;base64," + base64_1 + "\"/>"
                + "<image x=\"100\" y=\"20\" width=\"60\" height=\"20\" "
                + "xlink:href=\"data:image/svg+xml;base64," + base64_2 + "\"/>"
                + "</svg>";

        String result = SvgImageInliner.inlineSvgImages(input);

        assertTrue("First image should have i_0 prefix", result.contains("id=\"i_0_r\""));
        assertTrue("Second image should have i_1 prefix", result.contains("id=\"i_1_r\""));
        assertTrue("Should contain red fill", result.contains("fill=\"red\""));
        assertTrue("Should contain blue fill", result.contains("fill=\"blue\""));
    }

    @Test
    public void preservesNonSvgImages() {
        String input = "<svg><image x=\"0\" y=\"0\" width=\"16\" height=\"16\" "
                + "xlink:href=\"data:image/png;base64,iVBORw0KGgo=\"/></svg>";

        String result = SvgImageInliner.inlineSvgImages(input);

        assertTrue("PNG image should be preserved", result.contains("<image"));
        assertTrue("PNG data URI should be preserved",
                result.contains("data:image/png;base64,"));
    }

    @Test
    public void handlesMissingDimensions() {
        String innerSvg = "<svg><rect width=\"50\" height=\"20\" fill=\"#ccc\"/></svg>";
        String base64 = encodeSvg(innerSvg);

        String input = "<svg><image x=\"5\" y=\"10\" "
                + "xlink:href=\"data:image/svg+xml;base64," + base64 + "\"/></svg>";

        String result = SvgImageInliner.inlineSvgImages(input);

        assertFalse("Should not contain <image element", result.contains("<image"));
        assertTrue("Should still contain inner rect", result.contains("fill=\"#ccc\""));
    }

    @Test
    public void returnsInputUnchangedWhenNoImages() {
        String input = "<svg><rect width=\"100\" height=\"100\"/></svg>";
        String result = SvgImageInliner.inlineSvgImages(input);
        assertEquals(input, result);
    }

    @Test
    public void handlesNullInput() {
        assertEquals(null, SvgImageInliner.inlineSvgImages(null));
    }

    @Test
    public void handlesEmptyInput() {
        String result = SvgImageInliner.inlineSvgImages("");
        assertEquals("", result);
    }

    @Test
    public void computesCorrectScale() {
        // Inner SVG is 200x40, image element says 100x20 => scale should be 0.5,0.5
        String innerSvg = "<svg height=\"40\" width=\"200\" xmlns=\"http://www.w3.org/2000/svg\">"
                + "<rect width=\"200\" height=\"40\" fill=\"#000\"/>"
                + "</svg>";
        String base64 = encodeSvg(innerSvg);

        String input = "<svg><image height=\"20\" width=\"100\" x=\"0\" y=\"0\" "
                + "xlink:href=\"data:image/svg+xml;base64," + base64 + "\"/></svg>";

        String result = SvgImageInliner.inlineSvgImages(input);

        assertFalse("Should not contain <image", result.contains("<image"));
        assertTrue("Should contain scale", result.contains("scale(0.5000,0.5000)"));
    }

    @Test
    public void inlinesRealBadgeSvg() {
        // Simulate a real shields.io badge
        String innerSvg = "<svg height=\"20\" width=\"102\" "
                + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
                + "xmlns=\"http://www.w3.org/2000/svg\" >"
                + "<linearGradient id=\"s\" x2=\"0\" y2=\"100%\">"
                + "<stop offset=\"0\" stop-color=\"#bbb\" stop-opacity=\".1\"/>"
                + "<stop offset=\"1\" stop-opacity=\".1\"/>"
                + "</linearGradient>"
                + "<clipPath id=\"r\"><rect width=\"102\" height=\"20\" rx=\"3\" "
                + "fill=\"#fff\"/></clipPath>"
                + "<g transform=\"scale(1)\">"
                + "<rect width=\"59\" height=\"20\" fill=\"#24292E\"/>"
                + "<rect x=\"59\" width=\"43\" height=\"20\" fill=\"#bef5cb\"/>"
                + "<rect width=\"102\" height=\"20\" fill=\"url(#s)\"/>"
                + "</g>"
                + "<g fill=\"#fff\" text-anchor=\"middle\" "
                + "font-family=\"Verdana,Geneva,DejaVu Sans,sans-serif\" "
                + "text-rendering=\"geometricPrecision\" font-size=\"110\" "
                + "transform=\"scale(1)\">"
                + "<text x=\"305\" y=\"140\" transform=\"scale(.1)\" "
                + "textLength=\"490\">WMC: 18</text>"
                + "<text fill=\"#000\" x=\"795\" y=\"140\" transform=\"scale(.1)\" "
                + "textLength=\"330\">+29%</text>"
                + "</g></svg>";
        String base64 = encodeSvg(innerSvg);

        String input = "<svg><image height=\"20\" width=\"102\" x=\"2602.62\" "
                + "xlink:href=\"data:image/svg+xml;base64," + base64 + "\" y=\"928\"/></svg>";

        String result = SvgImageInliner.inlineSvgImages(input);

        assertFalse("Should not contain <image", result.contains("<image"));
        assertTrue("Should contain translate", result.contains("translate(2602.62,928)"));
        assertTrue("Should contain badge text WMC: 18", result.contains("WMC: 18"));
        assertTrue("Should contain change percentage +29%", result.contains("+29%"));
        assertTrue("Gradient ID should be de-duplicated", result.contains("id=\"i_0_s\""));
        assertTrue("ClipPath ID should be de-duplicated", result.contains("id=\"i_0_r\""));
        assertTrue("Gradient url ref should be updated", result.contains("url(#i_0_s)"));
    }

    @Test
    public void flattensStrokeStyleAttributes() {
        String input = "<svg><line style=\"stroke:#24292E;stroke-width:1;\" "
                + "x1=\"0\" x2=\"100\" y1=\"0\" y2=\"0\"/></svg>";

        String result = SvgImageInliner.inlineSvgImages(input);

        assertFalse("Should not contain style attr on line",
                result.contains("style=\""));
        assertTrue("Should have stroke attr",
                result.contains("stroke=\"#24292E\""));
        assertTrue("Should have stroke-width attr",
                result.contains("stroke-width=\"1\""));
    }

    @Test
    public void flattensMultipleStyleProperties() {
        String input = "<svg><rect style=\"stroke:#A0A0A0;stroke-width:1;\" "
                + "width=\"100\" height=\"20\"/></svg>";

        String result = SvgImageInliner.inlineSvgImages(input);

        assertFalse("Should not contain stroke style attr",
                result.contains("style=\"stroke:"));
        assertTrue("Should have stroke attr",
                result.contains("stroke=\"#A0A0A0\""));
        assertTrue("Should have stroke-width attr",
                result.contains("stroke-width=\"1\""));
    }

    @Test
    public void removesStyleAttrWhenStrokeNone() {
        String input = "<svg><rect fill=\"#F8F8F8\" "
                + "style=\"stroke:none;stroke-width:1;\" "
                + "width=\"100\" height=\"20\"/></svg>";

        String result = SvgImageInliner.inlineSvgImages(input);

        assertFalse("Should not contain style attr",
                result.contains("style=\""));
        assertTrue("Should have stroke=\"none\"",
                result.contains("stroke=\"none\""));
        assertTrue("Should have stroke-width",
                result.contains("stroke-width=\"1\""));
    }

    @Test
    public void preservesNonStrokeStyle() {
        // Non-stroke style attributes (like on root <svg>) should be kept
        String input = "<svg style=\"width:100px;height:50px;"
                + "background:#F8F8F8;\"><rect/></svg>";

        String result = SvgImageInliner.inlineSvgImages(input);

        assertTrue("Should keep non-stroke style",
                result.contains("style=\"width:100px"));
    }

    @Test
    public void flattensRealPlantUmlOutput() {
        String input = "<svg><rect fill=\"#F8F8F8\" height=\"143\" "
                + "style=\"stroke:#24292E;stroke-width:1;\" width=\"553\" "
                + "x=\"582\" y=\"34\"/>"
                + "<line style=\"stroke:#24292E;stroke-width:1;\" "
                + "x1=\"583\" x2=\"1134\" y1=\"66\" y2=\"66\"/>"
                + "<path d=\"M16.5,4 L370\" fill=\"#E0E0E0\" "
                + "style=\"stroke:#E0E0E0;stroke-width:1.5;\"/>"
                + "<polygon fill=\"#464646\" "
                + "style=\"stroke:#464646;stroke-width:1;\" "
                + "points=\"828,212 834,205 829,208\"/>"
                + "</svg>";

        String result = SvgImageInliner.inlineSvgImages(input);

        assertFalse("Should not contain stroke style attrs",
                result.contains("style=\"stroke:"));
        assertTrue("Rect should have stroke attr",
                result.contains("stroke=\"#24292E\""));
        assertTrue("Line should have stroke attr",
                result.contains("stroke=\"#24292E\""));
        assertTrue("Path should have stroke-width attr",
                result.contains("stroke-width=\"1.5\""));
        assertTrue("Polygon should have stroke attr",
                result.contains("stroke=\"#464646\""));
    }
}
