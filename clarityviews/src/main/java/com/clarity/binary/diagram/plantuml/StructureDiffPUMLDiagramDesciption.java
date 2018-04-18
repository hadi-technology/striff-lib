package com.clarity.binary.diagram.plantuml;

import com.clarity.binary.diagram.DiagramComponent;
import com.clarity.binary.diagram.scheme.DiagramColorScheme;
import com.clarity.binary.extractor.BinaryClassRelationship;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StructureDiffPUMLDiagramDesciption implements PUMLDiagramDescription {

    private Set<DiagramComponent> diagramComponents;
    private Map<String, DiagramComponent> allComponents;
    private List<String> deletedComponents;
    private List<String> addedComponents;
    private List<BinaryClassRelationship> deletedRelationships;
    private List<BinaryClassRelationship> addedRelationships;
    private DiagramColorScheme colorScheme;
    private List<String> modifiedComponents;
    private static final String PLANT_UML_BEGIN_STRING = "@startuml\nskinparam linetype ortho\n";
    private static final String PLANT_UML_END_STRING = "\n@enduml";
    private Set<BinaryClassRelationship> allRelationships;


    public StructureDiffPUMLDiagramDesciption(Set<DiagramComponent> diagramComponents,
                                              Set<BinaryClassRelationship> allRelationships, List<BinaryClassRelationship> deletedRelationships,
                                              List<BinaryClassRelationship> addedRelationships, List<String> deletedComponents,
                                              List<String> addedComponents, Map<String, DiagramComponent> allComponents, DiagramColorScheme colorScheme,
                                              List<String> modifiedComponents) {
        this.diagramComponents = diagramComponents;
        this.allComponents = allComponents;
        this.addedComponents = addedComponents;
        this.deletedComponents = deletedComponents;
        this.addedRelationships = addedRelationships;
        this.deletedRelationships = deletedRelationships;
        this.modifiedComponents = modifiedComponents;
        this.allRelationships = allRelationships;
        this.colorScheme = colorScheme;
    }

    @Override
    public String description() {
        return PLANT_UML_BEGIN_STRING + formPlantUMLSkinString(colorScheme) + new StructureDiffPUMLClassDescription(diagramComponents,
                deletedComponents, addedComponents, allComponents, colorScheme, modifiedComponents).value()
                + "\n" +
                new StructureDiffPUMLRelationsDescription(diagramComponents, allRelationships, deletedRelationships,
                        addedRelationships, colorScheme, modifiedComponents).value() +
                PLANT_UML_END_STRING;
    }

    private String formPlantUMLSkinString(DiagramColorScheme colorScheme) {
        return "skinparam defaultFontName " + colorScheme.defaultFontName() + "\n" + "skinparam backgroundColor  "
                + colorScheme.backgroundColor() + "\n" + "\n" + "skinparam classArrowColor "
                + colorScheme.classArrowColor() + "\nskinparam legendBackgroundColor " + colorScheme.legendBackgroundColor() + "\nskinparam classBackgroundColor "
                + colorScheme.classBackgroundColor() + "\n" + "skinparam classArrowFontColor "
                + colorScheme.classArrowFontColor() + "\n" + "skinparam classArrowFontSize "
                + colorScheme.classArrowFontSize() + "\n" + "skinparam classFontColor "
                + colorScheme.classFontColor() + "\n" + "skinparam classFontSize " + colorScheme.classFontSize()
                + "\n" + "skinparam classStereotypeFontColor " + colorScheme.classStereotypeFontColor() + "\n"
                + "skinparam classAttributeFontColor " + colorScheme.classAttributeFontColor() + "\n"
                + "skinparam classAttributeFontSize " + colorScheme.classAttributeFontSize() + "\n"
                + "skinparam classFontName " + colorScheme.classFontName() + "\n" + "skinparam classAttributeFontName "
                + colorScheme.classAttributeFontName() + "\n" + "skinparam titleFontColor "
                + colorScheme.titleFontColor() + "\n" + "skinparam packageBackgroundColor "
                + colorScheme.packageBackgroundColor() + "\n" + "skinparam titleFontName " + colorScheme.titleFontName()
                + "\n" + "skinparam packageBorderColor " + colorScheme.packageBorderColor() + "\n"
                + "skinparam packageFontColor " + colorScheme.packageFontColor() + "\n" + "skinparam packageFontName "
                + colorScheme.packageFontName() + "\n" + "skinparam packageFontStyle " + colorScheme.packageFontStyle()
                + "\n" + "skinparam packageFontSize " + colorScheme.packageFontSize()
                + "\n" + "skinparam classBorderThickness " + colorScheme.classBorderThickness() + "\n"
                + "skinparam classHeaderBackgroundColor " + colorScheme.classHeaderBackgroundColor() + "\n"
                + "skinparam classBorderColor " + colorScheme.classBorderColor() + "\nskinparam minClassWidth "
                + colorScheme.minClassWidth() + "\n";
    }
}
