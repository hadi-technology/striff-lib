package striff.test.model;

import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.clarpse.sourcemodel.Package;
import com.hadi.striff.diagram.SyntheticModuleSupport;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SyntheticModuleSupportTest {

    @Test(expected = IllegalStateException.class)
    public void moduleKeyRequiresModuleName() {
        Component component = new Component();
        component.setComponentType(OOPSourceModelConstants.ComponentType.FUNCTION);
        component.setComponentName("topLevelFn");
        SyntheticModuleSupport.moduleKey(component);
    }

    @Test
    public void moduleKeyIsQualifiedByThePackage() {
        Component component = new Component();
        component.setComponentType(OOPSourceModelConstants.ComponentType.FUNCTION);
        component.setComponentName("applyOrderUpdate");
        component.setModule("update");
        component.setPkg(new Package("src/orders", "src/orders"));

        assertEquals("src.orders.update", SyntheticModuleSupport.moduleKey(component));
    }

    @Test
    public void moduleKeyInTheRootPackageIsTheModuleNameAlone() {
        Component component = new Component();
        component.setComponentType(OOPSourceModelConstants.ComponentType.FUNCTION);
        component.setComponentName("topLevelFn");
        component.setModule("update");

        assertEquals("update", SyntheticModuleSupport.moduleKey(component));
    }

    @Test
    public void recognisesBothSpellingsOfASyntheticUniqueName() {
        assertTrue(SyntheticModuleSupport.isSyntheticUniqueName("module:update"));
        assertTrue(SyntheticModuleSupport.isSyntheticUniqueName("src.orders.module:update"));
        assertFalse(SyntheticModuleSupport.isSyntheticUniqueName("src.orders.Update"));
        assertFalse(SyntheticModuleSupport.isSyntheticUniqueName(null));
    }

    @Test
    public void syntheticUniqueNameCarriesThePackageThenThePrefix() {
        assertEquals("src.orders.module:update",
                SyntheticModuleSupport.syntheticUniqueName("src.orders", "update"));
        assertEquals("module:update", SyntheticModuleSupport.syntheticUniqueName("", "update"));
        assertEquals("module:update", SyntheticModuleSupport.syntheticUniqueName("update"));
    }

    @Test
    public void syntheticComponentInheritsPackageFromChild() {
        // Given a module-level function with a package
        OOPSourceCodeModel model = new OOPSourceCodeModel();
        Component fn = new Component();
        fn.setComponentName("topLevelFn");
        fn.setComponentType(OOPSourceModelConstants.ComponentType.FUNCTION);
        fn.setModule("cron");
        fn.setPkg(new Package("src", "src"));
        model.insertComponent(fn);

        // When synthetic components are created
        Map<String, Component> synthetics = SyntheticModuleSupport.syntheticComponentsByModule(model);

        // Then the synthetic module should inherit the package from its child
        Component synthetic = synthetics.get("src.cron");
        assertNotNull("Synthetic component should have package", synthetic.pkg());
        assertEquals("src", synthetic.pkg().name());
        assertEquals("src.module:cron", synthetic.uniqueName());
        assertEquals("cron", synthetic.name());
    }

    @Test
    public void syntheticComponentWithNestedPackage() {
        // Given a module-level function in a nested package
        OOPSourceCodeModel model = new OOPSourceCodeModel();
        Component fn = new Component();
        fn.setComponentName("someFn");
        fn.setComponentType(OOPSourceModelConstants.ComponentType.FUNCTION);
        fn.setModule("config");
        fn.setPkg(new Package("src/util/config", "src/util/config"));
        model.insertComponent(fn);

        // When synthetic components are created
        Map<String, Component> synthetics = SyntheticModuleSupport.syntheticComponentsByModule(model);

        // Then the synthetic module should have the nested package
        Component synthetic = synthetics.get("src.util.config.config");
        assertNotNull("Synthetic component should have package", synthetic.pkg());
        assertEquals("src/util/config", synthetic.pkg().name());
        assertEquals("src.util.config.module:config", synthetic.uniqueName());
    }

    @Test
    public void syntheticComponentModuleFieldInheritsPackage() {
        // Given a module-level field with a package
        OOPSourceCodeModel model = new OOPSourceCodeModel();
        Component field = new Component();
        field.setComponentName("topLevelField");
        field.setComponentType(OOPSourceModelConstants.ComponentType.MODULE_FIELD);
        field.setModule("data");
        field.setPkg(new Package("api/handlers", "api/handlers"));
        model.insertComponent(field);

        // When synthetic components are created
        Map<String, Component> synthetics = SyntheticModuleSupport.syntheticComponentsByModule(model);

        // Then the synthetic module should inherit the package from the field
        Component synthetic = synthetics.get("api.handlers.data");
        assertNotNull("Synthetic component should have package", synthetic.pkg());
        assertEquals("api/handlers", synthetic.pkg().name());
        assertEquals("api.handlers.module:data", synthetic.uniqueName());
    }
}
