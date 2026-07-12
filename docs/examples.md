# Examples Guide

The repository includes three example manifest bundles designed to generate meaningful `.c4` outputs and to populate the static site with realistic content.

## Available Examples

### 1. Hello Platform

File: [`../examples/manifests/hello-platform.yaml`](../examples/manifests/hello-platform.yaml)

Use it when you want a compact, easy-to-read example with:

- one namespace
- frontend, backend and PostgreSQL
- `ConfigMap`, `Secret`, `Service`, `Ingress`

Recommended command:

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse \
  -i examples/manifests/hello-platform.yaml \
  -o ./output/hello-platform
```

### 2. E-commerce With Observability

File: [`../examples/manifests/ecommerce-observability.yaml`](../examples/manifests/ecommerce-observability.yaml)

Use it when you want a richer demo with:

- API, worker and web services
- Redis and PostgreSQL
- Prometheus and Grafana
- grouping labels for higher-level storytelling

Recommended command:

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse \
  -i examples/manifests/ecommerce-observability.yaml \
  -o ./output/ecommerce \
  -g app.kubernetes.io/part-of
```

### 3. Multi-Namespace Gateway

File: [`../examples/manifests/multi-namespace-gateway.yaml`](../examples/manifests/multi-namespace-gateway.yaml)

Use it when you want to explain:

- cross-namespace topology
- shared ingress or edge patterns
- placeholder references for external dependencies

Recommended command:

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse \
  -i examples/manifests/multi-namespace-gateway.yaml \
  -o ./output/gateway \
  --rewrite-missing
```

## Placeholder: Website Cards

These examples are also referenced by the static site. If you later publish screenshots, each example should have:

- one rendered diagram
- one short narrative
- one "copy command" block

Suggested asset names:

- `docs/assets/example-hello-platform.png`
- `docs/assets/example-ecommerce-observability.png`
- `docs/assets/example-multi-namespace-gateway.png`

## Placeholder: Comparison Table

```text
[ comparison table placeholder ]
Columns:
- example
- namespaces
- main workloads
- integrations
- suggested flags
```
