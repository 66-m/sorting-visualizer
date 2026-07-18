# Adding an algorithm

## Steps

1. Create a class under `src/main/java/io/github/compilerstuck/sortingalgorithms/` that extends `SortingAlgorithm`.
2. Implement `sort()` (or the abstract entry your base expects). Use `ArrayModel` for reads/writes/swaps/metrics, call `delay(...)` for animation, and respect `isCancelled()`.
3. Register it in [`AlgorithmCatalog`](../src/main/java/io/github/compilerstuck/control/catalog/AlgorithmCatalog.java) with a **stable id** (kebab-case), display name, and constructor reference.
4. Optionally add a unit test under `src/test/java/io/github/compilerstuck/sortingalgorithms/` that sorts a small array headlessly (see existing tests).

## Checklist

- [ ] No `javax.swing` / `processing.core` imports in the algorithm class (ArchUnit)
- [ ] Catalog id unique and stable (used by user preferences)
- [ ] `./mvnw verify` passes

## Adding a visualization

1. Subclass `Visualization` under `visual/`.
2. Draw via `RenderContext` only (no `(PApplet)` casts).
3. Register in [`VisualizationCatalog`](../src/main/java/io/github/compilerstuck/control/catalog/VisualizationCatalog.java) with id, display name, factory, and `VisualConstraints` if size/image requirements apply.
4. Prefer a smoke path via `VisualizationCatalogSmokeTest` / headless render context.
