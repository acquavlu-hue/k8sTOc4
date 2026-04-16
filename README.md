# k8sToC4

`k8sToC4` is a Java CLI that reads Kubernetes manifests and produces C4-style output files (`spec.c4`, `model.c4`, `view.c4`) that can be used as a base for LikeC4 or Structurizr-style visualization workflows.

The repository now includes:

- product-style documentation in [`docs/`](docs/)
- a simple static project site in [`site/`](site/)
- ready-to-run manifest examples in [`examples/`](examples/)

## What It Does

The CLI parses Kubernetes resources and maps them into a C4-oriented model. Today the project already supports relationships such as:

- `Service -> Pods` via selectors
- `Ingress -> Service` via HTTP routes
- `Pods -> ConfigMap`
- `Pods -> Secret`
- `Pods -> PersistentVolumeClaim`

Supported resource families include:

- `Deployment`
- `StatefulSet`
- `Service`
- `Ingress`
- `ConfigMap`
- `Secret`
- `PersistentVolumeClaim`
- generic fallback resources

## Requirements

- Java 17+
- Maven 3.x

## Build

```bash
mvn -B -DskipTests=false package
```

The main artifact is generated in `target/`, for example:

```bash
target/k8stoc4-cli-1.0-SNAPSHOT.jar
```

The build also creates an executable wrapper named:

```bash
target/k8sToC4
```

## Usage

Parse a local manifest file:

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse -i examples/manifests/hello-platform.yaml -o ./output/hello-platform
```

Parse and group components by label:

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse \
  -i examples/manifests/ecommerce-observability.yaml \
  -o ./output/ecommerce \
  -g app.kubernetes.io/part-of
```

Discover from a live cluster:

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar discover -o ./output/live-cluster
```

Watch mode:

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar discover -o ./output/live-cluster -w -r 30
```

## Generated Files

When `-o` is provided, the CLI writes:

- `spec.c4`
- `model.c4`
- `view.c4`

Without `-o`, output is printed to stdout through the console writer path.

## Documentation

Start here:

- [Documentation index](docs/README.md)
- [Getting started](docs/getting-started.md)
- [Examples guide](docs/examples.md)
- [Architecture notes](docs/architecture.md)

## Project Site

Open the static landing page locally:

- [Project site](site/index.html)

It contains a polished overview of the tool, placeholder sections for generated artifacts, and shortcuts to the example manifests.

## Example Manifests

The repository includes starter examples designed to exercise the current CLI behavior:

- [Examples overview](examples/README.md)
- [Hello platform](examples/manifests/hello-platform.yaml)
- [E-commerce with observability](examples/manifests/ecommerce-observability.yaml)
- [Multi-namespace gateway](examples/manifests/multi-namespace-gateway.yaml)

## Code Structure

- `src/main/java/com/k8stoc4/cli`: CLI entrypoints and subcommands
- `src/main/java/com/k8stoc4/controller`: orchestration and input/output handling
- `src/main/java/com/k8stoc4/visitor`: model-building logic
- `src/main/java/com/k8stoc4/model`: internal C4 domain model
- `src/main/java/com/k8stoc4/render`: `.c4` rendering
- `src/test`: unit and integration-style tests

## Notes

- The codebase currently compiles for Java 17, even though some older docs mentioned Java 21.
- The static site is intentionally framework-free so it can be previewed by simply opening the HTML file.
- The documentation contains placeholder sections for screenshots and rendered diagrams, ready to be replaced with real assets once publishing starts.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Apache License 2.0. See [LICENSE](LICENSE).
