package com.k8stoc4.visitor;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import com.k8stoc4.model.C4Component;

import java.util.Map;

public class VisitorUtils {

    public static void accept(HasMetadata resource,
                              KubernetesResourceVisitor visitor) {
        if (resource instanceof Pod p) visitor.visit(p);
        else if (resource instanceof Deployment d) visitor.visit(d);
        else if (resource instanceof ReplicaSet rs) visitor.visit(rs);
        else if (resource instanceof StatefulSet s) visitor.visit(s);
        else if (resource instanceof Service s) visitor.visit(s);
        else if (resource instanceof Ingress i) visitor.visit(i);
        else if (resource instanceof io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress i) visitor.visit(i);
        else if (resource instanceof io.fabric8.kubernetes.api.model.extensions.Ingress i) visitor.visit(i);
        else visitor.visit(resource); // fallback generico
    }

    public static boolean containerMatchesSelector(C4Component component, Map<String, String> selector) {
        if (selector == null || selector.isEmpty()) return false;
        if (component.getLabels() == null || component.getLabels().isEmpty()) return false;

        for (Map.Entry<String, String> entry : selector.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String componentValue = component.getLabels().get(key);

            if (!value.equals(componentValue)) {
                return false;
            }
        }
        return true;
    }
}
