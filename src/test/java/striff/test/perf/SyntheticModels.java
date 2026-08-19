package striff.test.perf;

import com.hadi.clarpse.reference.ComponentReference;
import com.hadi.clarpse.reference.SimpleTypeReference;
import com.hadi.clarpse.reference.TypeExtensionReference;
import com.hadi.clarpse.sourcemodel.Component;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.clarpse.sourcemodel.OOPSourceModelConstants.ComponentType;
import com.hadi.clarpse.sourcemodel.Package;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds two source code models of a size and shape matching a real repository, deterministically
 * and without parsing anything.
 *
 * <p>Sized on a real repository whose comparison exhausted its memory budget: 452 files and 11,999
 * components per revision, so roughly 26 components per file. A synthetic model is used rather than
 * a checked-in repository because the merge is what is being measured, and a parse would dominate
 * both the wall clock and the allocation figure while adding run-to-run variance from the
 * parsers.
 *
 * <p>Every count here is fixed, so two runs build byte-identical models and an allocation figure is
 * comparable across builds.
 */
public final class SyntheticModels {

    /** Base components, one per source file, matching the reference repository's file count. */
    public static final int CLASSES = 452;
    /** Packages the classes are spread over. */
    public static final int PACKAGES = 60;
    /** Method children per class. */
    public static final int METHODS_PER_CLASS = 13;
    /** Field children per class. */
    public static final int FIELDS_PER_CLASS = 12;
    /** Outgoing references per class. */
    public static final int REFS_PER_CLASS = 8;
    /** Outgoing references per method. */
    public static final int REFS_PER_METHOD = 2;

    private SyntheticModels() {
    }

    /**
     * Builds the base revision.
     *
     * @return A model of {@value #CLASSES} classes and their members.
     */
    public static OOPSourceCodeModel baseRevision() {
        return build(-1, -1);
    }

    /**
     * Builds the head revision: three files edited, one class deleted, one class added. That is the
     * shape of an ordinary pull request, and it is deliberately small -- the cost being measured is
     * paid on the 449 files nothing touched.
     *
     * @return A model differing from {@link #baseRevision()} by a handful of components.
     */
    public static OOPSourceCodeModel headRevision() {
        OOPSourceCodeModel model = build(3, CLASSES - 1);
        Component added = classComponent(CLASSES, "Added");
        model.insertComponent(added);
        return model;
    }

    private static OOPSourceCodeModel build(final int editedUpTo, final int deletedClass) {
        OOPSourceCodeModel model = new OOPSourceCodeModel();
        for (int i = 0; i < CLASSES; i++) {
            if (i == deletedClass) {
                continue;
            }
            String suffix = i < editedUpTo ? "Edited" : "";
            Component cls = classComponent(i, suffix);
            List<Component> members = new ArrayList<>();
            for (int m = 0; m < METHODS_PER_CLASS; m++) {
                Component method = memberComponent(i, "method" + m + "(String, int)",
                        ComponentType.METHOD, suffix);
                for (int r = 0; r < REFS_PER_METHOD; r++) {
                    method.insertCmpRef(ref((i + r + 1) % CLASSES));
                }
                cls.insertChildComponent(method.uniqueName());
                members.add(method);
            }
            for (int f = 0; f < FIELDS_PER_CLASS; f++) {
                Component field = memberComponent(i, "field" + f, ComponentType.FIELD, suffix);
                cls.insertChildComponent(field.uniqueName());
                members.add(field);
            }
            for (int r = 0; r < REFS_PER_CLASS; r++) {
                cls.insertCmpRef(ref((i + r * 7 + 1) % CLASSES));
            }
            cls.insertCmpRef(new TypeExtensionReference(uniqueClassName((i + 11) % CLASSES)));
            model.insertComponent(cls);
            members.forEach(model::insertComponent);
        }
        return model;
    }

    private static ComponentReference ref(final int target) {
        return new SimpleTypeReference(uniqueClassName(target));
    }

    private static String packagePath(final int classIndex) {
        return "com/example/module" + (classIndex % PACKAGES);
    }

    private static String packageName(final int classIndex) {
        return "module" + (classIndex % PACKAGES);
    }

    private static String uniqueClassName(final int classIndex) {
        return packagePath(classIndex).replace('/', '.') + ".Class" + classIndex;
    }

    private static Component classComponent(final int i, final String bodySuffix) {
        Component cls = new Component();
        cls.setComponentType(ComponentType.CLASS);
        cls.setComponentName("Class" + i);
        cls.setName("Class" + i);
        cls.setPkg(new Package(packageName(i), packagePath(i)));
        cls.setSourceFilePath("/" + packagePath(i) + "/Class" + i + ".java");
        cls.setModule("core");
        cls.setComment("Documents Class" + i + ", one of a repository's many classes.");
        cls.setCodeFragment("Class" + i);
        cls.setCodeHash(("Class" + i + bodySuffix).hashCode());
        cls.insertAccessModifier("public");
        cls.setImports(imports(i));
        return cls;
    }

    private static Component memberComponent(final int classIndex, final String member,
                                             final ComponentType type, final String bodySuffix) {
        Component cmp = new Component();
        cmp.setComponentType(type);
        cmp.setComponentName("Class" + classIndex + "." + member);
        cmp.setName(member);
        cmp.setPkg(new Package(packageName(classIndex), packagePath(classIndex)));
        cmp.setSourceFilePath("/" + packagePath(classIndex) + "/Class" + classIndex + ".java");
        cmp.setModule("core");
        cmp.setComment("");
        cmp.setCodeFragment(type == ComponentType.FIELD ? "String" : "Map<String, List<String>>");
        cmp.setValue(type == ComponentType.FIELD ? "String" : "Map<String, List<String>>");
        cmp.setCodeHash((member + bodySuffix).hashCode());
        cmp.insertAccessModifier("private");
        cmp.setImports(imports(classIndex));
        return cmp;
    }

    private static Set<String> imports(final int classIndex) {
        Set<String> imports = new LinkedHashSet<>();
        imports.add("java.util.List");
        imports.add("java.util.Map");
        imports.add("java.util.Set");
        imports.add("java.util.Optional");
        for (int i = 0; i < 6; i++) {
            imports.add(uniqueClassName((classIndex + i * 5 + 1) % CLASSES));
        }
        return imports;
    }
}
