# Examples

This directory contains Kubernetes manifests that exercise meaningful `k8sToC4` mappings. They are intended as copy/paste starting points for local CLI runs and documentation screenshots.

## Manifests

- `manifests/hello-platform.yaml`: a compact platform with namespace, frontend, API, PostgreSQL, service routing, ConfigMap, Secret, PVC and Ingress.
- `manifests/ecommerce-observability.yaml`: a larger multi-namespace example with frontend, checkout, payments, Redis, Prometheus and Grafana. Labels are set up for `-g app.kubernetes.io/part-of`.
- `manifests/wordpress-stack.yaml`: a familiar WordPress/MySQL stack with storage and secret references.

## Suggested Runs

```bash
target/k8sToC4 parse \
  -i examples/manifests/hello-platform.yaml \
  -o output/hello-platform
```

```bash
target/k8sToC4 parse \
  -i examples/manifests/ecommerce-observability.yaml \
  -o output/ecommerce \
  -g app.kubernetes.io/part-of \
  --rewrite-missing
```

```bash
target/k8sToC4 parse \
  -i examples/manifests/wordpress-stack.yaml \
  -o output/wordpress \
  -g app
```

## Screenshot Slots

Place rendered screenshots in `docs/screenshots/` and update the placeholders in the root README:

- `docs/screenshots/cli-output.png`
- `docs/screenshots/namespace-overview.png`
- `docs/screenshots/label-group-view.png`
