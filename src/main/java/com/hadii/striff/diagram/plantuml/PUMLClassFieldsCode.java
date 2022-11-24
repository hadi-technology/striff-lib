package com.hadii.striff.diagram.plantuml;

import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.striff.ChangeSet;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.text.StiffComponentDocText;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class PUMLClassFieldsCode {

    private static final int MAX_ATTRIBUTE_SIZE = 20;
    private final Map<String, DiagramComponent> allComponents;
    private final Set<DiagramComponent> deletedComponents;
    private final Set<DiagramComponent> addedComponents;
    private final Set<DiagramComponent> modifiedComponents;
    private final DiagramDisplay diagramDisplay;

    PUMLClassFieldsCode(Map<String, DiagramComponent> allComponents, ChangeSet changeSet,
                        DiagramDisplay diagramDisplay) {
        this.allComponents = allComponents;
        this.addedComponents = changeSet.addedComponents();
        this.deletedComponents = changeSet.deletedComponents();
        this.modifiedComponents = changeSet.modifiedComponents();
        this.diagramDisplay = diagramDisplay;
    }

    public String value(Collection<DiagramComponent> cmps) {
        final StringBuilder tempStrBuilder = new StringBuilder();
        for (final DiagramComponent cmp : cmps) {
            String cmpPUMLStr = "";
            List<String> componentPUMLStrings = new ArrayList<>();
            if (cmp.modifiers().contains(
                    // Insert abstract keyword if cmp is Abstract class
                    OOPSourceModelConstants.getJavaAccessModifierMap().get(OOPSourceModelConstants.AccessModifiers.ABSTRACT))) {
                cmpPUMLStr += (OOPSourceModelConstants.getJavaAccessModifierMap().get(OOPSourceModelConstants.AccessModifiers.ABSTRACT)
                        + " ");
            }
            boolean largeComponent = cmp.children().size() > MAX_ATTRIBUTE_SIZE;
            // Insert cmp type name (eg: class, interface, etc...)
            cmpPUMLStr += cmp.componentType().getValue() + " ";
            // Insert the actual cmp unique name
            cmpPUMLStr += cmp.name() + " as \"";
            // Insert cmp display name
            if (largeComponent) {
                cmpPUMLStr += cmp.name() + " <b><color:"
                    + this.diagramDisplay.colorScheme().classFontColor() + ">(...)\"";
            } else {
                cmpPUMLStr += cmp.name() + "\"";
            }
            // Insert class generics if required
            if (cmp.codeFragment() != null) {
                cmpPUMLStr += (cmp.codeFragment());
            }
            // Custom circled character styling...
            if (cmp.componentType() == OOPSourceModelConstants.ComponentType.CLASS
                    || cmp.componentType() == OOPSourceModelConstants.ComponentType.STRUCT) {
                cmpPUMLStr += " << (C," + this.diagramDisplay.colorScheme().classCircledCharacterBackgroundColor() + ")"
                        + " >> ";
            }
            // Insert background color tag
            componentPUMLStrings.add(enhanceBaseCmp(cmp, cmpPUMLStr) + " {\n");
            // Stores the required length of lines in the cmp's doc text preamble
            int docTextCharLen = 80;
            // Get all child components
            Set<DiagramComponent> childComponents = new HashSet<>();
            cmp.children().forEach(s -> {
                DiagramComponent childComponent = allComponents.get(s);
                if (childComponent != null) {
                    childComponents.add(childComponent);
                }
            });
            // Group child components into method type and field types
            Set<DiagramComponent> methodChilds = childComponents.stream().filter(
                    diagramComponent -> diagramComponent.componentType().isMethodComponent())
                    .collect(Collectors.toSet());
            Set<DiagramComponent> fieldChilds = childComponents.stream().filter(
                    diagramComponent ->
                        diagramComponent.componentType() == OOPSourceModelConstants.ComponentType.ENUM_CONSTANT
                            || diagramComponent.componentType() == OOPSourceModelConstants.ComponentType.INTERFACE_CONSTANT
                            || diagramComponent.componentType()
                            == OOPSourceModelConstants.ComponentType.FIELD)
                    .collect(Collectors.toSet());
            // Insert PUML text for field children
            boolean zeroFields = true;
            for (DiagramComponent fieldChild : fieldChilds) {
                if (shouldDisplayChildCmp(largeComponent, fieldChild)) {
                    String childCmpPUMLStr = childComponentPUMLText(fieldChild);
                    if (!childCmpPUMLStr.isEmpty()) {
                        componentPUMLStrings.add(childCmpPUMLStr);
                        zeroFields = false;
                        if (getChildCmpDisplayText(fieldChild).length() > docTextCharLen) {
                            docTextCharLen = childCmpPUMLStr.replace(" ", "").length();
                        }
                    }
                }
            }
            if (!zeroFields) {
                componentPUMLStrings.add("--\n");
            }
            // Insert PUML text for method children
            boolean zeroMethods = true;
            for (DiagramComponent methodChild : methodChilds) {
                if (shouldDisplayChildCmp(largeComponent, methodChild)) {
                    String childCmpPUMLStr = childComponentPUMLText(methodChild);
                    if (!childCmpPUMLStr.isEmpty()) {
                        componentPUMLStrings.add(childCmpPUMLStr);
                        zeroMethods = false;
                        String methodChildDisplayTxt = getChildCmpDisplayText(methodChild);
                        if (methodChildDisplayTxt.length() > docTextCharLen) {
                            docTextCharLen = methodChildDisplayTxt.length();
                        }
                    }
                }
            }
            // If there were no methods, remove the previous separation line
            if (zeroMethods && !zeroFields) {
                componentPUMLStrings.remove(componentPUMLStrings.size() - 1);
            }
            // Generate cmp doc text last since it needs to fit within the cmp box
            // width constraints.
            if (cmp.comment() != null && !cmp.comment().isEmpty()) {
                String componentDoc = componentDocText(docTextCharLen, cmp);
                if (!componentDoc.isEmpty()) {
                    // Only insert line if the cmp displays children
                    if (zeroFields && zeroMethods) {
                        componentPUMLStrings.add(1, componentDoc + "\n");
                    } else {
                        componentPUMLStrings.add(1, componentDoc + "\n--\n");
                    }
                }
            }
            tempStrBuilder.append(StringUtils.join(componentPUMLStrings, " ")).append("}\n");
        }
        return tempStrBuilder.toString();
    }

    private boolean shouldDisplayChildCmp(boolean isLargeParentCmp, DiagramComponent childComponent) {
        return !isLargeParentCmp
                || addedComponents.contains(childComponent)
                || deletedComponents.contains(childComponent)
                || modifiedComponents.contains(childComponent);
    }

    private String childComponentPUMLText(DiagramComponent childCmp) {
        String childCmpPUMLStr = "";
        if ((childCmp.componentType() == OOPSourceModelConstants.ComponentType.METHOD)
                || childCmp.componentType().isVariableComponent()) {
            if (!childCmp.componentType().isBaseComponent()) {
                // if the field/method is abstract or static, add the {abstract}/{static} prefix..
                if (childCmp.modifiers().contains(
                        OOPSourceModelConstants.getJavaAccessModifierMap().get(OOPSourceModelConstants.AccessModifiers.ABSTRACT))) {
                    childCmpPUMLStr += ("{");
                    childCmpPUMLStr += OOPSourceModelConstants.getJavaAccessModifierMap()
                            .get(OOPSourceModelConstants.AccessModifiers.ABSTRACT);
                    childCmpPUMLStr += ("} ");
                }
                if (childCmp.modifiers().contains(
                        OOPSourceModelConstants.getJavaAccessModifierMap().get(OOPSourceModelConstants.AccessModifiers.STATIC))) {
                    childCmpPUMLStr += ("{");
                    childCmpPUMLStr += (OOPSourceModelConstants.getJavaAccessModifierMap()
                            .get(OOPSourceModelConstants.AccessModifiers.STATIC));
                    childCmpPUMLStr += ("} ");
                }
                childCmpPUMLStr += getChildCmpDisplayText(childCmp) + " ";
            }
        }
        // Handle modifiers...
        String visibilitySymbol;
        if (!childCmpPUMLStr.isEmpty()) {
            visibilitySymbol = visibilitySymbol(childCmp);
            // Add background color tag
            childCmpPUMLStr = visibilitySymbol + " "
                    + colorChildComponentBackground(childCmp, childCmpPUMLStr.trim()) + "\n";
        }
        return childCmpPUMLStr;
    }

    private String getChildCmpDisplayText(DiagramComponent childCmp) {
        String childCmpDisplayText = "";
        if (childCmp.componentType().isMethodComponent()
            || (childCmp.componentType().isVariableComponent()
            && childCmp.componentType() != OOPSourceModelConstants.ComponentType.ENUM_CONSTANT)) {
            childCmpDisplayText = childCmp.codeFragment();
        }
        if (childCmp.componentType() == OOPSourceModelConstants.ComponentType.ENUM_CONSTANT) {
            childCmpDisplayText = childCmp.name();
        }
        return childCmpDisplayText;
    }

    private String visibilitySymbol(DiagramComponent childCmp) {
        String visibilitySymbol = "";
        if (childCmp.modifiers().contains(
                OOPSourceModelConstants.getJavaAccessModifierMap().get(OOPSourceModelConstants.AccessModifiers.PUBLIC))) {
            visibilitySymbol += OOPSourceModelConstants.AccessModifiers.PUBLIC.getUMLClassDigramSymbol() + " ";
        } else if (childCmp.modifiers().contains(
                OOPSourceModelConstants.getJavaAccessModifierMap().get(OOPSourceModelConstants.AccessModifiers.PRIVATE))) {
            visibilitySymbol += OOPSourceModelConstants.AccessModifiers.PRIVATE.getUMLClassDigramSymbol() + " ";
        } else if (childCmp.modifiers().contains(
                OOPSourceModelConstants.getJavaAccessModifierMap().get(OOPSourceModelConstants.AccessModifiers.PROTECTED))) {
            visibilitySymbol += OOPSourceModelConstants.AccessModifiers.PROTECTED.getUMLClassDigramSymbol() + " ";
        } else {
            visibilitySymbol += OOPSourceModelConstants.AccessModifiers.NONE.getUMLClassDigramSymbol() + " ";
        }
        return visibilitySymbol;
    }

    /**
     * Generates PlantUML code for the given component's documentation.
     */
    private String componentDocText(int docTextCharLen, DiagramComponent component) {
        String commentStr = new StiffComponentDocText(component.comment().trim(), docTextCharLen).value();
        if (!commentStr.isEmpty()) {
            if (commentStr.length() < 800) {
                commentStr += "\n";
            } else {
                commentStr = commentStr.substring(0, 797).trim();
                commentStr += "...\n";
            }
        }
        return commentStr;
    }

    /**
     * Used for coloring the background of a specific child component (method, field, etc..)
     * within a base component.
     */
    private String colorChildComponentBackground(DiagramComponent childComponent, String text) {
        if (text.trim().isEmpty()) {
            return text;
        } else if (addedComponents.contains(childComponent)) {
            return "<back:" + this.diagramDisplay.colorScheme().addedComponentColor()
                + ">" + text + "</back>         ";
        } else if (deletedComponents.contains(childComponent)) {
            return "<back:" + this.diagramDisplay.colorScheme().deletedComponentColor() + ">"
                + text + "</back>         ";
        } else if (modifiedComponents.contains(childComponent)) {
            return "<back:" + this.diagramDisplay.colorScheme().modifiedComponentColor() + ">"
                + text + "</back>         ";
        } else {
            return text;
        }
    }

    /**
     * Used for coloring/bolding the entire background of the given base component.
     */
    private String enhanceBaseCmp(DiagramComponent cmp, String text) {
        String addedColor = this.diagramDisplay.colorScheme().addedComponentColor().replace("#", "");
        String deletedColor = this.diagramDisplay.colorScheme().deletedComponentColor().replace("#", "");
        String backgroundColorText =
            "#back:" + this.diagramDisplay.colorScheme().backgroundColor().replace("#", "");
        String headerColor = ";header:" + this.diagramDisplay.colorScheme().defaultClassHeaderColor().replace("#", "");
        if (addedComponents.contains(cmp)) {
            backgroundColorText = "#back:" + addedColor;
        } else if (deletedComponents.contains(cmp)) {
            backgroundColorText = "#back:" + deletedColor;
        }
        text += " " + backgroundColorText + headerColor;
        return text + " ";
    }
}
