# AGENTS.md

Notes for coding agents (and humans) working on this repo. Standard build/run/test commands
live in `README.md` and `CONTRIBUTING.md`; this file only covers non-obvious caveats.

Sorting Algorithm Visualizer is a single-module Maven **desktop GUI** app (one JVM) that pairs
a JavaFX/AtlantaFX Settings window with a libGDX/LWJGL3 OpenGL canvas.

## Toolchain
- Requires **JDK 25+** (`pom.xml` sets `maven.compiler.release=25`). JDK 21 will NOT compile
  this project.
- Use the Maven wrapper `./mvnw` (the Maven version is pinned in
  `.mvn/wrapper/maven-wrapper.properties`). If the wrapper cannot download Maven in a
  restricted sandbox, a system `mvn` of the same version works identically.

## Lint / test / build / run
- Lint: `./mvnw spotless:check` (Google Java Format); Error Prone runs during compilation.
  Auto-fix formatting with `./mvnw spotless:apply`.
- Full check (matches CI): `./mvnw --batch-mode clean verify`.
- Tests are headless: JavaFX/TestFX use Monocle (`prism.order=sw`, headless glass),
  configured in the `pom.xml` surefire block, so no display is needed for `test`/`verify`.
- Run the app: `./build` (or `./mvnw package`) then `./run` (needs `target/*.jar` +
  `target/dependency/*`). `./run` accepts the README launch flags.

## Running the GUI without a physical display
- The app needs a display. On a headless Linux VM use Xvfb or a VNC desktop (e.g.
  `DISPLAY=:1`); Mesa software OpenGL (llvmpipe) is enough for the libGDX canvas.
- Without an audio/MIDI device the app logs `MidiUnavailableException` /
  `Sound system unavailable, running without audio` and keeps running. This is expected.
- Closing the Settings window (or **Ctrl+Q** on the canvas) quits the whole app. **Esc** on
  the canvas cancels a run or focuses Settings; it does not quit.
- Two windows open (JavaFX Settings + libGDX Visualization).

## Conventions
- Java package root is `io.github._66_m` (Maven groupId `io.github.66-m`; a Java package
  cannot contain `-` or start with a digit).
- The user-preferences node deliberately stays at the legacy path
  `io/github/compilerstuck/sorting-visualizer` so existing users keep their settings. Do not
  change it.
