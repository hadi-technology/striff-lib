# Architecture Decision Record: SPI Extensions for Augmentation and Decoration

## Context
Striff-lib needs a way to extend diagram generation without forcing changes in core.
Use cases include:
* Adding components to the diagram based on external signals (e.g., metrics).
* Attaching extra data to components for downstream rendering.
* Injecting additional PlantUML text in a deterministic way.

These extensions must be optional, stable, and not break deterministic output.

## Status: **Accepted**

## Approaches

### 1. Java SPI (ServiceLoader) extension points
Define small, focused interfaces and discover implementations at runtime via
`ServiceLoader`. Provide deterministic ordering using a stable sort on `order()`
and class name. Allow users to disable augmentation from config.

### 2. Core-only feature flags
Build all extensions into the core library behind feature flags. This keeps
runtime predictable but requires core changes for every new behavior and
prevents external customization.

### 3. Custom plugin registry/injection
Expose an explicit registration API for extensions. This provides strong control
but adds lifecycle complexity and requires additional integration code in all
consumers.

## Decision
**Approach #1: Java SPI**

## Rationale
SPI gives a low-friction extension mechanism that works for both library users
and higher-level services (e.g., striff-api) without changing core. The
deterministic ordering requirement is addressed by sorting providers by
`order()` then class name. Optionality is preserved by allowing the caller to
disable augmenters via configuration.

## Implementation Details
The SPI interfaces are in `com.hadii.striff.spi`:
* `DiagramAugmenter`: runs during model construction and may add components or
  attach metadata via `DiagramComponent.putAugmentation(...)`.
* `ClassDecorator`: injects PlantUML inside class blocks at defined insertion points.
* `DiagramDecorator`: injects PlantUML at the diagram level.

Registration uses `META-INF/services/...` entries:
```
META-INF/services/com.hadii.striff.spi.DiagramAugmenter
META-INF/services/com.hadii.striff.spi.ClassDecorator
META-INF/services/com.hadii.striff.spi.DiagramDecorator
```

Augmenters can be disabled with:
```
new StriffConfig().setEnableAugmenters(false)
```

## Examples

### DiagramAugmenter
Add a synthetic component when a metric threshold is met and attach metadata:
```java
package com.example.striff.spi;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.parse.CodeDiff;
import com.hadii.striff.spi.DiagramAugmenter;

import java.util.Set;

public class MetricsAugmenter implements DiagramAugmenter {
    @Override
    public void augment(CodeDiff diff, Set<DiagramComponent> components) {
        // hypothetical: detect an impacted component name
        String impacted = "com.acme.ServiceA";
        DiagramComponent cmp = new DiagramComponent(impacted, diff.mergedModel());
        cmp.putAugmentation("oop_metrics.delta", 0.42);
        components.add(cmp);
    }

    @Override
    public int order() {
        return 50;
    }
}
```

### ClassDecorator
Inject a banner line inside each class box:
```java
package com.example.striff.spi;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.spi.ClassDecorator;
import com.hadii.striff.spi.ClassInsertionPoint;

import java.util.List;

public class BannerDecorator implements ClassDecorator {
    @Override
    public ClassInsertionPoint insertionPoint() {
        return ClassInsertionPoint.TOP;
    }

    @Override
    public List<String> decorateClass(DiagramComponent component, DiagramDisplay display) {
        return List.of(".. <<critical>> ..");
    }
}
```

### DiagramDecorator
Add a diagram-level legend or skinparam:
```java
package com.example.striff.spi;

import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.spi.DiagramDecorator;

import java.util.List;

public class LegendDecorator implements DiagramDecorator {
    @Override
    public List<String> decorateDiagram(DiagramDisplay display) {
        return List.of("legend left", "  Custom legend", "endlegend");
    }
}
```

### ServiceLoader registration
```
META-INF/services/com.hadii.striff.spi.DiagramAugmenter
com.example.striff.spi.MetricsAugmenter

META-INF/services/com.hadii.striff.spi.ClassDecorator
com.example.striff.spi.BannerDecorator

META-INF/services/com.hadii.striff.spi.DiagramDecorator
com.example.striff.spi.LegendDecorator
```

## Scenarios
* **Metric-driven impact**: add components whose OOP metrics changed even if they
  are not in the change set, then render the metric delta inside class boxes.
* **Compliance overlays**: inject “restricted” labels on classes in certain packages
  without modifying core diagram logic.
* **Security review**: decorate diagrams with a legend and special colors for
  components that interact with external systems.
* **Team conventions**: apply a uniform banner or watermark via a diagram decorator.
* **API extensions**: a service like striff-api can ship its own SPI implementations
  without changing striff-lib.

## Contributor Notes / FAQ
* **Ordering and determinism**: providers are sorted by `order()` then class name.
  If multiple providers interact, keep `order()` gaps and document expectations.
* **ClassLoader behavior**: `ServiceLoader` uses the current context classloader.
  Ensure your runtime sets it correctly if running in containers or custom classloaders.
* **Packaging**: your jar must include `META-INF/services/...` resources. If you
  use a shaded/fat jar, ensure service files are merged (not overwritten).
* **Error handling**: avoid throwing from SPI implementations; errors in
  augmenters/decorators will fail diagram generation.
* **Performance**: augmenters run once per diagram model; keep them fast and avoid
  expensive IO inside `augment(...)` unless cached.
* **Testing**: add isolated tests with test-only ServiceLoader resources (see
  `src/test/java/striff/test/spi`).
* **Opt-out**: users can disable `DiagramAugmenter` execution via
  `StriffConfig.setEnableAugmenters(false)` to restore “core-only” behavior.

## Consequences
* Extensions can be added without modifying core.
* Output remains deterministic across runs.
* Augmenters can be disabled to restore “core-only” behavior.
