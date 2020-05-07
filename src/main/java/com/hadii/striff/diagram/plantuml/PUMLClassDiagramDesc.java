package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.ChangeSet;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.scheme.DiagramColorScheme;
import com.hadii.striff.extractor.ComponentRelations;

import java.util.List;
import java.util.Map;
import java.util.Set;

final class PUMLClassDiagramDesc {

    private final Set<DiagramComponent> diagramComponents;
    private final Map<String, DiagramComponent> allComponents;
    private final List<DiagramComponent> deletedComponents;
    private final List<DiagramComponent> addedComponents;
    private final ComponentRelations deletedRelationships;
    private final ComponentRelations addedRelationships;
    private final DiagramColorScheme colorScheme;
    private static final String PLANT_UML_BEGIN_STRING = "@startuml\nskinparam linetype ortho\n";
    private static final String PLANT_UML_END_STRING = "\n@enduml";
    private final ComponentRelations allRelationships;


    public PUMLClassDiagramDesc(final ChangeSet changeSet, final DiagramColorScheme colorScheme,
                                final Set<DiagramComponent> diagramComponents,
                                Map<String, DiagramComponent> allComponents,
                                ComponentRelations allRelations) {
        this.diagramComponents = diagramComponents;
        this.allComponents = allComponents;
        this.addedComponents = changeSet.addedComponents();
        this.deletedComponents = changeSet.deletedComponents();
        this.addedRelationships = changeSet.addedRelations();
        this.deletedRelationships = changeSet.deletedRelations();
        this.allRelationships = allRelations;
        this.colorScheme = colorScheme;
    }

    public String description() {
        return PLANT_UML_BEGIN_STRING + formPlantUMLSkinString(colorScheme) + new PUMLClassFieldsDesc(diagramComponents,
                deletedComponents, addedComponents, allComponents, colorScheme).value()
                + "\n"
                + new PUMLClassRelationsDesc(diagramComponents, allRelationships, deletedRelationships,
                addedRelationships, colorScheme).value()
                + PLANT_UML_END_STRING;
    }

    private String formPlantUMLSkinString(DiagramColorScheme colorScheme) {
        return "hide empty methods\nhide empty attributes\nscale max 3000 width\n skinparam defaultFontName " + colorScheme.defaultFontName() + "\n" + "skinparam backgroundColor  "
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
