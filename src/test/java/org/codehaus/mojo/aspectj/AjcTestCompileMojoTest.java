package org.codehaus.mojo.aspectj;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.jupiter.api.Test;

class AjcTestCompileMojoTest extends AbstractMojoTestCase {

    @Test
    void executeWithSkip() throws Exception {
        final AjcTestCompileMojo testSubject = new AjcTestCompileMojo();
        System.setProperty(AjcTestCompileMojo.MAVEN_TEST_SKIP, "true");
        testSubject.execute();
    }

    @Test
    void executeWithoutSkip() throws Exception {
        final AjcTestCompileMojo testSubject = new AjcTestCompileMojo();

        try {
            testSubject.execute();
        } catch (Exception e) {
            // should throw exception, as superclass executes
            // and no setup has been done.
        }
    }
}
