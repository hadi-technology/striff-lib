package com.clarity.binary.diagram;

import com.clarity.invocation.ComponentInvocation;
import com.clarity.sourcemodel.Component;
import com.clarity.sourcemodel.OOPSourceCodeModel;
import com.clarity.sourcemodel.OOPSourceModelConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Enhances Clarpse's {@link Component} to facilitate generation of {@link com.clarity.binary.diagram.view.SDView}'s.
 */
public class DiagramComponent {

    private final Component cmp;
    private final OOPSourceCodeModel srcModel;
    private List<String> children = new ArrayList<>();

    public DiagramComponent(Component cmp, OOPSourceCodeModel srcModel) {
        this.cmp = cmp;
        this.srcModel = srcModel;
        if (srcModel != null) {
            for (String child : cmp.children()) {
                Optional<Component> childCmp = srcModel.getComponent(child);
                if (childCmp.isPresent()) {
                    if (childCmp.get().componentType() == OOPSourceModelConstants.ComponentType.FIELD
                            || childCmp.get().componentType() == OOPSourceModelConstants.ComponentType.INTERFACE_CONSTANT) {
                        children.add(new DiagramComponent(childCmp.get(), srcModel).uniqueName());
                    } else {
                        children.add(child);
                    }
                }
            }
        }
    }

    public List<String> children() {
        return this.children;
    }

    public String uniqueName() {
        if (this.cmp.componentType() == OOPSourceModelConstants.ComponentType.FIELD
                || this.cmp.componentType() == OOPSourceModelConstants.ComponentType.INTERFACE_CONSTANT) {
            return cmp.uniqueName() + "." + cmp.codeFragment();
        } else {
            return cmp.uniqueName();
        }
    }

    public String sourceFile() {
        return cmp.sourceFile();
    }

    public List<ComponentInvocation> componentInvocations(OOPSourceModelConstants.ComponentInvocations implementation) {
        return cmp.componentInvocations(implementation);
    }

    public Set<String> modifiers() {
        return cmp.modifiers();
    }

    public OOPSourceModelConstants.ComponentType componentType() {
        return cmp.componentType();
    }

    public String parentUniqueName() {
        return cmp.parentUniqueName();
    }

    public Set<ComponentInvocation> componentInvocations() {
        return cmp.componentInvocations();
    }

    public Iterable<? extends ComponentInvocation> invocations() {
        return cmp.invocations();
    }

    public String name() {
        return cmp.name();
    }

    public String code() {
        return this.cmp.code();
    }

    public String codeFragment() {
        return cmp.codeFragment();
    }

    public String comment() {
        return cmp.comment();
    }

    public void setName(String name) {
        this.cmp.setName(name);
    }
}
