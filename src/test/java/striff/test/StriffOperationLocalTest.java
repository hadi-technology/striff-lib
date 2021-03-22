package striff.test;

import com.hadii.clarpse.compiler.ClarpseProject;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFile;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.clarpse.sourcemodel.Component;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.clarpse.sourcemodel.OOPSourceModelConstants;
import com.hadii.striff.NoStructuralChangesException;
import com.hadii.striff.StriffDiagram;
import com.hadii.striff.StriffOperation;
import com.hadii.striff.diagram.DiagramCodeModel;
import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.scheme.LightDiagramColorScheme;
import com.hadii.striff.parse.DiffCodeModel;
import com.hadii.striff.parse.ParsedProject;
import net.sourceforge.plantuml.core.Diagram;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class StriffOperationLocalTest {

    @Test
    public void manualTest() throws Exception {
        String baseRepoOwner = "arduino";
        String repoName = "Arduino";
        String prNumber = "10204";
        String token = "73789b8b05af63609db4239b36a8791c94fce850";
        Lang language = Lang.JAVA;

        List<String> changedFiles = pullRequestChangedFiles(baseRepoOwner, repoName, token, prNumber);

        DiagramCodeModel oldModel = getGitHubRepoModel(baseRepoOwner, repoName, "master", token,
                                                       language);
        DiagramCodeModel newModel = getGitHubRepoModel(
                "ricardojlrufino", repoName, "pr-format-selection", token, language);
        List<StriffDiagram> striffs = new StriffOperation(new DiffCodeModel(
                oldModel, newModel), 10, changedFiles).result();
        for (int i = 0; i < striffs.size(); i ++) {
            File diagramFile = new File("/home/muntazir/Desktop/sdTest_ " + String.valueOf(i) +
                                            ".svg");
            PrintWriter writer = new PrintWriter(diagramFile);
            writer.println(striffs.get(i).svg());
            writer.close();
        }
    }

    public static DiagramCodeModel getGitHubRepoModel(String repoOwner, String repoName, String ref, String token, Lang lang)
            throws Exception {
        String url = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/zipball/"
            + URLEncoder.encode(ref.trim(), "UTF-8") + "?access_token=" + token;
        final URL repoUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) repoUrl.openConnection();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(new BufferedInputStream(conn.getInputStream(), 1024), baos);
        final ProjectFiles sourceFiles =
                extractProjectFromArchive(new ByteArrayInputStream(baos.toByteArray()),
                                                                  null, lang);
        OOPSourceCodeModel model = new ParsedProject(sourceFiles).model();
        return new DiagramCodeModel(model);
    }

    public static List<String> pullRequestChangedFiles(String repoOwner, String repoName, String token, String prNumber) throws IOException, IOException {

        List<String> changedFiles = new ArrayList<>();
        String url = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/pulls/" + prNumber + "/files?access_token=" + token;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        System.out.println("Response Code : "
                                   + response.getStatusLine().getStatusCode());
        JSONArray json = new JSONArray(EntityUtils.toString(response.getEntity()));
        for (int i = 0; i < json.length(); i++) {
            changedFiles.add("/" + json.getJSONObject(i).getString("filename"));
        }
        return changedFiles;
    }

    public static ProjectFiles extractProjectFromArchive(final InputStream is, String project, Lang language)
            throws Exception {
        final ProjectFiles projectFiles = new ProjectFiles(language);
        boolean currentlyExtractingProject = false;
        boolean finishedExtracting = false;
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(is);
            ZipEntry entry = zis.getNextEntry();
            // iterates over entries in the zip file
            while ((entry != null) && !finishedExtracting) {
                entry.getName().substring(entry.getName().lastIndexOf(".") + 1, entry.getName().length());
                if (!entry.isDirectory() && (currentlyExtractingProject)
                        && checkIfFileHasExtension(entry.getName(), language.fileExtensions())) {
                    projectFiles.insertFile(new ProjectFile(entry.getName().replace(" ", "_")
                                                                 .substring(entry.getName().indexOf("/") + 1),
                                                            new String(org.apache.commons.io.IOUtils.toByteArray(zis), StandardCharsets.UTF_8)));
                } else {
                    // if the project name is specified then keep extracting all
                    // the files in the project
                    if (((project != null) && !project.isEmpty()) && entry.getName().contains(project)) {
                        currentlyExtractingProject = true;
                    }
                    // if the project name is not specified, then extract
                    // everything
                    else if ((project == null) || (project.isEmpty())) {
                        currentlyExtractingProject = true;
                    }
                    // if the project name is specified then stop extracting
                    // once the project has been extracted
                    else if ((project != null) && (!project.isEmpty()) && !entry.getName().contains(project)
                            && (projectFiles.files().size() > 0)) {
                        currentlyExtractingProject = false;
                        finishedExtracting = true;
                    }
                }

                zis.closeEntry();
                entry = zis.getNextEntry();
            }

            // ensure we actually found some valid source files!
            if ((projectFiles.files().size() < 1)) {
                System.out.println("No " + language.value() + " source files were found in the uploaded zip project!");
            }
        } catch (final Exception e) {
            throw new Exception("Error while  reading " + language.value() + " source files from zip!", e);
        } finally {
            if (zis != null) {
                zis.close();
            }
            if (is != null) {
                is.close();
            }
        }
        return projectFiles;
    }

    public static boolean checkIfFileHasExtension(String s, String[] extn) {
        return Arrays.stream(extn).anyMatch(entry -> s.endsWith(entry));
    }
}
