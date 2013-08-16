file = new File(basedir, "build.log")
assert file.exists()
execLine = file.readLines().find { line -> line.contains( ' Running : ajc ' ) }
assert execLine != null
assert execLine.contains( " -Xajruntimetarget:1.2 " )