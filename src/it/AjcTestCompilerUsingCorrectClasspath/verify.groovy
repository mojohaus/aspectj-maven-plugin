file = new File(basedir, "target/test-classes/builddef.lst")
assert file.exists()
// options set through setters, such as -showWeaveInfo and -verbose, can come before or after -classpath
def lines = file.readLines()
def classpath = lines.indexOf('-classpath')
assert classpath >= 0
assert lines.get(classpath + 1).contains('junit')
