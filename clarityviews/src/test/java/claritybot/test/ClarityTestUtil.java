package claritybot.test;

import com.clarity.binary.parse.ParsedProject;
import com.clarity.compiler.Lang;
import com.clarity.compiler.RawFile;
import com.clarity.compiler.SourceFiles;
import com.clarity.sourcemodel.OOPSourceCodeModel;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClarityTestUtil {

    public static OOPSourceCodeModel getGitHubRepoModel(String repoOwner, String repoName, String ref, String token, Lang lang)
            throws Exception {

        String url = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/zipball/" + URLEncoder.encode(ref.trim(), "UTF-8") + "?access_token=" + token;
        final URL repoUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) repoUrl.openConnection();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(new BufferedInputStream(conn.getInputStream(), 1024), baos);
        final SourceFiles sourceFiles = extractProjectFromArchive(new ByteArrayInputStream(baos.toByteArray()),
                null, lang);
        OOPSourceCodeModel model = new ParsedProject(sourceFiles).model();
        return model;
    }

    private static List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();

        try (InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }

        return filenames;
    }

    public static SourceFiles extractProjectFromArchive(final InputStream is, String project, Lang language)
            throws Exception {

        final SourceFiles sourceFiles = new SourceFiles(language);
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
                        && entry.getName().endsWith(language.fileExt())) {
                    sourceFiles.insertFile(new RawFile(entry.getName().replace(" ", "_"),
                            new String(IOUtils.toByteArray(zis), StandardCharsets.UTF_8)));
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
                            && (sourceFiles.getFiles().size() > 0)) {
                        currentlyExtractingProject = false;
                        finishedExtracting = true;
                    }
                }

                zis.closeEntry();
                entry = zis.getNextEntry();
            }

            // ensure we actually found some valid source files!
            if ((sourceFiles.getFiles().size() < 1)) {
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
        return sourceFiles;
    }

    private static InputStream getResourceAsStream(String resource) {
        final InputStream in = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? ClarityTestUtil.class.getResourceAsStream(resource) : in;
    }

    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static SourceFiles SourceFilesObjFromResourceDir(String dir) throws IOException {

        SourceFiles req = new SourceFiles(Lang.JAVA);
        List<String> fileNames = getResourceFiles(dir);
        for (String s : fileNames) {
            req.insertFile(new RawFile(s.replace(".txt", ".java"),
                    IOUtils.toString(ClarityTestUtil.class.getResourceAsStream(dir + s), "UTF-8")));
        }
        return req;
    }
}
