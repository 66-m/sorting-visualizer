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

See [README](README.md) for scripts (`./build`, fullscreen/portrait, release fat JAR).

## Project docs

- [Architecture](docs/architecture.md)
- [Add an algorithm / visualization](docs/add-algorithm.md)

## Pull requests

- Prefer small, focused changes.
- Run `./mvnw verify` before opening a PR.
- Describe **why** the change exists and how you tested it (use the PR template).
- Do not introduce Processing or Swing imports into `sortingalgorithms` / shuffle / model layers.

## Code style

CI runs Spotless (Google Java Format) and Error Prone during `./mvnw verify`. Format locally with:

```sh
./mvnw spotless:apply
```
