def log = new File(basedir, "build.log")
assert log.exists()
// The first execution weaves from scratch, the second one has to notice that a dependency was touched in between.
assert log.text.contains(":compile (first)")
assert log.text.contains(":compile (second)")
assert !log.text.contains("No modifications found skipping aspectJ compile")
assert new File(basedir, "app/target/classes/Model.class").exists()
