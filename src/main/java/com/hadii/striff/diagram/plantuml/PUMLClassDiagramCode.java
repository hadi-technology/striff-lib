package com.hadii.striff.diagram.plantuml;

import com.hadii.striff.diagram.display.DiagramColorScheme;
import com.hadii.striff.spi.DiagramDecorator;

import java.util.List;

final class PUMLClassDiagramCode {

    private static final String PLANT_UML_BEGIN_STRING = "@startuml\n";
    private static final String PLANT_UML_END_STRING = "\n@enduml";
    private final String code;

    PUMLClassDiagramCode(PUMLDiagramData data) {
        String diagramDecorations = diagramDecoratorsText(data);
        this.code = PLANT_UML_BEGIN_STRING
                + plantUMLStyleBlock(data.diagramDisplay().colorScheme())
                + plantUMLSkinParamText(data.diagramDisplay().colorScheme())
                + "\n" + diagramDecorations + new PUMLPackageCode(data).value()
                + "\n"
                + new PUMLClassRelationsCode(data).value()
                + PLANT_UML_END_STRING;
    }

    public String code() {
        return this.code;
    }

    private String plantUMLStyleBlock(DiagramColorScheme colorScheme) {
        return "<style>\n"
                + "classDiagram {\n"
                + "class {\n"
                + "      FontSize: " + colorScheme.classFontSize() + ";\n"
                + "header {\n"
                + "        FontSize: " + colorScheme.classFontSize() + ";\n"
                + "        FontColor: " + colorScheme.classFontColor() + ";\n"
                + "      }\n"
                + "}\n"

                + "spot{\n"                + "}\n"
                + "  spotClass {\n"
                + "    BackgroundColor: #dfdfdf;\n"
                + "    FontColor: " + colorScheme.classBorderColor() + ";\n"
                + "    LineColor: " + colorScheme.classBorderColor() + ";\n"
                + "    FontSize: 16;\n"
                + "    FontStyle: bold;\n"
                + "  }\n"
                + "  spotAbstractClass {\n"
                + "    FontColor: " + colorScheme.classBorderColor() + ";\n"
                + "    LineColor: " + colorScheme.classBorderColor() + ";\n"
                + "    FontSize: 16;\n"
                + "    FontStyle: bold;\n"
                + "  }\n"
                + "  spotInterface {\n"
                + "    FontColor: " + colorScheme.classBorderColor() + ";\n"
                + "    LineColor: " + colorScheme.classBorderColor() + ";\n"
                + "    FontSize: 16;\n"
                + "    FontStyle: bold;\n"
                + "  }\n"
                + "  spotStruct {\n"
                + "    FontColor: " + colorScheme.classBorderColor() + ";\n"
                + "    LineColor: " + colorScheme.classBorderColor() + ";\n"
                + "    FontSize: 16;\n"
                + "    FontStyle: bold;\n"
                + "  }\n"
                + "  spotEnum {\n"
                + "    FontColor: " + colorScheme.classBorderColor() + ";\n"
                + "    LineColor: " + colorScheme.classBorderColor() + ";\n"
                + "    FontSize: 16;\n"
                + "    FontStyle: bold;\n"
                + "  }\n"
                + "}\n"
                + "</style>\n";
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
                + "\nskinparam classBorderColor " + colorScheme.classBorderColor()
                + "\nskinparam classBorderThickness " + colorScheme.classBorderThickness()
                + "\nskinparam classFontName " + colorScheme.classFontName()
                + "\nskinparam classAttributeFontName " + colorScheme.classAttributeFontName()
                + "\nskinparam titleFontColor " + colorScheme.titleFontColor()
                + "\nskinparam packageBackgroundColor " + colorScheme.packageBackgroundColor()
                + "\nskinparam groupInheritance 2"
                + "\nskinparam titleFontName " + colorScheme.titleFontName()
                + "\nskinparam packageBorderColor " + colorScheme.packageBorderColor()
                + "\nskinparam packageFontColor " + colorScheme.packageFontColor()
                + "\nskinparam packageFontName " + colorScheme.packageFontName()
                + "\nskinparam packageFontStyle " + colorScheme.packageFontStyle();
    }

    private String diagramDecoratorsText(PUMLDiagramData data) {
        StringBuilder builder = new StringBuilder();
        for (DiagramDecorator decorator : data.diagramDecorators()) {
            List<String> extra = decorator.decorateDiagram(data.diagramDisplay());
            if (extra == null || extra.isEmpty()) {
                continue;
            }
            for (String line : extra) {
                builder.append(line);
                if (!line.endsWith("\n")) {
                    builder.append("\n");
                }
            }
        }
        return builder.toString();
    }
}
