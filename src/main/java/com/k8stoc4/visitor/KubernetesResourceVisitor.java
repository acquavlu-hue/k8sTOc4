package com.k8stoc4.visitor;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;

public interface KubernetesResourceVisitor {
    void visit(Pod pod);
    void visit(Deployment deployment);
    void visit(ReplicaSet replicaSet);
    void visit(StatefulSet statefulSet);
    void visit(Service service);
    void visit(Ingress ingress);
    void visit(io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress ingress);
    void visit(io.fabric8.kubernetes.api.model.extensions.Ingress ingress);
    void visit(HasMetadata resource); // fallback per altri tipi
}
