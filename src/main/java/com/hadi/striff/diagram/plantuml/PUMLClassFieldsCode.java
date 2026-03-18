package com.hadi.striff.diagram.plantuml;

import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.spi.ClassDecorator;
import com.hadi.striff.spi.ClassInsertionPoint;
import com.hadi.striff.text.StriffComponentDocText;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class PUMLClassFieldsCode {

    private static final int MAX_ATTRIBUTE_SIZE = 20;
    private final OOPSourceCodeModel mergedModel;
    private final Set<String> deletedComponents;
    private final Set<String> addedComponents;
    private final Set<String> modifiedComponents;
    private final DiagramDisplay diagramDisplay;
    private final List<ClassDecorator> classDecorators;
    private int commentTextIndex = 1;

    PUMLClassFieldsCode(PUMLDiagramData data) {
        this.mergedModel = data.mergedModel();
        this.addedComponents = data.addedCmps();
        this.deletedComponents = data.deletedCmps();
        this.modifiedComponents = data.modifiedCmps();
        this.diagramDisplay = data.diagramDisplay();
        this.classDecorators = data.classDecorators();
    }

    public String value(Collection<DiagramComponent> cmps) {
        final StringBuilder tempStrBuilder = new StringBuilder();
        for (final DiagramComponent cmp : cmps) {
            String cmpPUMLStr = "";
            List<String> componentPUMLStrings = new ArrayList<>();
            if (cmp.modifiers().contains(
                    // Insert abstract keyword if cmp is Abstract class
                    OOPSourceModelConstants.getJavaAccessModifierMap()
                            .get(OOPSourceModelConstants.AccessModifiers.ABSTRACT))) {
                cmpPUMLStr += (OOPSourceModelConstants.getJavaAccessModifierMap()
                        .get(OOPSourceModelConstants.AccessModifiers.ABSTRACT)
                        + " ");
            }
            boolean largeComponent = cmp.children().size() > MAX_ATTRIBUTE_SIZE;
            // Insert cmp type name (eg: class, interface, etc...)
            cmpPUMLStr += cmp.componentType().getValue() + " ";
            // Insert the actual cmp unique name with hyphens instead of dots since
            // PUML attempts to parse dots as nested packages (we are handling
            // packages explicitly ourselves)
            cmpPUMLStr += PUMLHelper.pumlId(cmp.uniqueName()) + " as \"";
            // Insert cmp display name
            String displayName = cmp.componentName();
            if (cmp.augmentation("syntheticDisplayName").isPresent()) {
                displayName = cmp.augmentation("syntheticDisplayName").get().toString();
            } else if (cmp.module() != null && !cmp.module().isEmpty()) {
                // Strip module prefix from display name if redundant (e.g., "Module.ModuleName" -> "ModuleName")
                String module = cmp.module();
                if (displayName.startsWith(module + ".")) {
                    displayName = displayName.substring(module.length() + 1);
                }
            }
            // Insert change summary if applicable (inside quotes, after display name)
            String changeSummary = generateChangeSummary(cmp);
            if (largeComponent) {
                cmpPUMLStr += displayName + changeSummary + " <b><color:"
                        + this.diagramDisplay.colorScheme().classFontColor() + ">(...)\"";
            } else {
                cmpPUMLStr += displayName + changeSummary + "\"";
            }

            // Insert class generics if required
            if (cmp.codeFragment() != null) {
                cmpPUMLStr += (cmp.codeFragment());
            }

            // Insert synthetic module stereotypes if applicable
            if (cmp.augmentation("synthetic").isPresent()
                    && Boolean.TRUE.equals(cmp.augmentation("synthetic").get())) {
                String syntheticColor = this.diagramDisplay.colorScheme().syntheticStereotypeFontColor();
                cmpPUMLStr += " << (M," + syntheticColor + ") >><<synthetic>>";
            }

            // Insert background color tag
            componentPUMLStrings.add(enhanceBaseCmp(cmp, cmpPUMLStr) + " {\n");
            emitDecorators(componentPUMLStrings, ClassInsertionPoint.TOP, cmp);
            // Stores the required length of lines in the cmp's doc text preamble
            int docTextCharLen = 80;
            // Get all child components
            Set<DiagramComponent> childComponents = new HashSet<>();
            cmp.children().forEach(s -> {
                DiagramComponent childComponent = new DiagramComponent(mergedModel.getComponent(s).get(), mergedModel);
                if (childComponent != null) {
                    childComponents.add(childComponent);
                }
            });
            // Group child components into method type and field types
            Set<DiagramComponent> methodChilds = childComponents.stream().filter(
                    diagramComponent -> diagramComponent.componentType().isMethodComponent())
                    .collect(Collectors.toSet());
            Set<DiagramComponent> fieldChilds = childComponents.stream().filter(
                    diagramComponent -> diagramComponent
                            .componentType() == OOPSourceModelConstants.ComponentType.ENUM_CONSTANT
                            || diagramComponent
                                    .componentType() == OOPSourceModelConstants.ComponentType.INTERFACE_CONSTANT
                            || diagramComponent.componentType() == OOPSourceModelConstants.ComponentType.FIELD)
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
            emitDecorators(componentPUMLStrings, ClassInsertionPoint.AFTER_FIELDS, cmp);
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
            emitDecorators(componentPUMLStrings, ClassInsertionPoint.AFTER_METHODS, cmp);
            // Generate cmp doc text last since it needs to fit within the cmp box
            // width constraints.
            if (cmp.comment() != null && !cmp.comment().isEmpty()) {
                String componentDoc = componentDocText(docTextCharLen, cmp);
                if (!componentDoc.isEmpty()) {
                    // Only insert line if the cmp displays children
                    if (zeroFields && zeroMethods) {
                        componentPUMLStrings.add(this.commentTextIndex, componentDoc + "\n");
                    } else {
                        componentPUMLStrings.add(this.commentTextIndex, componentDoc + "--\n");
                    }
                }
            }
            emitDecorators(componentPUMLStrings, ClassInsertionPoint.BOTTOM, cmp);
            tempStrBuilder.append(StringUtils.join(componentPUMLStrings, " ")).append("}\n");
        }
        return tempStrBuilder.toString();
    }

    private void emitDecorators(List<String> out, ClassInsertionPoint point, DiagramComponent component) {
        if (classDecorators.isEmpty()) {
            return;
        }
        for (ClassDecorator decorator : classDecorators) {
            if (decorator.insertionPoint() != point) {
                continue;
            }
            List<String> extra = decorator.decorateClass(component, diagramDisplay);
            if (extra != null && !extra.isEmpty()) {
                for (String line : extra) {
                    if (line == null || line.isEmpty()) {
                        continue;
                    }
                    if (line.endsWith("\n")) {
                        out.add(line);
                    } else {
                        out.add(line + "\n");
                    }
                }
            }
        }
    }

    private boolean shouldDisplayChildCmp(boolean isLargeParentCmp, DiagramComponent childComponent) {
        return !isLargeParentCmp
                || addedComponents.contains(childComponent.uniqueName())
                || deletedComponents.contains(childComponent.uniqueName())
                || modifiedComponents.contains(childComponent.uniqueName());
    }

    private String childComponentPUMLText(DiagramComponent childCmp) {
        String childCmpPUMLStr = "";
        if ((childCmp.componentType() == OOPSourceModelConstants.ComponentType.METHOD)
                || (childCmp.componentType() == OOPSourceModelConstants.ComponentType.FUNCTION)
                || childCmp.componentType().isVariableComponent()) {
            if (!childCmp.componentType().isBaseComponent()) {
                // if the field/method is abstract or static, add the {abstract}/{static}
                // prefix..
                if (childCmp.modifiers().contains(
                        OOPSourceModelConstants.getJavaAccessModifierMap()
                                .get(OOPSourceModelConstants.AccessModifiers.ABSTRACT))) {
                    childCmpPUMLStr += ("{");
                    childCmpPUMLStr += OOPSourceModelConstants.getJavaAccessModifierMap()
                            .get(OOPSourceModelConstants.AccessModifiers.ABSTRACT);
                    childCmpPUMLStr += ("} ");
                }
                if (childCmp.modifiers().contains(
                        OOPSourceModelConstants.getJavaAccessModifierMap()
                                .get(OOPSourceModelConstants.AccessModifiers.STATIC))) {
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
            // Truncate long method signatures but preserve return type
            if (childCmp.componentType().isMethodComponent() && childCmpDisplayText != null) {
                childCmpDisplayText = truncateMethodSignature(childCmpDisplayText);
            }
        }
        if (childCmp.componentType() == OOPSourceModelConstants.ComponentType.ENUM_CONSTANT) {
            childCmpDisplayText = childCmp.name();
        }
        return childCmpDisplayText;
    }

    /**
     * Truncates long method signatures to "methodName(...) : ReturnType" format.
     * Short signatures like "ping() : String" are preserved as-is.
     */
    private String truncateMethodSignature(String signature) {
        if (signature == null || signature.isEmpty()) {
            return signature;
        }

        // Find the method name and opening parenthesis
        int openParenIndex = signature.indexOf('(');
        if (openParenIndex == -1) {
            return signature;  // Not a method signature, return as-is
        }

        // Check if there are parameters (content between parentheses)
        int closeParenIndex = signature.indexOf(')', openParenIndex);
        if (closeParenIndex == -1) {
            return signature;  // Malformed signature, return as-is
        }

        // If there are parameters between ( and ), truncate to (... )
        if (closeParenIndex > openParenIndex + 1) {
            // Extract method name and return type
            String methodName = signature.substring(0, openParenIndex);
            String rest = signature.substring(closeParenIndex + 1);

            // Check for return type (format: " : ReturnType")
            if (rest.trim().startsWith(":")) {
                return methodName + "(...)" + rest;
            } else {
                // No return type, just truncate parameters
                return methodName + "(...)";
            }
        }

        return signature;  // Short signature, return as-is
    }

    private String visibilitySymbol(DiagramComponent childCmp) {
        String visibilitySymbol = "";
        if (childCmp.modifiers().contains(
                OOPSourceModelConstants.getJavaAccessModifierMap()
                        .get(OOPSourceModelConstants.AccessModifiers.PUBLIC))) {
            visibilitySymbol += OOPSourceModelConstants.AccessModifiers.PUBLIC.getUMLClassDigramSymbol() + " ";
        } else if (childCmp.modifiers().contains(
                OOPSourceModelConstants.getJavaAccessModifierMap()
                        .get(OOPSourceModelConstants.AccessModifiers.PRIVATE))) {
            visibilitySymbol += OOPSourceModelConstants.AccessModifiers.PRIVATE.getUMLClassDigramSymbol() + " ";
        } else if (childCmp.modifiers().contains(
                OOPSourceModelConstants.getJavaAccessModifierMap()
                        .get(OOPSourceModelConstants.AccessModifiers.PROTECTED))) {
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
        String commentStr = new StriffComponentDocText(component.comment().trim(), docTextCharLen).value();
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
     * Used for coloring the background of a specific child component (method,
     * field, etc..)
     * within a base component.
     */
    private String colorChildComponentBackground(DiagramComponent childComponent, String text) {
        if (text.trim().isEmpty()) {
            return text;
        } else if (addedComponents.contains(childComponent.uniqueName())) {
            return "<back:" + this.diagramDisplay.colorScheme().addedComponentColor()
                    + ">" + text + "</back>         ";
        } else if (deletedComponents.contains(childComponent.uniqueName())) {
            return "<back:" + this.diagramDisplay.colorScheme().deletedComponentColor() + ">"
                    + text + "</back>         ";
        } else if (modifiedComponents.contains(childComponent.uniqueName())) {
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
        String backgroundColorText = "#back:" + this.diagramDisplay.colorScheme().backgroundColor().replace("#", "");
        String headerColor = ";header:" + this.diagramDisplay.colorScheme().defaultClassHeaderColor().replace("#", "");
        if (addedComponents.contains(cmp.uniqueName())) {
            backgroundColorText = "#back:" + addedColor;
        } else if (deletedComponents.contains(cmp.uniqueName())) {
            backgroundColorText = "#back:" + deletedColor;
        }
        text += " " + backgroundColorText + headerColor;
        return text + " ";
    }

    /**
     * Generates a change summary for the component showing added/deleted/modified children.
     * Format: " [ <color:#color>+count</color> ... ]"
     */
    private String generateChangeSummary(DiagramComponent cmp) {
        StringBuilder summary = new StringBuilder();

        int addedCount = 0;
        int deletedCount = 0;
        int modifiedCount = 0;

        // Count changes among the component's children
        for (String childName : cmp.children()) {
            if (addedComponents.contains(childName)) {
                addedCount++;
            }
            if (deletedComponents.contains(childName)) {
                deletedCount++;
            }
            if (modifiedComponents.contains(childName)) {
                modifiedCount++;
            }
        }

        // Build the summary string
        if (addedCount > 0 || deletedCount > 0 || modifiedCount > 0) {
            summary.append(" [ ");
            if (addedCount > 0) {
                String addedColor = this.diagramDisplay.colorScheme().addedComponentColor();
                summary.append("<color:").append(addedColor).append(">+").append(addedCount).append("</color> ");
            }
            if (deletedCount > 0) {
                String deletedColor = this.diagramDisplay.colorScheme().deletedComponentColor();
                summary.append("<color:").append(deletedColor).append(">-").append(deletedCount).append("</color> ");
            }
            if (modifiedCount > 0) {
                String modifiedColor = this.diagramDisplay.colorScheme().modifiedComponentColor();
                summary.append("<color:").append(modifiedColor).append(">+").append(modifiedCount).append("</color> ");
            }
            // Remove trailing space before closing bracket
            if (summary.charAt(summary.length() - 1) == ' ') {
                summary.setLength(summary.length() - 1);
            }
            summary.append("]");
        }

        return summary.toString();
    }
}
