package org.codehaus.mojo.aspectj;

/**
 * The MIT License
 *
 * Copyright 2005-2006 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests class {@link org.codehaus.mojo.aspectj.AjcHelper}
 *
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
class AjcHelperTest {
    @TempDir
    File tempDir;

    @Test
    void getAsCsv() {
        String[] tests = new String[] {"kaare", "java", "aspectJ"};
        assertEquals("kaare,java,aspectJ", AjcHelper.getAsCsv(tests));
    }

    /**
     * @throws Exception on test error
     */
    @Test
    void getSourcesEmptyBaseDir() throws Exception {
        List<String> baseDirs = new ArrayList<>();
        baseDirs.add("src/shouldNotExist");
        Set<String> sources = AjcHelper.getBuildFilesForSourceDirs(
                baseDirs, new String[] {AjcHelper.DEFAULT_INCLUDES}, new String[] {AjcHelper.DEFAULT_EXCLUDES});
        assertTrue(sources.isEmpty());
    }

    private File sourceTree() throws Exception {
        write("src/main/java/foo/Azpect.java");
        write("src/main/java/foo/Clazz.java");
        write("src/main/aspect/foo/OldStyleAspect.aj");
        return tempDir;
    }

    private void write(String path) throws Exception {
        File file = new File(tempDir, path);
        Files.createDirectories(file.getParentFile().toPath());
        Files.write(file.toPath(), new byte[0]);
    }

    private List<String> sourceDirs() {
        return Arrays.asList(
                new File(tempDir, "src/main/java").getPath(), new File(tempDir, "src/main/aspect").getPath());
    }

    private Set<String> names(Set<String> files) {
        Set<String> names = new TreeSet<>();
        for (String file : files) {
            names.add(new File(file).getName());
        }
        return names;
    }

    @Test
    void antStyleIncludeSelectsOnlyTheMatchingAspect() throws Exception {
        sourceTree();

        Set<String> sources =
                AjcHelper.getBuildFilesForSourceDirs(sourceDirs(), new String[] {"**/Old*eAspect.aj"}, null);

        assertEquals(Collections.singleton("OldStyleAspect.aj"), names(sources));
    }

    @Test
    void fullPathIncludeSelectsOnlyThatFile() throws Exception {
        sourceTree();

        Set<String> sources =
                AjcHelper.getBuildFilesForSourceDirs(sourceDirs(), new String[] {"foo/OldStyleAspect.aj"}, null);

        assertEquals(Collections.singleton("OldStyleAspect.aj"), names(sources));
    }

    @Test
    void antStyleExcludeDropsTheMatchingFiles() throws Exception {
        sourceTree();

        Set<String> sources = AjcHelper.getBuildFilesForSourceDirs(sourceDirs(), null, new String[] {"**/Az*.*"});

        assertEquals(new TreeSet<>(Arrays.asList("Clazz.java", "OldStyleAspect.aj")), names(sources));
    }

    @Test
    void fullPathExcludeDropsOnlyThatFile() throws Exception {
        sourceTree();

        Set<String> sources =
                AjcHelper.getBuildFilesForSourceDirs(sourceDirs(), null, new String[] {"foo/Azpect.java"});

        assertEquals(new TreeSet<>(Arrays.asList("Clazz.java", "OldStyleAspect.aj")), names(sources));
    }

    @Test
    void missingAspectDirectoryStillYieldsTheJavaSources() throws Exception {
        sourceTree();
        List<String> dirs = Arrays.asList(
                new File(tempDir, "src/main/java").getPath(), new File(tempDir, "src/no/aspects").getPath());

        Set<String> sources = AjcHelper.getBuildFilesForSourceDirs(dirs, null, null);

        assertEquals(new TreeSet<>(Arrays.asList("Azpect.java", "Clazz.java")), names(sources));
    }

    @Test
    void ajdtBuildFileListsAFileAndADirectory() throws Exception {
        write("src/main/aspect/org/codehaus/mojo/aspectj/OldStyleAspect.aj");
        write("src/main/java/Clazz.java");
        write("src/main/aspect/Other.aj");
        Files.write(
                new File(tempDir, "build.ajproperties").toPath(),
                ("src.includes = src/main/aspect/org/codehaus/mojo/aspectj/OldStyleAspect.aj,\\\n"
                                + "               src/main/java/\n")
                        .getBytes(StandardCharsets.ISO_8859_1));

        Set<String> sources = AjcHelper.getBuildFilesForAjdtFile("build.ajproperties", tempDir);

        assertEquals(new TreeSet<>(Arrays.asList("Clazz.java", "OldStyleAspect.aj")), names(sources));
    }

    @Test
    void ajdtBuildFileExcludesWinOverIncludes() throws Exception {
        write("src/main/java/Clazz.java");
        write("src/main/java/Azpect.java");
        Files.write(
                new File(tempDir, "build.ajproperties").toPath(),
                ("src.includes = src/main/java/\nsrc.excludes = src/main/java/Azpect.java\n")
                        .getBytes(StandardCharsets.ISO_8859_1));

        Set<String> sources = AjcHelper.getBuildFilesForAjdtFile("build.ajproperties", tempDir);

        assertEquals(Collections.singleton("Clazz.java"), names(sources));
    }

    @Test
    void ajdtBuildFileDirectoryIncludesTakeBothSourceKinds() throws Exception {
        write("src/main/aspect/OldStyleAspect.aj");
        write("src/main/java/Clazz.java");
        Files.write(
                new File(tempDir, "build.ajproperties").toPath(),
                ("src.includes = src/main/aspect/,\\\n               src/main/java/\n")
                        .getBytes(StandardCharsets.ISO_8859_1));

        Set<String> sources = AjcHelper.getBuildFilesForAjdtFile("build.ajproperties", tempDir);

        assertEquals(new TreeSet<>(Arrays.asList("Clazz.java", "OldStyleAspect.aj")), names(sources));
    }

    @Test
    void buildConfigFile() throws Exception {
        final String fileName = "test.lst";

        List<String> args = new ArrayList<>();
        args.add("-classpath");
        args.add("a:b:c");
        args.add("-showWeaveInfo");
        args.add("/home/aspectj/AFile");
        args.add("/home/aspectj/AnotherFile");

        AjcHelper.writeBuildConfigToFile(args, fileName, tempDir);
        assertTrue(new File(tempDir, fileName).exists(), "Config file not written to disk");
        List<String> readArgs = AjcHelper.readBuildConfigFile(fileName, tempDir);
        assertEquals(args, readArgs);
    }

    @Test
    void emptyDependencyArtifacts() {
        MavenProject project = new MavenProject();
        AjcHelper.createClassPath(project, Collections.emptyList(), Collections.emptyList());
    }

    @Test
    void projectDependenciesBeforeOthers() {
        DefaultArtifact projectArtifact = createFixedArtifact("project");
        projectArtifact.setFile(new File("../sibling-project/target/some.jar"));

        DefaultArtifact dependencyArtifact = createFixedArtifact("dependency");
        dependencyArtifact.setFile(new File("~/.m2/repository/coords/some.jar"));

        DefaultArtifact pluginArtifact = createFixedArtifact("plugin");
        pluginArtifact.setFile(new File("~/.m2/repository/coords/plugin.jar"));

        projectArtifact.setDependencyTrail(Collections.singletonList("group:project:1.0-SNAPSHOT"));
        dependencyArtifact.setDependencyTrail(
                Arrays.asList("group:project:1.0-SNAPSHOT", "group:dependency:1.0-SNAPSHOT"));

        MavenProject project = new MavenProject();
        project.setArtifacts(new LinkedHashSet<>(Arrays.asList(projectArtifact, dependencyArtifact)));

        String classPath =
                AjcHelper.createClassPath(project, Collections.singletonList(pluginArtifact), Collections.emptyList());
        assertTrue(classPath.contains("sibling-project"), "wrong dependency order in " + classPath);
        assertTrue(
                classPath.indexOf("sibling-project") < classPath.indexOf("some.jar"),
                "project artifact must precede dependency artifact in " + classPath);
        assertTrue(
                classPath.indexOf("some.jar") < classPath.indexOf("plugin.jar"),
                "dependency artifact must precede plugin artifact in " + classPath);
    }

    private static DefaultArtifact createFixedArtifact(String artifactId) {
        return new DefaultArtifact("group", artifactId, "1.0-SNAPSHOT", "compile", "type", "classifier", null);
    }
}
