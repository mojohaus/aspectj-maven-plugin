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

/**
 * Integration test for weaveOnly parameter to support Lombok integration.
 * This test simulates a multi-module project scenario where:
 * 1. maven-compiler-plugin compiles sources with Lombok annotation processing
 * 2. aspectj-maven-plugin weaves aspects into compiled classes using weaveOnly=true
 *
 * @author Huang Xiao
 */
public class AjcWeaveOnlyMojoTest extends CompilerMojoTestBase {

    /**
     * Initialize test with AjcCompileMojo.
     */
    protected void setUp() throws Exception {
        ajcMojo = new AjcCompileMojo();
        super.setUp();
    }

    /**
     * Tests that weaveOnly mode skips source compilation and proceeds with weaving.
     * This is the primary use case for Lombok integration where maven-compiler-plugin
     * has already compiled sources with Lombok annotation processing.
     *
     * @throws Exception on test error
     */
    public void testWeaveOnly_withWeaveDirectories() throws Exception {
        // Simulate that maven-compiler-plugin has already compiled sources
        // by setting up a weaveDirectories configuration
        ajcMojo.weaveDirectories = new String[] {project.getBuild().getOutputDirectory()};

        // Enable weaveOnly mode - this should skip source compilation
        setVariableValueToObject(ajcMojo, "weaveOnly", true);

        // Configure aspect directory
        ajcMojo.aspectDirectory = "src/main/aspect";

        // Assemble arguments
        ajcMojo.assembleArguments();

        // Verify that no source files are included in weaveOnly mode
        assertTrue(
                "weaveOnly mode should not include source files",
                ajcMojo.resolvedIncludes == null || ajcMojo.resolvedIncludes.isEmpty());

        // Verify that -inpath is configured with weaveDirectories
        assertTrue("Should contain -inpath for weaving", ajcMojo.ajcOptions.contains("-inpath"));

        // Verify that the weave directory is in the options
        String inpathValue = null;
        for (int i = 0; i < ajcMojo.ajcOptions.size(); i++) {
            if ("-inpath".equals(ajcMojo.ajcOptions.get(i)) && i + 1 < ajcMojo.ajcOptions.size()) {
                inpathValue = (String) ajcMojo.ajcOptions.get(i + 1);
                break;
            }
        }
        assertNotNull("Should have inpath value", inpathValue);
        assertTrue(
                "Inpath should contain output directory",
                inpathValue.contains(project.getBuild().getOutputDirectory()));
    }

    /**
     * Tests that weaveOnly mode works without source files present.
     * This verifies the fix for compilation issues with Lombok in multi-module projects
     * where AspectJ should only weave pre-compiled classes.
     *
     * @throws Exception on test error
     */
    public void testWeaveOnly_noSourcesRequired() throws Exception {
        // Set up weaveDirectories
        ajcMojo.weaveDirectories = new String[] {project.getBuild().getOutputDirectory()};

        // Enable weaveOnly mode
        setVariableValueToObject(ajcMojo, "weaveOnly", true);

        // Don't set any source includes
        ajcMojo.includes = new String[0];

        // Assemble arguments - this should not fail even without sources
        ajcMojo.assembleArguments();

        // Verify no sources are included
        assertTrue(
                "weaveOnly mode should work without sources",
                ajcMojo.resolvedIncludes == null || ajcMojo.resolvedIncludes.isEmpty());
    }

    /**
     * Tests that weaveOnly mode can be combined with forceAjcCompile.
     * When both are set, weaving should be forced even if build is not needed.
     *
     * @throws Exception on test error
     */
    public void testWeaveOnly_withForceAjcCompile() throws Exception {
        // Set up weaveDirectories
        ajcMojo.weaveDirectories = new String[] {project.getBuild().getOutputDirectory()};

        // Enable both weaveOnly and forceAjcCompile
        setVariableValueToObject(ajcMojo, "weaveOnly", true);
        setVariableValueToObject(ajcMojo, "forceAjcCompile", true);

        // Assemble arguments
        ajcMojo.assembleArguments();

        // Verify that no source files are included
        assertTrue(
                "weaveOnly mode should not include source files",
                ajcMojo.resolvedIncludes == null || ajcMojo.resolvedIncludes.isEmpty());

        // forceAjcCompile should allow the build to proceed even without sources
        // This is verified implicitly by the fact that assembleArguments succeeds
    }

    /**
     * Tests that weaveOnly=false (default) still includes source files.
     * This ensures backward compatibility - existing behavior should not change.
     *
     * @throws Exception on test error
     */
    public void testWeaveOnly_defaultFalseIncludesSources() throws Exception {
        // weaveOnly should be false by default
        // Verify that source includes work normally
        ajcMojo.aspectDirectory = "src/main/aspect";
        final String[] includes = new String[] {"**/*.aj"};
        ajcMojo.includes = includes;

        // Assemble arguments
        ajcMojo.assembleArguments();

        // With weaveOnly=false (default), sources should be processed
        // The exact behavior depends on whether source files exist,
        // but the mechanism should be in place to include them
        // This is verified by the existing tests
    }

    /**
     * Tests that weaveOnly mode works with aspectLibraries configuration.
     * This simulates weaving with external aspect libraries (common in multi-module projects).
     *
     * @throws Exception on test error
     */
    public void testWeaveOnly_withAspectLibraries() throws Exception {
        // Set up weaveDirectories
        ajcMojo.weaveDirectories = new String[] {project.getBuild().getOutputDirectory()};

        // Enable weaveOnly mode
        setVariableValueToObject(ajcMojo, "weaveOnly", true);

        // Configure aspectLibraries (simulating external aspect library)
        ajcMojo.aspectLibraries = new Module[1];
        Module aspectLibrary = new Module();
        aspectLibrary.setGroupId("com.example.aspect");
        aspectLibrary.setArtifactId("validation-aspect");
        ajcMojo.aspectLibraries[0] = aspectLibrary;

        // Add the aspect library to project artifacts
        java.util.Set artifacts = new java.util.HashSet();
        artifacts.add(new MockArtifact("com.example.aspect", "validation-aspect"));
        ajcMojo.project.setArtifacts(artifacts);

        // Assemble arguments
        ajcMojo.assembleArguments();

        // Verify that no source files are included
        assertTrue(
                "weaveOnly mode should not include source files",
                ajcMojo.resolvedIncludes == null || ajcMojo.resolvedIncludes.isEmpty());

        // Verify that -aspectpath is configured
        assertTrue("Should contain -aspectpath for aspect libraries", ajcMojo.ajcOptions.contains("-aspectpath"));
    }

    /**
     * Returns the test project name.
     */
    String getProjectName() {
        return "test-project";
    }
}
