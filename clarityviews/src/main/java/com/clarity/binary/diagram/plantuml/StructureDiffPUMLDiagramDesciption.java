package com.clarity.binary.diagram.plantuml;

import com.clarity.binary.DefaultText;
import com.clarity.binary.HtmlTagsStrippedText;
import com.clarity.binary.JavaDocSymbolStrippedText;
import com.clarity.binary.LineBreakedText;
import com.clarity.binary.SimplifiedJavaDocText;
import com.clarity.binary.diagram.DiagramConstants.BinaryClassAssociation;
import com.clarity.binary.diagram.DiagramConstants.DefaultClassMultiplicities;
import com.clarity.binary.diagram.scheme.DiagramColorScheme;
import com.clarity.binary.extractor.BinaryClassRelationship;
import com.clarity.binary.extractor.ColoredBinaryClassAssociation;
import com.clarity.invocation.ComponentInvocation;
import com.clarity.sourcemodel.Component;
import com.clarity.sourcemodel.OOPSourceModelConstants;
import com.clarity.sourcemodel.OOPSourceModelConstants.AccessModifiers;
import com.clarity.sourcemodel.OOPSourceModelConstants.ComponentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StructureDiffPUMLDiagramDesciption implements PUMLDiagramDescription {

    private Set<Component> diagramComponents;
    private Map<String, Component> allComponents;
    private Set<BinaryClassRelationship> binaryRelationships;
    private List<String> deletedComponents;
    private List<String> addedComponents;
    private List<BinaryClassRelationship> deletedRelationships;
    private List<BinaryClassRelationship> addedRelationships;
    private DiagramColorScheme colorScheme;

    public StructureDiffPUMLDiagramDesciption(Set<Component> diagramComponents,
                                              Set<BinaryClassRelationship> allRelationships, List<BinaryClassRelationship> deletedRelationships,
                                              List<BinaryClassRelationship> addedRelationships, List<String> deletedComponents,
                                              List<String> addedComponents, Map<String, Component> allComponents, DiagramColorScheme colorScheme) {
        this.diagramComponents = diagramComponents;
        this.allComponents = allComponents;
        this.addedComponents = addedComponents;
        this.deletedComponents = deletedComponents;
        this.binaryRelationships = allRelationships;
        this.addedRelationships = addedRelationships;
        this.deletedRelationships = deletedRelationships;
        this.colorScheme = colorScheme;
    }

    @Override
    public String description() {
        return classDesciptionString() + relationsDesciptionString();
    }

    public String classDesciptionString() {
        final StringBuilder tempStrBuilder = new StringBuilder();
        for (final Component component : diagramComponents) {
            String cmpPUMLStr = "";
            List<String> componentPUMLStrings = new ArrayList<String>();
            // determine if we have base component type and it is not a child of a method component type...
            if (component.componentType().isBaseComponent() && !component.uniqueName().contains("(")) {
                // list of methods from this component we don't want to display in the diagram
                List<String> ignoreMethods = new ArrayList<>();
                findMethodsToIgnoreInDiagram(component, component.uniqueName(), allComponents, ignoreMethods);
                if (component.modifiers().contains(
                        // if class is abstract...
                        OOPSourceModelConstants.getJavaAccessModifierMap().get(AccessModifiers.ABSTRACT))) {
                    cmpPUMLStr += (OOPSourceModelConstants.getJavaAccessModifierMap().get(AccessModifiers.ABSTRACT)
                            + " ");
                }
                // add component type name (eg: class, interface, etc...)
                cmpPUMLStr += component.componentType().getValue() + " ";
                // add the actual component short name
                cmpPUMLStr += (component.uniqueName() + " ");
                // add class generics if exist
                if (component.codeFragment() != null) {
                    cmpPUMLStr += (component.codeFragment());
                }
                // use special blue color for class stereotype
                if (component.componentType() == ComponentType.CLASS && !component.modifiers().contains("abstract")) {
                    cmpPUMLStr += " << (C,5599ff) >> ";
                } else if (component.componentType() == ComponentType.STRUCT) {
                    // use special orange color for struct stereotype
                    cmpPUMLStr += " << (S,ffbb55) >> ";
                }
                // open the brackets
                componentPUMLStrings
                        .add(colorClassBackground(component, addedComponents, deletedComponents, cmpPUMLStr) + " {\n");

                // used for setting the length of the doc comments
                int longestLine = 0;

                for (final String classChildCmpName : component.children()) {
                    final Component childCmp = allComponents.get(classChildCmpName);
                    String childCmpPUMLStr = "";
                    if ((childCmp.componentType() == ComponentType.METHOD
                            && !ignoreMethods.contains(childCmp.name()))
                            || childCmp.componentType().isVariableComponent()) {
                        if (childCmp.componentType().isMethodComponent()
                                && (childCmp.name().startsWith("get") || childCmp.name().startsWith("set"))) {
                            continue;
                        }
                        // start entering the fields and methods...
                        if ((childCmp != null) && !childCmp.componentType().isBaseComponent()) {
                            // if the field/method is abstract or static, add
                            // the {abstract}/{static} prefix..
                            if (childCmp.modifiers().contains(
                                    OOPSourceModelConstants.getJavaAccessModifierMap().get(AccessModifiers.ABSTRACT))) {
                                childCmpPUMLStr += ("{");
                                childCmpPUMLStr += OOPSourceModelConstants.getJavaAccessModifierMap()
                                        .get(AccessModifiers.ABSTRACT);
                                childCmpPUMLStr += ("} ");
                            }
                            if (childCmp.modifiers().contains(
                                    OOPSourceModelConstants.getJavaAccessModifierMap().get(AccessModifiers.STATIC))) {
                                childCmpPUMLStr += ("{");
                                childCmpPUMLStr += (OOPSourceModelConstants.getJavaAccessModifierMap()
                                        .get(AccessModifiers.STATIC));
                                childCmpPUMLStr += ("} ");
                            }

                            if (childCmp.componentType().isMethodComponent() || childCmp.componentType().isVariableComponent()) {
                                childCmpPUMLStr += childCmp.codeFragment() + " ";
                            }
                            if (childCmp.componentType() == ComponentType.ENUM) {
                                childCmpPUMLStr += childCmp.name() + " ";
                            }
                        }
                    }
                    // handle modifiers...
                    String visibilitySymbol = "";
                    if (!childCmpPUMLStr.isEmpty()) {
                        if (childCmp.modifiers().contains(
                                OOPSourceModelConstants.getJavaAccessModifierMap().get(AccessModifiers.PUBLIC))) {
                            visibilitySymbol += AccessModifiers.PUBLIC.getUMLClassDigramSymbol() + " ";
                        } else if (childCmp.modifiers().contains(
                                OOPSourceModelConstants.getJavaAccessModifierMap().get(AccessModifiers.PRIVATE))) {
                            visibilitySymbol += AccessModifiers.PRIVATE.getUMLClassDigramSymbol() + " ";
                        } else if (childCmp.modifiers().contains(
                                OOPSourceModelConstants.getJavaAccessModifierMap().get(AccessModifiers.PROTECTED))) {
                            visibilitySymbol += AccessModifiers.PROTECTED.getUMLClassDigramSymbol() + " ";
                        } else {
                            visibilitySymbol += AccessModifiers.NONE.getUMLClassDigramSymbol() + " ";
                        }
                    }
                    String finalPUMLText = visibilitySymbol + " "
                            + colorTextBackground(childCmp, addedComponents, deletedComponents, childCmpPUMLStr.trim());
                    componentPUMLStrings.add(finalPUMLText + "\n");
                    if (finalPUMLText.length() > longestLine) {
                        longestLine = finalPUMLText.length();
                    }
                }
                // if interface, add doc
                if (longestLine < 80) {
                    longestLine = 80;
                }
                if (component.componentType() == ComponentType.INTERFACE
                        || component.modifiers().contains("abstract")) {
                    if (component.comment() != null && !component.comment().isEmpty()) {
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
                                lines[i] = colorTextBackground(component, addedComponents, deletedComponents,
                                        lines[i].trim());
                            }
                            // adds the doc right after the component
                            // declaration (after the first element)
                            componentPUMLStrings.add(1,
                                    org.apache.commons.lang.StringUtils.join(lines, "\n") + "\n==\n");
                            componentPUMLStrings.add("\n");
                        }
                    }
                }
                tempStrBuilder.append(org.apache.commons.lang.StringUtils.join(componentPUMLStrings, " ") + "}\n");
            }

        }
        return tempStrBuilder.toString();
    }

    /**
     * Returns a list in the ignoreMethods variable of all the original component's methods that correspond to an
     * implemented/extended method specification. (We want to remove these because they make diagrams verbose).
     */
    private void findMethodsToIgnoreInDiagram(Component component, String originalComponent, Map<String, Component> allComponents, List<String> methodsToIgnore) {
        if (!component.componentInvocations(OOPSourceModelConstants.ComponentInvocations.EXTENSION).isEmpty()) {
            for (ComponentInvocation cmpInvc : component.componentInvocations(OOPSourceModelConstants.ComponentInvocations.EXTENSION)) {
                Component invkCmp = allComponents.get(cmpInvc.invokedComponent());
                if (invkCmp != null && !component.equals(invkCmp)) {
                    findMethodsToIgnoreInDiagram(invkCmp, originalComponent, allComponents, methodsToIgnore);
                }
            }
        }
        if (!component.componentInvocations(OOPSourceModelConstants.ComponentInvocations.IMPLEMENTATION).isEmpty()) {
            for (ComponentInvocation cmpInvc : component.componentInvocations(OOPSourceModelConstants.ComponentInvocations.IMPLEMENTATION)) {
                Component invkCmp = allComponents.get(cmpInvc.invokedComponent());
                if (invkCmp != null && !component.equals(invkCmp)) {
                    findMethodsToIgnoreInDiagram(invkCmp, originalComponent, allComponents, methodsToIgnore);
                }
            }
        }
        if (component != null && !component.uniqueName().equals(originalComponent)) {
            for (String child : component.children()) {
                Component childCmp = allComponents.get(child);
                if (childCmp != null && childCmp.componentType().isMethodComponent()) {
                    methodsToIgnore.add(childCmp.name());
                }
            }
        }

    }

    public String relationsDesciptionString() {
        final StringBuilder tempStrBuilder = new StringBuilder();
        for (BinaryClassRelationship relationship : binaryRelationships) {

            String classAName = relationship.getClassA().name();
            String classBName = relationship.getClassB().name();

            if ((classAName != null) && ((classBName != null) && !relationship.getClassA().uniqueName().contains("(")
                    && !relationship.getClassB().uniqueName().contains("(")
                    && diagramComponents.contains(relationship.getClassA())
                    && diagramComponents.contains(relationship.getClassB())
                    && relationship.getClassA().componentType().isBaseComponent()
                    && relationship.getClassB().componentType().isBaseComponent() && (relationship != null))) {
                final BinaryClassAssociation classAAssociation = relationship.getaSideAssociation();
                final BinaryClassAssociation classBAssociation = relationship.getbSideAssociation();
                // start building our string for side class A
                // insert class A short name
                tempStrBuilder.append(relationship.getClassA().uniqueName() + " ");
                // insert class B multiplicity if its not a zero to one
                // multiplicity..
                if (!relationship.getbSideMultiplicity().getValue().isEmpty() && !relationship.getbSideMultiplicity()
                        .getValue().equals(DefaultClassMultiplicities.ZEROTOONE.getValue())) {
                    tempStrBuilder.append("\"" + relationship.getbSideMultiplicity().getValue() + "\" ");
                }
                // insert class B association type
                tempStrBuilder.append(classBAssociation.getBackwardLinkEndingType());

                if (classAAssociation.getStrength() > classBAssociation.getStrength()) {
                    tempStrBuilder.append(new ColoredBinaryClassAssociation(classAAssociation,
                            arrowDiffColor(relationship, addedRelationships, deletedRelationships)).getyumlLinkType());
                } else {
                    tempStrBuilder.append(new ColoredBinaryClassAssociation(classBAssociation,
                            arrowDiffColor(relationship, addedRelationships, deletedRelationships)).getyumlLinkType());
                }
                // insert class A association type
                tempStrBuilder.append(classAAssociation.getForwardLinkEndingType());
                // insert class A multiplicity if its not a zero to one
                // multiplicity..
                if (!relationship.getaSideMultiplicity().getValue().isEmpty() && !relationship.getaSideMultiplicity()
                        .getValue().equals(DefaultClassMultiplicities.ZEROTOONE.getValue())) {
                    tempStrBuilder.append(" \"" + relationship.getaSideMultiplicity().getValue() + "\" ");
                }
                // insert class B name
                tempStrBuilder.append(" " + relationship.getClassB().uniqueName());
                tempStrBuilder.append("\n");
            }
        }
        return tempStrBuilder.toString();
    }

    private String colorTextBackground(Component cmp, List<String> addedComponents2, List<String> deletedComponents2,
                                       String text) {
        if (text.trim().isEmpty()) {
            return text;
        } else if (addedComponents2.contains(cmp.uniqueName())) {
            return "<back:" + colorScheme.addedComponentColor() + ">" + text + "</back>         ";
        } else if (deletedComponents2.contains(cmp.uniqueName())) {
            return "<back:" + colorScheme.deletedComponentColor() + ">" + text + "</back>         ";
        } else {
            return text;
        }
    }

    private String colorClassBackground(Component cmp, List<String> addedComponents2, List<String> deletedComponents2,
                                        String text) {
        if (addedComponents2.contains(cmp.uniqueName())) {
            return text + " " + colorScheme.addedComponentColor();
        } else if (deletedComponents2.contains(cmp.uniqueName())) {
            return text + " " + colorScheme.deletedComponentColor();
        } else {
            return text;
        }
    }

    private String arrowDiffColor(BinaryClassRelationship relation, List<BinaryClassRelationship> addedRelationships,
                                  List<BinaryClassRelationship> deletedRelationships) {
        for (BinaryClassRelationship bCR : addedRelationships) {
            if (bCR.equals(relation)) {
                return colorScheme.addedRelationColor();
            }
        }
        for (BinaryClassRelationship bCR : deletedRelationships) {
            if (bCR.equals(relation)) {
                return colorScheme.deletedRelationColor();
            }
        }
        return colorScheme.classArrowColor();
    }
}
