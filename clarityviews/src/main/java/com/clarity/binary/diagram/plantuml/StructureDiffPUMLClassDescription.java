package com.clarity.binary.diagram.plantuml;

import com.clarity.binary.DefaultText;
import com.clarity.binary.HtmlTagsStrippedText;
import com.clarity.binary.JavaDocSymbolStrippedText;
import com.clarity.binary.LineBreakedText;
import com.clarity.binary.SimplifiedJavaDocText;
import com.clarity.binary.diagram.DiagramComponent;
import com.clarity.binary.diagram.scheme.DiagramColorScheme;
import com.clarity.sourcemodel.OOPSourceModelConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StructureDiffPUMLClassDescription {

    private static int largeSize = 20;
    private Set<DiagramComponent> diagramComponents;
    private Map<String, DiagramComponent> allComponents;
    private List<String> deletedComponents;
    private List<String> addedComponents;
    private DiagramColorScheme colorScheme;
    private List<String> modifiedComponents;

    StructureDiffPUMLClassDescription(Set<DiagramComponent> diagramComponents,
                                      List<String> deletedComponents,
                                      List<String> addedComponents, Map<String, DiagramComponent> allComponents, DiagramColorScheme colorScheme,
                                      List<String> modifiedComponents) {
        this.diagramComponents = diagramComponents;
        this.allComponents = allComponents;
        this.addedComponents = addedComponents;
        this.deletedComponents = deletedComponents;
        this.modifiedComponents = modifiedComponents;
        this.colorScheme = colorScheme;
    }

    public String value() {
        final StringBuilder tempStrBuilder = new StringBuilder();
        for (final DiagramComponent component : diagramComponents) {
            String cmpPUMLStr = "";
            List<String> componentPUMLStrings = new ArrayList<>();
            // determine if we have base component type and it is not a child of a method component type...
            if (component.componentType().isBaseComponent() && !component.uniqueName().contains("(")) {
                // list of methods from this component we don't want to display in the diagram
                List<String> ignoreMethods = new ArrayList<>();
                if (component.modifiers().contains(
                        // if class is abstract...
                        OOPSourceModelConstants.getJavaAccessModifierMap().get(OOPSourceModelConstants.AccessModifiers.ABSTRACT))) {
                    cmpPUMLStr += (OOPSourceModelConstants.getJavaAccessModifierMap().get(OOPSourceModelConstants.AccessModifiers.ABSTRACT)
                            + " ");
                }

                boolean hasChanged = isChanged(component);

                // add component type name (eg: class, interface, etc...)
                cmpPUMLStr += component.componentType().getValue() + " ";

                // add the actual unique name
                String diagramCmpUniqueName = (component.uniqueName().replaceAll("-", "").replaceAll("\\.\\.+", ".") + " ");
                cmpPUMLStr += diagramCmpUniqueName + "as \" ";

                if (component.children().size() > largeSize) {
                    cmpPUMLStr += "<color:#ffe680><size:18><&magnifying-glass></size></color> ";
                }

                if (addedComponents.contains(component.uniqueName()) || deletedComponents.contains(component.uniqueName())) {
                    cmpPUMLStr += "<color:black>";
                }

                cmpPUMLStr += "<size:" + colorScheme.classFontSize() + "><&caret-left></size>" + component.name() +
                        "<size:" + colorScheme.classFontSize() + "><&caret-right></size> \"";

                // add class generics if exist
                if (component.codeFragment() != null) {
                    cmpPUMLStr += (component.codeFragment());
                }

                // use special blue color for class stereotype
                if (component.componentType() == OOPSourceModelConstants.ComponentType.CLASS && !component.modifiers().contains("abstract")) {
                    cmpPUMLStr += " << (C,5599ff) >> ";
                } else if (component.componentType() == OOPSourceModelConstants.ComponentType.STRUCT) {
                    // use special orange color for struct stereotype
                    cmpPUMLStr += " << (S,ffbb55) >> ";
                }

                // add background color tag
                componentPUMLStrings
                        .add(colorClassBackground(component, addedComponents, deletedComponents, cmpPUMLStr) + " {\n");

                // used for setting the length of the doc comments
                int longestLine = 0;

                // children are only displayed for base components that have changed or contain few children in them.
                if (hasChanged || component.children().size() <= largeSize) {
                    for (final String classChildCmpName : component.children()) {
                        // if the current base component has many children, only show the ones that have changed
                        if (component.children().size() <= largeSize || addedComponents.contains(classChildCmpName)
                                || deletedComponents.contains(classChildCmpName) || modifiedComponents.contains(classChildCmpName)) {
                            final DiagramComponent childCmp = allComponents.get(classChildCmpName);
                            String childCmpPUMLStr = "";
                            if ((childCmp.componentType() == OOPSourceModelConstants.ComponentType.METHOD
                                    && !ignoreMethods.contains(childCmp.name()))
                                    || childCmp.componentType().isVariableComponent()) {
                                // start entering the fields and methods...
                                if (!childCmp.componentType().isBaseComponent()) {
                                    // if the field/method is abstract or static, add
                                    // the {abstract}/{static} prefix..
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
                            // handle modifiers...
                            String visibilitySymbol = "";
                            if (!childCmpPUMLStr.isEmpty()) {
                                visibilitySymbol = visibilitySymbol(childCmp);
                            }
                            // add background color tag
                            String finalPUMLText = visibilitySymbol + " "
                                    + colorTextBackground(childCmp, childCmpPUMLStr.trim());
                            componentPUMLStrings.add(finalPUMLText + "\n");
                            if (finalPUMLText.length() > longestLine) {
                                longestLine = finalPUMLText.length();
                            }
                        }
                    }
                } else {
                    // hack to add some depth to the empty class diagram box...
                    for (int i = 0; i < 5; i++) {
                        componentPUMLStrings.add("\n");
                    }
                }
                // if interface, add doc
                if (longestLine < 80) {
                    longestLine = 80;
                }
                if (component.componentType() == OOPSourceModelConstants.ComponentType.INTERFACE
                        || component.modifiers().contains("abstract") || (!hasChanged && component.children().size() > largeSize)) {
                    if (component.comment() != null && !component.comment().isEmpty()) {
                        drawComponentComment(longestLine, component, componentPUMLStrings);
                    }
                }
                tempStrBuilder.append(org.apache.commons.lang.StringUtils.join(componentPUMLStrings, " ")).append("}\n");
            }
        }
        return tempStrBuilder.toString();
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

    private void drawComponentComment(int longestLine, DiagramComponent component, List<String> componentPUMLStrings) {
        String commentStr = new LineBreakedText(
                new JavaDocSymbolStrippedText(new HtmlTagsStrippedText(
                        new SimplifiedJavaDocText(new DefaultText(component.comment().trim())))),
                longestLine).value();
        if (!commentStr.isEmpty()) {
            if (commentStr.length() < 800) {
                commentStr += "\n";

            } else {
                commentStr = commentStr.substring(0, 797).trim();
                commentStr += "...\n";
            }
            // splits the comment into equal sized lines..
            String[] lines = commentStr.split("\n");
            for (int i = 0; i < lines.length; i++) {
                // replace curly brackets. If there is code in the comment, PlantUML will
                // throw an error unless we do so.
                lines[i] = colorTextBackground(component, lines[i].trim().replaceAll("\\{", "[").replaceAll("}", "]"));
            }
            // adds the doc right after the class declaration (after the first element)
            componentPUMLStrings.add(1,org.apache.commons.lang.StringUtils.join(lines, "\n"));
            // add double line underneath the comment only if the class has methods and fields
            if (component.children().size() > 0 && component.children().size() < largeSize) {
                componentPUMLStrings.set(1, componentPUMLStrings.get(1) + "\n--\n");
            }
            componentPUMLStrings.add("\n");
        }
    }

    private String colorTextBackground(DiagramComponent cmp, String text) {
        if (text.trim().isEmpty()) {
            return text;
        } else if (addedComponents.contains(cmp.uniqueName())) {
            return "<back:" + colorScheme.addedComponentColor() + ">" + text + "</back>         ";
        } else if (deletedComponents.contains(cmp.uniqueName())) {
            return "<back:" + colorScheme.deletedComponentColor() + ">" + text + "</back>         ";
        } else if (modifiedComponents.contains(cmp.uniqueName())) {
            return "<back:" + colorScheme.modifiedComponentColor() + ">" + text + "</back>         ";
        } else {
            return text;
        }
    }

    private boolean isChanged(DiagramComponent cmp) {
        if (addedComponents.contains(cmp.uniqueName()) || deletedComponents.contains(cmp.uniqueName())
                || modifiedComponents.contains(cmp.uniqueName())) {
            return true;
        } else {
            for (String childName : cmp.children()) {
                if (addedComponents.contains(childName) || deletedComponents.contains(childName)
                        || modifiedComponents.contains(childName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String colorClassBackground(DiagramComponent cmp, List<String> addedComponents2, List<String> deletedComponents2,
                                        String text) {
        if (addedComponents2.contains(cmp.uniqueName())) {
            return text + " " + colorScheme.addedComponentColor();
        } else if (deletedComponents2.contains(cmp.uniqueName())) {
            return text + " " + colorScheme.deletedComponentColor();
        } else {
            return text;
        }
    }
}