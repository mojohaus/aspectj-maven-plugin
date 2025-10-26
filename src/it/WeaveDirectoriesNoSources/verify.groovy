import java.io.*;

File targetClasses = new File(basedir, "target/classes");
File clazzFile = new File(targetClasses, "foo/Clazz.class");

// Verify that the weaved classes exist - this proves the plugin ran and didn't skip
if (!clazzFile.exists()) {
    throw new RuntimeException("Clazz.class was not found in target/classes - plugin execution was skipped!");
}

// Check that the build log shows the aspectj plugin ran
File buildLog = new File(basedir, "build.log");
String logContent = buildLog.text;
if (!logContent.contains("aspectj:")) {
    throw new RuntimeException("AspectJ plugin did not run - it was skipped!");
}

System.out.println("SUCCESS: AspectJ plugin executed with empty sources and weaveDirectories configured");
return true;
