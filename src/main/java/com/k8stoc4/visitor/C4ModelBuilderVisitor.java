package com.k8stoc4.visitor;

import com.k8stoc4.presenter.PresenterUtils;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.autoscaling.v2.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicy;
import io.fabric8.kubernetes.api.model.policy.v1.PodDisruptionBudget;
import io.fabric8.kubernetes.api.model.storage.StorageClass;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.k8stoc4.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.k8stoc4.visitor.VisitorUtils.containerMatchesSelector;

@Slf4j
@Getter
public class C4ModelBuilderVisitor implements KubernetesResourceVisitor {

    private final C4Model model = new C4Model();
    private String defaultNS = Constants.DEFAULT_NAMESPACE;

    private C4ModelBuilderVisitor(Optional<String> defaultNs) {
        defaultNs.ifPresent(s -> this.defaultNS = s);
    }

    public static class Builder {
        private Optional<String> defaultNamespace = Optional.empty();

        public Builder setDefaultNamespace(final Optional<String> defaultNamespace) {
            this.defaultNamespace = defaultNamespace;
            return this;
        }

        public C4ModelBuilderVisitor build() {
            return new C4ModelBuilderVisitor(defaultNamespace);
        }
    }

    public void addAllRelationships() {
        addServiceRelationships();
        addServiceToServiceRelationships();
        addHPARelationships();
        addPDBRelationships();
        addServiceAccountRelationships();
        addNetworkPolicyRelationships();
        addPVPVCRelationships();
        addStorageClassRelationships();
        addRBACRelationships();
    }

    public void groupComponentsByLabel(String labelKey) {
        for (C4Namespace namespace : model.getNamespaces().values()) {
            for (C4Component component : new HashSet<>(namespace.getComponents())) {
                String labelValue = component.getLabels().get(labelKey);
                if (labelValue != null && !labelValue.isEmpty()) {
                    C4LabelGroup group = namespace.getOrCreateLabelGroup(labelKey, labelValue);
                    group.addComponents(component);
                    namespace.removeComponent(component);
                }
            }
        }
    }

    @Override
    public void visit(StatefulSet statefulSet) {
        model.getSpecifications().add(statefulSet.getKind().toLowerCase());
        String ns = Optional.ofNullable(statefulSet.getMetadata().getNamespace()).orElse(defaultNS);
        C4Namespace namespace = getOrCreateSystem(ns);

        C4Component component = new C4Component(
                statefulSet,
                defaultNS.equals(Constants.DEFAULT_NAMESPACE)? statefulSet.getMetadata().getNamespace():defaultNS,
                statefulSet.getMetadata().getName(),
                statefulSet.getKind());

        processPodSpec(namespace, component, statefulSet.getSpec().getTemplate().getSpec(),
                statefulSet.getSpec().getTemplate().getMetadata().getLabels());

        namespace.addComponents(component);
    }

    @Override
    public void visit(ReplicaSet replicaSet) {
        model.getSpecifications().add(replicaSet.getKind().toLowerCase());
        String ns = Optional.ofNullable(replicaSet.getMetadata().getNamespace()).orElse(defaultNS);
        C4Namespace namespace = getOrCreateSystem(ns);

        C4Component component = new C4Component(
                replicaSet,
                defaultNS.equals(Constants.DEFAULT_NAMESPACE)? replicaSet.getMetadata().getNamespace():defaultNS,
                replicaSet.getMetadata().getName(),
                replicaSet.getKind());

        processPodSpec(namespace, component, replicaSet.getSpec().getTemplate().getSpec(),
                replicaSet.getSpec().getTemplate().getMetadata().getLabels());

        namespace.addComponents(component);
        addOwnerRelationship(namespace, component);
    }

