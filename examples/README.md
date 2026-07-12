# Example Manifests

This folder contains sample Kubernetes manifests prepared specifically for `k8sToC4`.

## Why They Exist

They serve three purposes:

- give contributors reproducible input files
- provide content for documentation and the static site
- help showcase relationships that the current visitor and renderer already understand

## Included Bundles

- [`manifests/hello-platform.yaml`](manifests/hello-platform.yaml)
- [`manifests/ecommerce-observability.yaml`](manifests/ecommerce-observability.yaml)
- [`manifests/multi-namespace-gateway.yaml`](manifests/multi-namespace-gateway.yaml)

## Quick Commands

```bash
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse -i examples/manifests/hello-platform.yaml -o ./output/hello-platform
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse -i examples/manifests/ecommerce-observability.yaml -o ./output/ecommerce -g app.kubernetes.io/part-of
java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar parse -i examples/manifests/multi-namespace-gateway.yaml -o ./output/gateway --rewrite-missing
```

## Placeholder: Expected Outputs

Add generated excerpts here once you decide which examples should be part of release screenshots.
