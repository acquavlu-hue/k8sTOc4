# Getting Started

## Project Overview

`k8sToC4` reads Kubernetes manifests and derives a C4-oriented architecture
model from them. Instead of limiting the output to an infrastructure or
topology snapshot, the tool tries to surface an architectural view of the
system: namespaces, workloads, relationships and boundaries that can be reused
in discussions, documentation and planning.

The project is inspired by kube-diagram, but it aims to extend that idea. The
intent is not only to visualize resources, but to derive an architecture model
that can help guide the evolution of both architecture and deployment choices.

## Goal

`k8sToC4` turns Kubernetes manifests into C4-oriented output files:

- `spec.c4`
- `model.c4`
- `view.c4`

These files can then be fed into your visualization flow.

In practice, they are meant to become a working architectural baseline that
helps teams understand what exists today and reason about what should change
next.

## Prerequisites

- Java 17 or newer
- Maven 3.x

## Build The CLI

```bash
mvn -B -DskipTests=false package
```

Expected artifacts:

- `target/k8stoc4-cli-1.0-SNAPSHOT.jar`
- `target/k8sToC4`

## First Run

Use one of the repository examples:

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse \
  -i examples/manifests/hello-platform.yaml \
  -o ./output/hello-platform
```

Generated files:

- `output/hello-platform/spec.c4`
- `output/hello-platform/model.c4`
- `output/hello-platform/view.c4`

## Useful Options

- `-i`, `--input`: input manifest file
- `-o`, `--output`: output directory
- `-n`, `--namespace`: force a default namespace
- `-g`, `--group-by-label`: group components by label
- `--rewrite-missing`: create placeholder entities for missing references
- `-e`, `--exclude-kind`: exclude kinds from generated views

## Example Commands

Group by application label:

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse \
  -i examples/manifests/ecommerce-observability.yaml \
  -o ./output/ecommerce \
  -g app.kubernetes.io/part-of
```

Rewrite missing dependencies:

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse \
  -i examples/manifests/multi-namespace-gateway.yaml \
  -o ./output/gateway \
  --rewrite-missing
```

Read from the cluster:

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar discover -o ./output/live-cluster
```

## Placeholder: Diagram Preview

Use this block later for a rendered preview image or embedded LikeC4 view.

```text
[ diagram preview placeholder ]
Suggested asset path: docs/assets/getting-started-overview.png
Suggested caption: "Namespace overview generated from hello-platform.yaml"
```

## Placeholder: Output Snippet

Use this block to paste a short excerpt from a generated `view.c4`.

```text
[ output snippet placeholder ]
view of hello-platform {
  title 'Namespaces / hello-platform'
  include hello-platform.**
}
```

## Next Steps

- browse the static site in [`../site/index.html`](../site/index.html)
- inspect the example manifests in [`../examples/`](../examples/)
- extend the docs with real screenshots once the visual pipeline is finalized
