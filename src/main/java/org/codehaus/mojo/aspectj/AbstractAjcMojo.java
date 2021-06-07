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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * The base class.
 *
 * @author Juraj Burian
 */
public abstract class AbstractAjcMojo extends AbstractMojo
{

    /**
     * The maven project.
     *
     */
    @Parameter( required = true, readonly = true, defaultValue = "${project}" )
    protected MavenProject project;

    /**
     * The basedir of the project.
     *
     */
    @Parameter( required = true, readonly = true, defaultValue = "${basedir}" )
    protected File basedir;

    /**
     * List of of modules to weave (into target directory). Corresponds to <code>ajc
     * -inpath</code> option (or <code>-injars</code> for pre-1.2 (which is not supported)).
     *
     */
    @Parameter
    protected Module[] weaveDependencies;

    /**
     * List of of directories with .class files to weave (into target directory).
     * Corresponds to <code>ajc -inpath</code> option.
     *
     * @since 1.4
     */
    @Parameter
    protected String[] weaveDirectories;

    /**
     * Java 9+ modules to build the module path from.
     * Corresponds to <code>ajc --module-path</code> option.
     *
     * @since 1.13
     */
    @Parameter
    protected Module[] javaModules;

    /**
     * Weave binary aspects from the jars.
     * The aspects should have been output by the same version of the compiler.
     * The modules must also be dependencies of the project.
     * Corresponds to <code>ajc -aspectpath</code> option
     *
     */
    @Parameter
    protected Module[] aspectLibraries;

    /**
     * Parameter which indicates an XML file containing AspectJ weaving instructions.
     * Assigning this plugin parameter adds the <code>-xmlConfigured</code> option to ajc.
     *
     * @see <a href="http://www.eclipse.org/aspectj/doc/next/devguide/ajc-ref.html">http://www.eclipse.org/aspectj/doc/next/devguide/ajc-ref.html</a>
     */
    @Parameter
    protected File xmlConfigured;

    /**
     * Skip plugin execution.
     *
     */
    @Parameter( defaultValue = "false", property = "aspectj.skip" )
    private boolean skip;

    /**
     * @return <code>true</code> if execution should be skipped, otherwise <code>false</code>
     */
    protected final boolean isSkip()
    {
        return skip;
    }

    /**
     * Parameter which indicates an XML file containing AspectJ weaving instructions.
     * Assigning this plugin parameter adds the <code>-xmlConfigured</code> option to ajc.
     *
     * @param xmlConfigured an XML file containing AspectJ weaving instructions.
     */
    public void setXmlConfigured( final File xmlConfigured )
    {
        try
        {
            final String path = xmlConfigured.getCanonicalPath();
            if ( !xmlConfigured.exists() )
            {
                getLog().warn( "xmlConfigured parameter invalid. [" + path + "] does not exist." );
            }
            else if ( !path.trim().toLowerCase().endsWith( ".xml" ) )
            {
                getLog().warn( "xmlConfigured parameter invalid. XML file name must end in .xml" );
            }
            else
            {
                this.xmlConfigured = xmlConfigured;
            }
        }
        catch ( Exception e )
        {
            getLog().error( "Exception setting xmlConfigured option", e );
        }
    }
}
