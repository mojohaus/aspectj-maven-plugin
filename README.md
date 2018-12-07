This is a fork of the aspectj-maven-plugin that has Java 11 support.

# Mojohaus AspectJ-Maven-Plugin

This plugin  weaves AspectJ aspects into your classes using the AspectJ compiler ("ajc").
Typically, aspects are used in one of two ways within your Maven reactors:

  * As part of a Single Project, implying aspects and code are defined within the same Maven project.
    This is the simplest approach to start out with; feel free to examine the
    "Examples: Single-project AspectJ use" to better understand single-project use.

  * As part of a Multi-module Maven Reactor where one/some project(s) contains aspects and other
    projects within the Maven reactor contain code using the aspects ("woven by the aspects").
    This is a more complex and powerful approach, best suited when several Maven projects should be woven
    by a common set of aspects. The "Examples: Multi-module AspectJ use" contains a basic walkthrough
    of this approach.
    
[Plugin documentation - available shortly](http://www.mojohaus.org/aspectj-maven-plugin/)
