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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests class {@link org.codehaus.mojo.aspectj.AbstractAjcCompiler}
 *
 * @author <a href="mailto:tel@objectnet.no">Thor Age Eldby</a>
 */
class AbstractAjcCompilerTest {

    /** Compiler mojo instance */
    private AjcTestCompileMojo ajcCompMojo;

    @BeforeEach
    void setUp() throws Exception {
        ajcCompMojo = new AjcTestCompileMojo();
        MavenProject project = new MavenProject(new Model());
        ajcCompMojo.project = project;
        ReflectionUtils.setVariableValueInObject(ajcCompMojo, "outputDirectory", getTestFile("target/test-classes"));
        ReflectionUtils.setVariableValueInObject(
                ajcCompMojo,
                "generatedTestSourcesDirectory",
                getTestFile("target/generated-test-sources/test-annotations"));
        project.setDependencyArtifacts(new HashSet());
    }

    /**
     * Verifies that if not stated no -inpath argument should be found in the ajc arguments
     * {@link AbstractAjcCompiler#execute()}
     *
     * @throws MojoExecutionException if the mojo fails to execute
     */
    @Test
    void getAjcArgumentsNoWeaveArtifacts() throws Exception {
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertFalse(args.contains("-inpath"));
    }

    /**
     * Tests that the compiler fails as it should if told to weave an artifact not listed in the project dependencies.
     */
    @Test
    void getAjcArgumentsWeaveArtifactsNotProjectDependecy() {
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId(mod1Group);
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId(mod1Artifact);
        ajcCompMojo.weaveDependencies = new Module[1];
        ajcCompMojo.weaveDependencies[0] = module1;
        assertThrows(
                MojoExecutionException.class, () -> ajcCompMojo.assembleArguments(), "Should fail quite miserably");
    }

    /**
     * Tests if modules told to weave that are found in the project dependencies actually are found in the .inpath ajc
     * argument,.
     *
     * @throws MojoExecutionException if the mojo fails to execute
     */
    @Test
    void getAjcArgumentsWeaveArtifacts() throws Exception {
        ajcCompMojo.weaveDependencies = new Module[2];
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId(mod1Group);
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId(mod1Artifact);
        ajcCompMojo.weaveDependencies[0] = module1;
        Module module2 = new Module();
        String mod2Group = "foooup";
        module2.setGroupId(mod2Group);
        String mod2Artifact = "bartifact";
        module2.setArtifactId(mod2Artifact);
        ajcCompMojo.weaveDependencies[1] = module2;
        // Modify project to include depencies
        Set artifacts = new HashSet();
        artifacts.add(new MockArtifact(mod1Group, mod1Artifact));
        artifacts.add(new MockArtifact(mod2Group, mod2Artifact));
        ajcCompMojo.project.setArtifacts(artifacts);
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertTrue(args.contains("-inpath"));
        Iterator it = args.iterator();
        while (!it.next().equals("-inpath")) {
            // don't do nothing
        }
        String weavePath = (String) it.next();
        assertTrue(weavePath.indexOf(File.pathSeparator) != -1);
        assertTrue(weavePath.indexOf(mod1Artifact) != -1);
        assertTrue(weavePath.indexOf(mod2Artifact) != -1);
    }

    /**
     * Tests if modules told to weave that are found in the project dependencies actually are found in the .inpath ajc
     * argument, considering classifiers.
     *
     * @throws MojoExecutionException if the mojo fails to execute
     */
    @Test
    void getAjcArgumentsWeaveArtifactsWithClassifier() throws Exception {
        ajcCompMojo.weaveDependencies = new Module[1];
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId(mod1Group);
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId(mod1Artifact);
        String mod1classifier = "dev";
        module1.setClassifier(mod1classifier);
        ajcCompMojo.weaveDependencies[0] = module1;
        String mod2classifier = "stable";
        // Modify project to include dependencies
        Set artifacts = new HashSet();
        artifacts.add(new MockArtifact(mod1Group, mod1Artifact, mod1classifier, "jar"));
        artifacts.add(new MockArtifact(mod1Group, mod1Artifact, mod2classifier, "jar"));
        ajcCompMojo.project.setArtifacts(artifacts);
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertTrue(args.contains("-inpath"));
        Iterator it = args.iterator();
        while (!it.next().equals("-inpath")) {
            // don't do nothing
        }
        String weavePath = (String) it.next();
        assertTrue(weavePath.indexOf(mod1Artifact) != -1);
        assertTrue(weavePath.indexOf(mod1classifier) != -1);
        assertFalse(weavePath.indexOf(mod2classifier) != -1);
    }

