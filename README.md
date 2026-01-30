# [striff-lib](https://striff.io)
[![maintained-by](https://img.shields.io/badge/Maintained%20by-Hadi%20Technologies-violet.svg)](https://hadi.ca) [![Maven Central](https://maven-badges.sml.io/maven-central/io.github.hadi-technology/striff-lib/badge.svg)](https://maven-badges.sml.io/maven-central/io.github.hadi-technology/striff-lib) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/f52c429a0a514abf86d252fe263d7c17)](https://app.codacy.com/gh/hadi-tech/striff-lib/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade) [![codecov](https://codecov.io/gh/hadi-tech/clarpse/branch/master/graph/badge.svg?token=7uf2jQMlH1)](https://codecov.io/gh/hadi-tech/clarpse) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)


### Architectural Diagrams, Made For Code Reviews.
Striffs leverage the basic premise surrounding the utility of line-wise code diffs at an architectural level, and encourage a more natural understanding of code changes through a "top-down" approach which more closely resembles the lens from which the system was designed and intended to be understood. 

![sample_striff](striff.png)

### Getting Started
* Ensure `graphviz` is installed on your system (required for SVG rendering).
* Java 17 and Maven 3.x are required (see `pom.xml`).
* Add the dependency:

```xml
<dependency>
  <groupId>io.github.hadi-technology</groupId>
  <artifactId>striff-lib</artifactId>
  <version>3.0.0</version>
</dependency>
```

* Build from source:

```bash
mvn clean package assembly:single
```

### Quickstart
Minimal example (see `src/test/java/striff/test/model/StriffAPITest.java` for more):

```java
import com.hadi.clarpse.compiler.ProjectFiles;
import com.hadi.striff.StriffConfig;
import com.hadi.striff.StriffOperation;
import com.hadi.striff.diagram.StriffDiagram;

import java.util.List;

ProjectFiles oldFiles = new ProjectFiles("/path/to/original/code");
ProjectFiles newFiles = new ProjectFiles("/path/to/modified/code");
List<StriffDiagram> striffs = new StriffOperation(
        oldFiles, newFiles, new StriffConfig()).result().diagrams();
for (StriffDiagram diagram : striffs) {
  String svg = diagram.svg();               // null if metadata-only
  String compressed = diagram.compressedSVG();
  int size = diagram.size();                // number of components
  var pkgs = diagram.containedPkgs();       // packages included
  var components = diagram.cmps();          // DiagramComponent set
  var relations = diagram.relations();      // RelationsMap
  var changeSet = diagram.changeSet();      // ChangeSet for this diff
}
```

### Configuration

#### Parsing and language support (Clarpse)
Striff uses the [Clarpse](https://github.com/hadi-tech/clarpse) parser under the hood to build the source model from your codebase.
Parsing is performed per language configured in `StriffConfig.setLanguages(...)`.

Supported languages (via Clarpse):
* Java
* TypeScript (Coming Soon)
* Python (Coming Soon)
* C# (Coming Soon)
* Go (Coming Soon)

Parsing failures (e.g., unsupported syntax) are reported by Clarpse and surfaced
through Striff as compile warnings on the output. Striff will still attempt to
return diagrams/metadata when possible.

#### File filters
Limit parsing and diagram generation to a specific file list:

```java
StriffConfig config = new StriffConfig()
        .setFilesFilter(List.of("/src/main/java/com/acme/Foo.java"));
```

Note: the file filter is applied to parsing, so only the filtered files are compiled
and considered for diagram components.

#### Styling and color schemes
Start from an existing scheme and override only what you need:

```java
DiagramColorScheme custom = DiagramColorSchemeOverride
        .from(new LightDiagramColorScheme())
        .setClassFontColor("#123456")
        .setPackageFontName("Courier New");

StriffConfig config = new StriffConfig()
        .setColorScheme(custom);
```

You can also apply a display-only override:

```java
DiagramDisplayOverride displayOverride = new DiagramDisplayOverride()
        .setClassFontColor("#123456");

StriffConfig config = new StriffConfig()
        .setDisplayOverride(displayOverride);
```

#### Metadata-only output
Skip rendering diagrams but keep metadata (components, relations, change set):

```java
StriffConfig config = new StriffConfig()
        .setMetadataOnly(true);
```

You can also cap rendering size; diagrams over the cap return metadata only:

```java
StriffConfig config = new StriffConfig()
        .setMaxComponentsPerDiagram(120);
```

#### Augmentation and decorators (SPI)
Striff supports extension points that can add components or decorate PlantUML output.

* `DiagramAugmenter` runs during model construction and can add components or attach
  metadata to existing components (via `DiagramComponent.putAugmentation(...)`).
* `ClassDecorator` and `DiagramDecorator` run during PlantUML generation and can
  inject extra PUML at specific insertion points.
* Architecture details and examples: `architecture/adr-002-spi-extensions.md`

Register implementations using Java `ServiceLoader`:

```
src/main/resources/META-INF/services/com.hadi.striff.spi.DiagramAugmenter
src/main/resources/META-INF/services/com.hadi.striff.spi.ClassDecorator
src/main/resources/META-INF/services/com.hadi.striff.spi.DiagramDecorator
```

Each file lists your implementation class names (one per line). Order is stable
using the `order()` method on each SPI.

If you have augmenters on the classpath, you can turn them off:

```java
StriffConfig config = new StriffConfig()
        .setEnableAugmenters(false);
```

### Examples
* Library usage: `src/test/java/striff/test/model/StriffAPITest.java`
* API usage: see striff-api tests (e.g., `src/test/java/com/hadi/striff/IntegrationTest.java`)

### Contributing
* Build: `mvn clean package assembly:single`
* Run tests: `mvn test`
* See `src/test/java/` for usage examples and regression tests.
