package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.display.DiagramColorScheme;
import com.hadii.striff.parse.CodeDiff;

import java.util.Set;

final class PUMLClassDiagramCode {

    private static final String PLANT_UML_BEGIN_STRING = "@startuml\n";
    private static final String PLANT_UML_END_STRING = "\n@enduml";
    private final String code;


    PUMLClassDiagramCode(final CodeDiff codeDiff, final DiagramDisplay diagramDisplay,
                         final Set<DiagramComponent> diagramComponents) {
        this.code = PLANT_UML_BEGIN_STRING
                + plantUMLSkinParamText(diagramDisplay.colorScheme())
                + new PUMLPackageCode(diagramDisplay, codeDiff, diagramComponents).value()
                + "\n"
                + new PUMLClassRelationsCode(diagramComponents, codeDiff, diagramDisplay).value()
                + PLANT_UML_END_STRING;
    }

    public String code() {
        return this.code;
    }

    private String plantUMLSkinParamText(DiagramColorScheme colorScheme) {
        return "hide empty methods"
                + "\nhide empty attributes"
                + "\nskinparam defaultFontName " + colorScheme.defaultFontName()
                + "\nskinparam backgroundColor  " + colorScheme.backgroundColor()
                + "\nskinparam classArrowColor " + colorScheme.classArrowColor()
                + "\nskinparam legendBackgroundColor " + colorScheme.legendBackgroundColor()
                + "\nskinparam classBackgroundColor " + colorScheme.objectColorBackground()
                + "\nskinparam classArrowFontColor " + colorScheme.classArrowFontColor()
                + "\nskinparam classArrowFontSize " + colorScheme.classArrowFontSize()
                + "\nskinparam classFontColor " + colorScheme.classFontColor()
                + "\nskinparam classStereotypeFontColor " + colorScheme.classStereoTypeFontColor()
                + "\nskinparam CircledCharacterFontColor " + colorScheme.classCircledCharacterFontColor()
                + "\nskinparam CircledCharacterFontSize " + colorScheme.classCircledCharacterFontSize()
                + "\nskinparam classAttributeFontColor " + colorScheme.classAttributeFontColor()
                + "\nskinparam classFontName " + colorScheme.classFontName()
                + "\nskinparam classAttributeFontName " + colorScheme.classAttributeFontName()
                + "\nskinparam titleFontColor " + colorScheme.titleFontColor()
                + "\nskinparam packageBackgroundColor " + colorScheme.packageBackgroundColor()
                + "\nskinparam groupInheritance 2"
               // + "\nskinparam linetype polyline"
                + "\nskinparam titleFontName " + colorScheme.titleFontName()
                + "\nskinparam packageBorderColor " + colorScheme.packageBorderColor()
                + "\nskinparam packageFontColor " + colorScheme.packageFontColor()
                + "\nskinparam packageFontName " + colorScheme.packageFontName()
                + "\nskinparam packageFontStyle " + colorScheme.packageFontStyle()
                + "\nskinparam classBorderThickness " + colorScheme.classBorderThickness()
                + "\nskinparam classHeaderBackgroundColor " + colorScheme.classHeaderBackgroundColor()
                + "\nskinparam classBorderColor " + colorScheme.classBorderColor()
                + "\nskinparam minClassWidth " + colorScheme.minClassWidth() + "\n";
    }
}
