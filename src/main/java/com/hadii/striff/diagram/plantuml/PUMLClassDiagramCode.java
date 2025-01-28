package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.diagram.display.DiagramColorScheme;

final class PUMLClassDiagramCode {

    private static final String PLANT_UML_BEGIN_STRING = "@startuml\n";
    private static final String PLANT_UML_END_STRING = "\n@enduml";
    private final String code;

    PUMLClassDiagramCode(PUMLDiagramData data) {
        this.code = PLANT_UML_BEGIN_STRING
                + plantUMLSkinParamText(data.diagramDisplay().colorScheme())
                + "\n" + new PUMLPackageCode(data).value()
                + "\n"
                + new PUMLClassRelationsCode(data).value()
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
                + "\nskinparam classFontSize " + colorScheme.classFontSize()
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
                + "\nskinparam packageFontStyle " + colorScheme.packageFontStyle();
    }
}
