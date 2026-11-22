# Mojo's AspectJ Maven Plugin

This plugin weaves AspectJ aspects into your classes using the AspectJ compiler ("ajc").
Typically, aspects are used in one of two ways within your Maven reactors:

- As part of a *Single Project*, implying aspects and code are defined within the same Maven project.
  This is the simplest approach to start out with; feel free to examine the
  "Examples: Single-project AspectJ use" to better understand single-project use.

- As part of a *Multi-module Maven Reactor* where one/some project(s) contains aspects and other
  projects within the Maven reactor contain code using the aspects ("woven by the aspects").
  This is a more complex and powerful approach, best suited when several Maven projects should be woven
  by a common set of aspects. The "Examples: Multi-module AspectJ use" contains a basic walkthrough
  of this approach.

The AspectJ Maven Plugin delegates most of its work to the AspectJ dependencies included within
the plugin. (Refer to <https://www.eclipse.org/aspectj/> for descriptions and documentation about
AspectJ itself).

## Requirements

The plugin requires Maven 3.9.0 or later and Java 8 or later.

## Goals Overview

- [aspectj:compile](./compile-mojo.html) weaves the main classes of the project.
- [aspectj:test-compile](./test-compile-mojo.html) weaves the test classes of the project.
- [aspectj:aspectj-report](./aspectj-report-mojo.html) creates the ajdoc report of the project.
- [aspectj:EclipseAjcMojo](./EclipseAjcMojo-mojo.html) creates the Eclipse AJDT configuration of the project.

See the [usage page](./usage.html) for a first example.
