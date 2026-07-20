# AGENTS.md

## Cursor Cloud specific instructions

Sorting Algorithm Visualizer: a single-module Maven **desktop GUI** app (one JVM) that
pairs a JavaFX/AtlantaFX Settings window with a libGDX/LWJGL3 OpenGL canvas. Standard
build/run/test commands live in `README.md` and `CONTRIBUTING.md` — use those; notes
below only cover non-obvious, environment-specific caveats.

### Toolchain
- Requires **JDK 26+** (`pom.xml` sets `maven.compiler.release=26`). JDK 26 (Temurin) is
  preinstalled and wired as the default `/usr/bin/java` via `update-alternatives`, so
  `./mvnw`/`./build`/`./run` pick it up automatically. `JAVA_HOME` is also exported in
  `~/.bashrc`. The stock system JDK is 21, which will NOT compile this project.
- Use the Maven wrapper `./mvnw` (Maven 3.9.9); there is no system `mvn`.

### Lint / test / build / run
- Lint: `./mvnw spotless:check` (Google Java Format) and Error Prone run during `verify`.
  Auto-fix with `./mvnw spotless:apply`.
- Full check (matches CI): `./mvnw --batch-mode clean verify -Dspotless.check.skip=true`.
  CI runs `spotless:check` as a separate step, then `verify` with the check skipped.
- Tests are headless: JavaFX/TestFX use Monocle (`prism.order=sw`, headless glass),
  configured in `pom.xml` surefire — no display needed for `mvn test`/`verify`.
- Run the app: `./build` (or `./mvnw package`) then `./run` (needs `target/*.jar` +
  `target/dependency/*`). `./run` accepts the README launch flags.

### Running the GUI in the cloud VM
- The app needs a display. Use `DISPLAY=:1` (the VNC desktop); Mesa provides software
  OpenGL (llvmpipe), which is enough for the libGDX canvas. No physical GPU is required.
- **Sound is unavailable** in the VM (no ALSA/MIDI device). On startup the app logs
  `MidiUnavailableException` / `Sound system unavailable, running without audio` and
  keeps running — this is expected, not a failure.
- Closing the Settings window or pressing **Esc** on the canvas quits the whole app, so
  avoid Esc during manual testing.
- Two windows open (JavaFX Settings + libGDX canvas). Use Alt+Tab to switch between them;
  they are not always tracked by `wmctrl`.
