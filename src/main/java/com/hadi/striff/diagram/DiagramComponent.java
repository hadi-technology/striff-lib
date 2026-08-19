package com.hadi.striff.diagram;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hadi.clarpse.reference.ComponentReference;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadi.clarpse.sourcemodel.Package;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DiagramComponent {

    private final Component cmp;
    private final List<String> children = new ArrayList<>();
    private final Map<String, Object> augmentations = new HashMap<>();

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
            // A presence test, so it asks the model whether it holds the child rather than
            // deep-copying one in order to throw the copy away. Runs once per child of every
            // component a diagram is built from.
            this.cmp.children().stream()
                    .filter(srcModel::containsComponent)
                    .forEach(children::add);
        }
    }

    public DiagramComponent(String componentName) {
        this();
        this.cmp.setComponentName(componentName);
    }

    /**
     * Keeps the copy on purpose. A DiagramComponent retains the component it is handed for its whole
     * life and exposes {@link #setName(String)} over it, so the model's own instance here would give
     * every caller of that setter the power to rename a component in the parsed model.
     */
    public DiagramComponent(String cmpName, OOPSourceCodeModel srcModel) {
        this(srcModel.copyOfComponent(cmpName).orElse(new Component()), srcModel);
    }

    @JsonIgnore
    public void putAugmentation(String key, Object value) {
        augmentations.put(key, value);
    }

    @JsonIgnore
    public Optional<Object> augmentation(String key) {
        return Optional.ofNullable(augmentations.get(key));
    }

    /**
     * Returns an unmodifiable list of child component names.
     * If the internal list is empty but the underlying component has children,
     * those are wrapped in an unmodifiable list as well.
     *
     * @return unmodifiable list of child component names
     */
    public List<String> children() {
        if (this.children.isEmpty() && !this.cmp.children().isEmpty()) {
            // If the internal list is empty but the underlying component has children,
            // return an unmodifiable view of the component's children
            return Collections.unmodifiableList(new ArrayList<>(this.cmp.children()));
        }
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
        if (this.cmp.pkg() == null) {
            return "";
        }
        // Use name() instead of toString() to avoid the "name: path" format
        // Convert package path separators from "/" to "." for serialization
        return this.cmp.pkg().name().replace("/", ".");
    }

    @JsonIgnore
    public Package pkg() {
        return this.cmp.pkg();
    }

    @JsonProperty("componentName")
    public String componentName() {
        return this.cmp.componentName();
    }

    @JsonIgnore
    public String module() {
        return this.cmp.module();
    }

    @JsonProperty("cyclo")
    public int cyclo() {
        return this.cmp.cyclo();
    }

    @JsonIgnore
    public Set<ComponentReference> internalDependencies() {
        return this.cmp.internalDependencies();
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
