# ADR-003: Stable SVG Component Mapping via Post-Processing

## Status
Accepted

## Context
Striff needs a reliable way to map SVG elements produced by PlantUML back to
`DiagramComponent` entries. PlantUML generates `data-qualified-name` attributes
in SVG output, but uses hyphenated IDs (e.g., `com-example-MyClass`) instead of
the original unique names (e.g., `com.example.MyClass`). This mismatch causes
problems for:

- Synthetic modules with colons (`module:cron` becomes `module-cron`)
- Classes with dots (`com.example.MyClass` becomes `com-example-MyClass`)
- Browser extension that needs to match SVG elements against API component IDs

We need SVG `data-qualified-name` attributes to exactly match `DiagramComponent.uniqueName()`
so the browser extension can reliably map between the visual diagram and the API.

## Decision
Use PlantUML's hyphenated IDs internally, then post-process the SVG to replace
`data-qualified-name` attribute values with the original `uniqueName`.

### Implementation Details

1. **PUML ID Generation**: `PUMLHelper.pumlId(uniqueName)` replaces `.` and `:` with `-`
   - `com.example.MyClass` → `com-example-MyClass`
   - `module:cron` → `module-cron`

2. **SVG Post-Processing**: `PUMLDiagram.stripQualifiedPumlIds()` does two things:
   - Strips qualified package prefixes from text content (e.g., `com.example.com-example-MyClass` → `com-example-MyClass`)
   - Replaces `data-qualified-name` attributes with original uniqueNames:
     - `data-qualified-name="com-example-MyClass"` → `data-qualified-name="com.example.MyClass"`
     - `data-qualified-name="module-cron"` → `data-qualified-name="module:cron"`

### Examples

| Component Type | uniqueName | PlantUML ID | Final data-qualified-name |
|----------------|------------|-------------|---------------------------|
| Regular class | `com.example.MyClass` | `com-example-MyClass` | `com.example.MyClass` |
| Nested class | `Outer.Inner` | `Outer-Inner` | `Outer.Inner` |
| Synthetic module | `module:cron` | `module-cron` | `module:cron` |
| Synthetic module | `module:src.main` | `module-src-main` | `module:src.main` |

## Consequences
- SVG `data-qualified-name` attributes exactly match `DiagramComponent.uniqueName()`
- Browser extension can reliably query elements by component ID
- Internal PUML IDs remain stable (hyphenated format)
- Simple string replacement post-processing is fast and predictable
- This is a breaking change for any code that expected PlantUML's hyphenated format

## Notes
- The original ADR proposed Base64 encoding with `cmp_` prefix, but that was never implemented
- The simpler hyphen-replacement approach has proven sufficient for all real-world use cases
