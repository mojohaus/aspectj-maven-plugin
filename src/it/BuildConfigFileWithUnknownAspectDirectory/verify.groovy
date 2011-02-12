file = new File(basedir, "build.log")
assert file.exists()
assert !file.getText().contains("woven aspect foo.OldStyleAspect")
