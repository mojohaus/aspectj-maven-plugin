file = new File(basedir, "build.log")
assert file.exists()
assert file.getText().contains("woven class foo.Clazz")
assert !file.getText().contains("woven class foo.Azpect")