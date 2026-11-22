def execLine = new File(basedir, 'build.log').readLines().find { line -> line.contains(' Running : ajc ') }
assert execLine != null
assert execLine.contains(' -XhasMember ')
