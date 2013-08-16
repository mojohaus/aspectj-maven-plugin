file = new File(basedir, "build.log")
assert file.exists()
execLine = file.readLines().find { line -> line.contains( ' Running : ajc ' ) }
assert execLine != null
assert execLine.contains( " -Xlintfile " )
assert execLine.contains( "XlintTestFile.properties" )
text = file.getText()
//Xlint:adviceDidNotMatch set to ignore
assert !text.contains("[Xlint:adviceDidNotMatch]")
//WARNING level is default for Xlint:invalidAbsoluteTypeName
assert text.contains("[WARNING] no match for this type name: Clazzz [Xlint:invalidAbsoluteTypeName]")