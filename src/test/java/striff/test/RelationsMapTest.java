package striff.test;

import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.striff.extractor.DiagramConstants.ComponentAssociation;
import com.hadii.striff.extractor.ComponentRelation;
import com.hadii.striff.extractor.RelationsMap;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests to ensure component relations are being extracted correctly.
 */
public class RelationsMapTest {

    private static Component classA;
    private static Component classB;
    private static Component classC;

    @BeforeClass
    public static void setup() {
        OOPSourceCodeModel codeModel = new OOPSourceCodeModel();

        classA = new Component();
        classA.setName("ClassA");
        classA.setComponentName("ClassA");
        classA.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        codeModel.insertComponent(classA);

        classB = new Component();
        classB.setName("ClassB");
        classB.setComponentName("ClassB");
        classB.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        codeModel.insertComponent(classB);

        classC = new Component();
        classC.setName("ClassC");
        classC.setComponentName("ClassC");
        classC.setComponentType(OOPSourceModelConstants.ComponentType.CLASS);
        codeModel.insertComponent(classC);
    }

    @Test
    public void testAllRels() {
        RelationsMap relMap = new RelationsMap();
        relMap.insertRelation(new ComponentRelation(
                classA, classB, null, ComponentAssociation.ASSOCIATION));
        relMap.insertRelation(new ComponentRelation(
                classA, classB, null, ComponentAssociation.AGGREGATION));
        assertEquals(2, relMap.allRels().size());
    }

    @Test
    public void testComponentRels() {
        RelationsMap relMap = new RelationsMap();
        relMap.insertRelation(new ComponentRelation(
                classA, classB, null, ComponentAssociation.ASSOCIATION));
        relMap.insertRelation(new ComponentRelation(
                classA, classB, null, ComponentAssociation.AGGREGATION));
        assertEquals(2, relMap.rels(classA).size());
    }

    @Test
    public void testContainRels() {
        RelationsMap relMap = new RelationsMap();
        ComponentRelation rel1 = new ComponentRelation(
                classA, classB, null, ComponentAssociation.ASSOCIATION);
        relMap.insertRelation(rel1);
        relMap.insertRelation(new ComponentRelation(
                classA, classB, null, ComponentAssociation.AGGREGATION));
        assertTrue(relMap.contains(rel1));
    }

    @Test
    public void testHasRelsForCmp() {
        RelationsMap relMap = new RelationsMap();
        ComponentRelation rel1 = new ComponentRelation(
                classA, classB, null, ComponentAssociation.ASSOCIATION);
        relMap.insertRelation(rel1);
        relMap.insertRelation(new ComponentRelation(
                classA, classB, null, ComponentAssociation.AGGREGATION));
        assertTrue(relMap.hasRels(classA.uniqueName()));
    }

    @Test
    public void testRelsByType() {
        RelationsMap relMap = new RelationsMap();
        ComponentRelation rel1 = new ComponentRelation(
                classA, classB, null, ComponentAssociation.ASSOCIATION);
        relMap.insertRelation(rel1);
        relMap.insertRelation(new ComponentRelation(
                classA, classB, null, ComponentAssociation.AGGREGATION));
        assertEquals(1, relMap.relsByType(ComponentAssociation.AGGREGATION).size());
    }

    @Test
    public void testSignificantRels() {
        RelationsMap relMap = new RelationsMap();
        ComponentRelation rel1 = new ComponentRelation(
                classA, classB, null, ComponentAssociation.ASSOCIATION);
        relMap.insertRelation(rel1);
        relMap.insertRelation(new ComponentRelation(
                classA, classB, null, ComponentAssociation.AGGREGATION));
        assertSame(relMap.significantRels(classA).stream().findFirst().get().associationType(),
                ComponentAssociation.AGGREGATION);
        assertEquals(1, relMap.significantRels(classA).size());
    }

    @Test
    public void testMostSignificantRel() {
        RelationsMap relMap = new RelationsMap();
        ComponentRelation rel1 = new ComponentRelation(
                classA, classB, null, ComponentAssociation.ASSOCIATION);
        relMap.insertRelation(rel1);
        relMap.insertRelation(new ComponentRelation(
                classA, classB, null, ComponentAssociation.AGGREGATION));
        assertSame(relMap.mostSignificantRelation(classA, classB).associationType(),
                ComponentAssociation.AGGREGATION);
    }

    @Test
    public void testRelSize() {
        RelationsMap relMap = new RelationsMap();
        ComponentRelation rel1 = new ComponentRelation(
                classA, classB, null, ComponentAssociation.ASSOCIATION);
        relMap.insertRelation(rel1);
        relMap.insertRelation(new ComponentRelation(
                classA, classB, null, ComponentAssociation.AGGREGATION));
        assertEquals(2, relMap.size());
        assertEquals(2, relMap.allRels().size());
    }

    @Test
    public void testFilteredRelations() {
        RelationsMap relMap = new RelationsMap();
        relMap.insertRelation(new ComponentRelation(classA, classB));
        relMap.insertRelation(new ComponentRelation(classB, classC));
        relMap.insertRelation(new ComponentRelation(classA, classC));
        assertTrue(relMap.hasRels("ClassA"));
        assertTrue(relMap.hasRels("ClassB"));
        Set<String> filterCmps = new HashSet<>(Arrays.asList(new String[] { "ClassA", "ClassC" }));
        RelationsMap filteredMap = relMap.filteredRelations(filterCmps);
        assertFalse(filteredMap.hasRels("ClassB"));
        assertTrue(filteredMap.hasRels("ClassA"));
    }

    @Test
    public void testFilteredRelationsAllComponents() {
        RelationsMap relMap = new RelationsMap();
        relMap.insertRelation(new ComponentRelation(classA, classB));
        relMap.insertRelation(new ComponentRelation(classB, classC));
        Set<String> filterCmps = new HashSet<>(Arrays.asList("ClassA", "ClassB", "ClassC"));
        RelationsMap filteredMap = relMap.filteredRelations(filterCmps);
        assertEquals("Filtered map should be empty", 0, filteredMap.size());
    }
}
