package com.k8stoc4.cli.commands;

import com.k8stoc4.controller.K8sToC4Controller;
import com.k8stoc4.controller.ResourceProvider;
import picocli.CommandLine;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

abstract class CommonCommand {

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "output dir",
            required = false
    )
    protected Optional<String> output;

    @CommandLine.Option(
            names = {"-g","--group-by-label"},
            description = "label key for grouping (e.g. app.kubernetes.io/name, app)",
            required = false
    )
    protected Optional<String> groupByLabel;

    @CommandLine.Option(
            names = {"--rewrite-missing"},
            description = "Whether to create entities for missing referenced objects.",
            defaultValue = "false",
            required = false
    )
    protected boolean rewriteMissing;

    @CommandLine.Option(
            names = {"-e", "--exclude-kind"},
            description = "The kinds to exclude from the views"
    )
    protected List<String> kindExclusions;

    protected K8sToC4Controller controller = null;

    protected CommonCommand() {}

    protected void initController(final ResourceProvider provider, final Optional<String> defaultNamespace) {
        this.controller = new K8sToC4Controller(
            provider,
            defaultNamespace,
            this.groupByLabel,
            this.rewriteMissing,
            new HashSet<>(Objects.requireNonNullElseGet(kindExclusions, List::of))
        );
    }
}
