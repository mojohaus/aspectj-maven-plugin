def generatedSource = new File( basedir, 'target/generated-sources/axistools/wsdl2java/com/foo/bar/one' )
assert generatedSource.exists();
return true;