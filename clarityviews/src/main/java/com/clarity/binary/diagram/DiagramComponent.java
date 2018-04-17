package com.clarity.binary.diagram;

import com.clarity.invocation.ComponentInvocation;
import com.clarity.sourcemodel.Component;
import com.clarity.sourcemodel.OOPSourceCodeModel;
import com.clarity.sourcemodel.OOPSourceModelConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enhances Clarpse's {@link Component} to facilitate generation of {@link com.clarity.binary.diagram.view.SDView}'s.
 */
public class DiagramComponent {

    private final Component cmp;
    private final OOPSourceCodeModel srcModel;
    private final int dit;
    public List<String> children = new ArrayList<>();
    public int noc;

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
        this.noc = calculateNoc();
        this.dit = calculateDit(this.cmp);
    }

    /**
     * Returns NOC of the current component, which is a measure of the the number of direct
     * subclasses of a class.
     */
    private int calculateNoc() {
        String currUniqueName = this.uniqueName();
        int noc = 0;
        if (this.componentType().isBaseComponent()) {
            for (Component cmp : srcModel.components().collect(Collectors.toList())) {
                if (cmp.componentType().isBaseComponent()) {
                    for (ComponentInvocation cmpInv : cmp.componentInvocations(OOPSourceModelConstants.ComponentInvocations.EXTENSION)) {
                        if (cmpInv.invokedComponent().equals(currUniqueName)) {
                            noc += 1;
                        }
                    }
                }
            }
        }
        return noc;
    }

    /**
     * Returns DIT of the current component, which is a measure of how far down a class is declared in
     * the inheritance hierarchy.
     */
    private int calculateDit(Component cmp) {
        if (cmp.componentType() == OOPSourceModelConstants.ComponentType.INTERFACE) {
            return 1;
        }

        int currentHighestScore = 1;

        for (ComponentInvocation cmpInv : cmp.componentInvocations(OOPSourceModelConstants.ComponentInvocations.EXTENSION)) {
            Optional<Component> invkCmp = srcModel.getComponent(cmpInv.invokedComponent());
            if (invkCmp.isPresent()) {
                int hierarchyScore = 1 + calculateDit(invkCmp.get());
                if (hierarchyScore > currentHighestScore) {
                    currentHighestScore = hierarchyScore;
                }
            }
        }
        return currentHighestScore;
    }

    public List<String> children() {
        return this.children;
    }

    public int noc() {
        return this.noc;
    }

    public int dit() {
        return this.dit;
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
