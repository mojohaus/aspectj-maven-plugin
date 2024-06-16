package org.codehaus.mojo.aspectj;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public class AjcTestCompileMojoTest extends AbstractMojoTestCase {

    public void testExecuteWithSkip() throws Exception {
        final AjcTestCompileMojo testSubject = new AjcTestCompileMojo();
        System.setProperty(AjcTestCompileMojo.MAVEN_TEST_SKIP, "true");
        testSubject.execute();
    }

    public void testExecuteWithoutSkip() throws Exception {
        final AjcTestCompileMojo testSubject = new AjcTestCompileMojo();

        try {
            testSubject.execute();
        } catch (Exception e) {
            // should throw exception, as superclass executes
            // and no setup has been done.
        }
    }
}
