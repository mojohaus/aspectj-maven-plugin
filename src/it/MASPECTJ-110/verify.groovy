log = new File( basedir, 'build.log' )
assert log.exists()

List<String> logFileLines = log.readLines();
println ' ==> Got [' + logFileLines.size() + '] lines in the LogFile'

// 1) The non-existent source roots *should not* be added to the compileSourceRoot list:
//
// [DEBUG] Not adding non-existent or already added aspectSourcePathDir [/Users/lj/Development/Research/AspectJ/aspectj-maven-plugin/target/it/MASPECTJ-110/src/main/nonexistentCompileDirectory] to compileSourceRoots.
// [DEBUG] Not adding non-existent or already added aspectSourcePathDir [/Users/lj/Development/Research/AspectJ/aspectj-maven-plugin/target/it/MASPECTJ-110/src/main/trivialaspects] to compileSourceRoots.
// [DEBUG] Not adding non-existent or already added aspectSourcePathDir [/Users/lj/Development/Research/AspectJ/aspectj-maven-plugin/target/it/MASPECTJ-110/src/main/aspect] to compileSourceRoots.
//
nonexistentAspectSourcePathDirLine = logFileLines.findAll {
  line -> line.contains( 'Not adding non-existent or already added aspectSourcePathDir' )
}

println 'Got nonexistentAspectSourcePathDirLine: ' + nonexistentAspectSourcePathDirLine

assert nonexistentAspectSourcePathDirLine.size() == 3
assert nonexistentAspectSourcePathDirLine.get(0).contains( 'src/main/nonexistentCompileDirectory]' )
assert nonexistentAspectSourcePathDirLine.get(1).contains( 'src/main/trivialaspects]' )
assert nonexistentAspectSourcePathDirLine.get(2).contains( 'src/main/aspect]' )

// 2) The three non-existent test source roots *should not* be added to the testCompileSourceRoot list:
//
// [DEBUG] Not adding non-existent or already added testAspectSourcePathDir [/Users/lj/Development/Research/AspectJ/aspectj-maven-plugin/target/it/MASPECTJ-110/src/test/aspect] to testCompileSourceRoots.
// [DEBUG] Not adding non-existent or already added testAspectSourcePathDir [/Users/lj/Development/Research/AspectJ/aspectj-maven-plugin/target/it/MASPECTJ-110/src/test/aspect] to testCompileSourceRoots.
// [DEBUG] Not adding non-existent or already added testAspectSourcePathDir [/Users/lj/Development/Research/AspectJ/aspectj-maven-plugin/target/it/MASPECTJ-110/src/test/nonexistentTestCompileDirectory] to testCompileSourceRoots.
//
nonexistentTestAspectSourcePathDirLine = logFileLines.findAll {
  line -> line.contains( 'Not adding non-existent or already added testAspectSourcePathDir' )
}

println  'Got nonexistentTestAspectSourcePathDirLine: ' + nonexistentTestAspectSourcePathDirLine

assert nonexistentTestAspectSourcePathDirLine.size() == 3
assert nonexistentTestAspectSourcePathDirLine.get(0).contains( 'src/test/aspect]' )
assert nonexistentTestAspectSourcePathDirLine.get(1).contains( 'src/test/aspect]' )
assert nonexistentTestAspectSourcePathDirLine.get(2).contains( 'src/test/nonexistentTestCompileDirectory]' )

// 3) The properly existing source root *should* be added to the compileSourceRoot list:
//
// [DEBUG] Adding existing aspectSourcePathDir [/Users/lj/Development/Research/AspectJ/aspectj-maven-plugin/target/it/MASPECTJ-110/src/main/trivialaspects] to compileSourceRoots.
//
existingAspectSourcePathDir = logFileLines.findAll {
  line -> line.contains( 'Adding existing aspectSourcePathDir' )
}

println 'Got existingAspectSourcePathDir: ' + existingAspectSourcePathDir

assert existingAspectSourcePathDir.size() == 1
assert existingAspectSourcePathDir.get(0).contains( 'src/main/trivialaspects]' )

// 4) The properly existing test source root *should* be added to the testCompileSourceRoot list:
//
// [DEBUG] Adding existing testAspectSourcePathDir [/Users/lj/Development/Research/AspectJ/aspectj-maven-plugin/target/it/MASPECTJ-110/src/test/trivialtestaspects] to testCompileSourceRoots.
//
existingTestAspectSourcePathDir = logFileLines.findAll {
  line -> line.contains( 'Adding existing testAspectSourcePathDir' )
}

println 'Got existingTestAspectSourcePathDir: ' + existingTestAspectSourcePathDir

assert existingTestAspectSourcePathDir.size() == 1
assert existingTestAspectSourcePathDir.get(0).contains( 'src/test/trivialtestaspects]' )

// 5) Finally, ensure that the compileSourceRoots and testCompileSourceRoots do not contain
//    any non-existent directories
/*
[echo] [compileSourceRoots] [/Users/lj/Development/Research/AspectJ/aspectj-maven-plugin/target/it/MASPECTJ-110/src/main/java, /Users/lj/Development/Research/AspectJ/aspectj-maven-plugin/target/it/MASPECTJ-110/src/main/trivialaspects]
[echo] [testCompileSourceRoots] [/Users/lj/Development/Research/AspectJ/aspectj-maven-plugin/target/it/MASPECTJ-110/src/test/java, /Users/lj/Development/Research/AspectJ/aspectj-maven-plugin/target/it/MASPECTJ-110/src/test/trivialtestaspects]
*/

resultingCompileSourceRoots = logFileLines.find {
  line -> line.contains( '[echo] [compileSourceRoots]' )
}
println "Got resultingCompileSourceRoots: " + resultingCompileSourceRoots
assert resultingCompileSourceRoots.contains( 'src/main/java' )
assert resultingCompileSourceRoots.contains( 'src/main/trivialaspects' )

resultingTestCompileSourceRoots = logFileLines.find {
  line -> line.contains( '[echo] [testCompileSourceRoots]' )
}

println "Got resultingTestCompileSourceRoots: " + resultingTestCompileSourceRoots
assert resultingTestCompileSourceRoots.contains( 'src/test/java' )
assert resultingTestCompileSourceRoots.contains( 'src/test/trivialtestaspects' )