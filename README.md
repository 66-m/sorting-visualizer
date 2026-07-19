<div align="center">

<img src="images/logo.png" alt="Logo" width="140" height="140">

# Sorting Algorithm Visualizer

See and hear sorting algorithms in real time - 22 algorithms, 30 visualizations on **libGDX**, live metrics, and MIDI audio.

[![CI](https://github.com/66-m/sorting-visualizer/actions/workflows/ci.yml/badge.svg)](https://github.com/66-m/sorting-visualizer/actions/workflows/ci.yml)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Latest Release](https://img.shields.io/github/v/release/66-m/sorting-visualizer)](https://github.com/66-m/sorting-visualizer/releases/latest)

[Download](https://github.com/66-m/sorting-visualizer/releases/latest) · [Issues](https://github.com/66-m/sorting-visualizer/issues)

<img src="images/demo.png" alt="Demo screenshot" width="85%">

</div>

## Quick start

Requires **[JDK 26+](https://jdk.java.net/26/)**.

Download [`sorting-visualizer.jar`](https://github.com/66-m/sorting-visualizer/releases/latest/download/sorting-visualizer.jar) from the latest release, then:

```sh
java --enable-native-access=ALL-UNNAMED \
  --add-opens=java.desktop/com.sun.media.sound=ALL-UNNAMED \
  -jar sorting-visualizer.jar
```

**Settings** is a JavaFX window (AtlantaFX). The visualization canvas is **libGDX** (`GdxRenderSystem`: batched 2D + ModelBatch 3D). Closing Settings or pressing **Esc** on the canvas quits the app.
### Launch flags

Pass any of these after the JAR name (order does not matter; `fullscreen` wins over `portrait`):

| Flag | Effect |
|------|--------|
| `fullscreen` | Fullscreen visualization on the chosen display |
| `portrait` | Tall portrait window (~9:16) |
| `--display=N` | Use display `N` (1-based; default is the primary) |

```sh
java --enable-native-access=ALL-UNNAMED \
  --add-opens=java.desktop/com.sun.media.sound=ALL-UNNAMED \
  -jar sorting-visualizer.jar fullscreen --display=2
```

## Features

### Algorithms (22)

| | | |
|---|---|---|
| Quick Sort (middle / dual pivot) | Merge Sort | Heap Sort |
| Shell Sort | Tim Sort | Insertion Sort |
| Selection / Double Selection | Bubble / Cocktail Shaker | Comb Sort |
| Gnome / Odd-Even / Cycle | Counting / Bucket / Pigeonhole | Radix LSD (base 10) |
| American Flag Sort | Gravity Sort | Bogo Sort |

Run one algorithm, or run a custom selection **in order** and optionally show a comparison table afterward.

### Visualizations (30)

| | | |
|---|---|---|
| Bars | Scatter Plot | Scatter Plot Linked |
| Number Plot | Disparity Graph | Disparity Graph Mirrored |
| Horizontal Pyramid | Color Gradient Graph | Circle |
| Disparity Circle | Disparity Circle Scatter | Disparity Circle Scatter Linked |
| Disparity Chords | Disparity Square Scatter | Swirl Dots |
| Phyllotaxis | Image Vertical | Image Horizontal |
| Hoops | Mosaic Squares | Morphing Shell (3D) |
| Sphere (3D) | Sphere Hoops (3D) | Spheric Disparity Lines (3D) |
| Disparity Sphere Hoops (3D) | Cube (3D) | Cubic Lines (3D) |
| Pyramid (3D) | Plane (3D) | Disparity Plane (3D) |

### Controls

| Control | Options |
|---------|---------|
| Array | Size up to 20 000; shuffle: random, reverse, almost sorted, sorted |
| Sorting | Algorithm picker, run-all with drag-reorder dialog, shuffle type |
| Speed | Five levels (steps per frame) |
| Appearance | Gradient presets + custom colors |
| Visualization | 30 modes; image path with validation for image viz |
| Sound | MIDI tones mapped to values; mute anytime |
| Metrics | Sorted %, comparisons, swaps, main/aux writes, estimated time |
| Session | Cancel mid-run; optional end-of-run comparison table (persisted prefs) |

## Build from source

```sh
git clone https://github.com/66-m/sorting-visualizer.git
cd sorting-visualizer

./mvnw clean package
./run
```

| Command | What it does |
|---------|----------------|
| `./build` | Package with tests |
| `./build skip-tests` | Fast package (skips tests) |
| `./run` | Launch the local build |

`./run` accepts the same [launch flags](#launch-flags) as the JAR. On Windows, use `build.cmd`, `run.cmd`, or `mvnw.cmd`.

For release fat JARs, SBOM, and `jpackage` builds, see [Contributing](CONTRIBUTING.md#packaging).

## Docs

- [Contributing](CONTRIBUTING.md)
- [Architecture](docs/architecture.md)
- [Add an algorithm](docs/add-algorithm.md)

## License

Copyright (C) 2020-2026 Marcel Mauel

Licensed under the [GNU Affero General Public License v3.0](https://www.gnu.org/licenses/agpl-3.0.html). See [`LICENSE`](LICENSE).
