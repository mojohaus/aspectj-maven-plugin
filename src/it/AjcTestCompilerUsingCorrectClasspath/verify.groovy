file = new File(basedir, "target/test-classes/builddef.lst")
assert file.exists()
assert file.readLines().get(4) == '-classpath'
assert file.readLines().get(5).contains('junit')
