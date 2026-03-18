# ADR-003: Stable SVG Component Mapping via Canonical PUML IDs

## Status
Accepted

## Context
Striff needs a reliable way to map SVG elements produced by PlantUML back to
`DiagramComponent` entries. The previous approach relied on PlantUML’s
`data-qualified-name` matching human-readable component names. That broke down
for names containing characters PlantUML normalizes (for example, `:` becomes
`.`) and for names affected by package qualification rules.

For greenfield use, we want a deterministic, reversible mapping that does not
depend on PlantUML’s internal normalization or rendering details, while keeping
`data-qualified-name` human-readable for consumers.

## Decision
We generate a canonical, PUML-safe ID from `DiagramComponent.uniqueName()` and
use that ID in the PUML source. The ID is computed as a URL-safe Base64 encoding
of the UTF-8 unique name, prefixed with `cmp_`.

After SVG generation, we post-process the SVG and rewrite every
`data-qualified-name` attribute back to the original `uniqueName` by decoding
that canonical ID. This keeps SVG attributes human-readable and ensures they map
1:1 to `DiagramComponent.uniqueName()`.

Implementation details:
- `PUMLHelper.pumlId(uniqueName)` returns the canonical ID.
- `PUMLHelper.decodePumlId(pumlId)` reverses it.
- `PUMLDiagram.stripQualifiedPumlIds(...)` replaces qualified IDs and then
  decodes `data-qualified-name` back to `uniqueName`.

## Consequences
- SVG mapping is stable and deterministic; `data-qualified-name` equals
  `DiagramComponent.uniqueName()`.
- Internal PUML IDs are opaque and PUML-safe, reducing the risk of collisions or
  normalization surprises.
- This is a breaking change for any code that previously expected PlantUML’s
  `data-qualified-name` format or hyphenated IDs.

## Notes
If consumers need both representations, we can inject an additional attribute
(e.g., `data-puml-id`) alongside `data-qualified-name` during SVG post-processing.
