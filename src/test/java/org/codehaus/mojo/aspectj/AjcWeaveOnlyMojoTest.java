package org.codehaus.mojo.aspectj;

import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for weaveOnly parameter to support Lombok integration.
 * This test simulates a multi-module project scenario where:
 * 1. maven-compiler-plugin compiles sources with Lombok annotation processing
 * 2. aspectj-maven-plugin weaves aspects into compiled classes using weaveOnly=true
 *
 * @author Huang Xiao
 */
class AjcWeaveOnlyMojoTest extends CompilerMojoTestBase {

    @Override
    AbstractAjcCompiler createMojo() {
        return new AjcCompileMojo();
    }

    /**
     * Tests that weaveOnly mode skips source compilation and proceeds with weaving.
     * This is the primary use case for Lombok integration where maven-compiler-plugin
     * has already compiled sources with Lombok annotation processing.
     *
     * @throws Exception on test error
     */
    @Test
    void weaveOnly_withWeaveDirectories() throws Exception {
        // Simulate that maven-compiler-plugin has already compiled sources
        // by setting up a weaveDirectories configuration
        ajcMojo.weaveDirectories = new String[] {project.getBuild().getOutputDirectory()};

        // Enable weaveOnly mode - this should skip source compilation
        ReflectionUtils.setVariableValueInObject(ajcMojo, "weaveOnly", true);

        // Configure aspect directory
        ajcMojo.aspectDirectory = "src/main/aspect";

        // Assemble arguments
        ajcMojo.assembleArguments();

        // Verify that no source files are included in weaveOnly mode
        assertTrue(
                ajcMojo.resolvedIncludes == null || ajcMojo.resolvedIncludes.isEmpty(),
                "weaveOnly mode should not include source files");

        // Verify that -inpath is configured with weaveDirectories
        assertTrue(ajcMojo.ajcOptions.contains("-inpath"), "Should contain -inpath for weaving");

        // Verify that the weave directory is in the options
        String inpathValue = null;
        for (int i = 0; i < ajcMojo.ajcOptions.size(); i++) {
            if ("-inpath".equals(ajcMojo.ajcOptions.get(i)) && i + 1 < ajcMojo.ajcOptions.size()) {
                inpathValue = (String) ajcMojo.ajcOptions.get(i + 1);
                break;
            }
        }
        assertNotNull(inpathValue, "Should have inpath value");
        assertTrue(
                inpathValue.contains(project.getBuild().getOutputDirectory()),
                "Inpath should contain output directory");
    }

    /**
     * Tests that weaveOnly mode works without source files present.
     * This verifies the fix for compilation issues with Lombok in multi-module projects
     * where AspectJ should only weave pre-compiled classes.
     *
     * @throws Exception on test error
     */
    @Test
    void weaveOnly_noSourcesRequired() throws Exception {
        // Set up weaveDirectories
        ajcMojo.weaveDirectories = new String[] {project.getBuild().getOutputDirectory()};

        // Enable weaveOnly mode
        ReflectionUtils.setVariableValueInObject(ajcMojo, "weaveOnly", true);

        // Don't set any source includes
        ajcMojo.includes = new String[0];

        // Assemble arguments - this should not fail even without sources
        ajcMojo.assembleArguments();

        // Verify no sources are included
        assertTrue(
                ajcMojo.resolvedIncludes == null || ajcMojo.resolvedIncludes.isEmpty(),
                "weaveOnly mode should work without sources");
    }

    /**
     * Tests that weaveOnly mode can be combined with forceAjcCompile.
     * When both are set, weaving should be forced even if build is not needed.
     *
     * @throws Exception on test error
     */
    @Test
    void weaveOnly_withForceAjcCompile() throws Exception {
        // Set up weaveDirectories
        ajcMojo.weaveDirectories = new String[] {project.getBuild().getOutputDirectory()};

        // Enable both weaveOnly and forceAjcCompile
        ReflectionUtils.setVariableValueInObject(ajcMojo, "weaveOnly", true);
        ReflectionUtils.setVariableValueInObject(ajcMojo, "forceAjcCompile", true);

        // Assemble arguments
        ajcMojo.assembleArguments();

        // Verify that no source files are included
        assertTrue(
                ajcMojo.resolvedIncludes == null || ajcMojo.resolvedIncludes.isEmpty(),
                "weaveOnly mode should not include source files");

        // forceAjcCompile should allow the build to proceed even without sources
        // This is verified implicitly by the fact that assembleArguments succeeds
    }

    /**
     * Tests that weaveOnly=false (default) still includes source files.
     * This ensures backward compatibility - existing behavior should not change.
     *
     * @throws Exception on test error
     */
    @Test
    void weaveOnly_defaultFalseIncludesSources() throws Exception {
        ajcMojo.aspectDirectory = "src/main/aspect";
        ajcMojo.includes = new String[] {"**/*.aj"};

        ajcMojo.assembleArguments();

        assertFalse((boolean) ReflectionUtils.getValueIncludingSuperclasses("weaveOnly", ajcMojo));
        assertNotNull(ajcMojo.resolvedIncludes);
    }

    /**
     * Tests that weaveOnly mode works with aspectLibraries configuration.
     * This simulates weaving with external aspect libraries (common in multi-module projects).
     *
     * @throws Exception on test error
     */
    @Test
    void weaveOnly_withAspectLibraries() throws Exception {
        // Set up weaveDirectories
        ajcMojo.weaveDirectories = new String[] {project.getBuild().getOutputDirectory()};

        // Enable weaveOnly mode
        ReflectionUtils.setVariableValueInObject(ajcMojo, "weaveOnly", true);

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
                ajcMojo.resolvedIncludes == null || ajcMojo.resolvedIncludes.isEmpty(),
                "weaveOnly mode should not include source files");

        // Verify that -aspectpath is configured
        assertTrue(ajcMojo.ajcOptions.contains("-aspectpath"), "Should contain -aspectpath for aspect libraries");
    }

    @Override
    String getProjectName() {
        return "test-project";
    }
}
