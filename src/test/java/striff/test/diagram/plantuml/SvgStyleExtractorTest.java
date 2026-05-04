package striff.test.diagram.plantuml;

import com.hadi.striff.diagram.plantuml.SvgStyleExtractor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SvgStyleExtractorTest {

    @Test
    public void convertsStrokeStylesToAttributes() {
        String svg = "<line style=\"stroke:#24292E;stroke-width:1;\" x1=\"6\" x2=\"157\" y1=\"332\" y2=\"332\"/>";
        String result = SvgStyleExtractor.extractStyles(svg);
        assertTrue(result.contains("stroke=\"#24292E\""));
        assertTrue(result.contains("stroke-width=\"1\""));
        assertFalse(result.contains("style="));
    }

    @Test
    public void convertsStrokeDasharray() {
        String svg = "<path d=\"M10,20 L30,40\" style=\"stroke:#464646;stroke-width:1;stroke-dasharray:7,7;\" fill=\"none\"/>";
        String result = SvgStyleExtractor.extractStyles(svg);
        assertTrue(result.contains("stroke=\"#464646\""));
        assertTrue(result.contains("stroke-width=\"1\""));
        assertTrue(result.contains("stroke-dasharray=\"7,7\""));
        assertFalse(result.contains("style="));
    }

    @Test
    public void keepsNonConvertiblePropertiesInStyle() {
        String svg = "<svg style=\"width: 2300px; height: 1322px; background: rgb(248, 248, 248);\" viewBox=\"0 0 2300 1322\"/>";
        String result = SvgStyleExtractor.extractStyles(svg);
        assertTrue(result.contains("style=\"width: 2300px; height: 1322px; background: rgb(248, 248, 248)\""));
        assertFalse(result.contains("stroke="));
    }

    @Test
    public void handlesMixedConvertibleAndNonConvertible() {
        String svg = "<rect style=\"stroke:#24292E;stroke-width:1;transform: scale(0.5);\" width=\"100\" height=\"50\"/>";
        String result = SvgStyleExtractor.extractStyles(svg);
        assertTrue(result.contains("stroke=\"#24292E\""));
        assertTrue(result.contains("stroke-width=\"1\""));
        assertTrue(result.contains("style=\"transform: scale(0.5)\""));
    }

    @Test
    public void passesThroughSvgWithoutStyles() {
        String svg = "<svg><rect width=\"100\" height=\"50\" fill=\"blue\"/></svg>";
        String result = SvgStyleExtractor.extractStyles(svg);
        assertEquals(svg, result);
    }

    @Test
    public void handlesMultipleElements() {
        String svg = "<svg>"
                + "<line style=\"stroke:#24292E;stroke-width:1;\" x1=\"0\" x2=\"100\" y1=\"0\" y2=\"0\"/>"
                + "<rect style=\"stroke:none;stroke-width:1;\" width=\"50\" height=\"30\"/>"
                + "<path d=\"M0,0\" style=\"stroke:#00CC00;stroke-width:1;\" fill=\"none\"/>"
                + "</svg>";
        String result = SvgStyleExtractor.extractStyles(svg);
        assertTrue(result.contains("stroke=\"#24292E\" stroke-width=\"1\""));
        assertTrue(result.contains("stroke=\"none\" stroke-width=\"1\""));
        assertTrue(result.contains("stroke=\"#00CC00\" stroke-width=\"1\""));
        assertFalse(result.contains("style="));
    }

    @Test
    public void removesStyleAttributeWhenFullyConverted() {
        String svg = "<line style=\"stroke:#24292E;stroke-width:1;\" x1=\"0\" x2=\"100\" y1=\"0\" y2=\"0\"/>";
        String result = SvgStyleExtractor.extractStyles(svg);
        assertFalse(result.contains("style"));
    }

    @Test
    public void handlesFontProperties() {
        String svg = "<text style=\"font-family:Consolas; font-size:14px;\" x=\"10\" y=\"20\">Hello</text>";
        String result = SvgStyleExtractor.extractStyles(svg);
        assertTrue(result.contains("font-family=\"Consolas\""));
        assertTrue(result.contains("font-size=\"14px\""));
        assertFalse(result.contains("style="));
    }

    @Test
    public void preservesExistingNativeAttributes() {
        String svg = "<path d=\"M10,20\" fill=\"none\" style=\"stroke:#00CC00;stroke-width:1;\"/>";
        String result = SvgStyleExtractor.extractStyles(svg);
        assertTrue(result.contains("fill=\"none\""));
        assertTrue(result.contains("stroke=\"#00CC00\""));
    }
}
