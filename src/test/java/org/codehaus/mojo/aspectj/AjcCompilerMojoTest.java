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

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Plugin testcases.
 *
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 *
 */
class AjcCompilerMojoTest extends CompilerMojoTestBase {

    /**
     *
     */
    @BeforeEach
    public void setUp() throws Exception {
        ajcMojo = new AjcCompileMojo();
        super.setUp();
    }

    /**
     * @throws Exception on test error
     */
    @Test
    void modificationSet() throws Exception {
        ajcMojo.aspectDirectory = "src/main/aspect";
        final String[] includes = new String[] {"org/codehaus/mojo/aspectj/OldStyleAspect.aj"};
        ajcMojo.setArgumentFileName("builddef.lst");
        FileUtils.fileDelete(project.getBuild().getDirectory() + ajcMojo.argumentFileName);

        ajcMojo.includes = new String[] {"org/codehaus/mojo/aspectj/OldStyleAspect.aj"};
        ajcMojo.assembleArguments();
        assertTrue(ajcMojo.isBuildNeeded(), "Build should be needed when no previous files are found");

        try {
            ajcMojo.ajcOptions.clear();
            ajcMojo.includes = includes;
            ajcMojo.execute();
        } catch (CompilationFailedException cfe) {
            // we're only testing modifications, don't care if it won't compile
        } catch (UnsupportedClassVersionError ucve) {
            // we're only testing modifications, don't care if it won't compile
        }

        ajcMojo.ajcOptions.clear();
        ajcMojo.includes = includes;
        ajcMojo.assembleArguments();
        assertFalse(ajcMojo.isBuildNeeded(), "A build has completed. No modifications done. no new build needed");

        ajcMojo.ajcOptions.clear();
        ajcMojo.includes = includes;
        ajcMojo.setShowWeaveInfo(true);
        ajcMojo.assembleArguments();
        assertTrue(ajcMojo.isBuildNeeded(), "One of the arguments has changed, a new build is needed");

        ajcMojo.ajcOptions.clear();
        ajcMojo.includes = includes;
        ajcMojo.assembleArguments();
        assertFalse(ajcMojo.isBuildNeeded(), "A build has completed. No modifications done. no new build needed");
        String currentDir = new File(".").getAbsolutePath();
        File aspect = new File(currentDir.substring(0, currentDir.length() - 1)
                + "src/test/projects/test-project/src/main/aspect/org/codehaus/mojo/aspectj/OldStyleAspect.aj");
        long timeStamp = System.currentTimeMillis();
        assertTrue(aspect.setLastModified(timeStamp), "Could not touch file: " + aspect.getAbsolutePath());
        assertTrue(ajcMojo.isBuildNeeded(), "One of the included files has changed. a new build is needed");
    }

    String getProjectName() {
        return "test-project";
    }
}