    /**
     * Tests if modules told to weave that are found in the project dependencies actually are found in the .inpath ajc
     * argument, considering type.
     *
     * @throws MojoExecutionException if the mojo fails to execute
     */
    @Test
    void getAjcArgumentsWeaveArtifactsWithType() throws Exception {
        ajcCompMojo.weaveDependencies = new Module[1];
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId(mod1Group);
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId(mod1Artifact);
        String mod1type = "foo";
        module1.setType(mod1type);
        ajcCompMojo.weaveDependencies[0] = module1;
        String mod2type = "bar";
        // Modify project to include dependencies
        Set artifacts = new LinkedHashSet();
        artifacts.add(new MockArtifact(mod1Group, mod1Artifact, null, mod1type));
        artifacts.add(new MockArtifact(mod1Group, mod1Artifact, null, mod2type));
        ajcCompMojo.project.setArtifacts(artifacts);
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertTrue(args.contains("-inpath"));
        Iterator it = args.iterator();
        while (!it.next().equals("-inpath")) {
            // don't do nothing
        }
        String weavePath = (String) it.next();
        assertTrue(weavePath.indexOf(mod1Artifact) != -1);
        assertTrue(weavePath.indexOf("." + mod1type) != -1);
        assertFalse(weavePath.indexOf(mod2type) != -1);
    }

    /**
     * Tests if modules told to weave classes that are found in directories.
     *
     * @throws Exception on test error
     */
    @Test
    void getAjcArgumentsWeaveDirectories() throws Exception {
        String dir1 = "target/classes1";
        String dir2 = "target/classes2";
        ajcCompMojo.weaveDirectories = new String[] {dir1, dir2};
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertTrue(args.contains("-inpath"));
        Iterator it = args.iterator();
        while (!it.next().equals("-inpath")) {
            // don't do nothing
        }
        String weavePath = (String) it.next();
        assertTrue(weavePath.indexOf(File.pathSeparator) != -1);
        assertTrue(weavePath.indexOf(dir1) != -1);
        assertTrue(weavePath.indexOf(dir2) != -1);
    }

    /**
     * Verifies that if not stated no -aspectpath argument should
     * be found in the ajc arguments
     * {@link AbstractAjcCompiler#execute()}
     *
     * @throws Exception on test error
     */
    @Test
    void getAjcArgumentsNoLibraryArtifacts() throws Exception {
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertFalse(args.contains("-aspectpath"));
    }

    /**
     * Tests that the compiler fails as it should if told to weave an library artifact not listed in the project
     * dependencies.
     */
    @Test
    void getAjcArgumentsLibraryArtifactsNotProjectDependency() {
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId(mod1Group);
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId(mod1Artifact);
        ajcCompMojo.aspectLibraries = new Module[1];
        ajcCompMojo.aspectLibraries[0] = module1;
        assertThrows(
                MojoExecutionException.class, () -> ajcCompMojo.assembleArguments(), "Should fail quite miserably");
    }

    /**
     * Tests if modules told to weave that are found in the project dependencies actually are found in the .inpath ajc
     * argument,.
     *
     * @throws Exception on test error
     */
    @Test
    void getAjcLibraryArtifacts() throws Exception {
        ajcCompMojo.aspectLibraries = new Module[2];
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId(mod1Group);
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId(mod1Artifact);
        ajcCompMojo.aspectLibraries[0] = module1;
        Module module2 = new Module();
        String mod2Group = "foooup";
        module2.setGroupId(mod2Group);
        String mod2Artifact = "bartifact";
        module2.setArtifactId(mod2Artifact);
        ajcCompMojo.aspectLibraries[1] = module2;
        // Modify project to include depencies
        Set artifacts = new HashSet();
        artifacts.add(new MockArtifact(mod1Group, mod1Artifact));
        artifacts.add(new MockArtifact(mod2Group, mod2Artifact));
        ajcCompMojo.project.setArtifacts(artifacts);
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertTrue(args.contains("-aspectpath"));
        Iterator it = args.iterator();
        while (!it.next().equals("-aspectpath")) {
            // don't do nothing
        }
        String weavePath = (String) it.next();
        assertTrue(weavePath.indexOf(File.pathSeparator) != -1);
        assertTrue(weavePath.indexOf(mod1Artifact) != -1);
        assertTrue(weavePath.indexOf(mod2Artifact) != -1);
    }

