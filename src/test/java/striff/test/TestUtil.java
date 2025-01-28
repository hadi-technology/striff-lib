package striff.test;

import com.hadii.clarpse.compiler.ClarpseProject;
import com.hadii.clarpse.compiler.Lang;
import com.hadii.clarpse.compiler.ProjectFiles;
import com.hadii.clarpse.sourcemodel.OOPSourceCodeModel;
import com.hadii.striff.diagram.StriffDiagram;

import org.apache.commons.io.IOUtils;
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
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TestUtil {

    public static boolean checkIfFileHasExtension(String s, String[] extn) {
        return Arrays.stream(extn).anyMatch(s::endsWith);
    }

    public static OOPSourceCodeModel sourceCodeModel(String testResourceZip, Lang language) throws Exception {
        final ProjectFiles pfs = new ProjectFiles(
            Objects.requireNonNull(Test.class.getResourceAsStream(testResourceZip)));
        return new ClarpseProject(pfs, language).result().model();
    }

    public static ProjectFiles githubProjectFiles(String repoOwner, String repoName,
                                                  String ref, Lang lang)
        throws Exception {
        String url = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/zipball/"
            + URLEncoder.encode(ref.trim(), StandardCharsets.UTF_8);
        final URL repoUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) repoUrl.openConnection();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(new BufferedInputStream(conn.getInputStream(), 1024), baos);
        ProjectFiles files = new ProjectFiles(new ByteArrayInputStream(baos.toByteArray()));
        files.shiftSubDirsLeft();
        return files;
    }

    public static List<String> pullRequestChangedFiles(String repoOwner, String repoName,
                                                       String prNumber) throws IOException {
        List<String> changedFiles = new ArrayList<>();
        String url = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/pulls/" + prNumber + "/files";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
        JSONArray json = new JSONArray(EntityUtils.toString(response.getEntity()));
        for (int i = 0; i < json.length(); i++) {
            changedFiles.add("/" + json.getJSONObject(i).getString("filename"));
        }
        return changedFiles;
    }

    public static void writeStriffsToDisk(List<StriffDiagram> striffs, String folderName) throws IOException {
        String striffDirectory = System.getProperty("java.io.tmpdir") + "/" + folderName;
        Files.createDirectories(Paths.get(striffDirectory));
        for (int i = 0; i < striffs.size(); i ++) {
            File diagramFile =
                new File(striffDirectory + "/" + i + ".svg");
            PrintWriter writer = new PrintWriter(diagramFile);
            writer.println(striffs.get(i).svg());
            writer.close();
            System.out.println("Wrote striff to " + diagramFile.getPath() + ".");
        }
    }
}
