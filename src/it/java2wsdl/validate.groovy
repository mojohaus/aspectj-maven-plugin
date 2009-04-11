def generatedWsdlFile = new File( basedir, 'target/wsdl2java-it-test-1.0-SNAPSHOT/WEB-INF/services/test.wsdl' )
assert generatedWsdlFile.exists();
return true;