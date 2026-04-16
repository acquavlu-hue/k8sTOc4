# Architecture Notes

## High-Level Flow

`k8sToC4` follows a straightforward pipeline:

1. read Kubernetes resources from a file or the API server
2. visit each resource and translate it into an internal C4-oriented model
3. enrich the model with inferred relationships
4. render the result into `spec.c4`, `model.c4` and `view.c4`

## Main Packages

### `com.k8stoc4.cli`

Contains the CLI entrypoint and commands:

- `Main`
- `ParseCommand`
- `DiscoverCommand`
- `CommonCommand`

This layer is responsible for command-line parsing and option handling through Picocli.

### `com.k8stoc4.controller`

Coordinates the execution flow:

- selects the resource provider
- builds the visitor
- triggers rendering
- writes output to stdout or files

`K8sToC4Controller` is the main orchestration class.

### `com.k8stoc4.visitor`

Implements the resource-to-model mapping.

Important pieces:

- `C4ModelBuilderVisitor`
- `KubernetesResourceVisitor`
- `VisitorUtils`

This is where Kubernetes-specific logic is translated into namespaces, components and relationships.

### `com.k8stoc4.model`

Contains the internal representation of the diagram domain:

- `C4Model`
- `C4Namespace`
- `C4Component`
- `C4Relationship`
- `C4LabelGroup`

### `com.k8stoc4.render`

Turns the internal model into the textual `.c4` artifacts.

Main class:

- `C4DslRenderer`

## Input Modes

The project currently supports two input strategies:

- file-based input through `FileInputProvider`
- cluster discovery through `KubeApiServerInputProvider`

This separation makes the CLI usable both for local examples and for live cluster snapshots.

## Output Modes

The project currently supports:

- console output via `SystemOutWriter`
- file output via `FileWriter`

When file output is selected, the CLI writes:

- `spec.c4`
- `model.c4`
- `view.c4`

## Relationship Inference

The tool already infers several useful connections, including:

- services targeting workloads through selectors
- ingresses routing to services
- pods consuming config maps
- pods consuming secrets
- pods mounting or referencing persistent storage

This inferred graph is what makes the generated output useful for architecture storytelling instead of raw resource listing.

## Extension Points

The current design is friendly to extension in three places:

- add support for new Kubernetes resource kinds in the visitor layer
- enrich the model with new inferred relationship types
- render new view styles or extra output flavors in the renderer layer

## Placeholder: Future Diagram

```text
[ architecture diagram placeholder ]
Suggested future asset: docs/assets/architecture-flow.png
Suggested blocks:
- CLI
- ResourceProvider
- Visitor
- C4Model
- Renderer
- Output writers
```

## Placeholder: Future Public Docs

If this project grows into a published developer portal, this page can become the source for:

- a "How it works" section on the website
- onboarding diagrams for contributors
- release notes that explain new resource support
