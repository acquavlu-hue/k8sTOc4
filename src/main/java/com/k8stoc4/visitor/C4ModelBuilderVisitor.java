package com.k8stoc4.visitor;

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

import static com.k8stoc4.visitor.VisitorUtils.containerMatchesSelector;

@Slf4j
@Getter
@Setter
public class C4ModelBuilderVisitor implements KubernetesResourceVisitor {

    private final C4Model model = new C4Model();

    private C4Namespace getOrCreateSystem(String ns) {
        return model.getNamespaces().computeIfAbsent(ns, C4Namespace::new);
    }

    public void addServiceRelationships() {
        for (String ns : model.getNamespaces().keySet()) {
            C4Namespace namespace = model.getNamespaces().get(ns);
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

    public void addAllRelationships() {
        addServiceRelationships();
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

    private void addHPARelationships() {
        for (String ns : model.getNamespaces().keySet()) {
            C4Namespace namespace = model.getNamespaces().get(ns);
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
        for (String ns : model.getNamespaces().keySet()) {
            C4Namespace namespace = model.getNamespaces().get(ns);
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
        for (String ns : model.getNamespaces().keySet()) {
            C4Namespace namespace = model.getNamespaces().get(ns);
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
        for (String ns : model.getNamespaces().keySet()) {
            C4Namespace namespace = model.getNamespaces().get(ns);
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
        for (String ns : model.getNamespaces().keySet()) {
            C4Namespace namespace = model.getNamespaces().get(ns);
            for (C4Component component : namespace.getComponents()) {
                if (component.getResource() instanceof PersistentVolumeClaim pvc) {
                    String volumeName = pvc.getSpec().getVolumeName();
                    if (volumeName == null) {
                        continue;
                    }
                    
                    // Cerca PV nei componenti globali
                    for (C4Component globalComp : model.getGlobalComponents()) {
                        if (globalComp.getKind().equals("PersistentVolume") && 
                            globalComp.getName().equals(volumeName)) {
                            C4Relationship rel = new C4Relationship(
                                globalComp.getId(),
                                ns + "." + component.getId(),
                                Constants.BOUNDS_RELATIONSHIP,
                                Constants.TECHNOLOGY_PV
                            );
                            namespace.addRelationship(rel);
                        }
                    }
                }
            }
        }
    }

    private void addStorageClassRelationships() {
        // Cerca PV globali
        for (C4Component scComp : model.getGlobalComponents()) {
            if (scComp.getResource() instanceof StorageClass sc) {
                String scName = sc.getMetadata().getName();
                
                // Cerca PV nei componenti globali e nei namespace
                List<C4Component> allComponents = new ArrayList<>(model.getGlobalComponents());
                for (C4Namespace ns : model.getNamespaces().values()) {
                    allComponents.addAll(ns.getComponents());
                }
                
                for (C4Component targetComp : allComponents) {
                    if (targetComp.getResource() instanceof PersistentVolume pv && pv.getSpec().getStorageClassName() != null) {
                        if (pv.getSpec().getStorageClassName().equals(scName)) {
                            String targetId = targetComp.getNamespace() != null 
                                ? targetComp.getNamespace() + "." + targetComp.getId()
                                : targetComp.getId();
                            C4Relationship rel = new C4Relationship(
                                scComp.getId(),
                                targetId,
                                Constants.BUNDS_RELATIONSHIP,
                                Constants.TECHNOLOGY_STORAGECLASS
                            );
                            if (targetComp.getNamespace() != null) {
                                for (C4Namespace ns : model.getNamespaces().values()) {
                                    if (ns.getComponents().contains(targetComp)) {
                                        ns.addRelationship(rel);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addRBACRelationships() {
        for (String ns : model.getNamespaces().keySet()) {
            C4Namespace namespace = model.getNamespaces().get(ns);
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

    @Override
    public void visit(StatefulSet statefulSet) {
        model.getSpecifications().add(statefulSet.getKind().toLowerCase());
        String ns = statefulSet.getMetadata().getNamespace();
        
        if (ns != null) {
            C4Namespace namespace = getOrCreateSystem(ns);

            C4Component component = new C4Component(
                    statefulSet,
                    ns,
                    statefulSet.getMetadata().getName(),
                    statefulSet.getKind());
            
            processPodSpec(namespace, component, statefulSet.getSpec().getTemplate().getSpec(),
                    statefulSet.getSpec().getTemplate().getMetadata().getLabels());
            
            namespace.addComponents(component);
        }
    }

    @Override
    public void visit(ReplicaSet replicaSet) {
        model.getSpecifications().add(replicaSet.getKind().toLowerCase());
        String ns = replicaSet.getMetadata().getNamespace();
        
        if (ns != null) {
            C4Namespace namespace = getOrCreateSystem(ns);

            C4Component component = new C4Component(
                    replicaSet,
                    ns,
                    replicaSet.getMetadata().getName(),
                    replicaSet.getKind());
            
            processPodSpec(namespace, component, replicaSet.getSpec().getTemplate().getSpec(),
                    replicaSet.getSpec().getTemplate().getMetadata().getLabels());
            
            namespace.addComponents(component);
        }
    }

    @Override
    public void visit(Pod pod) {
        model.getSpecifications().add(pod.getKind().toLowerCase());
        String ns = pod.getMetadata().getNamespace();
        
        if (ns != null) {
            C4Namespace namespace = getOrCreateSystem(ns);

            C4Component component = new C4Component(
                    pod,
                    ns,
                    pod.getMetadata().getName(),
                    pod.getKind());
            
            processPodSpec(namespace, component, pod.getSpec(),
                    pod.getMetadata().getLabels());
            
            namespace.addComponents(component);
        }
    }

    @Override
    public void visit(Deployment deployment) {
        model.getSpecifications().add(deployment.getKind().toLowerCase());
        String ns = deployment.getMetadata().getNamespace();
        
        if (ns != null) {
            C4Namespace namespace = getOrCreateSystem(ns);

            C4Component component = new C4Component(
                    deployment,
                    ns,
                    deployment.getMetadata().getName(),
                    deployment.getKind());
            
            processPodSpec(namespace, component, deployment.getSpec().getTemplate().getSpec(),
                    deployment.getSpec().getTemplate().getMetadata().getLabels());
            
            namespace.addComponents(component);
        }
    }

    @Override
    public void visit(ReplicaSet replicaSet) {
        model.getSpecifications().add(replicaSet.getKind().toLowerCase());
        String ns = Optional.ofNullable(replicaSet.getMetadata().getNamespace()).orElse(Constants.DEFAULT_NAMESPACE);
        C4Namespace namespace = getOrCreateSystem(ns);

        C4Component component = new C4Component(
                replicaSet,
                replicaSet.getMetadata().getNamespace(),
                replicaSet.getMetadata().getName(),
                replicaSet.getKind());
        
        processPodSpec(namespace, component, replicaSet.getSpec().getTemplate().getSpec(),
                replicaSet.getSpec().getTemplate().getMetadata().getLabels());
        
        namespace.addComponents(component);
    }

    @Override
    public void visit(Pod pod) {
        model.getSpecifications().add(pod.getKind().toLowerCase());
        String ns = Optional.ofNullable(pod.getMetadata().getNamespace()).orElse(Constants.DEFAULT_NAMESPACE);
        C4Namespace namespace = getOrCreateSystem(ns);

        C4Component component = new C4Component(
                pod,
                pod.getMetadata().getNamespace(),
                pod.getMetadata().getName(),
                pod.getKind());
        
        processPodSpec(namespace, component, pod.getSpec(),
                pod.getMetadata().getLabels());
        
        namespace.addComponents(component);
    }

    @Override
    public void visit(Deployment deployment) {
        model.getSpecifications().add(deployment.getKind().toLowerCase());
        String ns = Optional.ofNullable(deployment.getMetadata().getNamespace()).orElse(Constants.DEFAULT_NAMESPACE);
        C4Namespace namespace = getOrCreateSystem(ns);

        C4Component component = new C4Component(
                deployment,
                deployment.getMetadata().getNamespace(),
                deployment.getMetadata().getName(),
                deployment.getKind());
        
        processPodSpec(namespace, component, deployment.getSpec().getTemplate().getSpec(),
                deployment.getMetadata().getLabels());
        
        namespace.addComponents(component);
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

    private void addVolumeRelationship(C4Namespace namespace, C4Component component, Volume volume) {
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

    @Override
    public void visit(Service svc) {
        model.getSpecifications().add(svc.getKind().toLowerCase());
        String ns = svc.getMetadata().getNamespace();
        
        if (ns != null) {
            C4Namespace namespace = getOrCreateSystem(ns);

            C4Component service = new C4Component(svc, ns,
                    svc.getMetadata().getName(), svc.getKind());
            namespace.addComponents(service);
        } else {
            C4Component component = new C4Component(svc, null,
                    svc.getMetadata().getName(), svc.getKind());
            model.addGlobalComponent(component);
        }
    }

    @Override
    public void visit(Ingress ing) {
        model.getSpecifications().add(ing.getKind().toLowerCase());
        String ns = ing.getMetadata().getNamespace();
        
        if (ns != null) {
            C4Namespace namespace = getOrCreateSystem(ns);
            C4Component ingress = new C4Component(ing, ns,
                    ing.getMetadata().getName(), ing.getKind());
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
    }

    @Override
    public void visit(io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress ing) {
        model.getSpecifications().add(ing.getKind().toLowerCase());
        String ns = ing.getMetadata().getNamespace();
        
        if (ns != null) {
            C4Namespace namespace = getOrCreateSystem(ns);
            C4Component ingress = new C4Component(ing, ns,
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
    }

    public void visit(io.fabric8.kubernetes.api.model.extensions.Ingress ing) {
        model.getSpecifications().add(ing.getKind().toLowerCase());
        String ns = ing.getMetadata().getNamespace();
        
        if (ns != null) {
            C4Namespace namespace = getOrCreateSystem(ns);
            C4Component ingress = new C4Component(ing, ns,
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
    }

    @Override
    public void visit(HasMetadata resource) {
        model.getSpecifications().add(resource.getKind().toLowerCase());
        String ns = resource.getMetadata().getNamespace();
        
        if (ns != null) {
            C4Namespace system = getOrCreateSystem(ns);
            C4Component component = new C4Component(resource, ns, resource.getMetadata().getName(), resource.getKind());
            system.addComponents(component);
        } else {
            C4Component component = new C4Component(resource, null, resource.getMetadata().getName(), resource.getKind());
            model.addGlobalComponent(component);
        }
    }
 
 }
