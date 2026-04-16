package com.k8stoc4.visitor;

import com.k8stoc4.model.C4Component;
import com.k8stoc4.model.C4LabelGroup;
import com.k8stoc4.model.C4Model;
import com.k8stoc4.model.C4Namespace;
import com.k8stoc4.model.C4Relationship;
import com.k8stoc4.model.Constants;
import com.k8stoc4.presenter.PresenterUtils;
import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PersistentVolume;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeProjection;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.autoscaling.v2.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicy;
import io.fabric8.kubernetes.api.model.policy.v1.PodDisruptionBudget;
import io.fabric8.kubernetes.api.model.storage.StorageClass;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.k8stoc4.visitor.VisitorUtils.containerMatchesSelector;

@Slf4j
public final class C4ModelBuilderVisitor implements KubernetesResourceVisitor {

    private final C4Model model = new C4Model();
    private final String defaultNS;

    private C4ModelBuilderVisitor(final Optional<String> defaultNs) {
        this.defaultNS = defaultNs.orElse(Constants.DEFAULT_NAMESPACE);
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

    public C4Model getModel() {
        return this.model;
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

    public void groupComponentsByLabel(final String labelKey) {
        for (final C4Namespace namespace : model.getNamespaces().values()) {
            final List<C4Component> componentsToRemove = new ArrayList<>();
            for (final C4Component component : namespace.getComponents()) {
                final String labelValue = component.getResource().getMetadata().getLabels().get(labelKey);
                if (labelValue != null && !labelValue.isEmpty()) {
                    final C4LabelGroup group = namespace.getOrCreateLabelGroup(labelKey, labelValue);
                    group.addComponents(component);
                    componentsToRemove.add(component);
                }
            }
            componentsToRemove.forEach(namespace.getComponents()::remove);
        }
    }

    public void addMissingReferencedComponents() {
        this.model.getNamespaces().values().forEach(namespace -> {
            final Set<C4Component> missingComponents = new LinkedHashSet<>();
            final Set<C4Relationship> newRelationships = new LinkedHashSet<>();
            final Set<C4Relationship> faultyRelationships = new LinkedHashSet<>();
            for (final C4Relationship relationship : namespace.getRelationships()) {
                String newSource = relationship.getSource();
                if (this.model.searchComponentByRef(relationship.getSource()).isEmpty()) {
                    final String[] splitSourceRef = relationship.getSource().split("_", 2);
                    final C4Component missingComponent = C4Component.missing(namespace.getName(), splitSourceRef[1], Constants.MISSING_TYPE);
                    missingComponent.getAdditionalMetadata().put("typeHint", splitSourceRef[0].split("\\.", 2)[1]);
                    missingComponents.add(missingComponent);
                    newSource = missingComponent.getNamespace() + "." + missingComponent.getId();
                }
                String newTarget = relationship.getTarget();
                if (this.model.searchComponentByRef(relationship.getTarget()).isEmpty()) {
                    final String[] splitTargetRef = relationship.getTarget().split("_", 2);
                    final C4Component missingComponent = C4Component.missing(namespace.getName(), splitTargetRef[1], Constants.MISSING_TYPE);
                    missingComponent.getAdditionalMetadata().put("typeHint", splitTargetRef[0].split("\\.", 2)[1]);
                    missingComponents.add(missingComponent);
                    newTarget = missingComponent.getNamespace() + "." + missingComponent.getId();
                }
                if (!newSource.equals(relationship.getSource()) || !newTarget.equals(relationship.getTarget())) {
                    faultyRelationships.add(relationship);
                    newRelationships.add(new C4Relationship(newSource, newTarget, relationship.getDescription(), relationship.getTechnology(), relationship.getTag()));
                }
            }
            namespace.getRelationships().removeAll(faultyRelationships);
            namespace.getRelationships().addAll(newRelationships);
            namespace.getComponents().addAll(missingComponents);
        });
    }

    @Override
    public void visit(final StatefulSet statefulSet) {
        model.getSpecifications().add(statefulSet.getKind().toLowerCase());
        final String ns = Optional.ofNullable(statefulSet.getMetadata().getNamespace()).orElse(defaultNS);
        final C4Namespace namespace = getOrCreateSystem(ns);
        final C4Component component = new C4Component(statefulSet, ns, statefulSet.getMetadata().getName(), statefulSet.getKind());

        processPodSpec(namespace, component, statefulSet.getSpec().getTemplate().getSpec(),
                statefulSet.getSpec().getTemplate().getMetadata().getLabels());

        namespace.addComponents(component);
    }

    @Override
    public void visit(final ReplicaSet replicaSet) {
        model.getSpecifications().add(replicaSet.getKind().toLowerCase());
        final String ns = Optional.ofNullable(replicaSet.getMetadata().getNamespace()).orElse(defaultNS);
        final C4Namespace namespace = getOrCreateSystem(ns);
        final C4Component component = new C4Component(replicaSet, ns, replicaSet.getMetadata().getName(), replicaSet.getKind());

        processPodSpec(namespace, component, replicaSet.getSpec().getTemplate().getSpec(),
                replicaSet.getSpec().getTemplate().getMetadata().getLabels());

        namespace.addComponents(component);
        addOwnerRelationship(namespace, component);
    }

    @Override
    public void visit(final Pod pod) {
        model.getSpecifications().add(pod.getKind().toLowerCase());
        final String ns = Optional.ofNullable(pod.getMetadata().getNamespace()).orElse(defaultNS);
        final C4Namespace namespace = getOrCreateSystem(ns);
        final C4Component component = new C4Component(pod, ns, pod.getMetadata().getName(), pod.getKind());

        processPodSpec(namespace, component, pod.getSpec(),
                pod.getMetadata().getLabels());

        namespace.addComponents(component);
        addOwnerRelationship(namespace, component);
        if (pod.getSpec().getNodeName() != null) {
            final C4Relationship rel = new C4Relationship(
                    "node_" + PresenterUtils.sanitizeComponentId(pod.getSpec().getNodeName()),
                    component.getNamespace() + "." + component.getId(),
                    Constants.OWNER_RELATIONSHIP,
                    Constants.K8S_TECHNOLOGY
            );
            namespace.addRelationship(rel);
        }
    }

    @Override
    public void visit(final Deployment deployment) {
        model.getSpecifications().add(deployment.getKind().toLowerCase());
        final String ns = Optional.ofNullable(deployment.getMetadata().getNamespace()).orElse(defaultNS);
        final C4Namespace namespace = getOrCreateSystem(ns);
        final C4Component component = new C4Component(deployment, ns, deployment.getMetadata().getName(), deployment.getKind());

        processPodSpec(namespace, component, deployment.getSpec().getTemplate().getSpec(),
                deployment.getSpec().getTemplate().getMetadata().getLabels());

        namespace.addComponents(component);
    }

    @Override
    public void visit(final Service svc) {
        model.getSpecifications().add(svc.getKind().toLowerCase());
        final String ns = Optional.ofNullable(svc.getMetadata().getNamespace()).orElse(defaultNS);
        final C4Namespace namespace = getOrCreateSystem(ns);
        final C4Component service = new C4Component(svc, ns, svc.getMetadata().getName(), svc.getKind()) ;
        if (svc.getSpec().getType()!=null && svc.getSpec().getType().equals(Constants.EXTERNAL_SERVICE)){
            service.getTags().add(Constants.EXTERNAL_SERVICE);
            service.setDescription(svc.getSpec().getExternalName());
        }
        namespace.addComponents(service);
    }

    @Override
    public void visit(final Ingress ing) {
        final C4Component ingress = visitIngress(ing, () ->
            ing.getSpec()
                .getRules()
                .stream()
                .flatMap(r -> {
                    if (r.getHttp() != null) {
                        return r.getHttp()
                            .getPaths()
                            .stream()
                            .map(p -> p.getBackend().getService().getName());
                    } else {
                        return Stream.empty();
                    }
                })
                .distinct()
                .collect(Collectors.toList())
        );
        ingress.getAdditionalMetadata().put("hosts", ing.getSpec().getRules().stream().map(IngressRule::getHost).filter(Objects::nonNull).collect(Collectors.joining("\n")));
    }

    @Override
    public void visit(final io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress ing) {
        final C4Component ingress = visitIngress(ing, () ->
            ing.getSpec()
                .getRules()
                .stream()
                .flatMap(r -> {
                    if (r.getHttp() != null) {
                        return r.getHttp()
                            .getPaths()
                            .stream()
                            .map(p -> p.getBackend().getServiceName());
                    } else {
                        return Stream.empty();
                    }
                })
                .distinct()
                .collect(Collectors.toList())
        );
        ingress.getAdditionalMetadata().put("hosts", ing.getSpec().getRules().stream().map(io.fabric8.kubernetes.api.model.networking.v1beta1.IngressRule::getHost).filter(Objects::nonNull).collect(Collectors.joining("\n")));
    }

    @Override
    public void visit(final io.fabric8.kubernetes.api.model.extensions.Ingress ing) {
        final C4Component ingress = visitIngress(ing, () ->
            ing.getSpec()
                .getRules()
                .stream()
                .flatMap(r -> {
                    if (r.getHttp() != null) {
                        return r.getHttp()
                            .getPaths()
                            .stream()
                            .map(p -> p.getBackend().getServiceName());
                    } else {
                        return Stream.empty();
                    }
                })
                .distinct()
                .collect(Collectors.toList())
        );
        ingress.getAdditionalMetadata().put("hosts", ing.getSpec().getRules().stream().map(io.fabric8.kubernetes.api.model.extensions.IngressRule::getHost).filter(Objects::nonNull).collect(Collectors.joining("\n")));
    }

    @Override
    public void visit(final HasMetadata resource) {
        model.getSpecifications().add(resource.getKind().toLowerCase());
        final C4Component component = new C4Component(resource, resource.getMetadata().getNamespace(), resource.getMetadata().getName(), resource.getKind());
        
        if (isClusterScopedResource(resource)) {
            component.setNamespace(Constants.CLUSTER_LEVEL);
            model.addClusterScopedComponent(component);
        } else {
            final String ns = Optional.ofNullable(resource.getMetadata().getNamespace()).orElse(defaultNS);
            final C4Namespace system = getOrCreateSystem(ns);
            system.addComponents(component);
        }
    }

    private C4Namespace getOrCreateSystem(final String ns) {
        return model.getNamespaces().computeIfAbsent(ns, C4Namespace::new);
    }

    private boolean isClusterScopedResource(final HasMetadata resource) {
        return Constants.isClusterScoped(resource.getKind());
    }

    private void addServiceToServiceRelationships() {
        model.getNamespaces().values().forEach(namespace -> {
            final Map<String, C4Component> servicesByFqdn = new HashMap<>();
                model.getComponentsByKind(namespace.getName(), "service")
                        .forEach(s -> {
                            String name = s.getName();
                            String ns = s.getNamespace();
                            servicesByFqdn.put(name + "." + ns, s);
                            servicesByFqdn.put(name, s);
                            servicesByFqdn.put(name + "." + ns + ".svc", s);
                            servicesByFqdn.put(name + "." + ns + ".svc.cluster.local", s);
                        });
            model.getComponentsByKind(namespace.getName(), "deployment").forEach(component -> {
                final Deployment deployment = (Deployment) component.getResource();
                deployment.getSpec()
                    .getTemplate()
                    .getSpec()
                    .getContainers()
                    .forEach(container -> {
                        if (container.getEnv() == null) {
                            return;
                        }
                        container.getEnv().stream()
                            .map(EnvVar::getValue)
                            .filter(this::isServiceRef)
                            .forEach(value ->
                                servicesByFqdn.forEach((fqdn, service) -> {
                                    if (value.contains(fqdn)) {
                                        namespace.addRelationship(
                                                new C4Relationship(
                                                    component.getNamespace() + "." + component.getId(),
                                                    service.getNamespace() + "." + service.getId(),
                                                    Constants.ROUTES_TO_RELATIONSHIP,
                                                    Constants.TECHNOLOGY_TCP_HTTP,
                                                    Constants.SERVICE2SERVICE_TAG
                                                )
                                        );
                                    }
                                })
                            );
                    });
            });
        });
    }

    private boolean isServiceRef(final String value) {
        if (value == null) {
            return false;
        }
        return value.startsWith("http://") ||
                value.startsWith("https://") ||
                value.startsWith("amqp://") ||
                value.startsWith("mongodb://") ||
                value.startsWith("jdbc:") ||
                value.matches("[a-zA-Z0-9-]+\\.[a-zA-Z0-9]+\\.svc(.cluster.local)?");
    }

    private void addServiceRelationships() {
        for (final C4Namespace namespace : model.getNamespaces().values()) {
            for (final C4Component component : namespace.getComponents()) {
                if ("service".equalsIgnoreCase(component.getKind())) {
                    final Map<String, String> selector = ((Service)component.getResource()).getSpec().getSelector();
                    if (selector != null && !selector.isEmpty()) {
                        for (final C4Component targetComp : namespace.getComponents()) {
                            if (containerMatchesSelector(targetComp, selector)) {
                                final C4Relationship rel = new C4Relationship(
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
        for (final C4Namespace namespace : model.getNamespaces().values()) {
            for (final C4Component component : namespace.getComponents()) {
                if (component.getResource() instanceof HorizontalPodAutoscaler hpa) {
                    final String scaleTargetName = hpa.getSpec().getScaleTargetRef().getName();
                    final String scaleTargetKind = hpa.getSpec().getScaleTargetRef().getKind();

                    for (final C4Component targetComp : namespace.getComponents()) {
                        if (targetComp.getName().equals(scaleTargetName) &&
                                targetComp.getKind().equalsIgnoreCase(scaleTargetKind)) {
                            final C4Relationship rel = new C4Relationship(
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
        for (final C4Namespace namespace : model.getNamespaces().values()) {
            for (final C4Component component : namespace.getComponents()) {
                if (component.getResource() instanceof PodDisruptionBudget pdb) {
                    final Map<String, String> selector = pdb.getSpec().getSelector() != null
                            ? pdb.getSpec().getSelector().getMatchLabels()
                            : null;

                    if (selector != null && !selector.isEmpty()) {
                        for (final C4Component targetComp : namespace.getComponents()) {
                            if (containerMatchesSelector(targetComp, selector)) {
                                final C4Relationship rel = new C4Relationship(
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
        for (final C4Namespace namespace : model.getNamespaces().values()) {
            for (final C4Component component : namespace.getComponents()) {
                if (component.getResource() instanceof ServiceAccount sa) {
                    final String saName = sa.getMetadata().getName();
                    for (final C4Component targetComp : namespace.getComponents()) {
                        final HasMetadata resource = targetComp.getResource();
                        if (resource instanceof Deployment d && d.getSpec().getTemplate().getSpec().getServiceAccountName() != null) {
                            if (d.getSpec().getTemplate().getSpec().getServiceAccountName().equals(saName)) {
                                final C4Relationship rel = new C4Relationship(
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
                                final C4Relationship rel = new C4Relationship(
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
                                final C4Relationship rel = new C4Relationship(
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
        for (final C4Namespace namespace : model.getNamespaces().values()) {
            for (final C4Component component : namespace.getComponents()) {
                if (component.getResource() instanceof NetworkPolicy np) {
                    final String source = component.getNamespace() + "." + component.getId();

                    if (np.getSpec().getPodSelector() != null && np.getSpec().getPodSelector().getMatchLabels() != null) {
                        final Map<String, String> selector = np.getSpec().getPodSelector().getMatchLabels();

                        for (final C4Component targetComp : namespace.getComponents()) {
                            if (containerMatchesSelector(targetComp, selector)) {
                                final C4Relationship rel = new C4Relationship(
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
        for (final C4Component component : model.getClusterScopedComponents()) {
            if (component.getResource() instanceof PersistentVolume pv) {
                if (pv.getSpec().getClaimRef() != null) {
                    final String claimName = pv.getSpec().getClaimRef().getName();
                    final String claimNamespace = pv.getSpec().getClaimRef().getNamespace();

                    for (final C4Namespace namespace : model.getNamespaces().values()) {
                        if (namespace.getName().equals(claimNamespace)) {
                            for (final C4Component targetComp : namespace.getComponents()) {
                                if ("PersistentVolumeClaim".equalsIgnoreCase(targetComp.getKind()) &&
                                        targetComp.getName().equals(claimName)) {
                                    final C4Relationship rel = new C4Relationship(
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

        for (final C4Namespace namespace : model.getNamespaces().values()) {
            for (final C4Component pvcComponent : namespace.getComponents()) {
                if (pvcComponent.getResource() instanceof io.fabric8.kubernetes.api.model.PersistentVolumeClaim pvc) {
                    final String volumeName = pvc.getSpec().getVolumeName();
                    if (volumeName != null) {
                        for (final C4Component pvComponent : model.getClusterScopedComponents()) {
                            if (pvComponent.getResource() instanceof PersistentVolume pv &&
                                    pv.getMetadata().getName().equals(volumeName)) {
                                final C4Relationship rel = new C4Relationship(
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
        if (log.isInfoEnabled()) {
            log.info("Adding StorageClass relationships. Cluster scoped components: {}", model.getClusterScopedComponents().size());
        }

        for (final C4Component scComponent : model.getClusterScopedComponents()) {
            if (scComponent.getResource() instanceof StorageClass sc) {
                final String scName = sc.getMetadata().getName();
                if (log.isInfoEnabled()) {
                    log.info("Found StorageClass: {}", scName);
                }

                for (final C4Component pvComponent : model.getClusterScopedComponents()) {
                    if (pvComponent.getResource() instanceof PersistentVolume pv) {
                        final String pvStorageClassName = pv.getSpec().getStorageClassName();
                        if (log.isInfoEnabled()) {
                            log.info("PV {} has storageClassName: {}", pvComponent.getId(), pvStorageClassName);
                        }

                        if (pvStorageClassName != null && pvStorageClassName.equals(scName)) {
                            if (log.isInfoEnabled()) {
                                log.info("Creating relationship: {} -> {} (binds)", scComponent.getId(), pvComponent.getId());
                            }
                            final C4Relationship rel = new C4Relationship(
                                    scComponent.getId(),
                                    pvComponent.getId(),
                                    Constants.BINDS_RELATIONSHIP,
                                    Constants.TECHNOLOGY_STORAGECLASS
                            );
                            model.addRelationship(rel);
                            if (log.isInfoEnabled()) {
                                log.info("Relationship added successfully");
                            }
                        }
                    }
                }
            }
        }
    }

    private void addRBACRelationships() {
        for (final C4Namespace namespace : model.getNamespaces().values()) {
            for (final C4Component component : namespace.getComponents()) {
                final String source = component.getNamespace() + "." + component.getId();

                if (component.getResource() instanceof io.fabric8.kubernetes.api.model.rbac.RoleBinding rb) {
                    final String roleKind = rb.getRoleRef().getKind();
                    final String roleName = rb.getRoleRef().getName();

                    for (final C4Component targetComp : namespace.getComponents()) {
                        if (targetComp.getKind().equalsIgnoreCase(roleKind) &&
                                targetComp.getName().equals(roleName)) {
                            final C4Relationship rel = new C4Relationship(
                                    source,
                                    targetComp.getNamespace() + "." + targetComp.getId(),
                                    Constants.USES_RELATIONSHIP,
                                    "rbac"
                            );
                            namespace.addRelationship(rel);
                        }
                    }

                    for (final io.fabric8.kubernetes.api.model.rbac.Subject subject : rb.getSubjects()) {
                        if ("ServiceAccount".equalsIgnoreCase(subject.getKind())) {
                            for (final C4Component targetComp : namespace.getComponents()) {
                                if ("ServiceAccount".equalsIgnoreCase(targetComp.getKind()) &&
                                        targetComp.getName().equals(subject.getName())) {
                                    final C4Relationship rel = new C4Relationship(
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

    private void processPodSpec(final C4Namespace namespace, final C4Component component, final PodSpec podSpec, final Map<String, String> labels) {
        component.getResource().getMetadata().getLabels().putAll(labels);
        if (podSpec != null && podSpec.getContainers() != null) {
            podSpec.getContainers().forEach(c -> {
                component.getContainerImages().put(c.getName(), c.getImage());
                if (c.getEnvFrom() != null) {
                    for (final EnvFromSource envFrom : c.getEnvFrom()) {
                        addValueFromRelationship(namespace, component, envFrom);
                    }
                }
                if (c.getEnv() != null) {
                    for (final EnvVar e : c.getEnv()) {
                        if (e.getValueFrom() != null) {
                            addValueFromKeyRelationship(namespace, component, e.getValueFrom());
                        } else {
                            component.getEnv().put(e.getName(), e.getValue());
                        }
                    }
                }
            });
        }

        if (podSpec != null && podSpec.getVolumes() != null) {
            for (final Volume volume : podSpec.getVolumes()) {
                addVolumeRelationship(namespace, component, volume);
            }
        }
    }

    private void addVolumeRelationship(final C4Namespace namespace,
                                       final C4Component component,
                                       final Volume volume) {
        final String source = component.getNamespace() + "." + component.getId();
        String target = "";

        if (volume.getPersistentVolumeClaim() != null && volume.getPersistentVolumeClaim().getClaimName() != null) {
            target = component.getNamespace() + ".persistentvolumeclaim_" + volume.getPersistentVolumeClaim().getClaimName();
        }
        if (volume.getProjected() != null) {
            for (final VolumeProjection projection : volume.getProjected().getSources()) {
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
            namespace.addRelationship(new C4Relationship(source, PresenterUtils.sanitizeNamespacedId(target), Constants.MOUNT_RELATIONSHIP, Constants.VOLUME_TECHNOLOGY));
        }
    }

    private void addValueFromRelationship(final C4Namespace namespace, final C4Component component, final EnvFromSource valueFrom) {
        final String source = component.getNamespace() + "." + component.getId();
        if (valueFrom.getConfigMapRef() != null) {
            final String target = component.getNamespace() + ".configmap_" + valueFrom.getConfigMapRef().getName();
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.CONFIGMAP_TECHNOLOGY));
        }
        if (valueFrom.getSecretRef() != null) {
            final String target = component.getNamespace() + ".secret_" + valueFrom.getSecretRef().getName();
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.SECRET_TECHNOLOGY));
        }
    }

    private void addValueFromKeyRelationship(final C4Namespace namespace, final C4Component component, final EnvVarSource valueFrom) {
        final String source = component.getNamespace() + "." + component.getId();
        if (valueFrom.getConfigMapKeyRef() != null) {
            final String target = component.getNamespace() + ".configmap_" + valueFrom.getConfigMapKeyRef().getName();
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.CONFIGMAP_TECHNOLOGY));
        }
        if (valueFrom.getSecretKeyRef() != null) {
            final String target = component.getNamespace() + ".secret_" + valueFrom.getSecretKeyRef().getName();
            namespace.addRelationship(new C4Relationship(source, target, Constants.MOUNT_RELATIONSHIP, Constants.SECRET_TECHNOLOGY));
        }
    }

    private void addOwnerRelationship(final C4Namespace namespace, final C4Component component) {
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

    private C4Component visitIngress(final HasMetadata resource, final Supplier<List<String>> serviceNamesSupplier) {
        model.getSpecifications().add(resource.getKind().toLowerCase());
        final String ns = Optional.ofNullable(resource.getMetadata().getNamespace()).orElse(defaultNS);
        final C4Namespace namespace = getOrCreateSystem(ns);
        final C4Component ingress = new C4Component(resource, ns, resource.getMetadata().getName(), resource.getKind());
        namespace.addComponents(ingress);

        serviceNamesSupplier.get().forEach(svcName -> namespace.addRelationship(new C4Relationship(
                ingress.getNamespace() + "." + ingress.getId(),
                ingress.getNamespace() + ".service_" + svcName,
                Constants.ROUTES_HTTP_RELATIONSHIP,
                Constants.TECHNOLOGY_HTTP
        )));

        return ingress;
    }
}
