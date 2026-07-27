# Architecture

Versioned overview of the Sorting Algorithm Visualizer desktop app (libGDX rendering).

## Style

- **Monolith**: single Maven module, single JVM process
- **Dual toolkit UI**: libGDX/LWJGL3 canvas (`SortingVisualizerGame` + Screens + `GdxRenderSystem`) + JavaFX Settings (`SettingsFxController` / AtlantaFX Primer Light)
- **Worker thread**: algorithms run in `SortingSessionManager`; draw loop on the libGDX render thread
- **Ports**: `ArrayModel` (working + published snapshot), `DelayContext` / `FrameGate` (pacing + idle fence), `RenderSystem`, `Sound`, catalogs
- **Snapshots**: `SnapshotPublisher` copies working → published after `FrameGate.awaitIdle()`; visuals never write markers

Toolkit coexistence: bootstrap JavaFX with `Platform.startup` **before** `Lwjgl3Application`. Closing Settings (or Ctrl+Q on the canvas) shuts down both windows; canvas Esc cancels a run or focuses Settings.

## Coordinate spaces

| Space | Origin | Y | Used by |
|-------|--------|---|---------|
| **World2D** | bottom-left | up | Bars, graphs, circles, scatters, mosaics |
| **World3D** | scene center | up | boxes / quads / spheres / 3D lines |
| **Overlay** | top-left screen | down | HUD, `drawText`, image remap (`drawImageRemap` / Overlay shader) |

Engine owns cameras; visuals submit geometry in world units (or Overlay for text/pixels). See `.cursor/evals/04-libgdx-architecture/` for the migration history.

## Package map

```text
io.github.compilerstuck
├── control/
│   ├── DesktopLauncher      # main: JavaFX then Lwjgl3Application
│   ├── SortingVisualizerGame # Game; composition root + screen navigation
│   ├── AppContext           # Façade for Settings (+ shutdown handler)
│   ├── screen/              # VisualizerScreen, ResultsScreen
│   ├── catalog/             # AlgorithmCatalog, VisualizationCatalog
│   ├── config/
│   ├── model/               # ArrayController, SnapshotPublisher, FrameGate, session/state
│   ├── render/              # RenderSystem, GdxRenderSystem, GeometryBatch2D, FramePipeline, …
│   │   └── asset/           # AppAssets, ImageRepository, ImageHandle, ImageRemapRenderer
│   ├── shuffle/
│   └── ui/settingsfx/       # JavaFX Settings + headless vm/
├── sortingalgorithms/
├── visual/                  # Visualizations + VisMath + gradient/
└── sound/
```

## Frame pipeline

```mermaid
flowchart TB
  Game[SortingVisualizerGame]
  Game --> VizScreen[VisualizerScreen]
  Game --> ResScreen[ResultsScreen]
  VizScreen --> Begin[renderSystem.beginFrame]
  Begin --> Idle[FrameGate.awaitIdle]
  Idle --> Pub[SnapshotPublisher.publish]
  Pub --> Grant[FrameGate.grant]
  Grant --> Clear[clear]
  Clear --> Viz[viz.render reads published]
  Viz --> EndWorld[endWorld]
  EndWorld --> HUD[HudRenderer watermark + metrics]
  HUD --> End[endFrame]
  Algs[SortingAlgorithm] --> Delay[FrameGateDelayContext]
  Delay --> Gate[FrameGate]
```

## Key collaborators

| Piece | Role |
|-------|------|
| `SortingVisualizerGame` | Composition root: assets, AppContext, screen switch, shutdown |
| `VisualizerScreen` | Idle / setup delay / active sort + HUD; Esc cancels / focuses Settings; Ctrl+Q quits |
| `ResultsScreen` | Comparison table via `ResultsTableRenderer` |
| `AppContext` | Settings façade + snapshot publish + injected shutdown handler |
| `ArrayController` | Working array mutated by the sort worker |
| `SnapshotPublisher` | Copy-on-publish; `publishedView()` for visuals/sound |
| `AppAssets` | Owns FreeType HUD fonts (`flip=true` for Overlay); disposed by composition root |
| `ImageRepository` | Loads/resizes user images → `ImageHandle`; GDX impl uploads source texture once |
| `GdxRenderSystem` | World2D/World3D + Overlay; requires GL30 (`GeometryBatch2D`, instancing, `LineRenderer3D`) |
| `GeometryBatch2D` | Colored rect/circle quads + stroked lines/ellipses (grow-on-demand buffers) |
| `InstanceRenderer3D` | Hardware-instanced boxes / quads / spheres |
| `LineRenderer3D` | World-space 3D line segments (hairline or thick camera-facing quads) |
| `RenderSystem` | Idiomatic draw API: `fillRects` / `fillCircles` / `drawBoxes` / `drawImageRemap` / … |
| `HudRenderer` | Overlay watermark and metrics (after world pass; counters from working controller) |
| `FrameGateDelayContext` | Algorithm pacing; no graphics types |
| `VisualizationCatalog` | All visualizations with size/image constraints |
| `FrameGate` | Steps-per-frame engine + `awaitIdle` publish fence |
| `ConfigurableVisualization` | Optional per-viz settings; Customize dialog drafts then Apply |
| `VisualizationSettingsCodec` | Versioned JSON for clipboard export/import + `visualSettingsById` prefs blob |

## Lifecycle

Closing the Settings window (or **Ctrl+Q** on the canvas) quits both windows. **Esc** on the canvas cancels an active sort, dismisses the results table, or focuses Settings when idle.

Per-visualization appearance: Settings → Customize beside the visualization combo opens a draft dialog (Cancel / Reset / Import / Export / Apply) when the mode has knobs. Applied settings hot-update the live visual and persist under `UserPreferences.visualSettingsById`. Bars, Disparity Graph, Disparity Graph Mirrored, and Horizontal Pyramid intentionally have no Customize panel.

## Build & run

See [README](../README.md). ArchUnit bans `javafx.*` and `com.badlogic.gdx.*` from algorithms/model/shuffle/view-models; visuals must not depend on `Pixmap` / `Gdx` / `gdx.files` (image I/O lives in `control.render.asset`).
