# Contributing

## Development Setup

- Java 17 is the supported baseline for local development.
- Graphviz is required for SVG rendering checks when tests exercise PlantUML output.
- Maven is the build entry point for compile, test, and packaging workflows.

## Common Commands

```bash
mvn test -B
mvn -DskipTests compile -B
mvn package -B
```

## Code Style

- Keep public APIs documented with Javadoc.
- Prefer small focused classes over large multi-purpose utilities.
- Follow the existing non-`get*` accessor style used across the project.
- Preserve deterministic output ordering for anything that affects generated diagrams.
- For logging, use SLF4J parameterized messages rather than string concatenation.

## Tests

- Add focused regression tests for rendering changes.
- Prefer SVG-level assertions for visual PlantUML behavior when string-only assertions are too weak.
- Keep GitHub/network-backed tests optional or skipped by default so the suite remains reproducible offline.

## Pull Requests

- Include the problem being solved, the behavioral change, and the verification commands you ran.
- Update the README when public configuration or supported behavior changes.
- Keep SPI changes backward-compatible unless the PR explicitly proposes an API break.
