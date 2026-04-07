package com.k8stoc4.render;

import java.util.Map;
import java.util.Objects;

public final class ImageMapper {
    private static final String labeledResourcesPrefix = "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/";
    private static final String infrastructureResourcePrefix = "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/infrastructure_components/labeled/";

    private static final Map<String, String> typeImages = Map.ofEntries(
        Map.entry("namespace", labeledResourcesPrefix + "ns.svg"),
        Map.entry("daemonset", labeledResourcesPrefix + "ds.svg"),
        Map.entry("clusterrole", labeledResourcesPrefix + "c-role.svg"),
        Map.entry("configmap", labeledResourcesPrefix + "cm.svg"),
        Map.entry("clusterrolebinding", labeledResourcesPrefix + "crb.svg"),
        Map.entry("cronjob", labeledResourcesPrefix + "cronjob.svg"),
        Map.entry("customresourcedefinition", labeledResourcesPrefix + "crd.svg"),
        Map.entry("deployment", labeledResourcesPrefix + "deploy.svg"),
        Map.entry("group", labeledResourcesPrefix + "group.svg"),
        Map.entry("horizontalpodautoscaler", labeledResourcesPrefix + "hpa.svg"),
        Map.entry("ingress", labeledResourcesPrefix + "ing.svg"),
        Map.entry("job", labeledResourcesPrefix + "job.svg"),
        Map.entry("limits", labeledResourcesPrefix + "limits.svg"),
        Map.entry("networkpolicy", labeledResourcesPrefix + "netpol.svg"),
        Map.entry("pod", labeledResourcesPrefix + "pod.svg"),
        Map.entry("persistentvolume", labeledResourcesPrefix + "pv.svg"),
        Map.entry("persistentvolumeclaim", labeledResourcesPrefix + "pvc.svg"),
        Map.entry("quota", labeledResourcesPrefix + "quota.svg"),
        Map.entry("replicaset", labeledResourcesPrefix + "rs.svg"),
        Map.entry("rolebinding", labeledResourcesPrefix + "rb.svg"),
        Map.entry("role", labeledResourcesPrefix + "role.svg"),
        Map.entry("secret", labeledResourcesPrefix + "secret.svg"),
        Map.entry("service", labeledResourcesPrefix + "svc.svg"),
        Map.entry("serviceaccount", labeledResourcesPrefix + "sa.svg"),
        Map.entry("statefulset", labeledResourcesPrefix + "sts.svg"),
        Map.entry("storageclass", labeledResourcesPrefix + "sc.svg"),
        Map.entry("user", labeledResourcesPrefix + "user.svg"),
        Map.entry("volume", labeledResourcesPrefix + "vol.svg"),
        Map.entry("node", infrastructureResourcePrefix + "node.svg")
    );

    private ImageMapper() {}

    public static boolean typeHasImage(final String type) {
        return typeImages.containsKey(type);
    }

    public static String getImageUrlForType(final String type) {
        return typeImages.get(Objects.requireNonNull(type));
    }
}
