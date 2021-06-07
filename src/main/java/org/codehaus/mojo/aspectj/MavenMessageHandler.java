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

import org.apache.maven.plugin.logging.Log;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.MessageHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * MessageHandler implementation which uses the standard Maven Log to emit
 * messages from the AJC process. For warnings and error messages from the AJC,
 * the message detail (containing information about class and line number location)
 * is emitted as well.
 *
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MavenMessageHandler extends MessageHandler {


    // Internal state
    private static final List<IMessage.Kind> DEFAULT_DETAIL_TYPES;
    private Log log;
    private List<IMessage.Kind> showDetailsForMessageKindList;

    static {
        DEFAULT_DETAIL_TYPES = new ArrayList<IMessage.Kind>();
        DEFAULT_DETAIL_TYPES.add(IMessage.ERROR);
        DEFAULT_DETAIL_TYPES.add(IMessage.WARNING);
        DEFAULT_DETAIL_TYPES.add(IMessage.FAIL);
    }

    /**
     * Constructs a MessageHandler with a Maven plugin logger.
     *
     * @param log                           The active Maven Log.
     * @param showDetailsForMessageKindList A List holding all AJC message types for which this MavenMessageHandler
     *                                      should emit details onto the Maven log (i.e. class name,
     *                                      line/row number etc.)
     */
    public MavenMessageHandler(final Log log,
                               final List<IMessage.Kind> showDetailsForMessageKindList) {

        // Check sanity
        // assert log != null : "Cannot handle null log argument.";
        // assert showDetailsForMessageKindList != null : "Cannot handle null showDetailsForMessageKindList argument.";
        if (log == null) {
            throw new NullPointerException("Cannot handle null log argument.");
        }
        if (showDetailsForMessageKindList == null) {
            throw new NullPointerException("Cannot handle null showDetailsForMessageKindList argument.");
        }

        // Assign internal state
        this.log = log;
        this.showDetailsForMessageKindList = showDetailsForMessageKindList;

        if (log.isInfoEnabled()) {
            log.info("Showing AJC message detail for messages of types: " + showDetailsForMessageKindList);
        }
    }

    /**
     * Constructs a MessageHandler with a Maven plugin logger, and emitting detailed information for all
     * AJC message kinds the {@code DEFAULT_DETAIL_TYPES} List.
     *
     * @param log The active Maven Log.
     */
    public MavenMessageHandler(final Log log) {
        this(log, DEFAULT_DETAIL_TYPES);
    }

    /**
     * Copies output from the supplied message onto the active Maven Log.
     * If the message type (i.e. {@code message.getKind()}) is listed in the showDetailsForMessageKindList List,
     * the message is prefixed with location details (Class, row/line number etc.) as well.
     * <p>
     * {@inheritDoc}
     */
    public boolean handleMessage(final IMessage message) {

        // Compose the message text
        final StringBuilder builder = new StringBuilder(message.getMessage());
        if (isMessageDetailDesired(message)) {

            //
            // The AJC details are typically delivered on the format [fileName]:[lineNumber]
            // (i.e. /src/main/java/Clazz.java:16).
            //
            // Mimic this, and include the context of the message as well,
            // including guarding against NPEs.
            //
            final ISourceLocation sourceLocation = message.getSourceLocation();
            final String sourceFile = sourceLocation == null || sourceLocation.getSourceFile() == null
                    ? "<unknown source file>"
                    : sourceLocation.getSourceFile().getAbsolutePath();
            final String context = sourceLocation == null || sourceLocation.getContext() == null
                    ? ""
                    : sourceLocation.getContext() + "\n";
            final String line = sourceLocation == null
                    ? "<no line information>"
                    : "" + sourceLocation.getLine();

            builder.append("\n\t")
                    .append(sourceFile)
                    .append(":")
                    .append(line)
                    .append("\n")
                    .append(context);
        }

        final String messageText = builder.toString();

        if (isNotIgnored(message, IMessage.DEBUG)
                || isNotIgnored(message, IMessage.INFO)
                || isNotIgnored(message, IMessage.TASKTAG)) {

            // The DEBUG, INFO, and TASKTAG ajc message kinds are considered Maven Debug messages.
            log.debug(messageText);

        } else if (isNotIgnored(message, IMessage.WEAVEINFO)) {

            // The WEAVEINFO ajc message kind is considered Maven Info messages.
            log.info(messageText);

        } else if (isNotIgnored(message, IMessage.WARNING)) {

            // The WARNING ajc message kind is considered Maven Warn messages.
            log.warn(messageText);

        } else if(isNotIgnored(message, IMessage.ERROR)
                || isNotIgnored(message, IMessage.ABORT)
                || isNotIgnored(message, IMessage.FAIL)) {

            // We map ERROR, ABORT, and FAIL ajc message kinds to Maven Error messages.
            log.error(messageText);
        }

        // Delegate to normal handling.
        return super.handleMessage(message);
    }

    //
    // Private helpers
    //

    private boolean isMessageDetailDesired(final IMessage message) {

        if (message != null) {
            for (IMessage.Kind current : showDetailsForMessageKindList) {
                if (message.getKind().equals(current)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isNotIgnored(final IMessage message, final IMessage.Kind messageType) {
        return message.getKind().equals(messageType) && !isIgnoring(messageType);
    }
}