    @Override
    public void visit(Pod pod) {
        model.getSpecifications().add(pod.getKind().toLowerCase());
        String ns = Optional.ofNullable(pod.getMetadata().getNamespace()).orElse(defaultNS);
        C4Namespace namespace = getOrCreateSystem(ns);

        C4Component component = new C4Component(
                pod,
                pod.getMetadata().getNamespace(),
                pod.getMetadata().getName(),
                pod.getKind());

        processPodSpec(namespace, component, pod.getSpec(),
                pod.getMetadata().getLabels());

        namespace.addComponents(component);
        addOwnerRelationship(namespace, component);
        if (pod.getSpec().getNodeName() != null) {
            C4Relationship rel = new C4Relationship(
                    "node_" + PresenterUtils.sanitizeComponentId(pod.getSpec().getNodeName()),
                    component.getNamespace() + "." + component.getId(),
                    Constants.OWNER_RELATIONSHIP,
                    Constants.K8S_TECHNOLOGY
            );
            namespace.addRelationship(rel);
        }
    }

    @Override
    public void visit(Deployment deployment) {
        model.getSpecifications().add(deployment.getKind().toLowerCase());
        String ns = Optional.ofNullable(deployment.getMetadata().getNamespace()).orElse(defaultNS);
        C4Namespace namespace = getOrCreateSystem(ns);

        C4Component component = new C4Component(
                deployment,
                defaultNS.equals(Constants.DEFAULT_NAMESPACE)? deployment.getMetadata().getNamespace():defaultNS,
                deployment.getMetadata().getName(),
                deployment.getKind());

        processPodSpec(namespace, component, deployment.getSpec().getTemplate().getSpec(),
                deployment.getSpec().getTemplate().getMetadata().getLabels());

        namespace.addComponents(component);
    }

    @Override
    public void visit(Service svc) {
        model.getSpecifications().add(svc.getKind().toLowerCase());
        String ns = Optional.ofNullable(svc.getMetadata().getNamespace()).orElse(defaultNS);
        C4Namespace namespace = getOrCreateSystem(ns);

        C4Component service = new C4Component(svc, defaultNS.equals(Constants.DEFAULT_NAMESPACE)? svc.getMetadata().getNamespace():defaultNS,
                svc.getMetadata().getName(), svc.getKind());
        namespace.addComponents(service);
    }

    @Override
    public void visit(Ingress ing) {
        model.getSpecifications().add(ing.getKind().toLowerCase());
        String ns = Optional.ofNullable(ing.getMetadata().getNamespace()).orElse(defaultNS);
        C4Namespace namespace = getOrCreateSystem(ns);
        C4Component ingress =
                new C4Component(ing,
                        defaultNS.equals(Constants.DEFAULT_NAMESPACE)? ing.getMetadata().getNamespace():defaultNS,
                        ing.getMetadata().getName(),
                        ing.getKind());
        namespace.addComponents(ingress);

        for (IngressRule rule : ing.getSpec().getRules()) {
            rule.getHttp().getPaths().forEach(path -> {
                String svcName = path.getBackend().getService().getName();
                namespace.addRelationship(new C4Relationship(
                        ingress.getNamespace()+"."+ingress.getId(),
                        ingress.getNamespace()+".service_"+svcName,
                        Constants.ROUTES_HTTP_RELATIONSHIP,
                        Constants.TECHNOLOGY_HTTP
                ));
            });
        }
    }

    @Override
    public void visit(io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress ing) {
        model.getSpecifications().add(ing.getKind().toLowerCase());
        String ns = Optional.ofNullable(ing.getMetadata().getNamespace()).orElse(defaultNS);
        C4Namespace namespace = getOrCreateSystem(ns);
        C4Component ingress = new C4Component(ing, defaultNS.equals(Constants.DEFAULT_NAMESPACE)? ing.getMetadata().getNamespace():defaultNS,
                ing.getMetadata().getName(), ing.getKind());
        namespace.addComponents(ingress);

        for (io.fabric8.kubernetes.api.model.networking.v1beta1.IngressRule rule : ing.getSpec().getRules()) {
            rule.getHttp().getPaths().forEach(path -> {
                String svcName = path.getBackend().getServiceName();
                namespace.addRelationship(new C4Relationship(
                        ingress.getNamespace() + "." + ingress.getId(),
                        ingress.getNamespace() + ".service_" + svcName,
                        Constants.ROUTES_HTTP_RELATIONSHIP,
                        Constants.TECHNOLOGY_HTTP
                ));
            });
        }
    }

