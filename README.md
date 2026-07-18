<div align="center">

<img src="images/logo.png" alt="Logo" width="140" height="140">

# Sorting Algorithm Visualizer

See and hear sorting algorithms in real time — 22 algorithms, 30 visualizations (including 3D), live metrics, and MIDI audio.

[![CI](https://github.com/66-m/sorting-visualizer/actions/workflows/ci.yml/badge.svg)](https://github.com/66-m/sorting-visualizer/actions/workflows/ci.yml)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Latest Release](https://img.shields.io/github/v/release/66-m/sorting-visualizer)](https://github.com/66-m/sorting-visualizer/releases/latest)

[Download](https://github.com/66-m/sorting-visualizer/releases/latest) · [Issues](https://github.com/66-m/sorting-visualizer/issues)

<img src="images/demo.png" alt="Demo screenshot" width="85%">

</div>

## Requirements

- **JDK 25+** — [jdk.java.net/25](https://jdk.java.net/25/)
- A desktop environment with audio (MIDI) optional but recommended

## Quick start

Download [`sorting-visualizer.jar`](https://github.com/66-m/sorting-visualizer/releases/latest/download/sorting-visualizer.jar) from the latest release, then:

```sh
java -jar sorting-visualizer.jar
```

A visualization window and a settings window open together. Press **ESC** to quit.

## Build from source

```sh
git clone https://github.com/66-m/sorting-visualizer.git
cd sorting-visualizer

./mvnw clean package          # build + tests
./run                         # run with dependencies on the classpath
```

Convenience scripts:

| Command | What it does |
|---------|----------------|
| `./build` | Package with tests |
| `./build skip-tests` | Fast package (skips tests) |
| `./build release` | Clean build + fat JAR (`-Prelease`) |
| `./run` | Launch the local build |
| `./run fullscreen` | Launch in fullscreen |
| `./run fullscreen --display=2` | Fullscreen on display 2 (1-based; see below) |
| `./run portrait` | Launch in portrait window size |

Windows: use `build.cmd` / `run.cmd` the same way (e.g. `run.cmd fullscreen`). Or call `mvnw.cmd` directly.

Displays are numbered starting at **1** (Processing / Java order). On Linux you can list them with `xrandr --listmonitors`. Example: laptop primary = `--display=1`, HDMI = `--display=2`.

Release fat JAR (same layout as GitHub Releases):

```sh
./mvnw clean package -Prelease
java -jar target/sorting-visualizer-jar-with-dependencies.jar
```

`-Prelease` also writes a CycloneDX SBOM to `target/bom.json`.

Linux app-image (requires JDK `jpackage`, Linux host):

```sh
./mvnw clean verify -Pjpackage
# output under target/jpackage/sorting-visualizer/
```

Windows/macOS installers: run `jpackage` locally against the fat JAR (not covered by CI).

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

**2D** — bars, scatter (linked), number plot, disparity graphs, pyramids, circles & chords, swirl / phyllotaxis, hoops, mosaic, morphing shell, image vertical/horizontal, and more.

**3D** — sphere, sphere hoops, spheric disparity lines, cube, cubic lines, pyramid, plane, disparity plane / sphere hoops.

### Controls

| Control | Options |
|---------|---------|
| Array | Size up to 20 000; shuffle: random, reverse, almost sorted, sorted |
| Speed | Five animation speed levels |
| Appearance | Gradient presets + custom colors |
| Sound | MIDI tones mapped to values; mute anytime |
| Metrics | Sorted %, comparisons, swaps, main/aux writes, estimated time |
| Session | Cancel mid-run; optional end-of-run comparison table |

## Docs

- [Contributing](CONTRIBUTING.md)
- [Architecture](docs/architecture.md)
- [Add an algorithm](docs/add-algorithm.md)

## License

Copyright (C) 2020–2026 Marcel Mauel

Identities (kept separate on purpose):
- In-app visualizer watermark: **CompilerStuck**
- Copyright: **Marcel Mauel**
- GitHub / Maven (`io.github.66-m`): **66-m**

Licensed under the [GNU Affero General Public License v3.0](https://www.gnu.org/licenses/agpl-3.0.html). See [`LICENSE`](LICENSE).
