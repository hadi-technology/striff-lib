package striff.test.display;

import com.hadi.striff.diagram.display.DiagramColorScheme;
import com.hadi.striff.diagram.display.DiagramColorSchemeOverride;
import com.hadi.striff.diagram.display.LightDiagramColorScheme;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DiagramColorSchemeOverrideTest {

    @Test
    public void overrideUsesBaseWhenUnset() {
        DiagramColorScheme base = new LightDiagramColorScheme();
        DiagramColorSchemeOverride override = DiagramColorSchemeOverride.from(base);

        assertEquals(base.backgroundColor(), override.backgroundColor());
        assertEquals(base.classFontColor(), override.classFontColor());
        assertEquals(base.packageFontName(), override.packageFontName());
    }

    @Test
    public void overrideUsesCustomValuesWhenSet() {
        DiagramColorScheme base = new LightDiagramColorScheme();
        DiagramColorSchemeOverride override = DiagramColorSchemeOverride.from(base)
                .setClassFontColor("#123456")
                .setPackageFontName("Courier New");

        assertEquals("#123456", override.classFontColor());
        assertEquals("Courier New", override.packageFontName());
        assertEquals(base.backgroundColor(), override.backgroundColor());
    }
}
