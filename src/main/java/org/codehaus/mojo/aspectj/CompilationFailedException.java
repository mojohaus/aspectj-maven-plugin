package org.codehaus.mojo.aspectj;

import org.apache.maven.plugin.MojoExecutionException;
import org.aspectj.bridge.IMessage;

/**
 * Exception thrown when Ajc finds errors during compilation.
 *
 * @author Carlos Sanchez
 */
public final class CompilationFailedException extends MojoExecutionException {

    // Internal state
    private static final long serialVersionUID = 2558168648061612263L;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private CompilationFailedException(final String message) {
        super(message);
    }

    /**
     * Factory method which creates a CompilationFailedException from the supplied AJC IMessages.
     *
     * @param errors A non-empty array of IMessage objects which
     * @return A CompilationFailedException containing a string representation of the supplied errors.
     */
    public static CompilationFailedException create(final IMessage[] errors) {

        final StringBuilder sb = new StringBuilder();
        sb.append("AJC compiler errors:").append(LINE_SEPARATOR);
        for (final IMessage error : errors) {
            sb.append(error.toString()).append(LINE_SEPARATOR);
        }

        return new CompilationFailedException(sb.toString());
    }
}
