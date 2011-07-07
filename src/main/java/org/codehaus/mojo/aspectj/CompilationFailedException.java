package org.codehaus.mojo.aspectj;

import org.apache.maven.plugin.MojoExecutionException;
import org.aspectj.bridge.IMessage;

/**
 * Exception thrown when Ajc finds errors during compilation.
 *  
 * @author Carlos Sanchez <carlos@apache.org>
 */
public class CompilationFailedException extends MojoExecutionException
{
    /**
     * 
     */
    private static final long serialVersionUID = 2558168648061612263L;
    private IMessage[] errors;

    public CompilationFailedException( IMessage[] errors )
    {
        super( composeMessage( errors ) );
        this.errors = errors;
    }

    public IMessage[] getErrors()
    {
        return errors;
    }

    private static String composeMessage( IMessage[] errors ) 
    {
        StringBuffer sb = new StringBuffer();
        final String LINE_SEPARATOR = System.getProperty( "line.separator" );

        sb.append( "Compiler errors:" + LINE_SEPARATOR );
        for ( int i = 0; i < errors.length; i++ )
        {
            sb.append( errors[i].toString() );
            sb.append( LINE_SEPARATOR );
        }

        return sb.toString();
    }

}
