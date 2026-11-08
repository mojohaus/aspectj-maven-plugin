// Lombok-generated getName() exists only if javac ran the processor and ajc did not recompile the sources
def greeter = new File(basedir, "target/classes/it/Greeter.class")
assert greeter.exists()
def bytes = greeter.getText("ISO-8859-1")
assert bytes.contains("getName")
// the aspect was woven into the javac-compiled class
assert bytes.contains("TracingAspect")
def log = new File(basedir, "build.log").text
assert log.contains("weaveOnly mode")
