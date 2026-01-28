# [striff-lib](https://striff.io)
[![maintained-by](https://img.shields.io/badge/Maintained%20by-Hadii%20Technologies-violet.svg)](https://hadii.ca) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.hadii-tech/striff-lib/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.hadii-tech/striff-lib) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/f52c429a0a514abf86d252fe263d7c17)](https://app.codacy.com/gh/hadii-tech/striff-lib/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade) [![codecov](https://codecov.io/gh/hadii-tech/clarpse/branch/master/graph/badge.svg?token=7uf2jQMlH1)](https://codecov.io/gh/hadii-tech/clarpse) [![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-orange.svg)](https://www.gnu.org/licenses/agpl-3.0) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)


### Architectural Diagrams, Made For Code Reviews.
Striffs leverage the basic premise surrounding the utility of line-wise code diffs at an architectural level, and encourage a more natural understanding of code changes through a "top-down" approach which more closely resembles the lens from which the system was designed and intended to be understood. 

![sample_striff](striff.png)

### Getting Started
* Ensure `graphviz` is installed on your system.
* Execute `mvn clean package assembly:single` to  build jar. 
* `StriffAPITest.java` demonstrates how to use this library to generate striff diagrams. 
* If you want to avoid rendering very large diagrams, configure a hard cap (defaults to 120 components) via `StriffConfig.setMaxComponentsPerDiagram(...)`; diagrams over the cap will return metadata only.

### Versions & Requirements
* Java 17 (see `maven.compiler.source/target` in `pom.xml`).
* Maven 3.x.
* Graphviz (`dot`) installed and on PATH.

### Quickstart
Minimal example (full examples in `src/test/java/striff/test/StriffAPITest.java`):

```java
ProjectFiles oldFiles = new ProjectFiles("/path/to/original/code");
ProjectFiles newFiles = new ProjectFiles("/path/to/modified/code");
List<StriffDiagram> striffs = new StriffOperation(
        oldFiles, newFiles, new StriffConfig()).result().diagrams();
writeStriffsToDisk(striffs); // outputs SVGs to /tmp/striffs
```

### Known Limitations & Tuning
* Large codebases can produce very large diagrams; cap the output with
  `StriffConfig.setMaxComponentsPerDiagram(...)` (default is 120).
* If you see performance or memory issues, reduce scope by filtering files
  or increasing the component cap only for smaller modules.

