def log = new File(basedir, "build.log")
assert log.exists()
assert !log.text.contains("No sources found skipping aspectJ compile")
assert new File(basedir, "target/classes/builddef.lst").exists()
// the class from the weaveDependency ends up in the output directory
assert new File(basedir, "target/classes/localhost/maspectj86/Woven.class").exists()
