package striff.test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import java.io.IOException;

import org.apache.batik.transcoder.TranscoderException;
import org.junit.Before;
import org.junit.Test;

import com.hadii.striff.diagram.display.LightDiagramColorScheme;
import com.hadii.striff.diagram.display.MetricBadges;
import com.hadii.striff.metrics.MetricChange;

public class MetricBadgesTest {

        private MetricBadges metricBadges;

        @Before
        public void setup() {
                LightDiagramColorScheme mockColorScheme = new LightDiagramColorScheme();
                metricBadges = new MetricBadges(mockColorScheme);
        }

        @Test
        public void testGenerateBadgePositiveChange() throws IOException, TranscoderException {
                String badge = metricBadges.generateBadge("NOC", 10.0, 20.0, false, false);
                assertTrue("Badge should contain +100% change", badge.contains("+100%"));
        }

        @Test
        public void testGenerateBadgeNegativeChange() throws IOException, TranscoderException {
                String badge = metricBadges.generateBadge("NOC", 20.0, 10.0, false, false);
                assertTrue("Badge should contain -50% change", badge.contains("-50%"));
        }

        @Test
        public void testGenerateBadgeNoChange() throws IOException, TranscoderException {
                String badge = metricBadges.generateBadge("NOC", 10.0, 10.0, false, false);
                assertTrue("Badge should not have percentage text for no change", badge.contains("NOC: 10"));
        }

        @Test
        public void testGenerateBadgeDeletedComponent() throws IOException, TranscoderException {
                String badge = metricBadges.generateBadge("WMC", 15.0, 0.0, true, false);
                assertTrue("Badge should indicate component is deleted", badge.contains("\u2064"));
        }

        @Test
        public void testGenerateBadgeAddedComponent() throws IOException, TranscoderException {
                String badge = metricBadges.generateBadge("ENC", 0.0, 25.0, false, true);
                assertTrue("Badge should indicate component is added", badge.contains("\u2064"));
        }

        @Test
        public void testGenerateBadgeWholeNumberFormatting() throws IOException, TranscoderException {
                String badge = metricBadges.generateBadge("DIT", 10.0, 37.0, false, false);
                assertTrue("Badge should show whole numbers without decimals", badge.contains("DIT: 37"));
        }

        @Test
        public void testGenerateBadgeDecimalFormatting() throws IOException, TranscoderException {
                String badge = metricBadges.generateBadge("DIT", 10.0, 37.5, false, false);
                assertTrue("Badge should show decimals for non-whole numbers", badge.contains("DIT: 37.5"));
        }

        @Test
        public void testMetricBadgesWithNoChange() throws IOException, TranscoderException {
                MetricChange metricChange = new MetricChange("TestClass", 10, 10, 1.0, 1.0, 10, 10, 5, 5, 2, 2, 0.5,
                                0.5);
                String badges = metricBadges.metricBadges(metricChange, false, false);
                assertFalse("Badges should not contain percentage text for unchanged metrics", badges.contains("%"));
        }

        @Test
        public void testMetricBadgesOnlyNonZeroMetrics() throws IOException, TranscoderException {
                MetricChange metricChange = new MetricChange("TestClass", 0, 15, 1.0, 1.0, 0, 10, 0, 5, 0, 2, 0, 0.5);
                String badges = metricBadges.metricBadges(metricChange, false, false);
                assertFalse("Badges should exclude metrics with zero values", badges.contains("NOC: 0"));
        }
}
