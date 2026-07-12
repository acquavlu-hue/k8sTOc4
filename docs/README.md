# Documentation

This folder collects the project documentation for `k8sToC4`.

## Read In This Order

1. [Getting started](getting-started.md)
2. [Examples guide](examples.md)
3. [Architecture notes](architecture.md)

## Purpose

The documentation is organized to support three use cases:

- understanding what the CLI does
- trying it quickly with local manifests
- preparing material for a future public website or release page

## Project Overview

`k8sToC4` is a tool that reads Kubernetes manifests and derives a C4-oriented
architecture model from them. The project is not only interested in drawing
what is deployed today, but in extracting a model that is useful for reasoning
about how the system is structured.

The tool is inspired by kube-diagram, but the ambition is broader: move from
resource visualization to architectural derivation, so the generated output can
help teams guide architecture evolution and deployment evolution over time.

## Placeholder Policy

Some sections intentionally contain placeholders such as:

- generated diagram preview
- screenshot placeholder
- expected output snippet

They are there so the repo already has a usable information architecture even before the final branded assets exist.
