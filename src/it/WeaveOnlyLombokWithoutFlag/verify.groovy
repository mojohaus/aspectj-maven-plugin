// ajc recompiled the sources without Lombok, so getName() is unresolved (#231)
def log = new File(basedir, "build.log").text
assert log.contains("getName")
