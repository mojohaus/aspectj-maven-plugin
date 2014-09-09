log = new File( basedir, 'build.log' )
assert log.exists()

//
// #1) Validate the informational messages used in the MavenMessageHandler to define the detail level.
//
messageDetailNote = log.readLines().find {
  line -> line.contains( '[INFO] Showing AJC message detail for messages of types: [error, warning, fail]'
)}
assert messageDetailNote != null

//
// #2) Validate the exception messages, including line numbers and contexts
//
warning1 = log.readLines().find {
  line -> line.contains( '[WARNING] no match for this type name: ATestCase [Xlint:invalidAbsoluteTypeName]'
  )}
warning1TypeAndLineNumber = log.readLines().find {
  line -> line.contains( 'LineNumbersInAjcWarnings/src/main/aspect/Azpect.java:41'.replace('/', File.separator)
)}
warning1Context = log.readLines().find {
  line -> line.contains( '@Before ("execution (* ATestCase.*(..))")'
)}
warning2 = log.readLines().find {
  line -> line.contains( '[WARNING] The import java.io.File is never used'
)}
warning2TypeAndLineNumber = log.readLines().find {
  line -> line.contains( 'LineNumbersInAjcWarnings/src/main/java/Clazz.java:3'.replace('/', File.separator)
)}
warning2Context = log.readLines().find {
  line -> line.contains( 'import java.io.File;'
)}
warning3 = log.readLines().find {
  line -> line.contains( '[WARNING] The value of the parameter anUnusedArgument is not used'
  )}
warning3TypeAndLineNumber = log.readLines().find {
  line -> line.contains( 'LineNumbersInAjcWarnings/src/main/java/Clazz.java:14'.replace('/', File.separator)
  )}
warning3Context = log.readLines().find {
  line -> line.contains( 'public void print( String anUnusedArgument )'
  )}
warning4 = log.readLines().find {
  line -> line.contains( '[WARNING] The value of the local variable anUnusedLocalVariable is not used'
  )}
warning4TypeAndLineNumber = log.readLines().find {
  line -> line.contains( 'LineNumbersInAjcWarnings/src/main/java/Clazz.java:16'.replace('/', File.separator)
  )}
warning4Context = log.readLines().find {
  line -> line.contains( 'final int anUnusedLocalVariable = 42;'
  )}

// Validate that we have found the required lines
assert warning1 != null
assert warning1TypeAndLineNumber != null
assert warning1Context != null
assert warning2 != null
assert warning2TypeAndLineNumber != null
assert warning2Context != null
assert warning3 != null
assert warning3TypeAndLineNumber != null
assert warning3Context != null
assert warning4 != null
assert warning4TypeAndLineNumber != null
assert warning4Context != null