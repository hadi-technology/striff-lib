package striff.test;

import com.hadii.clarpse.compiler.Lang;
import com.hadii.striff.StriffConfig;
import com.hadii.striff.diagram.display.OutputMode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class StriffConfigTest {

    @Test
    public void testEmptyStriffConfigDefaults()  {
        StriffConfig sC = new StriffConfig();
        assertEquals(Lang.supportedLanguages().size(), sC.languages().size());
        assertEquals(OutputMode.DEFAULT, sC.outputMode);
        assertEquals(0, sC.filesFilter.size());
    }

    @Test
    public void testGetRidofNonValidFileFilterExtns()  {
        StriffConfig sC = new StriffConfig(OutputMode.DEFAULT, List.of(new String[] {"src.java",
        "lol.cakes"}));
        assertFalse(sC.filesFilter.contains("lol.cakes"));
    }
}
