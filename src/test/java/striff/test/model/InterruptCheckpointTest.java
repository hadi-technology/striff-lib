package striff.test.model;

import com.hadi.clarpse.reference.SimpleTypeReference;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants.ComponentType;
import com.hadi.striff.extractor.ComponentRelation;
import com.hadi.striff.extractor.DiagramConstants.ComponentAssociation;
import com.hadi.striff.extractor.ExtractedRelationships;
import com.hadi.striff.extractor.RelationsMap;
import com.hadi.striff.parse.CodeDiff;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.CancellationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Cooperative cancellation for the model-merge and relation-extraction phases (striff-api #289).
 *
 * <p>On a too-large repo the analysis used to wedge inside {@link CodeDiff}'s merge and
 * {@link ExtractedRelationships}: both walked tens of thousands of components with no interrupt
 * check, so the deadline's {@code Thread.interrupt()} was ignored and the pod only recovered via a
 * heavy JVM self-exit. These tests pin the two halves of the fix: an interrupted run aborts
 * promptly with a {@link CancellationException}, and an ordinary (never-interrupted) run produces
 * exactly the relations it did before -- the checkpoints are inert unless the thread is interrupted.
 */
public class InterruptCheckpointTest {

    @Before
    public void clearInterruptBefore() {
        // Guard against a stray interrupt flag leaking in from another test on this thread.
        Thread.interrupted();
    }

    @After
    public void clearInterruptAfter() {
        // Never let this test's flag leak out into tests that share this thread.
        Thread.interrupted();
    }

    private static Component newBaseComponent(String uniqueName) {
        Component cmp = new Component();
        cmp.setName(uniqueName);
        cmp.setComponentName(uniqueName);
        cmp.setComponentType(ComponentType.CLASS);
        return cmp;
    }

    /**
     * Builds a model large enough to iterate: {@code count} classes, each with a field-style
     * reference to a shared target class, so extraction has real per-component work to do.
     */
    private static OOPSourceCodeModel largeModel(int count) {
        OOPSourceCodeModel model = new OOPSourceCodeModel();
        Component target = newBaseComponent("Target");
        model.insertComponent(target);
        for (int i = 0; i < count; i++) {
            Component cmp = newBaseComponent("Class" + i);
            cmp.insertCmpRef(new SimpleTypeReference("Target"));
            model.insertComponent(cmp);
        }
        return model;
    }

    @Test
    public void extractionAbortsWhenTheCallingThreadIsInterrupted() {
        OOPSourceCodeModel model = largeModel(2000);

        Thread.currentThread().interrupt();
        try {
            new ExtractedRelationships(model);
            fail("relationship extraction should abort when the calling thread is interrupted");
        } catch (CancellationException expected) {
            // The interrupt flag must remain observable to callers after the throw.
            assertTrue("the interrupt flag must be re-asserted before throwing",
                    Thread.currentThread().isInterrupted());
        }
    }

    @Test
    public void mergeAbortsWhenTheCallingThreadIsInterrupted() {
        // Old model carries components the new model does not, so the merge has old-only work to do.
        OOPSourceCodeModel oldModel = largeModel(2000);
        OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        Component keep = newBaseComponent("Kept");
        newModel.insertComponent(keep);

        Thread.currentThread().interrupt();
        try {
            new CodeDiff(oldModel, newModel);
            fail("the model merge should abort when the calling thread is interrupted");
        } catch (CancellationException expected) {
            assertTrue("the interrupt flag must be re-asserted before throwing",
                    Thread.currentThread().isInterrupted());
        }
    }

    @Test
    public void normalExtractionIsUnaffectedByTheCheckpoints() {
        // A field-var composition, extracted on a thread that is never interrupted, must still yield
        // exactly the relation it always did -- proof the checkpoints do not alter output.
        OOPSourceCodeModel model = new OOPSourceCodeModel();

        Component classA = newBaseComponent("classA");
        Component classAField = new Component();
        classAField.setName("fieldVar");
        classAField.setComponentName("classA.fieldVar");
        classAField.setComponentType(ComponentType.FIELD);
        classA.insertChildComponent("classA.fieldVar");
        classAField.insertCmpRef(new SimpleTypeReference("classB"));
        classAField.insertAccessModifier("private");
        Component classB = newBaseComponent("classB");

        model.insertComponent(classA);
        model.insertComponent(classAField);
        model.insertComponent(classB);

        RelationsMap relations = new ExtractedRelationships(model).result();

        ComponentRelation expected = new ComponentRelation(classA, classB, null,
                ComponentAssociation.COMPOSITION);
        assertTrue("the checkpointed extraction must still produce the composition relation",
                relations.contains(expected));
        assertEquals("no extra relations may appear", 1, relations.allRels().size());
    }

    @Test
    public void normalMergeIsUnaffectedByTheCheckpoints() {
        // An old-only component must still enter the merged model, on a never-interrupted run.
        OOPSourceCodeModel oldModel = new OOPSourceCodeModel();
        oldModel.insertComponent(newBaseComponent("Kept"));
        oldModel.insertComponent(newBaseComponent("Gone"));

        OOPSourceCodeModel newModel = new OOPSourceCodeModel();
        newModel.insertComponent(newBaseComponent("Kept"));

        CodeDiff diff = new CodeDiff(oldModel, newModel);

        assertTrue("the surviving component must be in the merged model",
                diff.mergedModel().containsComponent("Kept"));
        assertTrue("the old-only component must be carried into the merged model",
                diff.mergedModel().containsComponent("Gone"));
        assertEquals(Set.of("Kept", "Gone"),
                diff.mergedModel().components().map(Component::uniqueName)
                        .collect(java.util.stream.Collectors.toSet()));
    }
}
