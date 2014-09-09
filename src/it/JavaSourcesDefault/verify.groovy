log = new File( basedir, 'build.log' )
assert log.exists()
fooClazz = new File( basedir, 'src/main/java/foo/Clazz.java' )
execLine = log.readLines().find { line -> line.contains( ' Running : ajc ' ) }
assert execLine.toUpperCase().endsWith( fooClazz.getAbsolutePath().toUpperCase() )