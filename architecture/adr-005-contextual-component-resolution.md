# Architecture Decision Record: Contextual Component Resolution with File Filtering

## Context
When `filesFilter` is set (e.g., only the 5 changed files in a PR), Clarpse only parses those files. The OOP model contains components from those files alone. If a filtered component references a non-filtered component (e.g., `ClassA extends ClassB` where only `ClassA.java` is in the filter), the relation is silently dropped because Clarpse classifies the unresolved reference as an external dependency and `ExtractedRelationships` only examines internal dependencies for specializations and realizations.

A previous approach parsed **all** files regardless of the filter and applied the filter only during diagram generation (commit `fd83e13`, reverted in `1e30573`). This was too slow for large codebases.

## Status: **Accepted**

## Approaches

### 1. Dynamically parse only the files needed for relation context.
After the initial filtered parse, scan component references for unresolved targets. Derive source file names from component unique names, locate them in `ProjectFiles`, parse just those files, and merge the results into the models before `CodeDiff` creation. Controlled by a config toggle (`resolveContextualComponents`).

### 2. Parse all files, filter during rendering.
Always parse the entire codebase regardless of `filesFilter`. The filter is applied only during `StriffDiagramModel` component selection. Simple but expensive for large codebases.

### 3. Do nothing — accept that filtered diagrams lack external context.
Relations to components outside the filter are simply not shown. Users who need full context must not use `filesFilter`.

## Decision
**Approach #1**

**Rationale:** This provides a middle ground between approaches #2 and #3. It avoids the performance cost of parsing an entire codebase (approach #2) while still showing relation context for key components. It is opt-in via `StriffConfig.setResolveContextualComponents(true)` so existing behavior is unchanged.

Key implementation details:
- Resolution happens **after** filtered parsing but **before** `CodeDiff` construction, so the entire downstream pipeline (relationship extraction, change set, gray contextual rendering) works without modification.
- Source files are located by deriving the file name from the component unique name (e.g., `com.sample.ClassB` -> `ClassB.java`) and searching `ProjectFiles.matchingFilesByName()`.
- `ExtractedRelationships` was updated to check both `internalDependencies()` and `externalDependencies()` for specialization and realization references, since Clarpse classifies references to unparsed components as external.
- Only one resolution pass is performed. Transitive references from newly resolved components are not followed, keeping the scope bounded.
