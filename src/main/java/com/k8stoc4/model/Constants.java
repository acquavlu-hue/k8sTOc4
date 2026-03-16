package com.k8stoc4.model;

import java.util.Set;

public final class Constants {
    public static final String INDENT = "    ";
    public static final String DEFAULT_NAMESPACE = "default";
    public static final String CLUSTER_LEVEL = "cluster";
    public static final String MOUNT_RELATIONSHIP = "mount";
    public static final String ROUTES_TO_RELATIONSHIP = "routes to";
    public static final String ROUTES_HTTP_RELATIONSHIP = "routes HTTP traffic";
    public static final String TECHNOLOGY_TCP_HTTP = "TCP/HTTP";
    public static final String TECHNOLOGY_HTTP = "HTTP";
    public static final String VOLUME_TECHNOLOGY = "volume";
    public static final String CONFIGMAP_TECHNOLOGY = "configmap";
    public static final String SECRET_TECHNOLOGY = "secret";
    public static final String SCALES_RELATIONSHIP = "scales";
    public static final String TECHNOLOGY_HPA = "hpa";
    public static final String PROTECTS_RELATIONSHIP = "protects";
    public static final String TECHNOLOGY_PDB = "pdb";
    public static final String USES_RELATIONSHIP = "uses";
    public static final String TECHNOLOGY_SERVICEACCOUNT = "serviceaccount";
    public static final String POLICY_RELATIONSHIP = "policy";
    public static final String TECHNOLOGY_NETWORKPOLICY = "networkpolicy";
    public static final String BOUNDS_RELATIONSHIP = "bounds";
    public static final String TECHNOLOGY_PV = "pv";
    public static final String TECHNOLOGY_STORAGECLASS = "storageclass";
    public static final String BINDS_RELATIONSHIP = "binds";
    public static final String OWNER_RELATIONSHIP = "controls";
    public static final String K8S_TECHNOLOGY = "k8s";
    public static final String SERVICE2SERVICE_TAG = "service2service";
    public static final String MISSING_TYPE = "missing";

    public static final Set<String> CLUSTER_SCOPED_RESOURCES = Set.of(
        "PersistentVolume",
        "StorageClass",
        "ClusterRole",
        "ClusterRoleBinding",
        "Node",
        "Namespace",
        "VolumeSnapshot",
        "VolumeSnapshotClass",
        "VolumeSnapshotContent",
        "CSIDriver",
        "CSINode",
        "CSIStorageCapacity",
        "MutatingWebhookConfiguration",
        "ValidatingWebhookConfiguration",
        "CustomResourceDefinition",
        "APIService",
        "RuntimeClass",
        "PriorityClass",
        "PodSecurityPolicy",
        "Lease",
        "IngressClass",
        "CertificateSigningRequest"
    );

    public static boolean isClusterScoped(final String kind) {
        return CLUSTER_SCOPED_RESOURCES.contains(kind);
    }

    private Constants() {}
}
