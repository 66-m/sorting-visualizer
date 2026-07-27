# Contributing

Thanks for contributing to the Sorting Algorithm Visualizer.

## License

This project is licensed under the **GNU Affero General Public License v3.0**.
By submitting a contribution, you agree that your work is licensed under the same terms. See [`LICENSE`](LICENSE).

## Development setup

- **JDK 26+**
- Clone and build:

```sh
./mvnw clean verify
./run
```

See [README](README.md) for scripts (`./build`, `./run`) and [launch flags](README.md#launch-flags).

## Packaging

Release fat JAR (same layout as GitHub Releases):

```sh
./mvnw clean package -Prelease
# or: ./build release
java --enable-native-access=ALL-UNNAMED \
  --add-opens=java.desktop/com.sun.media.sound=ALL-UNNAMED \
  -jar target/sorting-visualizer-jar-with-dependencies.jar
```

`-Prelease` also writes a CycloneDX SBOM to `target/bom.json`.

Linux app-image (requires JDK `jpackage`, Linux host):

```sh
./mvnw clean verify -Pjpackage
# output under target/jpackage/sorting-visualizer/
```

Windows/macOS installers: run `jpackage` locally against the fat JAR (not covered by CI).

## Project docs

- [Architecture](docs/architecture.md)
- [Add an algorithm / visualization](docs/add-algorithm.md)

## Pull requests

- Prefer small, focused changes.
- Run `./mvnw verify` before opening a PR.
- Describe **why** the change exists and how you tested it (use the PR template).

## Code style

CI runs Spotless (Google Java Format) and Error Prone during `./mvnw verify`. Format locally with:

```sh
./mvnw spotless:apply
```
