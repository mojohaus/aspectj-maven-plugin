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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

        MavenProject project = new MavenProject();
        project.setArtifacts(Collections.singleton(projectArtifact));
        project.setDependencyArtifacts(Collections.singleton(dependencyArtifact));

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
