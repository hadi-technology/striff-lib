package com.hadi.striff.diagram.display;

public final class DiagramColorSchemeOverride implements DiagramColorScheme {

    private final DiagramColorScheme base;

    private String defaultFontName;
    private String backgroundColor;
    private String defaultClassHeaderColor;
    private String classArrowFontName;
    private String classArrowColor;
    private String objectColorBackground;
    private String classFontSize;
    private String classArrowFontColor;
    private String classArrowFontSize;
    private String legendBackgroundColor;
    private String modifiedComponentColor;
    private String minClassWidth;
    private String classFontColor;
    private String classFontName;
    private String zoomOutIconColor;
    private String classBorderThickness;
    private String classAttributeFontName;
    private String titleFontColor;
    private String packageBackgroundColor;
    private String titleFontName;
    private String classHeaderBackgroundColor;
    private String packageBorderColor;
    private String packageBorderThickness;
    private String dropShadows;
    private String packageFontColor;
    private String arrowThickness;
    private String packageFontName;
    private String packageFontStyle;
    private String classBorderColor;
    private String addedComponentColor;
    private String addedRelationColor;
    private String deletedRelationColor;
    private String deletedComponentColor;

    private DiagramColorSchemeOverride(DiagramColorScheme base) {
        if (base == null) {
            throw new IllegalArgumentException("Base color scheme is required.");
        }
        this.base = base;
    }

    public static DiagramColorSchemeOverride from(DiagramColorScheme base) {
        return new DiagramColorSchemeOverride(base);
    }

    public DiagramColorSchemeOverride setDefaultFontName(String defaultFontName) {
        this.defaultFontName = defaultFontName;
        return this;
    }

    public DiagramColorSchemeOverride setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public DiagramColorSchemeOverride setDefaultClassHeaderColor(String defaultClassHeaderColor) {
        this.defaultClassHeaderColor = defaultClassHeaderColor;
        return this;
    }

    public DiagramColorSchemeOverride setClassArrowFontName(String classArrowFontName) {
        this.classArrowFontName = classArrowFontName;
        return this;
    }

    public DiagramColorSchemeOverride setClassArrowColor(String classArrowColor) {
        this.classArrowColor = classArrowColor;
        return this;
    }

    public DiagramColorSchemeOverride setObjectColorBackground(String objectColorBackground) {
        this.objectColorBackground = objectColorBackground;
        return this;
    }

    public DiagramColorSchemeOverride setClassFontSize(String classFontSize) {
        this.classFontSize = classFontSize;
        return this;
    }

    public DiagramColorSchemeOverride setClassArrowFontColor(String classArrowFontColor) {
        this.classArrowFontColor = classArrowFontColor;
        return this;
    }

    public DiagramColorSchemeOverride setClassArrowFontSize(String classArrowFontSize) {
        this.classArrowFontSize = classArrowFontSize;
        return this;
    }

    public DiagramColorSchemeOverride setLegendBackgroundColor(String legendBackgroundColor) {
        this.legendBackgroundColor = legendBackgroundColor;
        return this;
    }

    public DiagramColorSchemeOverride setModifiedComponentColor(String modifiedComponentColor) {
        this.modifiedComponentColor = modifiedComponentColor;
        return this;
    }

    public DiagramColorSchemeOverride setMinClassWidth(String minClassWidth) {
        this.minClassWidth = minClassWidth;
        return this;
    }

    public DiagramColorSchemeOverride setClassFontColor(String classFontColor) {
        this.classFontColor = classFontColor;
        return this;
    }

    public DiagramColorSchemeOverride setClassFontName(String classFontName) {
        this.classFontName = classFontName;
        return this;
    }

    public DiagramColorSchemeOverride setZoomOutIconColor(String zoomOutIconColor) {
        this.zoomOutIconColor = zoomOutIconColor;
        return this;
    }

    public DiagramColorSchemeOverride setClassBorderThickness(String classBorderThickness) {
        this.classBorderThickness = classBorderThickness;
        return this;
    }

    public DiagramColorSchemeOverride setClassAttributeFontName(String classAttributeFontName) {
        this.classAttributeFontName = classAttributeFontName;
        return this;
    }

    public DiagramColorSchemeOverride setTitleFontColor(String titleFontColor) {
        this.titleFontColor = titleFontColor;
        return this;
    }

    public DiagramColorSchemeOverride setPackageBackgroundColor(String packageBackgroundColor) {
        this.packageBackgroundColor = packageBackgroundColor;
        return this;
    }

    public DiagramColorSchemeOverride setTitleFontName(String titleFontName) {
        this.titleFontName = titleFontName;
        return this;
    }

    public DiagramColorSchemeOverride setClassHeaderBackgroundColor(String classHeaderBackgroundColor) {
        this.classHeaderBackgroundColor = classHeaderBackgroundColor;
        return this;
    }

    public DiagramColorSchemeOverride setPackageBorderColor(String packageBorderColor) {
        this.packageBorderColor = packageBorderColor;
        return this;
    }

    public DiagramColorSchemeOverride setPackageBorderThickness(String packageBorderThickness) {
        this.packageBorderThickness = packageBorderThickness;
        return this;
    }

    public DiagramColorSchemeOverride setDropShadows(String dropShadows) {
        this.dropShadows = dropShadows;
        return this;
    }

