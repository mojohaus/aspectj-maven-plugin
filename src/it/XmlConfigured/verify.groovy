file = new File(basedir, "build.log")
assert file.exists()
execLine = file.readLines().find { line -> line.contains( ' Running : ajc ' ) }
assert execLine != null
assert execLine.contains(" -xmlConfigured ")
assert execLine.contains("aopTestExample.xml ")

text = file.getText()
assert text.contains("Aspect 'Azpect' is scoped to apply against types matching pattern 'IncludedClazz'")
assert text.contains("Type 'ExcludedClazz' not woven by aspect 'Azpect' due to scope exclusion in XML definition")
assert !text.contains("Type 'IncludedClazz' not woven by aspect 'Azpect' due to scope exclusion in XML definition")
