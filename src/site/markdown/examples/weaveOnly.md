Weaving classes compiled by another compiler
============================================

Since 1.17.0, `weaveOnly` makes the plugin weave the classes in `weaveDirectories` and `weaveDependencies`
without compiling any source file. Use it when `maven-compiler-plugin` has already compiled the project, for
example with the Lombok annotation processor, which `ajc` cannot run. Without `weaveOnly`, `ajc` compiles the
sources again, bypasses Lombok and fails on the generated members.

The example compiles with `maven-compiler-plugin` first, which runs before the plugin in the same phase because
the compiler plugin is bound by the lifecycle, and then weaves the output directory in place:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <annotationProcessorPaths>
          <path>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.48</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>aspectj-maven-plugin</artifactId>
      <executions>
        <execution>
          <goals>
            <goal>compile</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <weaveOnly>true</weaveOnly>
        <weaveDirectories>
          <weaveDirectory>${project.build.outputDirectory}</weaveDirectory>
        </weaveDirectories>
      </configuration>
    </plugin>
  </plugins>
</build>
```

The property is `aspectj.weaveOnly`. With the default `false` nothing changes.