    public DiagramColorSchemeOverride setPackageFontColor(String packageFontColor) {
        this.packageFontColor = packageFontColor;
        return this;
    }

    public DiagramColorSchemeOverride setArrowThickness(String arrowThickness) {
        this.arrowThickness = arrowThickness;
        return this;
    }

    public DiagramColorSchemeOverride setPackageFontName(String packageFontName) {
        this.packageFontName = packageFontName;
        return this;
    }

    public DiagramColorSchemeOverride setPackageFontStyle(String packageFontStyle) {
        this.packageFontStyle = packageFontStyle;
        return this;
    }

    public DiagramColorSchemeOverride setClassBorderColor(String classBorderColor) {
        this.classBorderColor = classBorderColor;
        return this;
    }

    public DiagramColorSchemeOverride setAddedComponentColor(String addedComponentColor) {
        this.addedComponentColor = addedComponentColor;
        return this;
    }

    public DiagramColorSchemeOverride setAddedRelationColor(String addedRelationColor) {
        this.addedRelationColor = addedRelationColor;
        return this;
    }

    public DiagramColorSchemeOverride setDeletedRelationColor(String deletedRelationColor) {
        this.deletedRelationColor = deletedRelationColor;
        return this;
    }

    public DiagramColorSchemeOverride setDeletedComponentColor(String deletedComponentColor) {
        this.deletedComponentColor = deletedComponentColor;
        return this;
    }

    @Override
    public String defaultFontName() {
        return pick(defaultFontName, base.defaultFontName());
    }

    @Override
    public String backgroundColor() {
        return pick(backgroundColor, base.backgroundColor());
    }

    @Override
    public String defaultClassHeaderColor() {
        return pick(defaultClassHeaderColor, base.defaultClassHeaderColor());
    }

    @Override
    public String classArrowFontName() {
        return pick(classArrowFontName, base.classArrowFontName());
    }

    @Override
    public String classArrowColor() {
        return pick(classArrowColor, base.classArrowColor());
    }

    @Override
    public String objectColorBackground() {
        return pick(objectColorBackground, base.objectColorBackground());
    }

    @Override
    public String classFontSize() {
        return pick(classFontSize, base.classFontSize());
    }

    @Override
    public String classArrowFontColor() {
        return pick(classArrowFontColor, base.classArrowFontColor());
    }

    @Override
    public String classArrowFontSize() {
        return pick(classArrowFontSize, base.classArrowFontSize());
    }

    @Override
    public String legendBackgroundColor() {
        return pick(legendBackgroundColor, base.legendBackgroundColor());
    }

    @Override
    public String modifiedComponentColor() {
        return pick(modifiedComponentColor, base.modifiedComponentColor());
    }

    @Override
    public String minClassWidth() {
        return pick(minClassWidth, base.minClassWidth());
    }

    @Override
    public String classFontColor() {
        return pick(classFontColor, base.classFontColor());
    }

    @Override
    public String classFontName() {
        return pick(classFontName, base.classFontName());
    }

    @Override
    public String zoomOutIconColor() {
        return pick(zoomOutIconColor, base.zoomOutIconColor());
    }

    @Override
    public String classBorderThickness() {
        return pick(classBorderThickness, base.classBorderThickness());
    }

    @Override
    public String classAttributeFontName() {
        return pick(classAttributeFontName, base.classAttributeFontName());
    }

    @Override
    public String titleFontColor() {
        return pick(titleFontColor, base.titleFontColor());
    }

    @Override
    public String packageBackgroundColor() {
        return pick(packageBackgroundColor, base.packageBackgroundColor());
    }

    @Override
    public String titleFontName() {
        return pick(titleFontName, base.titleFontName());
    }

    @Override
    public String classHeaderBackgroundColor() {
        return pick(classHeaderBackgroundColor, base.classHeaderBackgroundColor());
    }

    @Override
    public String packageBorderColor() {
        return pick(packageBorderColor, base.packageBorderColor());
    }

    @Override
    public String packageBorderThickness() {
        return pick(packageBorderThickness, base.packageBorderThickness());
    }

    @Override
    public String dropShadows() {
        return pick(dropShadows, base.dropShadows());
    }

    @Override
    public String packageFontColor() {
        return pick(packageFontColor, base.packageFontColor());
    }

    @Override
    public String arrowThickness() {
        return pick(arrowThickness, base.arrowThickness());
    }

    @Override
    public String packageFontName() {
        return pick(packageFontName, base.packageFontName());
    }

    @Override
    public String packageFontStyle() {
        return pick(packageFontStyle, base.packageFontStyle());
    }

    @Override
    public String classBorderColor() {
        return pick(classBorderColor, base.classBorderColor());
    }

    @Override
    public String addedComponentColor() {
        return pick(addedComponentColor, base.addedComponentColor());
    }

    @Override
    public String addedRelationColor() {
        return pick(addedRelationColor, base.addedRelationColor());
    }

    @Override
    public String deletedRelationColor() {
        return pick(deletedRelationColor, base.deletedRelationColor());
    }

    @Override
    public String deletedComponentColor() {
        return pick(deletedComponentColor, base.deletedComponentColor());
    }

    private static String pick(String overrideValue, String baseValue) {
        return overrideValue != null ? overrideValue : baseValue;
    }
}
