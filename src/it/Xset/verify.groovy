file = new File(basedir, "build.log")
assert file.exists()
execLine = file.readLines().find { line -> line.contains( ' Running : ajc ' ) }
assert execLine != null
assert execLine.contains( " -Xset:overWeaving=true,avoidFinal=false " ) || execLine.contains( " -Xset:avoidFinal=false,overWeaving=true " )