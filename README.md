# MojoHaus AspectJ Maven Plugin

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/aspectj-maven-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.codehaus.mojo/aspectj-maven-plugin)
[![GitHub CI](https://github.com/mojohaus/aspectj-maven-plugin/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/mojohaus/aspectj-maven-plugin/actions/workflows/maven.yml?query=branch%3Amaster)

The AspectJ Maven Plugin weaves AspectJ aspects into your classes with the AspectJ compiler `ajc`,
either within a single project or across the modules of a reactor. It requires Maven 3.9.0 or later
and Java 8 or later.

The [plugin site](https://www.mojohaus.org/aspectj-maven-plugin/) has the
[usage page](https://www.mojohaus.org/aspectj-maven-plugin/usage.html), examples for single-project
and multi-module use, and the `ajc` option reference. Contributor and agent notes are in
[AGENTS.md](AGENTS.md).

## Contributing

The first step is to create [an appropriate issue](https://github.com/mojohaus/aspectj-maven-plugin/issues). Describe the problem/idea you have and create an appropriate pull request.

Conventions for the build, the tests and the fixtures are in [AGENTS.md](AGENTS.md).

Test your changes locally using

```shell
mvn clean verify -Pdocs,run-its
```

If you need to contact a committer, please consider getting active [on the mailing lists](https://groups.google.com/forum/#!forum/mojohaus-dev).

## Releasing

* Make sure `gpg-agent` is running.
* Make sure all tests pass `mvn clean verify -Prun-its`
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```

