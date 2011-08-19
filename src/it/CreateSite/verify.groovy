indexFile = new File(basedir, "target/site/aspectj-report/index.html")
assert indexFile.exists()
clazzFile = new File(basedir, "target/site/aspectj-report/Clazz.html")
assert clazzFile.getText().contains("Advised&nbsp;by:")
