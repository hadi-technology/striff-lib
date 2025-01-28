package com.hadii.striff.diagram;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hadii.clarpse.reference.ComponentReference;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.clarpse.sourcemodel.Package;
import com.hadii.striff.metrics.MetricChange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DiagramComponent {

    private final Component cmp;
    private final List<String> children = new ArrayList<>();
    private MetricChange metricChange;

    /**
     * If serialization only is needed, this no-arg constructor
     * can be omitted. Required if deserialization is needed.
     */
    public DiagramComponent() {
        this.cmp = new Component();
    }

    public DiagramComponent(Component cmp, OOPSourceCodeModel srcModel) {
        if (cmp == null) {
            this.cmp = new Component();
        } else {
            this.cmp = cmp;
        }
        if (srcModel != null) {
            this.cmp.children().stream()
                    .filter(child -> srcModel.getComponent(child).isPresent())
                    .forEach(children::add);
        }
    }

    public DiagramComponent(String componentName) {
        this();
        this.cmp.setComponentName(componentName);
    }

    public DiagramComponent(String cmpName, MetricChange metricChange, OOPSourceCodeModel srcModel) {
        this(srcModel.getComponent(cmpName).orElse(new Component()), srcModel);
        this.metricChange = metricChange;
    }

    public DiagramComponent(String cmpName, OOPSourceCodeModel srcModel) {
        this(srcModel.getComponent(cmpName).orElse(new Component()), srcModel);
    }

    @JsonProperty("metricChange")
    public MetricChange getMetricChange() {
        return metricChange;
    }

    public boolean hasMetricChange() {
        return metricChange != null;
    }

    @JsonProperty("children")
    public List<String> children() {
        return Collections.unmodifiableList(this.children);
    }

    @JsonProperty("uniqueName")
    public String uniqueName() {
        return cmp.uniqueName();
    }

    public List<ComponentReference> references(OOPSourceModelConstants.TypeReferences implementation) {
        return cmp.references(implementation);
    }

    @JsonProperty("modifiers")
    public Set<String> modifiers() {
        return this.cmp.modifiers();
    }

    @JsonProperty("componentType")
    public OOPSourceModelConstants.ComponentType componentType() {
        return this.cmp.componentType();
    }

    @JsonIgnore
    public String parentUniqueName() {
        return this.cmp.parentUniqueName();
    }

    @JsonProperty("refs")
    private Set<String> refs() {
        return this.cmp.references().stream().map(ref -> ref.toString()).collect(Collectors.toSet());
    }

    public Set<ComponentReference> references() {
        return this.cmp.references();
    }

    @JsonProperty("name")
    public String name() {
        return this.cmp.name();
    }

    @JsonIgnore
    public String codeFragment() {
        return this.cmp.codeFragment();
    }

    @JsonIgnore
    public int componentHashCode() {
        return this.cmp.codeHash();
    }

    @JsonProperty("comment")
    public String comment() {
        return this.cmp.comment();
    }

    @JsonProperty("sourceFile")
    public String sourceFile() {
        return this.cmp.sourceFile();
    }

    @JsonIgnore
    public void setName(String name) {
        this.cmp.setName(name);
    }

    @JsonProperty("package")
    private String packageName() {
        return this.cmp.pkg().toString();
    }

    @JsonIgnore
    public Package pkg() {
        return this.cmp.pkg();
    }

    @JsonProperty("componentName")
    public String componentName() {
        return this.cmp.componentName();
    }

    @Override
    public int hashCode() {
        return this.uniqueName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DiagramComponent)) {
            return false;
        }
        DiagramComponent other = (DiagramComponent) obj;
        return Objects.equals(this.uniqueName(), other.uniqueName());
    }

    @Override
    public String toString() {
        return this.uniqueName();
    }
}
