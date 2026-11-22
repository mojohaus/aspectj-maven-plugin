Configuring AspectJ report
==========================

Example of ajdoc report creation.

**Note:** Since this report is using `tools.jar` as a system scoped dependency
you will need to make sure that your JAVA_HOME environment variable
is set to a JDK before you can use this report.

```xml
<project>
  ...
  <dependencies>
    ...
    <dependency>
      <groupId>org.aspectj</groupId>
      <artifactId>aspectjrt</artifactId>
      <version><!-- latest release --></version>
      <scope>compile</scope>
    </dependency>
    ...
  </dependencies>
  ...
  <reporting>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>aspectj-maven-plugin</artifactId>
        <configuration>
          <verbose>true</verbose>
          <privateScope>true</privateScope>
          <complianceLevel>1.5</complianceLevel>
          <ajdtBuildDefFile>build.ajproperties</ajdtBuildDefFile>
        </configuration>
        <reportSets>
          <reportSet>
            <reports>
              <report>aspectj-report</report>
            </reports>
          </reportSet>
        </reportSets>
      </plugin>
    </plugins>
  </reporting>
  ...
</project>
```

