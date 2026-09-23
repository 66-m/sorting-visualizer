# Contributing

Thanks for contributing to the Sorting Algorithm Visualizer.

## License

This project is licensed under the **GNU Affero General Public License v3.0**.
By submitting a contribution, you agree that your work is licensed under the same terms. See [`LICENSE`](LICENSE).

## Development setup

- **JDK 25+** (LTS; e.g. [Temurin 25](https://adoptium.net/temurin/releases/?version=25))
- Clone and build:

```sh
./mvnw clean verify
./run
```

See [README](README.md) for scripts (`./build`, `./run`) and [launch flags](README.md#launch-flags).

## Packaging

Cross-platform fat JAR (the `sorting-visualizer.jar` attached to releases):

```sh
./mvnw clean package -Prelease
# or: ./build release
java --enable-native-access=ALL-UNNAMED \
  --add-opens=java.desktop/com.sun.media.sound=ALL-UNNAMED \
  -jar target/sorting-visualizer-jar-with-dependencies.jar
```

`-Prelease` also writes a CycloneDX SBOM to `target/bom.json`, and pulls OpenJFX
`javafx-graphics` natives for **win / linux / mac (x64)** so the Linux-built fat JAR runs on
those desktops (host-only resolution would ship Linux `.so` files alone).

Native app image with a bundled, trimmed Java runtime (for the OS you build on; jpackage cannot
cross-build):

```sh
./mvnw clean verify -Pjpackage -DskipTests
# Linux:   target/jpackage/sorting-visualizer/bin/sorting-visualizer
# Windows: target/jpackage/Sorting Visualizer/Sorting Visualizer.exe
# macOS:   target/jpackage/Sorting Visualizer.app
```

Add `--self-check` to any launcher (or to `java -jar …`) to verify natives, the JavaFX toolkit
and bundled resources without opening a window. Installer options live in
`packaging/jpackage/`; icons in `packaging/icons/`.

## How releases work

Releases are built by [`.github/workflows/release.yml`](.github/workflows/release.yml); nothing
is built or uploaded by hand.

1. Merge the changes you want to ship into `main` (merging alone never publishes anything).
2. Tag the commit and push the tag:

   ```sh
   git tag v2.1.0
   git push origin v2.1.0
   ```

3. The workflow runs the tests, builds the JAR and SBOM, builds and `--self-check`s an app
   image on Linux, Windows, macOS (Apple Silicon) and macOS (Intel), turns each into an installer
   (`.deb`, `.msi`, `.dmg`) plus portable archives, and publishes a GitHub Release with notes
   generated from the merged PRs (grouped by label, see `.github/release.yml`).

The version comes only from the tag (`vMAJOR.MINOR.PATCH`, major ≥ 1): `pom.xml` holds a
`0.0.0-SNAPSHOT` placeholder, so there are no version-bump commits. Pushes to other branches
that touch packaging (`pom.xml`, `packaging/`, the release workflow, …) run the same pipeline as
a dry run without publishing, so packaging changes are proven before they are merged. It can
also be started manually from the Actions tab (dry run).

## Project docs

- [Architecture](docs/architecture.md)
- [Add an algorithm / visualization](docs/add-algorithm.md)

## Pull requests

- Prefer small, focused changes.
- Run `./mvnw verify` before opening a PR (CI also runs it on every pushed branch).
- Describe **why** the change exists and how you tested it (use the PR template).

## Code style

`./mvnw verify` runs Spotless (Google Java Format), Error Prone, the tests and a JaCoCo
coverage check (report in `target/site/jacoco/`). Format locally with:

```sh
./mvnw spotless:apply
```
