package striff.test;

import com.hadii.clarpse.compiler.Lang;
import com.hadii.striff.diagram.StriffCodeModel;
import com.hadii.striff.extractor.DiagramConstants;
import com.hadii.striff.extractor.ComponentRelation;
import com.hadii.striff.extractor.ExtractedRelationships;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests to ensure component relations are being extracted correctly.
 */
public class ExtractedRelationshipsIntegrationTest {

    private static StriffCodeModel codeModel;
    private static ExtractedRelationships relations;

    @BeforeClass
    public static void setup() throws Exception {
        codeModel = new StriffCodeModel(TestUtil.sourceCodeModel("/yao-main.zip", Lang.GOLANG));
        relations = new ExtractedRelationships(codeModel);
    }

    @Test
    public void spotCheckChartStructAggregationRelationships() {
        Set<ComponentRelation> chartStructRelations = relations.result().rels(
            codeModel.component("chart.Chart")
        );
        assertTrue(relations.result().rels(
                                codeModel.component("chart.Chart"))
                            .toString()
                            .equals(
                                "[ComponentRelation{originalComponent=Chart, targetComponent=API," +
                                    " targetMultiplicity=, associationType=AGGREGATION}, " +
                                    "ComponentRelation{originalComponent=Chart, " +
                                    "targetComponent=Filter, targetMultiplicity=, " +
                                    "associationType=AGGREGATION}, " +
                                    "ComponentRelation{originalComponent=Chart, " +
                                    "targetComponent=Page, targetMultiplicity=, " +
                                    "associationType=AGGREGATION}]"));
    }

    @Test
    public void spotCheckJwtClaimsStructNoRelationships() {
        assertTrue(relations.result().rels(codeModel.component("helper.JwtClaims")).isEmpty());
    }


    @Test
    public void spotCheckPageStructAggregationRelationship() {
        assertTrue(relations.result().rels(codeModel.component("share.Page"))
                            .stream().anyMatch(
                componentRelation -> componentRelation
                    .toString()
                    .equals("ComponentRelation{originalComponent=Page, targetComponent=Render, " +
                                "targetMultiplicity=, associationType=AGGREGATION}")));
    }

    @Test
    public void testSpecializationRelationship() {
        assertTrue(relations.result().relsByType(DiagramConstants.ComponentAssociation.SPECIALIZATION)
                            .toString().equals("[ComponentRelation{originalComponent=Render, " +
                                                   "targetComponent=Importable, " +
                                                   "targetMultiplicity=, " +
                                                   "associationType=SPECIALIZATION}, " +
                                                   "ComponentRelation{originalComponent=Column, " +
                                                   "targetComponent=Importable, " +
                                                   "targetMultiplicity=, " +
                                                   "associationType=SPECIALIZATION}, " +
                                                   "ComponentRelation{originalComponent=Filter, " +
                                                   "targetComponent=Importable, " +
                                                   "targetMultiplicity=, " +
                                                   "associationType=SPECIALIZATION}, " +
                                                   "ComponentRelation{originalComponent=API, " +
                                                   "targetComponent=Importable, " +
                                                   "targetMultiplicity=, " +
                                                   "associationType=SPECIALIZATION}, " +
                                                   "ComponentRelation{originalComponent=Page, " +
                                                   "targetComponent=Importable, " +
                                                   "targetMultiplicity=, " +
                                                   "associationType=SPECIALIZATION}]"));
    }

    @Test
    public void testAssociationRelationship() {
        assertTrue(relations.result().relsByType(DiagramConstants.ComponentAssociation.WEAK_ASSOCIATION)
                            .toString().equals("[ComponentRelation{originalComponent=Importer, " +
                                                   "targetComponent=Binding, targetMultiplicity=," +
                                                   " associationType=WEAK_ASSOCIATION}, " +
                                                   "ComponentRelation{originalComponent=WorkFlow," +
                                                   " targetComponent=Input, targetMultiplicity=, " +
                                                   "associationType=WEAK_ASSOCIATION}, " +
                                                   "ComponentRelation{originalComponent=Importer," +
                                                   " targetComponent=Source, targetMultiplicity=," +
                                                   " associationType=WEAK_ASSOCIATION}, " +
                                                   "ComponentRelation{originalComponent=Importer," +
                                                   " targetComponent=Mapping, " +
                                                   "targetMultiplicity=, " +
                                                   "associationType=WEAK_ASSOCIATION}, " +
                                                   "ComponentRelation{originalComponent=WorkFlow," +
                                                   " targetComponent=Condition, " +
                                                   "targetMultiplicity=, " +
                                                   "associationType=WEAK_ASSOCIATION}, " +
                                                   "ComponentRelation{originalComponent=WorkFlow," +
                                                   " targetComponent=Next, targetMultiplicity=, " +
                                                   "associationType=WEAK_ASSOCIATION}]"));
    }

    @Test
    public void testRealizationRelationship() {
        assertTrue(relations.result().relsByType(DiagramConstants.ComponentAssociation.REALIZATION)
                            .toString().equals("[]"));
    }

    @Test
    public void testCompositionRelationship() {
        assertTrue(relations.result().relsByType(DiagramConstants.ComponentAssociation.COMPOSITION)
                            .toString().equals("[]"));
    }
}
