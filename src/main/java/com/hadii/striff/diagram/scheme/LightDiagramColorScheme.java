package com.hadii.striff.diagram.scheme;

public class LightDiagramColorScheme implements DiagramColorScheme {

    @Override
    public String defaultFontName() {
        return "Consolas,Menlo,Liberation Mono";
    }

    @Override
    public String backgroundColor() {
        return "transparent";
    }

    @Override
    public String classArrowFontName() {
        return "Consolas,Menlo,Liberation Mono";
    }

    @Override
    public String classArrowColor() {
        return "#464646";
    }

    @Override
    public String objectColorBackground() {
        return "#f8f8ff";
    }

    @Override
    public String classArrowFontColor() {
        return classAttributeFontColor();
    }

    @Override
    public String classArrowFontSize() {
        return "18";
    }

    @Override
    public String classFontColor() {
        return objectColorBackground();
    }

    @Override
    public String classFontName() {
        return "Consolas,Menlo,Liberation Mono";
    }

    @Override
    public String zoomOutIconColor() {
        return "#93f3ff";
    }

    @Override
    public String classAttributeFontColor() {
        return "#24292e";
    }

    @Override
    public String classBorderThickness() {
        return "0.4";
    }

    @Override
    public String classAttributeFontName() {
        return "Consolas,Menlo,Liberation Mono";
    }

    @Override
    public String titleFontColor() {
        return "Black";
    }

    @Override
    public String packageBackgroundColor() {
        return "#E0E0E0";
    }

    @Override
    public String titleFontName() {
        return "Copperplate Gothic Light";
    }

    @Override
    public String classHeaderBackgroundColor() {
        return classAttributeFontColor();
    }

    @Override
    public String packageBorderColor() {
        return "#E0E0E0";
    }

    @Override
    public String packageBorderThickness() {
        return "0";
    }

    @Override
    public String dropShadows() {
        return "true";
    }

    @Override
    public String packageFontColor() {
        return classAttributeFontColor();
    }

    @Override
    public String arrowThickness() {
        return "1";
    }

    @Override
    public String packageFontName() {
        return "Consolas,Menlo,Liberation Mono";
    }

    @Override
    public String packageFontStyle() {
        return "plain";
    }

    @Override
    public String classBorderColor() {
        return classAttributeFontColor();
    }

    @Override
    public String classCircledCharacterFontColor() {
        return objectColorBackground();
    }

    @Override
    public String addedComponentColor() {
        return "#bef5cb";
    }

    @Override
    public String deletedComponentColor() {
        return "#fdaeb7";
    }

    @Override
    public String classCircledCharacterFontSize() {
        return "10";
    }

    @Override
    public String classCircledCharacterBackgroundColor() {
        return "#d4d4d4";
    }

    @Override
    public String structCircledCharacterBackgroundColor() {
        return "#d4d4d4";
    }

    @Override
    public String classStereoTypeFontColor() {
        return "#888888";
    }

    @Override
    public String modifiedComponentColor() {
        return "#bbe5f8";
    }

    @Override
    public String minClassWidth() {
        return "400";
    }

    @Override
    public String addedRelationColor() {
        return "#00cc00";
    }

    @Override
    public String deletedRelationColor() {
        return "#cc0000";
    }

    @Override
    public String legendBackgroundColor() {
        return objectColorBackground();
    }
}
