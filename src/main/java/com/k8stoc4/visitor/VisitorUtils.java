package com.k8stoc4.visitor;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import com.k8stoc4.model.C4Component;

import java.util.Map;

public final class VisitorUtils {
    private VisitorUtils() {}

    public static void accept(final HasMetadata resource,
                              final KubernetesResourceVisitor visitor) {
        if (resource instanceof Pod p) {
            visitor.visit(p);
        } else if (resource instanceof Deployment d) {
            visitor.visit(d);
        } else if (resource instanceof ReplicaSet rs) {
            visitor.visit(rs);
        } else if (resource instanceof StatefulSet s) {
            visitor.visit(s);
        } else if (resource instanceof Service s) {
            visitor.visit(s);
        } else if (resource instanceof Ingress i) {
            visitor.visit(i);
        } else if (resource instanceof io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress i) {
            visitor.visit(i);
        } else if (resource instanceof io.fabric8.kubernetes.api.model.extensions.Ingress i) {
            visitor.visit(i);
        } else {
            visitor.visit(resource); // fallback generico
        }
    }

    public static boolean containerMatchesSelector(final C4Component component, final Map<String, String> selector) {
        if (selector == null || selector.isEmpty()) {
            return false;
        }

        Map<String, String> podLabels=null;
        if(component.getResource() instanceof Deployment d){
            podLabels = d.getSpec().getTemplate().getMetadata().getLabels();
        }else if (component.getResource() instanceof StatefulSet s){
            podLabels = s.getSpec().getTemplate().getMetadata().getLabels();
        }

        if (podLabels == null || podLabels.isEmpty()) {
            return false;
        }

        for (final Map.Entry<String, String> entry : selector.entrySet()) {
            final String key = entry.getKey();
            final String selectorLabelValue = entry.getValue();
            final String podLabelValue = podLabels.get(key);

            if (!selectorLabelValue.equals(podLabelValue)) {
                return false;
            }
        }
        return true;
    }
}