    // MASPECTJ-103
    @Test
    void getAJcEmptyClassifier() throws Exception {
        String groupId = "groupId";
        String artifactId = "artifactId";
        String classifier = ""; // could be result of filtering properties

        Module module = new Module();
        module.setGroupId(groupId);
        module.setArtifactId(artifactId);
        module.setClassifier(classifier);

        ajcCompMojo.aspectLibraries = new Module[] {module};

        Set artifacts = new HashSet();
        artifacts.add(new MockArtifact(groupId, artifactId));
        ajcCompMojo.project.setArtifacts(artifacts);
        ajcCompMojo.assembleArguments();
        // should not fail
    }

    /**
     * Tests that weaveOnly mode skips source file compilation and only weaves class files.
     * When weaveOnly is true, source files should not be added to ajcOptions.
     *
     * @throws Exception on test error
     */
    @Test
    void weaveOnly_skipsSourceFiles() throws Exception {
        // Set up weaveDirectories to provide classes to weave
        String dir1 = "target/classes";
        ajcCompMojo.weaveDirectories = new String[] {dir1};

        // Enable weaveOnly mode
        ReflectionUtils.setVariableValueInObject(ajcCompMojo, "weaveOnly", true);

        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;

        // Should have -inpath argument for weaving
        assertTrue(args.contains("-inpath"), "weaveOnly mode should include -inpath argument");

        // resolvedIncludes should be empty in weaveOnly mode (no source files)
        assertTrue(ajcCompMojo.resolvedIncludes.isEmpty(), "weaveOnly mode should not include source files");
    }

    /**
     * Tests that weaveOnly mode includes weaveDirectories in -inpath.
     *
     * @throws Exception on test error
     */
    @Test
    void weaveOnly_includesWeaveDirectories() throws Exception {
        String dir1 = "target/classes1";
        String dir2 = "target/classes2";
        ajcCompMojo.weaveDirectories = new String[] {dir1, dir2};

        // Enable weaveOnly mode
        ReflectionUtils.setVariableValueInObject(ajcCompMojo, "weaveOnly", true);

        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;

        assertTrue(args.contains("-inpath"), "Should contain -inpath");
        Iterator it = args.iterator();
        while (!it.next().equals("-inpath")) {
            // find -inpath position
        }
        String weavePath = (String) it.next();
        assertTrue(weavePath.indexOf(dir1) != -1, "Should include first weave directory");
        assertTrue(weavePath.indexOf(dir2) != -1, "Should include second weave directory");
    }

    /**
     * Tests that weaveOnly mode works with weaveDependencies.
     *
     * @throws Exception on test error
     */
    @Test
    void weaveOnly_withWeaveDependencies() throws Exception {
        // Setup weaveDependencies
        ajcCompMojo.weaveDependencies = new Module[1];
        Module module1 = new Module();
        String mod1Group = "test.group";
        module1.setGroupId(mod1Group);
        String mod1Artifact = "test.artifact";
        module1.setArtifactId(mod1Artifact);
        ajcCompMojo.weaveDependencies[0] = module1;

        // Add to project artifacts
        Set artifacts = new HashSet();
        artifacts.add(new MockArtifact(mod1Group, mod1Artifact));
        ajcCompMojo.project.setArtifacts(artifacts);

        // Enable weaveOnly mode
        ReflectionUtils.setVariableValueInObject(ajcCompMojo, "weaveOnly", true);

        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;

        assertTrue(args.contains("-inpath"), "Should contain -inpath");
        Iterator it = args.iterator();
        while (!it.next().equals("-inpath")) {
            // find -inpath position
        }
        String weavePath = (String) it.next();
        assertTrue(weavePath.indexOf(mod1Artifact) != -1, "Should include weave dependency");

        // resolvedIncludes should be empty in weaveOnly mode
        assertTrue(ajcCompMojo.resolvedIncludes.isEmpty(), "weaveOnly mode should not include source files");
    }

    /**
     * Tests that source files are still resolved when weaveOnly is left at its default.
     */
    @Test
    void weaveOnly_defaultIsFalse() throws Exception {
        ajcCompMojo.assembleArguments();

        assertFalse((boolean) ReflectionUtils.getValueIncludingSuperclasses("weaveOnly", ajcCompMojo));
        assertNotNull(ajcCompMojo.resolvedIncludes);
    }

    private static File getTestFile(String path) {
        return new File(System.getProperty("basedir", System.getProperty("user.dir")), path);
    }
}