    @Override
    public void visit(io.fabric8.kubernetes.api.model.extensions.Ingress ing) {
        model.getSpecifications().add(ing.getKind().toLowerCase());
        String ns = Optional.ofNullable(ing.getMetadata().getNamespace()).orElse(defaultNS);
        C4Namespace namespace = getOrCreateSystem(ns);
        C4Component ingress = new C4Component(ing, ing.getMetadata().getNamespace(),
                ing.getMetadata().getName(), ing.getKind());
        ingress.setDescription(ing.getSpec().getRules().get(0).getHost());
        namespace.addComponents(ingress);

        for (io.fabric8.kubernetes.api.model.extensions.IngressRule rule : ing.getSpec().getRules()) {
            rule.getHttp().getPaths().forEach(path -> {
                String svcName = path.getBackend().getServiceName();
                namespace.addRelationship(new C4Relationship(
                        ingress.getNamespace() + "." + ingress.getId(),
                        ingress.getNamespace() + ".service_" + svcName,
                        Constants.ROUTES_HTTP_RELATIONSHIP,
                        Constants.TECHNOLOGY_HTTP
                ));
            });
        }
    }

    @Override
    public void visit(HasMetadata resource) {
        model.getSpecifications().add(resource.getKind().toLowerCase());
        C4Component component = new C4Component(resource, resource.getMetadata().getNamespace(), resource.getMetadata().getName(), resource.getKind());
        
        if (isClusterScopedResource(resource)) {
            component.setNamespace(Constants.CLUSTER_LEVEL);
            model.addClusterScopedComponent(component);
        } else {
            String ns = Optional.ofNullable(resource.getMetadata().getNamespace()).orElse(defaultNS);
            C4Namespace system = getOrCreateSystem(ns);
            system.addComponents(component);
        }
    }

    private C4Namespace getOrCreateSystem(String ns) {
        return model.getNamespaces().computeIfAbsent(ns, C4Namespace::new);
    }

    private boolean isClusterScopedResource(HasMetadata resource) {
        return Constants.isClusterScoped(resource.getKind());
    }

    private void addServiceToServiceRelationships() {

        model.getNamespaces().values().forEach(namespace -> {
            Map<String, C4Component> servicesByFqdn =
                    model.getComponentsByKind(namespace.getName(), "service").stream()
                            .collect(Collectors.toMap(
                                    s -> s.getName() + "." + s.getNamespace(),
                                    Function.identity()
                            ));
            model.getComponentsByKind(namespace.getName(), "deployment").forEach(component -> {

                Deployment deployment = (Deployment) component.getResource();
                deployment.getSpec()
                        .getTemplate()
                        .getSpec()
                        .getContainers()
                        .forEach(container -> {
                            if (container.getEnv() == null) return;
                            container.getEnv().stream()
                                    .map(EnvVar::getValue)
                                    .filter(this::isHttpUrl)
                                    .forEach(value ->
                                            servicesByFqdn.forEach((fqdn, service) -> {
                                                if (value.contains(fqdn)) {
                                                    namespace.addRelationship(
                                                            buildRelationship(component, service,"#service2Service")
                                                    );
                                                }
                                            })
                                    );
                        });
            });
        });
    }


