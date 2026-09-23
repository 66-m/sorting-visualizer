<div align="center">

<img src="images/logo.png" alt="Logo" width="140" height="140">

# Sorting Algorithm Visualizer

See and hear sorting algorithms in real time — 23 algorithms, 30 visualizations, live metrics, and MIDI audio.

[![CI](https://github.com/66-m/sorting-visualizer/actions/workflows/ci.yml/badge.svg)](https://github.com/66-m/sorting-visualizer/actions/workflows/ci.yml)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Latest Release](https://img.shields.io/github/v/release/66-m/sorting-visualizer)](https://github.com/66-m/sorting-visualizer/releases/latest)

[Download](https://github.com/66-m/sorting-visualizer/releases/latest) · [Issues](https://github.com/66-m/sorting-visualizer/issues)

<img src="images/demo.png" alt="Demo screenshot" width="85%">

</div>

## Install

Download the file for your system from the [latest release](https://github.com/66-m/sorting-visualizer/releases/latest). **No Java install needed**: each package bundles its own runtime.

| System | Download |
|--------|----------|
| Windows 10/11 (x64) | `sorting-visualizer-<version>-windows-x64.msi` (installer) or `…-windows-x64.zip` (portable, run `Sorting Visualizer.exe`) |
| macOS, Apple Silicon | `sorting-visualizer-<version>-macos-arm64.dmg` |
| macOS, Intel | `sorting-visualizer-<version>-macos-x64.dmg` |
| Linux (x64), Ubuntu 24.04+ / Debian 13+ | `sorting-visualizer-<version>-linux-x64.deb` |
| Linux (x64), any distribution | `sorting-visualizer-<version>-linux-x64.tar.gz`: extract, then run `sorting-visualizer/bin/sorting-visualizer` |

The packages are not code-signed yet, so the first launch shows a warning:

- **macOS:** right-click the app and choose **Open** (or *System Settings → Privacy & Security → Open Anyway*).
- **Windows:** SmartScreen may say "Windows protected your PC": choose **More info → Run anyway**.

### Already have Java?

With Java 25 or newer installed, you can also use the cross-platform [`sorting-visualizer.jar`](https://github.com/66-m/sorting-visualizer/releases/latest/download/sorting-visualizer.jar) (Windows, Linux and Intel Macs; on Apple Silicon use the `.dmg`):

```sh
java --enable-native-access=ALL-UNNAMED --add-opens=java.desktop/com.sun.media.sound=ALL-UNNAMED -jar sorting-visualizer.jar
```

### Launch flags

Optional, after the program or JAR name (`fullscreen` wins over `portrait`):

| Flag | Effect |
|------|--------|
| `fullscreen` | Exclusive fullscreen visualization (hides desktop panels) |
| `portrait` | Tall ~9:16 window |
| `--display=N` | Visualization on display `N` (1-based; default is 2 when multiple monitors exist) |
| `--self-check` | Verify the installation (natives, UI toolkit, resources) and exit without opening a window |

## Features

- Classic and niche sorts (Quick, Merge, Heap, Radix, Bogo, …), singly or run-all with a comparison table
- 2D and 3D visualizations, custom colors, optional MIDI sound
- Array sizes up to 100 000; live metrics and CSV export

## Build from source

Requires **[JDK 25+](https://adoptium.net/temurin/releases/?version=25)** (the current LTS); Maven comes with the repo (`./mvnw`).

```sh
git clone https://github.com/66-m/sorting-visualizer.git
cd sorting-visualizer
./mvnw clean package
./run
```

| Command | What it does |
|---------|----------------|
| `./build` | Package with tests |
| `./build skip-tests` | Fast package |
| `./run` | Launch (same flags as above) |

On Windows: `build.cmd`, `run.cmd`, or `mvnw.cmd`. Packaging and how releases are made: [Contributing](CONTRIBUTING.md#packaging).

## Docs

- [Contributing](CONTRIBUTING.md)
- [Architecture](docs/architecture.md)
- [Add an algorithm](docs/add-algorithm.md)

## License

Copyright (C) 2020-2026 Marcel Mauel

Licensed under the [GNU Affero General Public License v3.0](https://www.gnu.org/licenses/agpl-3.0.html). See [`LICENSE`](LICENSE).
