package org.codehaus.mojo.aspectj;


public class NoSourcesAvailableTest
    extends CompilerMojoTestBase
{
    /**
     * 
     */
    protected void setUp()
        throws Exception
    {
        ajcMojo = new AjcCompileMojo();
        super.setUp();
    }
    
    /**
     * @throws Exception
     */
    public void testWithNoSources()
        throws Exception
    {
        try
        {
            ajcMojo.setComplianceLevel( "1.5" );
            ajcMojo.aspectDirectory = "src/main/aspect";
            ajcMojo.execute();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    String getProjectName()
    {
        return "no-sources-project";
    }

}
