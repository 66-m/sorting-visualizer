<div align="center">

<img src="images/logo.png" alt="Logo" width="140" height="140">

# Sorting Algorithm Visualizer

Watch and listen to 23 sorting algorithms in real time, with 30 visualizations, live metrics and MIDI sound.

[![CI](https://github.com/66-m/sorting-visualizer/actions/workflows/ci.yml/badge.svg)](https://github.com/66-m/sorting-visualizer/actions/workflows/ci.yml)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Latest Release](https://img.shields.io/github/v/release/66-m/sorting-visualizer)](https://github.com/66-m/sorting-visualizer/releases/latest)

[Download](https://github.com/66-m/sorting-visualizer/releases/latest) · [Issues](https://github.com/66-m/sorting-visualizer/issues)

<img src="images/demo.png" alt="Demo screenshot" width="85%">

</div>

## Install

Download the file for your system from the [latest release](https://github.com/66-m/sorting-visualizer/releases/latest).

| System | Download |
|--------|----------|
| Windows 10/11 (x64) | `sorting-visualizer-<version>-windows-x64.msi`, or the portable `…-windows-x64.zip` (run `Sorting Visualizer.exe`) |
| macOS, Apple Silicon | `sorting-visualizer-<version>-macos-arm64.dmg` |
| macOS, Intel | `sorting-visualizer-<version>-macos-x64.dmg` |
| Linux (x64), Ubuntu 24.04+ or Debian 13+ | `sorting-visualizer-<version>-linux-x64.deb` |
| Linux (x64), any distribution | `sorting-visualizer-<version>-linux-x64.tar.gz` (extract it and run `sorting-visualizer/bin/sorting-visualizer`) |

The packages are not signed yet, so your system warns you on first launch:

- **macOS:** right-click the app and choose **Open**, or use *System Settings → Privacy & Security → Open Anyway*.
- **Windows:** if SmartScreen says "Windows protected your PC", choose **More info → Run anyway**.

### Already have Java?

With Java 25 or newer you can also run the [`sorting-visualizer.jar`](https://github.com/66-m/sorting-visualizer/releases/latest/download/sorting-visualizer.jar) on Windows, Linux and Intel Macs. On Apple Silicon, use the `.dmg`.

```sh
java --enable-native-access=ALL-UNNAMED --add-opens=java.desktop/com.sun.media.sound=ALL-UNNAMED -jar sorting-visualizer.jar
```

If the app doesn't start, it shows a message saying why. The two most common causes:

- **"needs Java 25 or newer"**: `java` (or the `.jar` file association) still points to an older Java. Check with `java -version`, or use the installer, which includes Java.
- **"needs OpenGL 3.3"**: update your graphics driver. Remote desktop sessions and virtual machines often don't provide OpenGL 3.3.

### Launch flags

Add these after the program or JAR name. If you pass both `fullscreen` and `portrait`, `fullscreen` wins.

| Flag | Effect |
|------|--------|
| `fullscreen` | Show the visualization in exclusive fullscreen |
| `portrait` | Use a tall window (about 9:16) |
| `--display=N` | Show the visualization on display `N`, counting from 1 (default: 2 if you have several monitors) |
| `--self-check` | Check that the installation works, then exit without opening a window |

## Features

- Classic and unusual sorts (Quick, Merge, Heap, Radix, Bogo and more), one at a time or all in a row with a comparison table
- 2D and 3D visualizations, custom colors and optional MIDI sound
- Arrays of up to 100,000 elements, with live metrics

## Build from source

You need **[JDK 25 or newer](https://adoptium.net/temurin/releases/?version=25)**. Maven is included (`./mvnw`).

```sh
git clone https://github.com/66-m/sorting-visualizer.git
cd sorting-visualizer
./mvnw clean package
./run
```

| Command | What it does |
|---------|----------------|
| `./build` | Build and run the tests |
| `./build skip-tests` | Build without tests |
| `./run` | Start the app (accepts the flags above) |

On Windows, use `build.cmd`, `run.cmd` and `mvnw.cmd`. For packaging and releases, see [Contributing](CONTRIBUTING.md#packaging).

## Docs

- [Contributing](CONTRIBUTING.md)
- [Architecture](docs/architecture.md)
- [Add an algorithm](docs/add-algorithm.md)

## License

Copyright (C) 2020-2026 Marcel Mauel

Licensed under the [GNU Affero General Public License v3.0](https://www.gnu.org/licenses/agpl-3.0.html). See [`LICENSE`](LICENSE).
