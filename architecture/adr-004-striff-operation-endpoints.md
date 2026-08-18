# Architecture Decision Record: Striff Operation Endpoints

## Context

Striff-lib provides structural diff diagrams for code reviews. The core entry point is `StriffOperation`, which supports three construction modes:

1. **Full Pipeline** - Parse source files, compute diffs, and render diagrams
2. **Incremental** - Reuse a cached base model and parse only changed files
3. **Render-Only** - Re-use an already-computed `CodeDiff` for multiple renders

The library needed to support:
- Multiple render passes with different configurations without re-parsing
- Efficient diagram generation for large codebases
- Incremental parsing for small changes (CI/CD PR workflows)

## Status: **Accepted**

## Approaches

### 1. Single constructor with full pipeline only
Parse source files and render diagrams in a single operation. For additional renders,
repeat the entire pipeline including parsing.

**Pros**: Simple API, no intermediate state management
**Cons**: Expensive re-parsing for each render pass

### 2. Dual constructor approach (full pipeline + render-only)
Provide two constructors:
- Full pipeline: parses source files and produces `CodeDiff` + diagrams
- Render-only: accepts pre-built `CodeDiff` and produces diagrams only

**Pros**: Enables multiple renders without re-parsing, supports background thread processing
**Cons**: Caller must manage intermediate state between passes

### 3. Caching with invalidation
Maintain internal cache of parsed models with automatic invalidation based on file
modification times or content hashes.

**Pros**: Transparent to caller, automatic cache management
**Cons**: Complex cache invalidation logic, memory overhead, stale data risks

## Decision

**Approach #2: Dual constructor approach**

## Rationale

The dual constructor approach provides explicit control over when parsing occurs
while enabling efficient multi-pass rendering. The render-only constructor is
essential for:
- **Multiple diagram styles**: Render same diff with different configurations
- **Performance**: Second render takes ~200-400ms vs ~4-8 seconds for full pipeline
- **Background processing**: Generate base diagram synchronously, then process
  additional renders on background threads

Callers explicitly manage the intermediate `CodeDiff` and `compileFailures` between
passes, avoiding hidden state and making performance characteristics transparent.

## Implementation Details

### Full Pipeline Constructor

```java
public StriffOperation(ProjectFiles originalPFs, ProjectFiles newPFs, StriffConfig config)
    throws IOException, PUMLDrawException, CompileException
```

**Process**:
1. Validates project files and filters languages
2. Parses source files using ClarpseProject (one per language)
3. Generates OOPSourceCodeModel for old and new code
4. Creates CodeDiff (merges models, extracts relationships)
5. Renders diagrams as SVG

**Intermediate State Access**:
- `codeDiff()` - the computed CodeDiff for re-rendering
- `compileFailures()` - parsing warnings/errors

### Render-Only Constructor

```java
public StriffOperation(CodeDiff codeDiff, StriffConfig config, Set<CompileFailure> compileFailures)
    throws IOException, PUMLDrawException
```

**Purpose**: Re-render with different configurations without re-parsing.

**Use Cases**:
- Re-render with different diagram styles or layouts
- Multiple render passes from the same parsed data
- Background thread processing for secondary renders

**Requirements**: Caller must provide valid CodeDiff from prior full-pipeline run

### Incremental Parsing Constructor

```java
public StriffOperation(OOPSourceCodeModel baseModel, ProjectFiles newFiles,
                       Set<String> changedFiles, StriffConfig config)
    throws IOException, PUMLDrawException, CompileException
```

**Purpose**: Reuse a cached base model and parse only changed files.

**Process**:
1. Accepts a cached `OOPSourceCodeModel` from a previous run (typically via `codeDiff().newModel()`)
2. Parses only the files specified in `changedFiles` from the new ProjectFiles
3. Merges the changed components with a copy of the base model to create the "new" state
4. Generates CodeDiff comparing the base model (old) vs updated model (new)

**Use Cases**:
- CI/CD pipelines processing PRs with small changes (1-50 files out of 1000s)
- Iterative development where only a subset of files changes between runs
- Scenarios where the base codebase is parsed once and reused across multiple operations

**Performance**: For a 1000-file codebase with 5 changed files:
- Full pipeline: parses 2000 files (1000 old + 1000 new)
- Incremental: parses 5 files only

**Requirements**:
- Caller must provide a cached base `OOPSourceCodeModel` from a prior full-pipeline run
- Caller must identify which files have changed
- Changed files must exist in the provided `newFiles` ProjectFiles

### Core Components

**CodeDiff**: Represents the merged comparison of two code models

| Property | Description |
|----------|-------------|
| `mergedModel` | Combined old + new components (old-only components preserved) |
| `oldModel` | Snapshot of original codebase |
| `newModel` | Snapshot of updated codebase |
| `changeSet` | Computed differences (components and relations) |
| `relationsMap` | All relationships extracted from merged model |

**Key Optimization**: Extracts relationships **once** on the merged model, then filters
by component names to get old/new relations. This avoids redundant extraction.

**ChangeSet**: Computes differences between two OOPSourceCodeModel instances

| Output | Description |
|-------|-------------|
| `addedComponents` | Components in new but not old |
| `deletedComponents` | Components in old but not new |
| `modifiedComponents` | Components in both with different codeHash |
| `addedRelations` | Relations in new but not old |
| `deletedRelations` | Relations in old but not new |
| `keyRelationsComponents` | Components involved in added/deleted relations |

**RelationsMap**: Immutable map of all relationships in a codebase

- **Source**: ExtractedRelationships (one extraction per CodeDiff)
- **Content**: All component relations (associations, dependencies)
- **Operations**: `filteredRelations(Set<String> componentNames)` for subset filtering

