log = new File( basedir, 'build.log' )
assert log.exists()
execLine = log.readLines().find { line -> line.contains( ' Running : ajc ' ) }
assert execLine == null