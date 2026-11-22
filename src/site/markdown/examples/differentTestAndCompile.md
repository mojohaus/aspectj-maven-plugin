Example of different configurations for compile and test-compile goal
=====================================================================

To have different weaving configurations for the main and test classes
you can do as shown below.

```xml
<project>
  ...
  <dependencies>
    ...
    <dependency>
      <groupId>org.aspectj</groupId>
      <artifactId>aspectjrt</artifactId>
      <version><!-- latest release --></version>
    </dependency>
    ...
  </dependencies>
  ...
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>aspectj-maven-plugin</artifactId>
        <executions>
          <execution>
            <id>compile</id>
            <configuration>
              <ajdtBuildDefFile>build.ajproperties</ajdtBuildDefFile>
            </configuration>
            <goals>
              <goal>compile</goal>
            </goals>
          </execution>
          <execution>
            <id>test-compile</id>
            <configuration>
              <ajdtBuildDefFile>test.ajproperties</ajdtBuildDefFile>
            </configuration>
            <goals>
              <goal>test-compile</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      ...
    </plugins>
  <build>
  ...
</project>
```

