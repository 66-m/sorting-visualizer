# Adding an algorithm

## Steps

1. Create a class under `src/main/java/io/github/_66_m/sortingalgorithms/` that extends `SortingAlgorithm`.
2. Implement `sort()` (or the abstract entry your base expects). Use `ArrayModel` for reads/writes/swaps/metrics, call `delay(...)` for animation, and respect `isCancelled()`.
3. Register it in [`AlgorithmCatalog`](../src/main/java/io/github/_66_m/control/catalog/AlgorithmCatalog.java) with a **stable id** (kebab-case), display name, and constructor reference. The catalog is the only place the display name lives; do not set a name in the algorithm class.
4. That's it for tests: `SortingAlgorithmsTest` runs every catalog entry over many sizes and input shapes and requires the output to be exactly the sorted input. `CatalogConsistencyTest` fails if the class is not registered.

## Checklist

- [ ] No `javax.swing` / `processing.core` imports in the algorithm class (ArchUnit)
- [ ] Catalog id and display name unique; id stable (used by user preferences)
- [ ] `./mvnw verify` passes

## Adding a visualization

1. Subclass `Visualization` under `visual/` and implement `update(float delta)`.
2. Draw via `RenderSystem` batch APIs (`fillRects`, `fillCircles`, `drawBoxes`, …). Do not open raw libGDX batches from visuals.
3. Register in [`VisualizationCatalog`](../src/main/java/io/github/_66_m/control/catalog/VisualizationCatalog.java) with id, display name, factory, and `VisualConstraints` if size/image requirements apply.
4. Prefer a smoke path via `VisualizationCatalogSmokeTest` / `FakeRenderSystem`.

## Making a visualization customizable

1. Add a settings record under `control/config/visual/` implementing `VisualizationSettings` (see `HoopsSettings` for the smallest example): an `ID`, range/default constants, a compact constructor that clamps, and a `SCHEMA`:

   ```java
   public static final SettingsSchema<HoopsSettings> SCHEMA =
       SettingsSchema.of(
           ID,
           HoopsSettings.class,
           new SettingsSchema.DoubleParam(
               "radiusScale", "Radius", "LAYOUT",
               RADIUS_SCALE_MIN, RADIUS_SCALE_MAX, DEFAULT_RADIUS_SCALE, "%.3f"));

   public static HoopsSettings defaults() {
     return SCHEMA.defaults();
   }
   ```

   Param keys must match the record components (checked when the schema is created). Params are listed in customize-panel order; `section` groups rows under a heading.
2. Register the schema in `VisualizationSettingsSchemas`.

The JSON codec (saved preferences and clipboard import/export) and the customize panel are generated from the schema; there is no per-visualization codec or panel class to write. If you change an existing schema, run the tests with `-Dgolden.update=true` and review the diff of `src/test/resources/golden/` before committing: those files pin the saved-settings format.