    private C4Relationship buildRelationship(C4Component source, C4Component target, String tag) {
        return new C4Relationship(
                source.getNamespace() + "." + source.getId(),
                target.getNamespace() + "." + target.getId(),
                Constants.ROUTES_TO_RELATIONSHIP,
                Constants.TECHNOLOGY_TCP_HTTP,
                tag
        );
    }

    private boolean isHttpUrl(String value) {
        return value != null &&
                (value.startsWith("http://") || value.startsWith("https://"));
    }

    private void addServiceRelationships() {
        for (C4Namespace namespace : model.getNamespaces().values()) {
            for (C4Component component : namespace.getComponents()) {
                if (component.getKind().equalsIgnoreCase("service")) {
                    Map<String, String> selector = ((Service)component.getResource()).getSpec().getSelector();

                    if (selector != null && !selector.isEmpty()) {
                        for (C4Component targetComp : namespace.getComponents()) {
                            if (containerMatchesSelector(targetComp, selector)) {
                                C4Relationship rel = new C4Relationship(
                                        component.getNamespace() + "." + component.getId(),
                                        targetComp.getNamespace() + "." + targetComp.getId(),
                                        Constants.ROUTES_TO_RELATIONSHIP,
                                        Constants.TECHNOLOGY_TCP_HTTP
                                );
                                namespace.addRelationship(rel);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addHPARelationships() {
        for (C4Namespace namespace : model.getNamespaces().values()) {
            for (C4Component component : namespace.getComponents()) {
                if (component.getResource() instanceof HorizontalPodAutoscaler hpa) {
                    String scaleTargetName = hpa.getSpec().getScaleTargetRef().getName();
                    String scaleTargetKind = hpa.getSpec().getScaleTargetRef().getKind();

                    for (C4Component targetComp : namespace.getComponents()) {
                        if (targetComp.getName().equals(scaleTargetName) &&
                                targetComp.getKind().equalsIgnoreCase(scaleTargetKind)) {
                            C4Relationship rel = new C4Relationship(
                                    component.getNamespace() + "." + component.getId(),
                                    targetComp.getNamespace() + "." + targetComp.getId(),
                                    Constants.SCALES_RELATIONSHIP,
                                    Constants.TECHNOLOGY_HPA
                            );
                            namespace.addRelationship(rel);
                        }
                    }
                }
            }
        }
    }

    private void addPDBRelationships() {
        for (C4Namespace namespace : model.getNamespaces().values()) {
            for (C4Component component : namespace.getComponents()) {
                if (component.getResource() instanceof PodDisruptionBudget pdb) {
                    Map<String, String> selector = pdb.getSpec().getSelector() != null
                            ? pdb.getSpec().getSelector().getMatchLabels()
                            : null;

                    if (selector != null && !selector.isEmpty()) {
                        for (C4Component targetComp : namespace.getComponents()) {
                            if (containerMatchesSelector(targetComp, selector)) {
                                C4Relationship rel = new C4Relationship(
                                        component.getNamespace() + "." + component.getId(),
                                        targetComp.getNamespace() + "." + targetComp.getId(),
                                        Constants.PROTECTS_RELATIONSHIP,
                                        Constants.TECHNOLOGY_PDB
                                );
                                namespace.addRelationship(rel);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addServiceAccountRelationships() {
        for (C4Namespace namespace : model.getNamespaces().values()) {
            for (C4Component component : namespace.getComponents()) {
                if (component.getResource() instanceof ServiceAccount sa) {
                    String saName = sa.getMetadata().getName();

                    for (C4Component targetComp : namespace.getComponents()) {
                        HasMetadata resource = targetComp.getResource();
                        if (resource instanceof Deployment d && d.getSpec().getTemplate().getSpec().getServiceAccountName() != null) {
                            if (d.getSpec().getTemplate().getSpec().getServiceAccountName().equals(saName)) {
                                C4Relationship rel = new C4Relationship(
                                        targetComp.getNamespace() + "." + targetComp.getId(),
                                        component.getNamespace() + "." + component.getId(),
                                        Constants.USES_RELATIONSHIP,
                                        Constants.TECHNOLOGY_SERVICEACCOUNT
                                );
                                namespace.addRelationship(rel);
                            }
                        }
                        if (resource instanceof StatefulSet s && s.getSpec().getTemplate().getSpec().getServiceAccountName() != null) {
                            if (s.getSpec().getTemplate().getSpec().getServiceAccountName().equals(saName)) {
                                C4Relationship rel = new C4Relationship(
                                        targetComp.getNamespace() + "." + targetComp.getId(),
                                        component.getNamespace() + "." + component.getId(),
                                        Constants.USES_RELATIONSHIP,
                                        Constants.TECHNOLOGY_SERVICEACCOUNT
                                );
                                namespace.addRelationship(rel);
                            }
                        }
                        if (resource instanceof Pod p && p.getSpec().getServiceAccountName() != null) {
                            if (p.getSpec().getServiceAccountName().equals(saName)) {
                                C4Relationship rel = new C4Relationship(
                                        targetComp.getNamespace() + "." + targetComp.getId(),
                                        component.getNamespace() + "." + component.getId(),
                                        Constants.USES_RELATIONSHIP,
                                        Constants.TECHNOLOGY_SERVICEACCOUNT
                                );
                                namespace.addRelationship(rel);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addNetworkPolicyRelationships() {
        for (C4Namespace namespace : model.getNamespaces().values()) {
            for (C4Component component : namespace.getComponents()) {
                if (component.getResource() instanceof NetworkPolicy np) {
                    String source = component.getNamespace() + "." + component.getId();

                    if (np.getSpec().getPodSelector() != null && np.getSpec().getPodSelector().getMatchLabels() != null) {
                        Map<String, String> selector = np.getSpec().getPodSelector().getMatchLabels();

                        for (C4Component targetComp : namespace.getComponents()) {
                            if (containerMatchesSelector(targetComp, selector)) {
                                C4Relationship rel = new C4Relationship(
                                        source,
                                        targetComp.getNamespace() + "." + targetComp.getId(),
                                        Constants.POLICY_RELATIONSHIP,
                                        Constants.TECHNOLOGY_NETWORKPOLICY
                                );
                                namespace.addRelationship(rel);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addPVPVCRelationships() {
        for (C4Component component : model.getClusterScopedComponents()) {
            if (component.getResource() instanceof PersistentVolume pv) {
                if (pv.getSpec().getClaimRef() != null) {
                    String claimName = pv.getSpec().getClaimRef().getName();
                    String claimNamespace = pv.getSpec().getClaimRef().getNamespace();

                    for (C4Namespace namespace : model.getNamespaces().values()) {
                        if (namespace.getName().equals(claimNamespace)) {
                            for (C4Component targetComp : namespace.getComponents()) {
                                if (targetComp.getKind().equalsIgnoreCase("PersistentVolumeClaim") &&
                                        targetComp.getName().equals(claimName)) {
                                    C4Relationship rel = new C4Relationship(
                                            component.getId(),
                                            targetComp.getNamespace() + "." + targetComp.getId(),
                                            Constants.BOUNDS_RELATIONSHIP,
                                            Constants.TECHNOLOGY_PV
                                    );
                                    model.addRelationship(rel);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (C4Namespace namespace : model.getNamespaces().values()) {
            for (C4Component pvcComponent : namespace.getComponents()) {
                if (pvcComponent.getResource() instanceof io.fabric8.kubernetes.api.model.PersistentVolumeClaim pvc) {
                    String volumeName = pvc.getSpec().getVolumeName();
                    if (volumeName != null) {
                        for (C4Component pvComponent : model.getClusterScopedComponents()) {
                            if (pvComponent.getResource() instanceof PersistentVolume pv &&
                                    pv.getMetadata().getName().equals(volumeName)) {
                                C4Relationship rel = new C4Relationship(
                                        pvComponent.getId(),
                                        pvcComponent.getNamespace() + "." + pvcComponent.getId(),
                                        Constants.BOUNDS_RELATIONSHIP,
                                        Constants.TECHNOLOGY_PV
                                );
                                model.addRelationship(rel);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addStorageClassRelationships() {
        log.info("Adding StorageClass relationships. Cluster scoped components: {}", model.getClusterScopedComponents().size());

        for (C4Component scComponent : model.getClusterScopedComponents()) {
            if (scComponent.getResource() instanceof StorageClass sc) {
                String scName = sc.getMetadata().getName();
                log.info("Found StorageClass: {}", scName);

                for (C4Component pvComponent : model.getClusterScopedComponents()) {
                    if (pvComponent.getResource() instanceof PersistentVolume pv) {
                        String pvStorageClassName = pv.getSpec().getStorageClassName();
                        log.info("PV {} has storageClassName: {}", pvComponent.getId(), pvStorageClassName);

                        if (pvStorageClassName != null && pvStorageClassName.equals(scName)) {
                            log.info("Creating relationship: {} -> {} (binds)", scComponent.getId(), pvComponent.getId());
                            C4Relationship rel = new C4Relationship(
                                    scComponent.getId(),
                                    pvComponent.getId(),
                                    Constants.BUNDS_RELATIONSHIP,
                                    Constants.TECHNOLOGY_STORAGECLASS
                            );
                            model.addRelationship(rel);
                            log.info("Relationship added successfully");
                        }
                    }
                }
            }
        }
    }

    private void addRBACRelationships() {
        for (C4Namespace namespace : model.getNamespaces().values()) {
            for (C4Component component : namespace.getComponents()) {
                String source = component.getNamespace() + "." + component.getId();

                if (component.getResource() instanceof io.fabric8.kubernetes.api.model.rbac.RoleBinding rb) {
                    String roleKind = rb.getRoleRef().getKind();
                    String roleName = rb.getRoleRef().getName();

                    for (C4Component targetComp : namespace.getComponents()) {
                        if (targetComp.getKind().equalsIgnoreCase(roleKind) &&
                                targetComp.getName().equals(roleName)) {
                            C4Relationship rel = new C4Relationship(
                                    source,
                                    targetComp.getNamespace() + "." + targetComp.getId(),
                                    Constants.USES_RELATIONSHIP,
                                    "rbac"
                            );
                            namespace.addRelationship(rel);
                        }
                    }

                    for (io.fabric8.kubernetes.api.model.rbac.Subject subject : rb.getSubjects()) {
                        if (subject.getKind().equalsIgnoreCase("ServiceAccount")) {
                            for (C4Component targetComp : namespace.getComponents()) {
                                if (targetComp.getKind().equalsIgnoreCase("ServiceAccount") &&
                                        targetComp.getName().equals(subject.getName())) {
                                    C4Relationship rel = new C4Relationship(
                                            targetComp.getNamespace() + "." + targetComp.getId(),
                                            source,
                                            Constants.USES_RELATIONSHIP,
                                            "rbac"
                                    );
                                    namespace.addRelationship(rel);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void processPodSpec(C4Namespace namespace, C4Component component, PodSpec podSpec, Map<String, String> labels) {
        if (podSpec != null && podSpec.getContainers() != null) {
            Container c = podSpec.getContainers().get(0);
            component.setImage(c.getImage());
            component.setMetadata(labels);

            if (c.getEnvFrom() != null) {
                for (EnvFromSource envFrom : c.getEnvFrom()) {
                    addValueFromRelationship(namespace, component, envFrom);
                }
            }
            if (c.getEnv() != null) {
                for (EnvVar e : c.getEnv()) {
                    if (e.getValueFrom() != null) {
                        addValueFromKeyRelationship(namespace, component, e.getValueFrom());
                    } else {
                        component.getEnv().put(e.getName(), e.getValue());
                    }
                }
            }
        }

        if (podSpec != null && podSpec.getVolumes() != null) {
            for (Volume volume : podSpec.getVolumes()) {
                addVolumeRelationship(namespace, component, volume);
            }
        }
    }

    private void addVolumeRelationship(C4Namespace namespace,
                                       C4Component component,
                                       Volume volume) {
        String source = component.getNamespace() + "." + component.getId();
        String target = "";

        if (volume.getPersistentVolumeClaim() != null && volume.getPersistentVolumeClaim().getClaimName() != null) {
            target = component.getNamespace() + ".persistentvolumeclaim_" + volume.getPersistentVolumeClaim().getClaimName();
        }
        if (volume.getProjected() != null) {
            for (VolumeProjection projection : volume.getProjected().getSources()) {
                if (projection.getConfigMap() != null) {
                    target = component.getNamespace() + ".configmap_" + projection.getConfigMap().getName();
                } else if (projection.getSecret() != null) {
                    target = component.getNamespace() + ".secret_" + projection.getSecret().getName();
                }
            }
        }
        if (volume.getConfigMap() != null) {
            target = component.getNamespace() + ".configmap_" + volume.getConfigMap().getName();
        }
        if (volume.getSecret() != null) {
            target = component.getNamespace() + ".secret_" + volume.getSecret().getSecretName();
        }
        if (volume.getEmptyDir() != null){
            //target = component.getNamespace() + ".secret_" + volume.getSecret().getSecretName();
        }

        if (!target.isEmpty()) {
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.VOLUME_TECHNOLOGY));
        }
    }

    private void addValueFromRelationship(C4Namespace namespace, C4Component component, EnvFromSource valueFrom) {
        String source = component.getNamespace() + "." + component.getId();
        if (valueFrom.getConfigMapRef() != null) {
            String target = component.getNamespace() + ".configmap_" + valueFrom.getConfigMapRef().getName();
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.CONFIGMAP_TECHNOLOGY));
        }
        if (valueFrom.getSecretRef() != null) {
            String target = component.getNamespace() + ".secret_" + valueFrom.getSecretRef().getName();
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.SECRET_TECHNOLOGY));
        }
    }

    private void addValueFromKeyRelationship(C4Namespace namespace, C4Component component, EnvVarSource valueFrom) {
        String source = component.getNamespace() + "." + component.getId();
        if (valueFrom.getConfigMapKeyRef() != null) {
            String target = component.getNamespace() + ".configmap_" + valueFrom.getConfigMapKeyRef().getName();
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.CONFIGMAP_TECHNOLOGY));
        }
        if (valueFrom.getSecretKeyRef() != null) {
            String target = component.getNamespace() + ".secret_" + valueFrom.getSecretKeyRef().getName();
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.SECRET_TECHNOLOGY));
        }
    }

    private void addOwnerRelationship(C4Namespace namespace, C4Component component) {
        component.getResource().getMetadata().getOwnerReferences().forEach(ownerReference -> {
            if (Constants.isClusterScoped(ownerReference.getKind())) {
                final C4Relationship rel = new C4Relationship(
                        ownerReference.getKind().toLowerCase() + "_" + PresenterUtils.sanitizeComponentId(ownerReference.getName()),
                        component.getNamespace() + "." + component.getId(),
                        Constants.OWNER_RELATIONSHIP,
                        Constants.K8S_TECHNOLOGY
                );
                model.addRelationship(rel);
            } else {
                final C4Relationship rel = new C4Relationship(
                        namespace.getName() + "." + ownerReference.getKind().toLowerCase() + "_" + ownerReference.getName(),
                        component.getNamespace() + "." + component.getId(),
                        Constants.OWNER_RELATIONSHIP,
                        Constants.K8S_TECHNOLOGY
                );
                namespace.addRelationship(rel);
            }
        });
    }
}
