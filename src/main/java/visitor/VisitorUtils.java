package visitor;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import model.C4Component;

import java.util.Map;

public class VisitorUtils {

    public static void accept(HasMetadata resource,
                              KubernetesResourceVisitor visitor) {
        if (resource instanceof Deployment d) visitor.visit(d);
        else if (resource instanceof Service s) visitor.visit(s);
        else if (resource instanceof Ingress i) visitor.visit(i);
        else visitor.visit(resource); // fallback generico
    }

    public static boolean containerMatchesSelector(
            C4Component component,
            Map<String, String> selector) {
        if (selector == null || selector.isEmpty()) return false;
        if (component.getMetadata() == null) return false;

        for (Map.Entry<String, String> entry : selector.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!value.equals(component.getMetadata().get(key))) {
                return false; // almeno una label non corrisponde
            }
        }
        return true; // tutte le label corrispondono
    }
}
