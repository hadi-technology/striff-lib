package striff.test.model;

import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.striff.diagram.SyntheticModuleSupport;
import org.junit.Test;

public class SyntheticModuleSupportTest {

    @Test(expected = IllegalStateException.class)
    public void moduleKeyRequiresModuleName() {
        Component component = new Component();
        component.setComponentType(OOPSourceModelConstants.ComponentType.FUNCTION);
        component.setComponentName("topLevelFn");
        SyntheticModuleSupport.moduleKey(component);
    }
}
