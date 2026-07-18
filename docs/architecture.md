# Architecture

Versioned overview of the Sorting Algorithm Visualizer desktop app (post-P2).

## Style

- **Monolith** — single Maven module, single JVM process
- **Dual UI** — Processing `PApplet` canvas (`MainController`) + Swing settings (`Settings`)
- **Worker thread** — algorithms run in `SortingSessionManager`; draw loop on Processing’s thread
- **Partial hexagonal ports** — `ArrayModel`, `RenderContext` / `ProcessingContext`, `Sound`, catalogs

## Package map

```text
io.github.compilerstuck
├── control/                 # Orchestration, UI, model, config, render ports
│   ├── MainController       # PApplet entry; implements RenderContext
│   ├── AppContext           # Injectable façade for Settings (no static hub for UI ops)
│   ├── catalog/             # AlgorithmCatalog, VisualizationCatalog + descriptors
│   ├── config/              # Constants, DelayStrategy, ShuffleType, Brand, prefs
│   ├── model/               # ArrayController, session/state, FrameGate, cancel
│   ├── render/              # RenderContext / Headless / LoadedImage
│   ├── shuffle/             # Shuffle strategies
│   └── ui/                  # Settings frame + settings/* panels, theme
├── sortingalgorithms/       # SortingAlgorithm base + implementations
├── visual/                  # Visualization subclasses + Marker + gradient/
└── sound/                   # Sound, MidiSys, SilentSound, HeadlessSound
```

## Runtime collaboration

```mermaid
flowchart TB
  subgraph ui [UI]
    Settings[Settings JFrame]
    Main[MainController PApplet]
  end
  subgraph core [Core]
    App[AppContext]
    Array[ArrayController]
    Session[SortingSessionManager]
    Gate[FrameGate]
  end
  subgraph ports [Adapters]
    Viz[Visualization]
    Snd[Sound MidiSys]
    Render[RenderContext]
  end
  Settings --> App
  Main --> App
  App --> Array
  App --> Session
  App --> Gate
  Session --> Algs[SortingAlgorithm]
  Algs --> Array
  Main --> Viz
  Main --> Render
  Viz --> Render
  Algs --> Snd
```

## Key collaborators

| Piece | Role |
|-------|------|
| `AppContext` | Size, speed, step engine, algorithm/visual/sound/gradient accessors for Settings |
| `AlgorithmCatalog` / `VisualizationCatalog` | Stable IDs + factories; Settings builds instances from these |
| `FrameGate` | Optional steps-per-frame engine (`-Dsv.stepEngine=true` or Settings checkbox) |
| `RenderContext` | Processing drawing API without casts in visuals |
| `CancellationToken` | Cooperative cancel from UI → session → algorithms |

## Build & run

See [README](../README.md) and [CONTRIBUTING](../CONTRIBUTING.md). Architecture rules are enforced in tests via ArchUnit (`ArchitectureTest`).
