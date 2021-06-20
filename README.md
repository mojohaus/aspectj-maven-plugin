# Mojohaus AspectJ Maven Plugin

This is the [aspectj-maven-plugin](https://www.mojohaus.org/aspectj-maven-plugin/).

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/aspectj-maven-plugin.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.codehaus.mojo%22%20AND%20a%3A%22aspectj-maven-plugin%22)
[![GitHub CI](https://github.com/mojohaus/aspectj-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/mojohaus/aspectj-maven-plugin/actions/workflows/maven.yml)

## Overview

This plugin weaves AspectJ aspects into your classes using the AspectJ compiler `ajc`.
Typically, aspects are used in one of two ways within your Maven reactors:

  * As part of a Single Project, implying aspects and code are defined within the same Maven project.
    This is the simplest approach to start out with; feel free to examine the
    "Examples: Single-project AspectJ use" to better understand single-project use.

  * As part of a Multi-module Maven Reactor where one/some project(s) contains aspects and other
    projects within the Maven reactor contain code using the aspects ("woven by the aspects").
    This is a more complex and powerful approach, best suited when several Maven projects should be woven
    by a common set of aspects. The "Examples: Multi-module AspectJ use" contains a basic walk-through
    of this approach.

## Contributing

The first step is to create [an appropriate issue](https://github.com/mojohaus/aspectj-maven-plugin/issues). Describe the problem/idea you have and create an appropriate pull request.

Test you changes locally using 

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
