package com.k8stoc4.visitor;

import com.k8stoc4.model.*;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KubernetesC4FromYamlVisitorTest {

    @Test
    void testComplexYamlParsing() throws Exception {
        try (KubernetesClient client = new KubernetesClientBuilder().build();
             FileInputStream fis = new FileInputStream("src/main/resources/complex.yaml")) {

            List<HasMetadata> resources = client.load(fis).items();
            C4ModelBuilderVisitor visitor = new C4ModelBuilderVisitor.Builder().build();

            for (HasMetadata r : resources) {
                VisitorUtils.accept(r, visitor);
            }

            visitor.addAllRelationships();

            C4Model model = visitor.getModel();

            verifyNamespace(model);
            verifyComponents(model);
            verifyClusterScoped(model);
            verifySpecifications(model);
            verifyDeploymentToServiceRelationships(model);
            verifyIngressToServiceRelationships(model);
            verifyPodConfigRelationships(model);
            verifyHPARelationships(model);
            verifyPDBRelationships(model);
            verifyServiceAccountRelationships(model);
        }
    }

    private void verifyNamespace(C4Model model) {
        Map<String, C4Namespace> namespaces = model.getNamespaces();
        assertTrue(namespaces.containsKey("demo-app"), "Should contain demo-app namespace");

        C4Namespace namespace = namespaces.get("demo-app");
        assertEquals("demo-app", namespace.getName());
    }

    private void verifyClusterScoped(C4Model model) {
        assertFalse(model.getClusterScopedComponents().isEmpty(), "Should have cluster-scoped components");

        C4Component pv = model.getClusterScopedComponents().stream()
                .filter(c -> c.getKind().equals("PersistentVolume"))
                .findFirst()
                .orElse(null);

        assertNotNull(pv, "Should have PersistentVolume as cluster-scoped component");
        assertEquals("demo-pv", pv.getName());
        assertEquals(Constants.CLUSTER_LEVEL, pv.getNamespace());
    }

    private void verifyComponents(C4Model model) {
        C4Namespace namespace = model.getNamespaces().get("demo-app");
        assertNotNull(namespace, "demo-app namespace should exist");

        Map<String, C4Component> components = namespace.getComponents().stream()
                .collect(java.util.stream.Collectors.toMap(C4Component::getId, c -> c));

        assertEquals(22, components.size(), "Should have 22 components (PersistentVolume is now cluster-scoped, Namespace is also cluster-scoped)");

        C4Component postgresDeployment = components.get("statefulset_postgres");
        assertNotNull(postgresDeployment, "Postgres statefulset should exist");
        assertEquals("postgres", postgresDeployment.getName());
        assertEquals("StatefulSet", postgresDeployment.getKind());
        assertEquals("demo-app", postgresDeployment.getNamespace());
        assertEquals("postgres:14", postgresDeployment.getImage());
        assertEquals(Map.of("app", "postgres"), postgresDeployment.getResource().getMetadata().getLabels());

        C4Component postgresService = components.get("service_postgres");
        assertNotNull(postgresService, "Postgres service should exist");
        assertEquals("postgres", postgresService.getName());
        assertEquals("Service", postgresService.getKind());

        C4Component redisDeployment = components.get("deployment_redis");
        assertNotNull(redisDeployment, "Redis deployment should exist");
        assertEquals("redis", redisDeployment.getName());
        assertEquals("Deployment", redisDeployment.getKind());
        assertEquals("redis:7", redisDeployment.getImage());
        assertEquals(Map.of("app", "redis"), redisDeployment.getResource().getMetadata().getLabels());

        C4Component redisService = components.get("service_redis");
        assertNotNull(redisService, "Redis service should exist");
        assertEquals("redis", redisService.getName());
        assertEquals("Service", redisService.getKind());

        C4Component messageBrokerDeployment = components.get("deployment_message-broker");
        assertNotNull(messageBrokerDeployment, "Message broker deployment should exist");
        assertEquals("message-broker", messageBrokerDeployment.getName());
        assertEquals("rabbitmq:3-management", messageBrokerDeployment.getImage());
        assertEquals(Map.of("app", "rabbitmq"), messageBrokerDeployment.getResource().getMetadata().getLabels());

        C4Component backendDeployment = components.get("deployment_backend");
        assertNotNull(backendDeployment, "Backend deployment should exist");
        assertEquals("backend", backendDeployment.getName());
        assertEquals("yourorg/backend:1.0", backendDeployment.getImage());
        assertEquals(Map.of("app", "backend"), backendDeployment.getResource().getMetadata().getLabels());

        C4Component authDeployment = components.get("deployment_auth");
        assertNotNull(authDeployment, "Auth deployment should exist");
        assertEquals("auth", authDeployment.getName());
        assertEquals("yourorg/auth:1.0", authDeployment.getImage());

        C4Component frontendDeployment = components.get("deployment_frontend");
        assertNotNull(frontendDeployment, "Frontend deployment should exist");
        assertEquals("frontend", frontendDeployment.getName());
        assertEquals("nginx:stable", frontendDeployment.getImage());

        C4Component configMap = components.get("configmap_frontend-config");
        assertNotNull(configMap, "Frontend configmap should exist");
        assertEquals("frontend-config", configMap.getName());
        assertEquals("ConfigMap", configMap.getKind());

        C4Component secret = components.get("secret_db-credentials");
        assertNotNull(secret, "DB credentials secret should exist");
        assertEquals("db-credentials", secret.getName());
        assertEquals("Secret", secret.getKind());

        C4Component ingress = components.get("ingress_demo-ingress");
        assertNotNull(ingress, "Demo ingress should exist");
        assertEquals("demo-ingress", ingress.getName());
        assertEquals("Ingress", ingress.getKind());

        C4Component pvc = components.get("persistentvolumeclaim_postgres-pvc");
        assertNotNull(pvc, "Postgres PVC should exist");
        assertEquals("postgres-pvc", pvc.getName());
        assertEquals("PersistentVolumeClaim", pvc.getKind());

        C4Component hpa = components.get("horizontalpodautoscaler_backend-hpa");
        assertNotNull(hpa, "Backend HPA should exist");
        assertEquals("backend-hpa", hpa.getName());
        assertEquals("HorizontalPodAutoscaler", hpa.getKind());

        C4Component pdb = components.get("poddisruptionbudget_backend-pdb");
        assertNotNull(pdb, "Backend PDB should exist");
        assertEquals("backend-pdb", pdb.getName());
        assertEquals("PodDisruptionBudget", pdb.getKind());

        C4Component serviceAccount = components.get("serviceaccount_demo-app-sa");
        assertNotNull(serviceAccount, "Service account should exist");
        assertEquals("demo-app-sa", serviceAccount.getName());
        assertEquals("ServiceAccount", serviceAccount.getKind());

        C4Component role = components.get("role_demo-app-role");
        assertNotNull(role, "Role should exist");
        assertEquals("demo-app-role", role.getName());
        assertEquals("Role", role.getKind());

        C4Component roleBinding = components.get("rolebinding_demo-app-rb");
        assertNotNull(roleBinding, "Role binding should exist");
        assertEquals("demo-app-rb", roleBinding.getName());
        assertEquals("RoleBinding", roleBinding.getKind());


        C4Component daemonSet = components.get("daemonset_node-logger");
        assertNotNull(daemonSet, "DaemonSet should exist");
        assertEquals("node-logger", daemonSet.getName());
        assertEquals("DaemonSet", daemonSet.getKind());
    }

    private void verifySpecifications(C4Model model) {
        assertTrue(model.getSpecifications().contains("namespace"), "Should contain namespace");
        assertTrue(model.getSpecifications().contains("statefulset"), "Should contain statefulset");
        assertTrue(model.getSpecifications().contains("deployment"), "Should contain deployment");
        assertTrue(model.getSpecifications().contains("daemonset"), "Should contain daemonset");
        assertTrue(model.getSpecifications().contains("service"), "Should contain service");
        assertTrue(model.getSpecifications().contains("ingress"), "Should contain ingress");
        assertTrue(model.getSpecifications().contains("configmap"), "Should contain configmap");
        assertTrue(model.getSpecifications().contains("secret"), "Should contain secret");
        assertTrue(model.getSpecifications().contains("persistentvolumeclaim"), "Should contain persistentvolumeclaim");
        assertTrue(model.getSpecifications().contains("persistentvolume"), "Should contain persistentvolume");
        assertTrue(model.getSpecifications().contains("serviceaccount"), "Should contain serviceaccount");
        assertTrue(model.getSpecifications().contains("role"), "Should contain role");
        assertTrue(model.getSpecifications().contains("rolebinding"), "Should contain rolebinding");
        assertTrue(model.getSpecifications().contains("horizontalpodautoscaler"), "Should contain horizontalpodautoscaler");
        assertTrue(model.getSpecifications().contains("poddisruptionbudget"), "Should contain poddisruptionbudget");
    }

    private void verifyDeploymentToServiceRelationships(C4Model model) {
        C4Namespace namespace = model.getNamespaces().get("demo-app");
        assertNotNull(namespace, "Namespace should exist");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.service_postgres")
                    && r.getTarget().equals("demo-app.statefulset_postgres")
                    && r.getDescription().equals(Constants.ROUTES_TO_RELATIONSHIP)),
                "Service postgres should route to statefulset postgres");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.service_redis")
                    && r.getTarget().equals("demo-app.deployment_redis")
                    && r.getDescription().equals(Constants.ROUTES_TO_RELATIONSHIP)),
                "Service redis should route to deployment redis");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.service_backend")
                    && r.getTarget().equals("demo-app.deployment_backend")
                    && r.getDescription().equals(Constants.ROUTES_TO_RELATIONSHIP)),
                "Service backend should route to deployment backend");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.service_auth")
                    && r.getTarget().equals("demo-app.deployment_auth")
                    && r.getDescription().equals(Constants.ROUTES_TO_RELATIONSHIP)),
                "Service auth should route to deployment auth");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.service_frontend")
                    && r.getTarget().equals("demo-app.deployment_frontend")
                    && r.getDescription().equals(Constants.ROUTES_TO_RELATIONSHIP)),
                "Service frontend should route to deployment frontend");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.service_message-broker")
                    && r.getTarget().equals("demo-app.deployment_message-broker")
                    && r.getDescription().equals(Constants.ROUTES_TO_RELATIONSHIP)),
                "Service message-broker should route to deployment message-broker");
    }

    private void verifyIngressToServiceRelationships(C4Model model) {
        C4Namespace namespace = model.getNamespaces().get("demo-app");
        assertNotNull(namespace, "Namespace should exist");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.ingress_demo-ingress")
                    && r.getTarget().equals("demo-app.service_frontend")
                    && r.getDescription().equals(Constants.ROUTES_HTTP_RELATIONSHIP)),
                "Ingress should route HTTP traffic to frontend service");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.ingress_demo-ingress")
                    && r.getTarget().equals("demo-app.service_backend")
                    && r.getDescription().equals(Constants.ROUTES_HTTP_RELATIONSHIP)),
                "Ingress should route HTTP traffic to backend service");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.ingress_demo-ingress")
                    && r.getTarget().equals("demo-app.service_auth")
                    && r.getDescription().equals(Constants.ROUTES_HTTP_RELATIONSHIP)),
                "Ingress should route HTTP traffic to auth service");
    }

    private void verifyPodConfigRelationships(C4Model model) {
        C4Namespace namespace = model.getNamespaces().get("demo-app");
        assertNotNull(namespace, "Namespace should exist");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.deployment_frontend")
                    && r.getTarget().equals("demo-app.configmap_frontend-config")
                    && r.getTechnology().equals(Constants.CONFIGMAP_TECHNOLOGY)),
                "Frontend deployment should mount frontend-config configmap");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.deployment_backend")
                    && r.getTarget().equals("demo-app.secret_db-credentials")
                    && r.getTechnology().equals(Constants.SECRET_TECHNOLOGY)),
                "Backend deployment should mount db-credentials secret (envFrom)");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.statefulset_postgres")
                    && r.getTarget().equals("demo-app.secret_db-credentials")
                    && r.getTechnology().equals(Constants.SECRET_TECHNOLOGY)),
                "Postgres statefulset should mount db-credentials secret (env)");
    }

    private void verifyHPARelationships(C4Model model) {
        C4Namespace namespace = model.getNamespaces().get("demo-app");
        assertNotNull(namespace, "Namespace should exist");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.horizontalpodautoscaler_backend-hpa")
                    && r.getTarget().equals("demo-app.deployment_backend")
                    && r.getDescription().equals(Constants.SCALES_RELATIONSHIP)
                    && r.getTechnology().equals(Constants.TECHNOLOGY_HPA)),
                "HPA should scale backend deployment");
    }

    private void verifyPDBRelationships(C4Model model) {
        C4Namespace namespace = model.getNamespaces().get("demo-app");
        assertNotNull(namespace, "Namespace should exist");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.poddisruptionbudget_backend-pdb")
                    && r.getTarget().equals("demo-app.deployment_backend")
                    && r.getDescription().equals(Constants.PROTECTS_RELATIONSHIP)
                    && r.getTechnology().equals(Constants.TECHNOLOGY_PDB)),
                "PDB should protect backend deployment");
    }

    private void verifyServiceAccountRelationships(C4Model model) {
        C4Namespace namespace = model.getNamespaces().get("demo-app");
        assertNotNull(namespace, "Namespace should exist");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.statefulset_postgres")
                    && r.getTarget().equals("demo-app.serviceaccount_demo-app-sa")
                    && r.getDescription().equals(Constants.USES_RELATIONSHIP)
                    && r.getTechnology().equals(Constants.TECHNOLOGY_SERVICEACCOUNT)),
                "Postgres statefulset should use demo-app-sa service account");

        assertTrue(namespace.getRelationships().stream()
                .anyMatch(r -> r.getSource().equals("demo-app.deployment_backend")
                    && r.getTarget().equals("demo-app.serviceaccount_demo-app-sa")
                    && r.getDescription().equals(Constants.USES_RELATIONSHIP)
                    && r.getTechnology().equals(Constants.TECHNOLOGY_SERVICEACCOUNT)),
                "Backend deployment should use demo-app-sa service account");
    }
}
