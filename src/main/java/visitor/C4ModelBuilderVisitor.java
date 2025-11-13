package visitor;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;
import lombok.Getter;
import lombok.Setter;
import model.*;

import java.util.*;

import static visitor.VisitorUtils.containerMatchesSelector;

@Getter
@Setter
public class C4ModelBuilderVisitor implements KubernetesResourceVisitor {

    private final C4Model model = new C4Model();


    private C4Namespace getOrCreateSystem(String ns) {
        return model.getNamespaces().computeIfAbsent(ns, C4Namespace::new);
    }

    @Override
    public void visit(Deployment dep) {
        String ns = Optional.ofNullable(dep.getMetadata().getNamespace()).orElse("default");
        C4Namespace namespace = getOrCreateSystem(ns);

        C4Component component = new C4Component(dep.getMetadata().getName(), "Deployment");
        PodSpec podSpec = dep.getSpec().getTemplate().getSpec();
        if (podSpec != null && podSpec.getContainers() != null) {
            Container c = podSpec.getContainers().get(0);
            component.setImage(c.getImage());
            //component.setMetadata(c.g);
            if (c.getEnv() != null) {
                for (EnvVar e : c.getEnv()) component.getEnv().put(e.getName(), e.getValue());
            }
        }
        namespace.addCompoments(component);
    }

    @Override
    public void visit(Service svc) {
        String ns = Optional.ofNullable(svc.getMetadata().getNamespace()).orElse("default");
        C4Namespace namespace = getOrCreateSystem(ns);

        C4Component container = new C4Component(svc.getMetadata().getName(), "Service");
        namespace.addCompoments(container);

        Map<String, String> selector = svc.getSpec().getSelector();
        if (selector != null && !selector.isEmpty()) {
            for (C4Namespace system : model.getNamespaces().values()) {
                for (C4Component components : system.getComponents()) {
                    if (containerMatchesSelector(components, selector)) {
                        model.addRelationship(new C4Relationship(
                                svc.getMetadata().getName(),
                                components.getName(),
                                "routes to",
                                "TCP/HTTP"
                        ));
                    }
                }
            }
        }
    }

    @Override
    public void visit(Ingress ing) {
        String ns = Optional.ofNullable(ing.getMetadata().getNamespace()).orElse("default");
        C4Namespace system = getOrCreateSystem(ns);

        C4Component container = new C4Component(ing.getMetadata().getName(), "Ingress");
        system.addCompoments(container);
        for (IngressRule rule : ing.getSpec().getRules()) {
            rule.getHttp().getPaths().forEach(path -> {
                String svcName = path.getBackend().getService().getName();
                model.addRelationship(new C4Relationship(
                        ing.getMetadata().getName(),
                        svcName,
                        "routes HTTP traffic",
                        "HTTP"
                ));
            });
        }
    }

    @Override
    public void visit(HasMetadata resource) {
        String ns = Optional.ofNullable(resource.getMetadata().getNamespace()).orElse("default");
        C4Namespace system = getOrCreateSystem(ns);

        C4Component container = new C4Component(resource.getMetadata().getName(), resource.getKind());
        system.addCompoments(container);
    }
}
