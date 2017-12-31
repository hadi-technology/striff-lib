package com.clarity.binary.diagram.display;

import com.clarity.sourcemodel.Component;
import com.clarity.sourcemodel.OOPSourceModelConstants;

import java.util.Map;

public class DiagramMethodDisplayName implements DiagramDisplayName {

    private String displayName;


    public DiagramMethodDisplayName(Component methodComponent, Map<String, Component> allComponents) {

        displayName = methodComponent.name() + "(";
        for (String methodChild : methodComponent.children()) {
            Component child = allComponents.get(methodChild);
            if (child != null && child.componentType() == OOPSourceModelConstants.ComponentType.METHOD_PARAMETER_COMPONENT) {
                if (child.value() != null) {
                    displayName += child.value() + ", ";
                } else if (child.componentInvocations(OOPSourceModelConstants.ComponentInvocations.DECLARATION).size() > 0) {
                    String methodParamType = child.componentInvocations(OOPSourceModelConstants.ComponentInvocations.DECLARATION).get(0).invokedComponent();
                    if (methodParamType.contains(".")) {
                        methodParamType = methodParamType.substring(methodParamType.lastIndexOf(".") + 1);
                    }
                    displayName += methodParamType + ", ";
                }
            }
            }
        displayName = displayName.trim();
        if (displayName.endsWith(",")) {
            displayName = displayName.substring(0, displayName.length() - 1);
        }
        displayName += ")";
    }

    @Override
    public String value() {
        return this.displayName;
    }
}
