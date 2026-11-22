# Agent guide for aspectj-maven-plugin

This file is for automated agents and human contributors working on this repository. It records
what the code and the POMs do not make obvious.

## Layout

This is a single-module Maven plugin. The goals are `compile` (`AjcCompileMojo`), `test-compile`
(`AjcTestCompileMojo`), `aspectj-report` (`AjcReportMojo`, ajdoc) and `EclipseAjcMojo`. Both
compile goals share `AbstractAjcCompiler`, which assembles the `ajc` arguments.

- `src/main/resources/aspectj-report*.properties` hold the report texts; the `nl` and `sv`
  translations are checked by the l10n report.
- The site is built from Markdown pages in `src/site/markdown`; `.md.vm` files go through Velocity.

## Build gates

- `spotless-maven-plugin` from mojo-parent runs in `process-sources`. Locally it applies the
  formatting; when the `CI` environment variable is set it only checks. Run `mvn spotless:apply`
  before committing. It formats Java with palantir-java-format, sorts the POM, and also reformats
  Markdown, including this file and the README.
- Main code must use the Java 8 API: the compiler runs with `--release 8` (`mojo.java.target`),
  and the `enforce-bytecode-version` rule rejects dependencies built for a newer JDK. AspectJ
  above 1.8.13 needs JDK 8 at least.
- On JDK 8 the `tools-jar` profile adds `tools.jar` as a system dependency for ajdoc.
- The plugin compiles against the latest Maven 3.9.x (`mavenVersion`) and declares a literal Maven
  3.9.0 as its `<prerequisites>`; `minimalMavenBuildVersion` holds the build to the same floor. Keep
  the two apart: raising `mavenVersion` must not raise the prerequisite. The AspectJ version is the
  `aspectjVersion` property, and the ITs receive it as `@aspectjVersion@`.
- Fields of `AbstractAjcMojo` and `AbstractAjcCompiler` are plugin parameters; renaming one
  changes the plugin's configuration API.

## Unit tests

- They live in `src/test/java`. The mojos are created with `new` and configured through their
  fields or by reflection; there is no Maven project built from a POM.
- `CompilerMojoTestBase` points the mojo at a fixture project under `src/test/projects` and
  deletes its `target` directory after each test. Surefire excludes the Java files of those
  fixture projects; they are compiled by `ajc` in the tests, not by the build.

## Integration tests

- They live in `src/it`, one directory per test, and run with the `run-its` profile. The
  invoker execution installs the plugin into `target/local-repo` itself, so no separate
  `mvn install` is needed. Run a subset with:

  ```shell
  mvn -P run-its verify -Dinvoker.test=Xlint,XhasMember
  ```
- They run in parallel, one per CPU core (`invoker.parallelThreads` 1C), and every forked Maven
  gets `JAVA_TOOL_OPTIONS=-XX:TieredStopAtLevel=1 -XX:+UseSerialGC`. A test must not depend on
  another test's output or on running alone.
- The default goals are `clean test`; most tests set their own `invoker.goals`. Tests expected
  to fail set `invoker.buildResult = failure`. `TOFIX_MASPECTJ-4` documents an unfixed issue
  and is expected to fail.
- `src/it/settings.xml` adds your local repository as a remote, and the fixture POMs take real
  artifacts from Maven Central, so the ITs need network access on a clean repository.
- The post-build hook is `verify.groovy`. Write it as `assert` statements. Invoker fails a script
  whose last expression is anything other than `true` or `null`, so a trailing `list.each { }`
  fails the test; use a `for` loop.

## Continuous integration

- `.github/workflows/maven.yml` calls the MojoHaus `maven-verify.yml` workflow in
  `mojohaus/.github`. It runs `-P "?run-its" verify` on every JDK on Linux, the newest JDK on macOS, and the oldest and newest on Windows
  (`matrix-exclude`), plus a site build.
- Branches pushed to this repository build under the `push` event; the `pull_request` run of the
  same commit is skipped.

## Fixtures and dependencies

- POMs under `src/it` and `src/test/projects` pin old versions on purpose. Do not update them.
- Dependabot updates are monthly and grouped. `plexus-xml` stays below 4 because 4.x depends on
  the Maven 4 XML API.

## Documentation updates

A change that users or contributors can notice ships with the documentation it affects, in the
same pull request. A refactor with no visible effect needs none.

- A new goal, parameter or default: the mojo Javadoc, which becomes the goal page, and the goal
  list in `src/site/markdown/index.md`.
- New or changed usage: an example in `src/site/markdown/usage.md` or under
  `src/site/markdown/examples`. The pages are plain Markdown: in the first plugin declaration of
  the usage page write `<version><!-- latest release --></version>`, and leave the version out of
  other snippets.
- A new `ajc` option: the matching page under `src/site/markdown/ajc_reference`.
- A page added, removed or renamed: the menu in `src/site/site.xml`. Never rename a published
  page; its URL is linked from elsewhere.
- The Maven or Java requirement, or how to build and release: `README.md`.
- A new convention, build gate or test fixture: this file.

Check the result with `mvn site` and open `target/site/index.html`.

## Pull requests

- Squash, merge and rebase merges are all enabled. Release Drafter, through the shared
  MojoHaus `release-drafter.yml` workflow in `mojohaus/.github`, builds the release notes from pull request titles
  and labels, so the title should describe the change for a user.

