// The argument files must not end up in the directories that get packaged ...
[
    "classes/builddef.lst",
    "test-classes/builddef.lst",
    "classes/builddef-test.lst",
    "test-classes/builddef-test.lst"
].each { assert !new File(basedir, "target/$it").exists() : it }

// ... but in the configured directory.
[
    "aspectj-build/builddef.lst",
    "aspectj-build/builddef-test.lst"
].each { assert new File(basedir, "target/$it").exists() : it }

// The classes were actually compiled.
assert new File(basedir, "target/classes/Application.class").exists()
assert new File(basedir, "target/test-classes/DummyTest.class").exists()
