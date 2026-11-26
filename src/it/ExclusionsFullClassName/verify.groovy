file = new File(basedir, "build.log")
assert file.exists()
def log = file.getText()
assert log.contains("woven class foo.Clazz")
assert log.contains("woven aspect foo.OldStyleAspect")
// Azpect is an @Aspect, so ajc reports it as "woven aspect", not "woven class"
assert !log.contains("woven aspect foo.Azpect")
assert !log.contains("woven class foo.Azpect")
