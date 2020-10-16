package com.hadii.striff.diagram.plantuml;

import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.scheme.DiagramColorScheme;
import com.hadii.striff.parse.DiffCodeModel;
import com.hadii.striff.text.StiffComponentDocText;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class PUMLClassFieldsCode {

    private static final int MAX_ATTRIBUTE_SIZE = 20;
    private final Set<DiagramComponent> diagramComponents;
    private final Map<String, DiagramComponent> allComponents;
    private final Set<DiagramComponent> deletedComponents;
    private final Set<DiagramComponent> addedComponents;
    private final DiagramColorScheme colorScheme;

    PUMLClassFieldsCode(Set<DiagramComponent> diagramComponents,
                        DiffCodeModel mergedModel, DiagramColorScheme colorScheme) {
        this.diagramComponents = diagramComponents;
        this.allComponents = mergedModel.mergedModel().components();
        this.addedComponents = mergedModel.changeSet().addedComponents();
        this.deletedComponents = mergedModel.changeSet().deletedComponents();
        this.colorScheme = colorScheme;
    }

    public String value() {
        final StringBuilder tempStrBuilder = new StringBuilder();
        for (final DiagramComponent component : diagramComponents) {
            String cmpPUMLStr = "";
            List<String> componentPUMLStrings = new ArrayList<>();
            if (component.modifiers().contains(
                    // Insert abstract keyword if component is Abstract class
                    OOPSourceModelConstants.getJavaAccessModifierMap().get(OOPSourceModelConstants.AccessModifiers.ABSTRACT))) {
                cmpPUMLStr += (OOPSourceModelConstants.getJavaAccessModifierMap().get(OOPSourceModelConstants.AccessModifiers.ABSTRACT)
                        + " ");
            }
            boolean largeComponent = component.children().size() > MAX_ATTRIBUTE_SIZE;
            // Insert component type name (eg: class, interface, etc...)
            cmpPUMLStr += component.componentType().getValue() + " ";
            // Insert the actual component unique name
            String diagramCmpUniqueName = (component.uniqueName().replaceAll("-", "").replaceAll("\\.\\.+", ".") + " ");
            cmpPUMLStr += diagramCmpUniqueName + "as \"";
            // Insert component display name
            if (addedComponents.contains(component) || deletedComponents.contains(component)) {
                cmpPUMLStr += "<color:black>" + component.name() + "</color>\"";
            } else {
                if (largeComponent) {
                    cmpPUMLStr += component.name() + " <b><color:" + colorScheme.classStereoTypeFontColor() + ">(...)\"";
                } else {
                    cmpPUMLStr += component.name() + "\"";
                }
            }
            // Insert class generics if required
            if (component.codeFragment() != null) {
                cmpPUMLStr += (component.codeFragment());
            }
            // Custom circled character styling...
            if (component.componentType() == OOPSourceModelConstants.ComponentType.CLASS && !component.modifiers().contains("abstract")) {
                cmpPUMLStr += " << (C," + colorScheme.classCircledCharacterBackgroundColor() + ") >> ";
            } else if (component.componentType() == OOPSourceModelConstants.ComponentType.STRUCT) {
                cmpPUMLStr += " << (S," + colorScheme.structCircledCharacterBackgroundColor() + ") >> ";
            }
            // Insert background color tag
            componentPUMLStrings.add(colorBaseComponentBackground(component, cmpPUMLStr) + " {\n");
            // Stores the required length of lines in the component's doc text preamble
            int docTextLineLength = 80;
            // Get all child components
            Set<DiagramComponent> childComponents = new HashSet<>();
            component.children().forEach(s -> {
                DiagramComponent childComponent = allComponents.get(s);
                if (childComponent != null) {
                    childComponents.add(childComponent);
                }
            });
            // Group child components into method type and field types
            Set<DiagramComponent> methodChilds = childComponents.stream().filter(diagramComponent -> diagramComponent.componentType().isMethodComponent())
                    .collect(Collectors.toSet());
            Set<DiagramComponent> fieldChilds = childComponents.stream().filter(diagramComponent -> diagramComponent.componentType().isVariableComponent())
                    .collect(Collectors.toSet());
            // Insert PUML text for field children
            boolean zeroFields = true;
            for (DiagramComponent fieldChild : fieldChilds) {
                if (shouldDisplayChildCmp(largeComponent, fieldChild)) {
                    String childCmpPUMLStr = childComponentPUMLText(fieldChild);
                    if (!childCmpPUMLStr.isEmpty()) {
                        componentPUMLStrings.add(childCmpPUMLStr);
                        zeroFields = false;
                        if (childCmpPUMLStr.length() > docTextLineLength) {
                            docTextLineLength = childCmpPUMLStr.length();
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
                        if (childCmpPUMLStr.length() > docTextLineLength) {
                            docTextLineLength = childCmpPUMLStr.length();
                        }
                    }
                }
            }
            // If there were no methods, remove the previous separation line
            if (zeroMethods && !zeroFields) {
                componentPUMLStrings.remove(componentPUMLStrings.size() - 1);
            }
            // Generate component doc text last since it needs to fit within the component box width constraints.
            if (component.comment() != null && !component.comment().isEmpty()) {
                String componentDoc = componentDocText(docTextLineLength, component);
                if (componentDoc != null && !componentDoc.isEmpty()) {
                    // Only insert line if the component displays children
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
                || deletedComponents.contains(childComponent);
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
                if (childCmp.componentType().isMethodComponent() || (childCmp.componentType().isVariableComponent() && childCmp.componentType() != OOPSourceModelConstants.ComponentType.ENUM_CONSTANT)) {
                    childCmpPUMLStr += childCmp.codeFragment() + " ";
                }
                if (childCmp.componentType() == OOPSourceModelConstants.ComponentType.ENUM_CONSTANT) {
                    childCmpPUMLStr += childCmp.name() + " ";
                }
            }
        }
        // Handle modifiers...
        String visibilitySymbol = "";
        if (!childCmpPUMLStr.isEmpty()) {
            visibilitySymbol = visibilitySymbol(childCmp);
            // Add background color tag
            childCmpPUMLStr = visibilitySymbol + " "
                    + colorChildComponentBackground(childCmp, childCmpPUMLStr.trim()) + "\n";
        }
        return childCmpPUMLStr;
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
    private String componentDocText(int docTextLineLength, DiagramComponent component) {
        String commentStr = new StiffComponentDocText(component.comment().trim(), docTextLineLength).value();
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
     * Used for coloring the background of a specific child component (method, field, etc..)within a base component.
     */
    private String colorChildComponentBackground(DiagramComponent childComponent, String text) {
        if (text.trim().isEmpty()) {
            return text;
        } else if (addedComponents.contains(childComponent)) {
            return "<back:" + colorScheme.addedComponentColor() + ">" + text + "</back>         ";
        } else if (deletedComponents.contains(childComponent)) {
            return "<back:" + colorScheme.deletedComponentColor() + ">" + text + "</back>         ";
        } else {
            return text;
        }
    }

    /**
     * Used for coloring the entire background of the given base component.
     */
    private String colorBaseComponentBackground(DiagramComponent component, String text) {
        if (addedComponents.contains(component)) {
            return text + " " + colorScheme.addedComponentColor();
        } else if (deletedComponents.contains(component)) {
            return text + " " + colorScheme.deletedComponentColor();
        } else {
            return text;
        }
    }
}
