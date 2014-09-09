// An unused import statement, should yield the following from AJC:
// [WARNING] The import java.io.File is never used
import java.io.File;

/**
 * The Class to get woven
 * 
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public class Clazz
{
    // An unused argument, should yield the following from AJC:
    // [WARNING] The value of the parameter anUnusedArguent is not used
    public void print( String anUnusedArgument )
    {
        final int anUnusedLocalVariable = 42;

        System.out.println( "Weave me" );
    }
}