## Examples

### Code Review - Single Comparison

```java
StriffOperation op = new StriffOperation(originalFiles, newFiles, new StriffConfig());
StriffOutput output = op.result();
// output.diagrams() → List<StriffDiagram> with SVG content
```

### Code Review - Multiple Renders

```java
// First pass: full pipeline
StriffOperation op = new StriffOperation(originalFiles, newFiles, config);
StriffOutput baseOutput = op.result();
CodeDiff cachedDiff = op.codeDiff();
Set<CompileFailure> failures = op.compileFailures();

// Second pass: re-render with different configuration
StriffConfig newConfig = new StriffConfig()
    .setLayoutEngine(LayoutEngine.SMETANA)
    .setZoomLevel(2.0);
StriffOperation newOp = new StriffOperation(cachedDiff, newConfig, failures);
StriffOutput newOutput = newOp.result();
```

**Performance**: Second render ~200-400ms (no re-parsing)

### CI/CD Integration - Incremental Parsing

```java
// Initial run: parse full codebase
ProjectFiles originalFiles = loadProjectFiles("base-repo.zip");
ProjectFiles newFiles = loadProjectFiles("feature-repo.zip");
StriffConfig config = new StriffConfig().setLanguages(List.of(Lang.JAVA));
StriffOperation initialOp = new StriffOperation(originalFiles, newFiles, config);

// Cache the new model for subsequent runs
OOPSourceCodeModel baseModel = initialOp.codeDiff().newModel();

// Subsequent PR: parse only changed files
ProjectFiles prFiles = loadProjectFiles("pr-repo.zip");
Set<String> changedFilePaths = getChangedFilesFromGit(); // e.g., ["/src/ClassA.java", "/src/ClassB.java"]

StriffOperation prOp = new StriffOperation(baseModel, prFiles, changedFilePaths, config);
StriffOutput prOutput = prOp.result();
```

**Performance**: For a 1000-file codebase with 5 changed files:
- Full pipeline: ~4000ms (parses 2000 files)
- Incremental: ~200-400ms (parses 5 files)

## Performance Characteristics

### Timing Breakdown (1000-file Java codebase)

| Stage | Time | % |
|-------|------|---|
| File I/O | 200ms | 5% |
| Clarpse parsing | 800ms | 20% |
| Reference classification | 200ms | 5% |
| ExtractedRelationships | 1800ms | 45% |
| ChangeSet computation | 400ms | 10% |
| Model merge + copy | 600ms | 15% |
| **Total** | **4000ms** | **100%** |

### Bottlenecks

1. **ExtractedRelationships** (45% of time) - Single extraction on merged model
   (optimized from original three extractions: old, new, and merged)

2. **Defensive copying** in OOPSourceCodeModel.copyOfComponent() creates deep copies
   on every access

3. **Full parsing** - For large codebases with few changes, full parsing is inefficient.
   Use incremental parsing constructor to parse only changed files.

## Architectural Decisions

### 1. Single Extraction on Merged Model

**Decision**: Extract relationships once on the merged model, then filter by
component names.

**Rationale**:
- Avoids redundant parsing of the same relationships
- Ensures consistent relationship classification
- Simplifies code paths

**Trade-off**: Slightly more memory usage (stores relations for all components)

### 2. Render-Only Constructor

**Decision**: Support re-rendering from pre-built CodeDiff.

**Rationale**:
- Enables multiple renders with different configurations
- Supports background thread processing for secondary renders
- Avoids expensive re-parsing

**Requirements**: Caller must manage CodeDiff and compile failures between passes

### 3. Language Auto-Detection

**Decision**: Automatically detect which languages have files and skip compilation
for others.

**Rationale**:
- Prevents compilation failures for missing language files
- Improves performance by skipping unnecessary work

### 4. Package Compression (v3.8.0)

**Decision**: Compress synthetic intermediate packages at depth > 2.

**Rationale**:
- Deep namespaces (e.g., `com.hadi.striff.diagram`) show as full paths
- Shallow packages (e.g., `tests.test_tutorial`) remain as groupings
- Reduces visual clutter in diagrams

### 5. Incremental Parsing (v3.9.0)

**Decision**: Support incremental parsing via a dedicated constructor.

**Rationale**:
- For CI/CD PR workflows, typically only 1-50 files change out of 1000s
- Parsing only changed files reduces time from ~4000ms to ~200-400ms
- Leverages Clarpse's `pathsToAnalyze` parameter to skip unchanged files
- Enables efficient repeated comparisons against a stable base

**Requirements**:
- Caller must cache the base `OOPSourceCodeModel` from a prior full-pipeline run
- Caller must identify which files have changed (e.g., via git diff)
- Changed files must be present in the `newFiles` ProjectFiles

**Trade-off**: Caller must manage additional state (cached base model + changed file list)

## Scenarios

* **Standard code review**: Single full-pipeline pass with default configuration
* **Style variations**: Multiple renders with different color schemes or layouts
* **Large codebase reviews**: Incremental parsing for PRs affecting small file subsets
* **Cached diff analysis**: Re-render same CodeDiff with filters or focus options
* **Background processing**: Generate base diagram synchronously, then process
  additional renders on background threads

## Consequences

* Multiple render passes are efficient (~200-400ms after initial parse)
* Caller must explicitly manage intermediate state (CodeDiff, compileFailures, base model for incremental)
* No hidden caching or state management complexity
* Clear performance characteristics (parsing vs rendering)
* Supports both synchronous and background rendering workflows
* Incremental parsing provides significant speedup for small change sets (10-20x faster than full pipeline)
