package striff.test.model;

import com.hadi.clarpse.compiler.ClarpseProject;
import com.hadi.clarpse.compiler.CompileResult;
import com.hadi.clarpse.compiler.Lang;
import com.hadi.clarpse.compiler.ProjectFile;
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadi.striff.diagram.ComponentHelper;
import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.StriffDiagramModel;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.diagram.display.LightDiagramColorScheme;
import com.hadi.striff.diagram.plantuml.PUMLDiagram;
import com.hadi.striff.diagram.plantuml.PUMLDiagramData;
import com.hadi.striff.parse.CodeDiff;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A synthetic module is identified by its package as well as its name, so two files of the same
 * name in different directories are two modules rather than one.
 */
public class SyntheticModuleIdentityTest {

    private static final Pattern DATA_QUALIFIED_NAME = Pattern.compile("data-qualified-name=\"([^\"]+)\"");

    private static final String ORDERS_UPDATE = "src.orders.module:update";
    private static final String BILLING_UPDATE = "src.billing.module:update";

    @Test
    public void sameNamedFilesInDifferentPackagesAreSeparateModules() throws Exception {
        StriffDiagramModel diagramModel = diagramOfTwoUpdateFiles();
        Set<String> uniqueNames = uniqueNames(diagramModel);

        assertTrue("Expected both modules, got: " + uniqueNames, uniqueNames.contains(ORDERS_UPDATE));
        assertTrue("Expected both modules, got: " + uniqueNames, uniqueNames.contains(BILLING_UPDATE));
    }

    @Test
    public void eachModuleHoldsOnlyItsOwnMembers() throws Exception {
        StriffDiagramModel diagramModel = diagramOfTwoUpdateFiles();

        Set<String> ordersMembers = Set.copyOf(module(diagramModel, ORDERS_UPDATE).children());
        Set<String> billingMembers = Set.copyOf(module(diagramModel, BILLING_UPDATE).children());

        assertTrue("Orders module should hold its own function: " + ordersMembers,
                ordersMembers.stream().anyMatch(name -> name.contains("applyOrderUpdate")));
        assertTrue("Orders module should not hold the billing function: " + ordersMembers,
                ordersMembers.stream().noneMatch(name -> name.contains("applyBillingUpdate")));
        assertTrue("Billing module should hold its own function: " + billingMembers,
                billingMembers.stream().anyMatch(name -> name.contains("applyBillingUpdate")));
        assertTrue("Billing module should not hold the orders function: " + billingMembers,
                billingMembers.stream().noneMatch(name -> name.contains("applyOrderUpdate")));
    }

    @Test
    public void eachModuleSitsInItsOwnPackage() throws Exception {
        StriffDiagramModel diagramModel = diagramOfTwoUpdateFiles();

        assertEquals("src.orders", ComponentHelper.packagePath(module(diagramModel, ORDERS_UPDATE).pkg()));
        assertEquals("src.billing", ComponentHelper.packagePath(module(diagramModel, BILLING_UPDATE).pkg()));
    }

    /**
     * The unique name is qualified, but the label a reader sees stays the file's own name.
     */
    @Test
    public void theDisplayedNameStaysShort() throws Exception {
        StriffDiagramModel diagramModel = diagramOfTwoUpdateFiles();

        assertEquals("update", module(diagramModel, ORDERS_UPDATE).name());
        assertEquals("update", module(diagramModel, BILLING_UPDATE).name());
    }

    /**
     * Every component maps back from the rendered diagram by its unique name, and the two modules
     * stay distinct under the dot-to-hyphen spelling a consumer may also match on.
     */
    @Test
    public void uniqueNamesRoundTripThroughThePlantUmlPath() throws Exception {
        CodeDiff diff = diffOfTwoUpdateFiles();
        StriffDiagramModel diagramModel = new StriffDiagramModel(diff, Set.of());
        Set<DiagramComponent> diagramCmps = diagramModel.diagramCmps();

        DiagramDisplay display = new DiagramDisplay(new LightDiagramColorScheme(),
                diagramCmps.stream()
                        .map(cmp -> ComponentHelper.packagePath(cmp.pkg()))
                        .collect(Collectors.toSet()));
        PUMLDiagramData data = new PUMLDiagramData(
                diagramModel.diagramRels(),
                diff.changeSet().addedRelations(),
                diff.changeSet().deletedRelations(),
                display,
                diff.mergedModel(),
                diff.changeSet().addedComponents(),
                diff.changeSet().deletedComponents(),
                diff.changeSet().modifiedComponents(),
                diagramCmps);

        String svg = new PUMLDiagram(data).svgText();
        Set<String> svgNames = dataQualifiedNames(svg);

        assertTrue("Rendered diagram should carry " + ORDERS_UPDATE + ", got: " + svgNames,
                svgNames.contains(ORDERS_UPDATE));
        assertTrue("Rendered diagram should carry " + BILLING_UPDATE + ", got: " + svgNames,
                svgNames.contains(BILLING_UPDATE));
        assertTrue("Every component should map back from the diagram by its unique name. "
                        + "Components=" + uniqueNames(diagramModel) + ", diagram=" + svgNames,
                svgNames.containsAll(uniqueNames(diagramModel)));

        // A consumer matching ids with dots replaced by hyphens must not conflate the two.
        Set<String> hyphenated = uniqueNames(diagramModel).stream()
                .map(name -> name.replace(".", "-"))
                .collect(Collectors.toSet());
        assertEquals("Dot-to-hyphen spellings must stay distinct: " + hyphenated,
                uniqueNames(diagramModel).size(), hyphenated.size());
    }

    private static DiagramComponent module(StriffDiagramModel diagramModel, String uniqueName) {
        return diagramModel.diagramCmps().stream()
                .filter(cmp -> cmp.uniqueName().equals(uniqueName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No module " + uniqueName
                        + " in " + uniqueNames(diagramModel)));
    }

    private static Set<String> uniqueNames(StriffDiagramModel diagramModel) {
        return diagramModel.diagramCmps().stream()
                .map(DiagramComponent::uniqueName)
                .collect(Collectors.toSet());
    }

    private static Set<String> dataQualifiedNames(String svg) {
        Set<String> names = new HashSet<>();
        Matcher matcher = DATA_QUALIFIED_NAME.matcher(svg);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private static StriffDiagramModel diagramOfTwoUpdateFiles() throws Exception {
        return new StriffDiagramModel(diffOfTwoUpdateFiles(), Set.of());
    }

    private static CodeDiff diffOfTwoUpdateFiles() throws Exception {
        ProjectFiles newFiles = new ProjectFiles();
        newFiles.insertFile(new ProjectFile("/src/orders/update.py", "def applyOrderUpdate():\n    return 1\n"));
        newFiles.insertFile(new ProjectFile("/src/billing/update.py", "def applyBillingUpdate():\n    return 2\n"));

        return new CodeDiff(compileModel(new ProjectFiles()), compileModel(newFiles));
    }

    private static OOPSourceCodeModel compileModel(ProjectFiles files) throws Exception {
        CompileResult result = new ClarpseProject(files, Lang.PYTHON).result();
        Assert.assertTrue("Compile failures: " + result.failures(), result.failures().isEmpty());
        return result.model();
    }
}
