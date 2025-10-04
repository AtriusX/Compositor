# Compositor

Compositor is an experimental Gradle plugin which exists to simplify the process of defining composite builds in gradle.
The primary goal of this plugin is to function as a development aid.

## Primary Purpose

As a tool, this will allow you to swap out project dependencies defined in your projects with local project equivalents.
This can be extremely beneficial when you are attempting to debug and test changes that need to be made in either a
3rd-party or common library.

### Why Make This?

Previously, if you wanted to test changes that needed to be made in a dependency of your project, you had a few options.

You could make your changes in the corresponding dependency project, then:
* Publish them to the library, and hope they work
* Publish the dependency to `mavenLocal`, and configure your project to pull your dependency from there
* Manually configure dependency substitution in your buildscript

These solutions work already, but they all come with caveats which can hinder developer speed like:
* Requiring the developer to go through the usual deployment and publishing cycle before they can fully test their work
* Requiring edits to the buildscript which may or may not be ideal to keep in the project repository, forcing the 
developer to be mindful to not commit the edits
* Requiring the developer to remember potentially clunky Gradle snippets which are not always in use, or may not be
consistently present
* Clunky composite management when dealing with multiple composites

This plugin solves these issues by introducing a simple, config-based approach to this problem.

### Limitations

While this project can help you configure project composition, there's a couple of caveats to consider.

- This plugin is only meant to function with Gradle projects. If you attempt to composite in a project using another
build system (such as Maven), you likely will not have much success.
- When you composite a project, there isn't any way for us to verify that the replacement project correctly maps to the
active dependency. It'll be your responsibility to ensure you are wiring in the right modules.

It's worth pointing out that these issues are inherent to build composition in general, and there's not really a way
around this with or without this plugin.

## Getting Started

To begin using this plugin, you'll need to add the plugin to your `settings.gradle` or `settings.gradle.kts` file.

**NOTE:** This plugin requires control that supersedes typical project-level plugins, as it performs dependency substitution
under the hood. If you attempt to place the plugin in your typical `build.gradle` or `build.gradle.kts` files, it will not work.

### Kotlin
<!-- x-release-please-start-version -->
```kotlin
// settings.gradle.kts
plugins {
    id("xyz.atrius.compositor") version "0.1.9"
}
```

### Groovy
```groovy
// settings.gradle
plugins {
    id 'xyz.atrius.compositor' version '0.1.9'
}
```
<!-- x-release-please-end -->

#### Additional Note

The plugin contains a second embedded plugin, which will automatically be loaded into your project by this one after it
has been configured. Defining it manually is not necessary. The embedded plugin exists to create the necessary Gradle
tasks described [below](#usage). All other primary logic is handled by the primary plugin.

## Usage

The plugin provides a set of Gradle tasks to help with use of the tool. Either for creating/deleting the composites
configuration, or for enabling/disabling it.

The tasks are as follows:
- `createCompositesConfig`
- `deleteCompositesConfig`
- `enableComposites`
- `disableComposites`

You should begin by running `./gradlew createCompositesConfig` to generate the default configuration.

After that, you'll get a file under your projects `gradle` directory which should look somewhat like this:

```yaml
enabled: true
composites: {}
```

Composites can be defined for your project via scopes under the `composites` property.

```yaml
composites:
  some:
    external:
      dependency: "../some/local/replacement"
```

This will replace the dependency named `some.external:dependency` with the corresponding local project. Be sure to refresh
your Gradle project in your IDE after making these changes, as they may not take effect immediately.

## Alternative Declarations

To help simplify this further, we also support defining dependency mappings in other ways.

### Scope Conjugation

The plugin supports scope conjugation, which can be helpful for dependency groups.

```yaml
composites:
  some.external:
    dependencyA: "../some/local/replacement"
    dependencyB: "../some/other/local/replacement"
```

The plugin will resolve this configuration into two dependency mappings:
- `some.external:dependencyA`: `../some/local/replacement`
- `some.external:dependencyB`: `../some/other/local/replacement`

### Literal Formatting

In some cases, we may want to be a bit clearer about which dependencies are being replaced. To help with this, it's also
entirely possible to simply specify the dependency coordinate in its entirety:

```yaml
composites:
  some.external:dependencyA: "../some/local/replacement"
  some.external:dependencyB: "../some/other/local/replacement"
```

The plugin will resolve these as they're written above.

### Referencing Subprojects

In more complex projects, it might be necessary to reference only a small part of a larger repository. This won't apply
in single-module projects, but it could be useful or even necessary when working with multi-module ones. This can be done
easily by providing a subproject reference to your local dependency path:

```yaml
composites:
  some.external.complex:dependency: "../some/local/project:example"
```

In the configuration above, this replaces `some.external.complex:dependency` with the subproject `example` located in
the `../some/local/project` directory. If no project specifier is provided, it will default to the root project.

### Managing Composites

Managing composites using this tool can be done in either a global or granular manner. At a granular level, you can
simply comment out or remove the dependencies you no longer want composited:

```yaml
composites:
#  some.external:dependency: "../disabled/replacement"
#  other:
#    external:
#      project: "../other/disabled/replacement"
  enabled.external:
    dependency: "../enabled/replacement"
```

Globally, you can either disable the configuration, or delete it entirely. Either of these options can be done manually
or by using the provided Gradle tasks referenced [above](#usage). Re-enabling composites can be done by setting the `enabled`
field in the composites file to `true`, or by re-running the `createCompositesConfig` task if the file was deleted.

### Benefits

Unlike the existing solutions referenced [above](#why-make-this), this solves all the issues described by treating composites
as something managed and entirely separate from the buildscripts. Orchestrating multiple composites is now functionally a
breeze, and there is no longer the worry of having to manage the configuration of these from a place where they can be
accidentally committed. Simply generate the composites configuration, provide your composite mappings, and you're good
to go!

### Supporting The Project