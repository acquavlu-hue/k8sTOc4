package com.k8stoc4.render;

import java.util.Map;
import java.util.Objects;

public final class ImageMapper {
    private static final Map<String, String> typeImages = Map.ofEntries(
        Map.entry("namespace", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/ns.svg"),
        Map.entry("daemonset", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/ds.svg"),
        Map.entry("clusterrole", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/c-role.svg"),
        Map.entry("configmap", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/cm.svg"),
        Map.entry("clusterrolebinding", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/crb.svg"),
        Map.entry("cronjob", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/cronjob.svg"),
        Map.entry("customresourcedefinition", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/crd.svg"),
        Map.entry("deployment", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/deploy.svg"),
        Map.entry("group", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/group.svg"),
        Map.entry("horizontalpodautoscaler", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/hpa.svg"),
        Map.entry("ingress", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/ing.svg"),
        Map.entry("job", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/job.svg"),
        Map.entry("limits", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/limits.svg"),
        Map.entry("networkpolicy", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/netpol.svg"),
        Map.entry("pod", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/pod.svg"),
        Map.entry("persistentvolume", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/pv.svg"),
        Map.entry("persistentvolumeclaim", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/pvc.svg"),
        Map.entry("quota", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/quota.svg"),
        Map.entry("replicaset", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/rs.svg"),
        Map.entry("rolebinding", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/rb.svg"),
        Map.entry("role", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/role.svg"),
        Map.entry("secret", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/secret.svg"),
        Map.entry("service", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/svc.svg"),
        Map.entry("serviceaccount", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/sa.svg"),
        Map.entry("statefulset", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/sts.svg"),
        Map.entry("storageclass", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/sc.svg"),
        Map.entry("user", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/user.svg"),
        Map.entry("volume", "https://github.com/kubernetes/community/raw/refs/heads/main/icons/svg/resources/labeled/vol.svg")
    );

    private ImageMapper() {}

    public static boolean typeHasImage(final String type) {
        return typeImages.containsKey(type);
    }

    public static String getImageUrlForType(final String type) {
        return typeImages.get(Objects.requireNonNull(type));
    }
}
