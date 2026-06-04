# Contributing to k8sToC4 CLI

We welcome contributions! This document outlines how to propose changes, run tests, and submit patches.

## How to contribute
- Fork the repository.
- Create a feature branch: `git checkout -b feature/YourFeature` or a fix branch: `git checkout -b fix/YourBug`.
- Implement changes with tests when appropriate.
- Run tests locally: `mvn -B -DskipTests=false test` or build: `mvn -B package`.
- Ensure code compiles with Java 17.
- Open a pull request against the main branch with a short, descriptive title.

## Code style and quality
- Use Java 21 features where appropriate.
- Keep changes small and focused per commit.
- Add or update tests to cover new behavior.
- Update the README with any user-facing changes.

## Running locally
- Build and package: `mvn -B -DskipTests=false package`.
- Run the CLI jar (built with the shade plugin): `java -jar target/k8stoc4-cli-1.0-SNAPSHOT.jar --help`.

## Release process (high level)
- Bump version in `pom.xml` as part of a release.
- Optional: publish artifacts to your internal or public Maven repository.

Thank you for your contributions!
