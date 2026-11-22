Usage
=====

This plugin weaves AspectJ aspects into your classes using the AspectJ compiler ("ajc").
There are typically two ways to package and use aspects in your Maven reactors; either as a standalone
project where aspects and code are defined within the same Maven project - or as part of a multi-module
Maven reactor where one/some project(s) contains aspects and other projects within the Maven reactor
contain code using the aspects ("woven by the aspects").

Below follows a standard single-project usage example.

```xml
<project>
  ...
  <dependencies>
    ...
    <dependency>
      <groupId>org.aspectj</groupId>
      <artifactId>aspectjrt</artifactId>
      <version>1.9.7</version>
    </dependency>
    ...
  </dependencies>
  ...
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>aspectj-maven-plugin</artifactId>
        <version><!-- latest release --></version>
        <executions>
          <execution>
            <goals>
              <goal>compile</goal>       <!-- use this goal to weave all your main classes -->
              <goal>test-compile</goal>  <!-- use this goal to weave all your test classes -->
            </goals>
          </execution>
        </executions>
      </plugin>
      ...
    </plugins>
  </build>
  ...
</project>
```

Upgrading AspectJ
-----------------

The frequency of releases of this plugin is lower than of AspectJ.
If you want to use a different version of AspectJ than the one the plugin uses by default, you also need to upgrade the
aspectjtools within the plugin.

```xml
<project>
  ...
  <properties>
    <aspectj.version>1.9.n</aspectj.version> <!-- specify your version -->
  </properties>

  <dependencies>
    ...
    <dependency>
      <groupId>org.aspectj</groupId>
      <artifactId>aspectjrt</artifactId>
      <version>${aspectj.version}</version>
    </dependency>
    ...
  </dependencies>
  ...
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>aspectj-maven-plugin</artifactId>
        <dependencies>
          <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjtools</artifactId>
            <version>${aspectj.version}</version>
          </dependency>
        </dependencies>
      </plugin>
      ...
    </plugins>
  </build>
  ...
</project>
```

Reproducible builds
-------------------

By default the plugin writes its argument file `builddef.lst` to the output directory, so it ends up in the JAR.
The file holds absolute paths, which differ between machines and make the artifact non-reproducible. Since 1.17.0,
`argumentFileDirectory` moves the file elsewhere; point it at a directory below `target`:

```xml
<configuration>
  <argumentFileDirectory>${project.build.directory}/ajc</argumentFileDirectory>
</configuration>
```

If you configure one directory for both the `compile` and the `test-compile` goal, give them different
`argumentFileName` values, or the second file overwrites the first.

